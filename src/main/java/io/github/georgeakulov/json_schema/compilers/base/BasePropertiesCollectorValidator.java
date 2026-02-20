package io.github.georgeakulov.json_schema.compilers.base;

import com.fasterxml.jackson.core.JsonPointer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.github.georgeakulov.json_schema.compilers.ICompiler.IValidatorAction;
import io.github.georgeakulov.json_schema.compilers.IValidationContext;
import io.github.georgeakulov.json_schema.compilers.IValidator;
import io.github.georgeakulov.json_schema.results.IValidationResult;
import io.github.georgeakulov.json_schema.results.ValidationResultFactory;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;

import java.util.List;

public abstract class BasePropertiesCollectorValidator implements IValidator {

    protected final IValidatorAction original;
    private final List<IValidatorAction> validators;

    public BasePropertiesCollectorValidator(IValidatorAction original, List<IValidatorAction> validators) {
        this.original = original;
        this.validators = validators;
    }

    @Override
    public Publisher<IValidationResult> validate(JsonNode instance, JsonPointer instancePtr, IValidationContext context) {
        var id = original.compileAction().createId(instancePtr);
        if(instance.isObject()) {
            return Flux.fromIterable(validators)
                    .parallel()
                    .flatMap(validator -> validator.validator().validate(instance, instancePtr, context))
                    .sequential()
                    .collectList()
                    .flatMapMany(list ->
                            Flux.fromIterable(list)
                                    .concatWith(validate(id, list, (ObjectNode) instance, instancePtr, context))
                    );
        }
        return ValidationResultFactory.createOk(id).publish();
    }

    protected abstract  Publisher<IValidationResult> validate(
            IValidationResult.IValidationId id,
            List<IValidationResult> internalResults,
            ObjectNode instance,
            JsonPointer instancePtr,
            IValidationContext context);
}
