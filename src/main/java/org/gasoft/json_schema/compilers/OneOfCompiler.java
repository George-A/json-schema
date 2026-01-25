package org.gasoft.json_schema.compilers;

import com.fasterxml.jackson.databind.JsonNode;
import org.gasoft.json_schema.results.EErrorType;
import org.gasoft.json_schema.results.IValidationResult;
import org.gasoft.json_schema.results.ValidationError;
import org.gasoft.json_schema.results.ValidationResultFactory;
import org.jspecify.annotations.Nullable;
import reactor.core.publisher.Flux;

import java.util.List;

public class OneOfCompiler extends BaseSomeOfCompiler {

    @Override
    public String getKeyword() {
        return "oneOf";
    }

    @Override
    public @Nullable IValidator compile(JsonNode schemaNode, CompileContext compileContext, IValidationResult.ISchemaLocator schemaLocator) {
        List<IValidator> validators = prepareValidators(schemaNode, schemaLocator, compileContext);
        return (instance, instancePtr, context) ->
            Flux.fromIterable(validators)
                    .flatMap(validator -> validator.validate(instance, instancePtr, context))
                    .filter(IValidationResult::isOk)
                    .subscribeOn(compileContext.getConfig().getScheduler())
                    .collectList()
                    .map(list -> {
                        var id = ValidationResultFactory.createId(schemaLocator, instancePtr);
                        if(list.isEmpty()) {
                            return ValidationError.create(id, EErrorType.ONE_OF_EMPTY);
                        }
                        else if(list.size() > 1) {
                            return ValidationError.create(id, EErrorType.ONE_OF_MORE_THAN_ONE);
                        }
                        return ValidationResultFactory.createContainer(id)
                                .appendAll(list);
                    });
    }
}
