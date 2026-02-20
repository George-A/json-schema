package io.github.georgeakulov.json_schema.compilers;

import com.fasterxml.jackson.databind.JsonNode;
import io.github.georgeakulov.json_schema.compilers.base.BaseNumberCompiler;
import io.github.georgeakulov.json_schema.dialects.Defaults;
import io.github.georgeakulov.json_schema.results.EErrorType;
import io.github.georgeakulov.json_schema.results.IValidationResult;
import io.github.georgeakulov.json_schema.results.ValidationError;
import io.github.georgeakulov.json_schema.results.ValidationResultFactory;

import java.net.URI;
import java.util.stream.Stream;

public class MaximumCompiler extends BaseNumberCompiler {

    @Override
    public String getKeyword() {
        return "maximum";
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
    protected IValidationResult analyse(IValidationResult.IValidationId id, JsonNode schemaValue, JsonNode instanceValue, int compareResult) {
        if(compareResult < 0) {
            return ValidationError.create(id, EErrorType.MAXIMUM, instanceValue, schemaValue);
        }
        return ValidationResultFactory.createOk(id);
    }
}
