package org.gasoft.json_schema.compilers;

import com.fasterxml.jackson.core.JsonPointer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.gasoft.json_schema.results.IValidationResult;
import org.gasoft.json_schema.results.IValidationResult.ISchemaLocator;
import org.gasoft.json_schema.results.ValidationResultFactory;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class AdditionalPropertiesCompiler implements INamedCompiler, IValidatorsTransformer {

    @Override
    public String getKeyword() {
        return "additionalProperties";
    }

    @Override
    public IValidator compile(JsonNode schemaNode, CompileContext compileContext, ISchemaLocator schemaLocator) {
        return compileContext.compile(schemaNode, schemaLocator);
    }

    @Override
    public void preprocess(IPreprocessorMediator mediator, String keyword, JsonNode node, JsonPointer pointer) {
        mediator.process(pointer);
    }

    @Override
    public void transform(Map<String, IValidatorAction> validators, CompileContext compileContext) {

        var current = validators.get(getKeyword());
        if(current == null) {
            return;
        }

        var preferValidators = Stream.of("properties", "patternProperties")
                .map(validators::remove)
                .filter(Objects::nonNull)
                .toList();

        validators.put(getKeyword(), new AdditionalItemsAction(current, preferValidators));
    }

    private class AdditionalItemsAction extends BasePropertiesCollectorValidator implements IValidatorAction {

        private AdditionalItemsAction(IValidatorAction original, List<IValidatorAction> preferred) {
            super(original, preferred);
        }

        @Override
        public IValidator validator() {
            return this;
        }

        @Override
        public ICompileAction compileAction() {
            return original.compileAction();
        }

        protected Publisher<IValidationResult> validate(
                IValidationResult.IValidationId id,
                List<IValidationResult> internalResults,
                ObjectNode instance,
                JsonPointer instancePtr,
                IValidationContext context) {

            Set<String> evaluatedFields = filterAnnotations(internalResults, instancePtr);

            return Flux.fromStream(instance.propertyStream())
                    .filter(prop -> !evaluatedFields.contains(prop.getKey()))
                    .flatMap(prop -> {
                        JsonPointer instanceConcretePtr = instancePtr.appendProperty(prop.getKey());
                        return Mono.from(original.validator().validate(prop.getValue(), instanceConcretePtr, context))
                                .map(validationResult -> {

                                    // Analyse field checking results
                                    if (validationResult.isOk()) {
                                        return ValidationResultFactory.createAnnotation(id.getSchemaLocator(), instanceConcretePtr);
                                    } else {
                                        return validationResult;
                                    }
                                });
                    })
                    .reduce(
                            ValidationResultFactory.createContainer(id),
                            ValidationResultFactory.ValidationResultContainer::append
                    )
                    .map(val -> val);
        }
    }

    private Set<String> filterAnnotations(List<IValidationResult> validationResults, JsonPointer childOf) {
        return validationResults.stream()
                .flatMap(IValidationResult::asStream)
                .filter(vr -> vr.getType() == IValidationResult.Type.ANNOTATION)
                .map(IValidationResult::getId)
                .filter(id -> isChildOf(id, childOf))
                .map(id -> id.getInstanceRef().last().getMatchingProperty())
                .collect(Collectors.toSet());
    }

    private boolean isChildOf(IValidationResult.IValidationId id, JsonPointer pointer) {
        return id.getInstanceRef().head().equals(pointer);
    }
}
