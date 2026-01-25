package org.gasoft.json_schema.compilers;

import com.fasterxml.jackson.core.JsonPointer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import org.gasoft.json_schema.results.IValidationResult;
import org.gasoft.json_schema.results.IValidationResult.ISchemaLocator;
import org.gasoft.json_schema.results.ValidationResultFactory;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class ItemsCompilerFactory implements ICompilerFactory {

    @Override
    public Stream<String> getSupportedKeywords() {
        return Stream.of("items");
    }

    @Override
    public ICompiler getCompiler(String keyword) {
        return new ItemsCompiler();
    }

    private static class ItemsCompiler implements ICompiler {

        private ISchemaLocator schemaLocation;
        private IValidator validator;
        private int prefixItemsCount;
        private CompileConfig config;

        @Override
        public IValidator compile(JsonNode schemaNode, CompileContext compileContext, ISchemaLocator schemaLocator) {
            this.schemaLocation = schemaLocator;
            this.validator = compileContext.compile(schemaNode, schemaLocator);
            prefixItemsCount = resolveMinIndexForValidate(compileContext);
            this.config = compileContext.getConfig();
            return this::validate;
        }

        private Publisher<IValidationResult> validate(JsonNode instance, JsonPointer instancePtr, IValidationContext validationContext) {
            var id = ValidationResultFactory.createId(schemaLocation, instancePtr);

            var evaluated = ToArrayWrapper.tryWrap(instance, config);
            if(evaluated.isArray() && instance.size() > prefixItemsCount) {

                return Flux.range(prefixItemsCount, evaluated.size() - prefixItemsCount)
                        .flatMap(idx -> {
                            var idxPtr = instancePtr.appendIndex(idx);
                            return ValidationResultFactory.tryAppendAnnotation(
                                    () -> validator.validate(evaluated.get(idx), idxPtr, validationContext),
                                    ValidationResultFactory.createId(schemaLocation, idxPtr)
                            );
                        })
                        .subscribeOn(config.getScheduler())
                        .reduce(
                                ValidationResultFactory.createContainer(id),
                                ValidationResultFactory.ValidationResultContainer::append
                        )
                        .map(val -> val);
            }
            return ValidationResultFactory.createOk(id).publish();
        }

        @Override
        public void resolveCompilationOrder(List<ICompileAction> current, CompileContext compileContext, ISchemaLocator schemaLocator) {
            IntStream.range(0, current.size())
                    .filter(i -> current.get(i).keyword().equals("items"))
                    .findAny()
                    .ifPresent(idx -> current.add(current.remove(idx)));
        }

        private int resolveMinIndexForValidate(CompileContext compileContext) {
            ICompiler compiler = compileContext.optEvaluatedCompiler("prefixItems");
            if(compiler instanceof PrefixItemsFactory.PrefixItemsCompiler) {
                return ((PrefixItemsFactory.PrefixItemsCompiler)compiler).getValidateItemsCount();
            }
            return 0;
        }

        @Override
        public void preprocess(IPreprocessorMediator mediator, String keyword, JsonNode node, JsonPointer pointer) {
            mediator.process(pointer);
        }
    }

    private static class ToArrayWrapper extends ArrayNode {

        public ToArrayWrapper(JsonNode node) {
            super(null);
            this.add(node);
        }

        private static JsonNode tryWrap(JsonNode node, CompileConfig config) {
            if(node.isArray()) {
                return node;
            }
            if(config.isAllowTreatAsArray()) {
                return new ToArrayWrapper(node);
            }
            return node;
        }
    }
}
