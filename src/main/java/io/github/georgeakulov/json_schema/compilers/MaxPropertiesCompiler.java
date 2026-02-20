package io.github.georgeakulov.json_schema.compilers;

import io.github.georgeakulov.json_schema.compilers.base.BaseIntegerCompiler;
import io.github.georgeakulov.json_schema.dialects.Defaults;
import io.github.georgeakulov.json_schema.results.EErrorType;
import io.github.georgeakulov.json_schema.results.IValidationResult.ISchemaLocator;
import io.github.georgeakulov.json_schema.results.ValidationError;
import io.github.georgeakulov.json_schema.results.ValidationResultFactory;

import java.net.URI;
import java.util.stream.Stream;

import static io.github.georgeakulov.json_schema.common.LocatedSchemaCompileException.checkIt;

public class MaxPropertiesCompiler extends BaseIntegerCompiler {

    @Override
    public String getKeyword() {
        return "maxProperties";
    }

    @Override
    public Stream<URI> getVocabularies() {
        return Stream.of(
                Defaults.DRAFT_2020_12_VALIDATION,
                Defaults.DRAFT_2019_09_VALIDATION,
                Defaults.DRAFT_07_CORE
        );
    }

    @Override
    protected IValidator compile(int maxProperties, CompileContext compileContext, ISchemaLocator schemaLocation) {

        checkIt(maxProperties >= 0, schemaLocation,
                "The value of {0} keyword must be non negative integer. Actual: {1}", getKeyword(), maxProperties);

        return (instance, instancePtr, context) -> {

            var id = ValidationResultFactory.createId(schemaLocation, instancePtr);
            if(instance.isObject() && instance.size() > maxProperties) {
                return ValidationError.create(
                        id, EErrorType.MAX_PROPERTIES, maxProperties, instance.size()
                ).publish();
            }
            return ValidationResultFactory.createOk(id).publish();
        };
    }
}
