package org.gasoft.json_schema.compilers;

import com.fasterxml.jackson.databind.JsonNode;
import org.gasoft.json_schema.loaders.IReferenceResolver.IResolutionResult;
import org.gasoft.json_schema.results.IValidationResult.ISchemaLocator;
import org.jspecify.annotations.Nullable;

import static org.gasoft.json_schema.common.LocatedSchemaCompileException.checkIt;
import static org.gasoft.json_schema.compilers.IdCompiler.isSame;

public class DynamicRefCompiler implements INamedCompiler {

    @Override
    public String getKeyword() {
        return "$dynamicRef";
    }

    @Override
    public @Nullable IValidator compile(JsonNode schemaNode, CompileContext compileContext, ISchemaLocator schemaLocator) {

        checkIt(schemaNode.isTextual(),schemaLocator, "The %s keyword value must be an string", getKeyword());

        IResolutionResult result = compileContext.resolveDynamicRef(schemaNode.textValue(), schemaLocator);
        ISchemaLocator locator = result.getResolvedLocator();
        if (isSame(schemaLocator, result.getResolvedLocator())) {
//            System.out.println("Supress same: " + result.getResolvedLocator() + " at current " + schemaLocator);
            locator = schemaLocator;
        }
        JsonNode navigatedToPtr = result.getSchema().at(result.getReferencedPtr());
        checkIt(!navigatedToPtr.isMissingNode(), schemaLocator,"Invalid %s keyword value resolution result. Can`t detect subschema",
                result);

        return compileContext.compile(navigatedToPtr, locator);
    }
}
