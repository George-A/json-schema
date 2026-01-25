package org.gasoft.json_schema.compilers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.google.common.collect.Sets;
import org.gasoft.json_schema.common.JsonNodeComparator;
import org.gasoft.json_schema.results.EErrorType;
import org.gasoft.json_schema.results.IValidationResult.ISchemaLocator;
import org.gasoft.json_schema.results.ValidationError;
import org.gasoft.json_schema.results.ValidationResultFactory;
import org.jspecify.annotations.NonNull;

import java.util.Set;

import static org.gasoft.json_schema.common.LocatedSchemaCompileException.checkIt;
import static org.gasoft.json_schema.common.LocatedSchemaCompileException.create;

public class EnumCompiler implements INamedCompiler {

    @Override
    public String getKeyword() {
        return "enum";
    }

    @Override
    public @NonNull IValidator compile(JsonNode schemaNode, CompileContext compileContext, ISchemaLocator schemaLocator) {

        checkIt(schemaNode.isArray(), schemaLocator, "The %s keyword value must be an array. Actual: %s", getKeyword(), schemaNode.getNodeType());
        ArrayNode array = (ArrayNode) schemaNode;
        checkIt(!array.isEmpty(), schemaLocator, "The %s keyword must contains as least one element", getKeyword());
        final Set<JsonNode> set = Sets.newTreeSet(JsonNodeComparator.JSON_NODE_COMPARATOR);
        schemaNode.values().forEachRemaining(element -> {
            if(set.contains(element)) {
                throw create(schemaLocator, "Not UNIQUE element in %s keyword value found. %s", getKeyword(), element);
            }
            set.add(element);
        });

        return (node, instancePtr, context) -> {

            var id = ValidationResultFactory.createId(schemaLocator, instancePtr);
            if(set.contains(node)) {
                return ValidationResultFactory.createOk(id).publish();
            }
            return ValidationError.create(id, EErrorType.ENUM, node, schemaNode)
                    .publish();
        };
    }
}
