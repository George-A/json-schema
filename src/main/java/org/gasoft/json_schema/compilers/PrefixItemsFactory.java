package org.gasoft.json_schema.compilers;

import com.fasterxml.jackson.core.JsonPointer;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.Lists;
import org.gasoft.json_schema.results.IValidationResult;
import org.gasoft.json_schema.results.IValidationResult.ISchemaLocator;
import org.gasoft.json_schema.results.ValidationResultFactory;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.stream.Stream;

import static org.gasoft.json_schema.common.LocatedSchemaCompileException.checkIt;

public class PrefixItemsFactory implements ICompilerFactory {

    @Override
    public Stream<String> getSupportedKeywords() {
        return Stream.of("prefixItems");
    }

    @Override
    public ICompiler getCompiler(String keyword) {
        return new PrefixItemsCompiler();
    }

    static class PrefixItemsCompiler implements ICompiler {

        private ISchemaLocator schemaLocation;
        private final List<IValidator> validators = Lists.newArrayList();

        @Override
        public boolean isSaveCompilerToCompileContext() {
            return true;
        }

        @Override
        public IValidator compile(JsonNode schemaNode, CompileContext compileContext, ISchemaLocator schemaLocator) {
            this.schemaLocation = schemaLocator;
            checkIt(schemaNode.isArray() && !schemaNode.isEmpty(), schemaLocator,
                    "The %s keyword value must be non empty array. Actual: %s", "prefixItems", schemaNode.getNodeType());
            for (int idx = 0; idx < schemaNode.size(); idx++) {
                validators.add(compileContext.compile(schemaNode.get(idx), schemaLocator.appendIndex(idx)));
            }
            return this::validate;
        }

        public int getValidateItemsCount() {
            return validators.size();
        }

        private Publisher<IValidationResult> validate(JsonNode instance, JsonPointer instancePtr, IValidationContext validationContext) {
            var id = ValidationResultFactory.createId(schemaLocation, instancePtr);
            if(instance.isArray()) {

                return Flux.range(0, Math.min(instance.size(), validators.size()))
                        .parallel()
                        .flatMap(idx -> {
                            var itemPtr = instancePtr.appendIndex(idx);
                            return ValidationResultFactory.tryAppendAnnotation(
                                    () -> validators.get(idx).validate(instance.get(idx), itemPtr, validationContext),
                                    ValidationResultFactory.createId(schemaLocation, itemPtr)
                            );
                        })
                        .sequential()
                        .reduce(
                                ValidationResultFactory.createContainer(id),
                                ValidationResultFactory.ValidationResultContainer::append
                        )
                        .map(value -> value);
            }
            return ValidationResultFactory.createOk(id).publish();
        }
    }
}
