package io.github.georgeakulov.json_schema.compilers.base;

import com.fasterxml.jackson.databind.JsonNode;
import io.github.georgeakulov.json_schema.compilers.CompileContext;
import io.github.georgeakulov.json_schema.compilers.INamedCompiler;
import io.github.georgeakulov.json_schema.compilers.IValidator;
import io.github.georgeakulov.json_schema.compilers.Utils;
import io.github.georgeakulov.json_schema.results.IValidationResult.ISchemaLocator;


public abstract class BaseIntegerCompiler implements INamedCompiler {

    protected abstract IValidator compile(int value, CompileContext compileContext, ISchemaLocator schemaLocation);

    @Override
    public IValidator compile(JsonNode schemaNode, CompileContext compileContext, ISchemaLocator schemaLocator) {
        int value = Utils.getCheckedInteger(schemaLocator,schemaNode, "The %s of properties must be integer. Actual: %s", getKeyword(), schemaNode);
        return compile(value, compileContext, schemaLocator);
    }
}
