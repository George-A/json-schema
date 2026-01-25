package org.gasoft.json_schema.compilers;

import com.fasterxml.jackson.core.JsonPointer;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.Lists;
import org.gasoft.json_schema.compilers.ICompiler.ICompileAction;
import org.gasoft.json_schema.compilers.ICompiler.IValidatorAction;
import org.gasoft.json_schema.dialects.DialectRegistry;
import org.gasoft.json_schema.dialects.DialectResolver;
import org.gasoft.json_schema.dialects.VocabularyRegistry;
import org.gasoft.json_schema.loaders.SchemasRegistry;
import org.gasoft.json_schema.results.IValidationResult;
import org.gasoft.json_schema.results.IValidationResult.ISchemaLocator;
import org.gasoft.json_schema.results.ValidationResultFactory;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Compiler {

    public Compiler() {
    }

    public Function<JsonNode, Publisher<IValidationResult>> compileSchema(JsonNode schema, @Nullable URI defaultSchemaUri, @Nullable CompileConfig config) {
        config = config == null ? new CompileConfig() : config;
        SchemasRegistry registry = new SchemasRegistry(
                new DialectResolver(DialectRegistry.getInstance(), new VocabularyRegistry()),
                config
        );
        ISchemaLocator locator = registry.registerInitialSchema(schema, defaultSchemaUri);
        CompileContext context = new CompileContext(config)
                .withCompiler(this)
                .withRegistry(registry);

        IValidator validator = context.compile(schema, locator);

        return instance -> Mono.from(validator.validate(instance, JsonPointer.empty(), new SimpleValidationContext()));
    }

    // Root compiler
    public Function<JsonNode, Publisher<IValidationResult>> compileSchema(JsonNode schema) {
        return compileSchema(schema, null, null);
    }

    IValidator compile(JsonNode schema, CompileContext parentContext, ISchemaLocator schemaLocator) {

        Invoke invoke = new Invoke();
        IValidator recursiveValidator = parentContext.setCompileData(schemaLocator, invoke);
        if(recursiveValidator != null) {
            return recursiveValidator;
        }

        CompileContext compileContext = parentContext.onNewSchemaObject();

        if(schema.isBoolean()) {
            invoke.laterValidator = new SchemaAsBooleanCompiler().compile(schema, parentContext, schemaLocator);
        }
        else if(schema.isObject()) {

            List<ICompileAction> foundCompilers = prepareCompilers(schema, schemaLocator, compileContext);

            if(foundCompilers.isEmpty()) {
                invoke.laterValidator = schemaOk(schemaLocator);
            }
            else {

                var keywordValidators = createValidators(foundCompilers, compileContext);

                transformValidators(keywordValidators, compileContext);

                invoke.laterValidator = (node, instancePtr, context) -> {
                    var ctxt = context.recreate(instancePtr);
                    return Flux.fromIterable(keywordValidators.values())
                            .map(IValidatorAction::validator)
                            .flatMap(validator -> validator.validate(node, instancePtr, ctxt))
                            .reduce(
                                    ValidationResultFactory.createContainer(schemaLocator, instancePtr),
                                    ValidationResultFactory.ValidationResultContainer::append
                            )
                            .map(value -> value);
                };
            }

        } else {
            invoke.laterValidator = schemaOk(schemaLocator);
        }

        return invoke.laterValidator;
    }

    private void transformValidators(Map<String, IValidatorAction> keywordValidators, CompileContext compileContext) {
        CommonCompilersFactory.getCompilerRegistry()
                .getTransformers()
                .stream().sorted(Comparator.comparing(IValidatorsTransformer::getOrder))
                .forEach(transformer -> transformer.transform(keywordValidators, compileContext));
    }

    private IValidator schemaOk(ISchemaLocator schemaLocation) {
        return (instance, instancePtr, context) ->
                ValidationResultFactory
                        .createOk(schemaLocation, instancePtr)
                        .publish();
    }

    private Map<String, IValidatorAction> createValidators(List<ICompileAction> foundCompilers, CompileContext compileContext) {

        return foundCompilers.stream()
                .map(action -> {
//                    System.out.println("Compile: " + action.keyword() + ", loc:" + action.locator());
                    IValidator validator = action.compiler().compile(action.schemaNode(), compileContext, action.locator());
                    if(action.compiler().isSaveCompilerToCompileContext()) {
                        compileContext.addEvaluatedCompilerToContext(action.keyword(), action.compiler());
                    }
                    if(validator != null) {
                        return new ValidatorAction(validator, action);
                    }
                    return null;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(
                        item -> item.compileAction.keyword(),
                        Function.identity()
                ));
    }

    private static List<ICompileAction> prepareCompilers(JsonNode schema, ISchemaLocator locator, CompileContext compileContext) {
        List<@NonNull ICompileAction> foundCompilers = schema.propertyStream()
                .map(entry -> {
                    var compiler = compileContext.getDialect(locator)
                            .optCompiler(entry.getKey());
                    if(compiler != null) {
                        return CompileAction.of(entry.getKey(), compiler, entry.getValue(), locator.appendProperty(entry.getKey()));
                    }
                    return null;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        // Sort compiler for compilation order
        for (@NonNull ICompileAction compileAction : Lists.newArrayList(foundCompilers)) {
            compileAction.compiler().resolveCompilationOrder(foundCompilers, compileContext, locator);
        }

        return foundCompilers;
    }

    record CompileAction(String keyword, ICompiler compiler, JsonNode schemaNode, ISchemaLocator locator) implements ICompileAction {
        static ICompileAction of(String keyword, ICompiler compiler, JsonNode schemaNode, ISchemaLocator locator) {
            return  new CompileAction(keyword, compiler, schemaNode, locator);
        }
    }

    record ValidatorAction(IValidator validator, ICompileAction compileAction) implements IValidatorAction {
    }

    public static class Invoke implements IValidator {
        IValidator laterValidator;

        @Override
        public Publisher<IValidationResult> validate(JsonNode instance, JsonPointer instanceLocation, IValidationContext context) {
            return laterValidator.validate(instance, instanceLocation, context);
        }
    }
}
