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

public class MinPropertiesCompiler extends BaseIntegerCompiler {

    @Override
    public String getKeyword() {
        return "minProperties";
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
    protected IValidator compile(int minProperties, CompileContext compileContext, ISchemaLocator schemaLocation) {

        checkIt(minProperties >= 0, schemaLocation,
                "The {0} keyword value must be non negative integer. Actual: {1}", getKeyword(), minProperties);

        return (instance, instancePtr, context) -> {

            var id = ValidationResultFactory.createId(schemaLocation, instancePtr);

            if(instance.isObject() && instance.size() < minProperties) {
                return ValidationError.create(id, EErrorType.MIN_PROPERTIES, minProperties, instance.size())
                        .publish();
            }
            return ValidationResultFactory.createOk(id).publish();
        };
    }
}
