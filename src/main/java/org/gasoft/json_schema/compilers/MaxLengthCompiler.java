package org.gasoft.json_schema.compilers;

import com.fasterxml.jackson.databind.JsonNode;
import org.gasoft.json_schema.results.EErrorType;
import org.gasoft.json_schema.results.IValidationResult.ISchemaLocator;

public class MaxLengthCompiler extends BaseLengthCompiler {

    @Override
    public String getKeyword() {
        return "maxLength";
    }

    @Override
    public IValidator compile(JsonNode schemaNode, CompileContext compileContext, ISchemaLocator schemaLocator) {
        return super.create(
                schemaLocator,
                (expected, actual) -> actual <= expected,
                EErrorType.MAX_LENGTH,
                schemaNode
        );
    }
}
