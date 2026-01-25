package org.gasoft.json_schema.compilers;

import com.fasterxml.jackson.databind.JsonNode;
import org.gasoft.json_schema.results.EErrorType;
import org.gasoft.json_schema.results.IValidationResult;
import org.gasoft.json_schema.results.ValidationError;
import org.gasoft.json_schema.results.ValidationResultFactory;

public class MinimumCompiler extends BaseNumberCompiler {

    @Override
    public String getKeyword() {
        return "minimum";
    }

    @Override
    protected IValidationResult analyse(IValidationResult.IValidationId id, JsonNode schemaValue, JsonNode instanceValue, int compareResult) {
        if(compareResult > 0) {
            return ValidationError.create(id, EErrorType.MINIMUM, instanceValue, schemaValue);
        }
        return ValidationResultFactory.createOk(id);
    }
}
