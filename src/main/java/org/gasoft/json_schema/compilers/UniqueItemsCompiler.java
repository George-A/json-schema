package org.gasoft.json_schema.compilers;

import com.fasterxml.jackson.databind.JsonNode;
import org.gasoft.json_schema.results.EErrorType;
import org.gasoft.json_schema.results.IValidationResult.ISchemaLocator;
import org.gasoft.json_schema.results.ValidationError;
import org.gasoft.json_schema.results.ValidationResultFactory;
import org.jspecify.annotations.Nullable;

import java.util.HashSet;
import java.util.Set;

import static org.gasoft.json_schema.common.LocatedSchemaCompileException.checkIt;

public class UniqueItemsCompiler implements INamedCompiler {

    @Override
    public String getKeyword() {
        return "uniqueItems";
    }

    @Override
    public @Nullable IValidator compile(JsonNode schemaNode, CompileContext compileContext, ISchemaLocator schemaLocator) {
        checkIt(schemaNode.isBoolean(), schemaLocator,
                "The %s keyword value must be the boolean", getKeyword());
        if(schemaNode.booleanValue()) {
            return (instance, instanceLocation, context) -> {
                var id = ValidationResultFactory.createId(schemaLocator, instanceLocation);
                if(instance.isArray()) {
                    Set<JsonNode> checkedSet = new HashSet<>();
                    for(int idx = 0; idx < instance.size(); idx++) {
                        var node = instance.get(idx);
                        if(!checkedSet.add(node)) {
                            return ValidationError.create(id, EErrorType.UNIQUE_ITEMS, instanceLocation.appendIndex(idx))
                                    .publish();
                        }
                    }
                }
                return ValidationResultFactory.createOk(id).publish();
            };
        }
        return null;
    }
}
