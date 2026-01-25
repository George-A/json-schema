package org.gasoft.json_schema.compilers;

import org.gasoft.json_schema.results.EErrorType;
import org.gasoft.json_schema.results.IValidationResult.ISchemaLocator;
import org.gasoft.json_schema.results.ValidationError;
import org.gasoft.json_schema.results.ValidationResultFactory;

import static org.gasoft.json_schema.common.LocatedSchemaCompileException.checkIt;

public class MinItemsCompiler extends BaseIntegerCompiler {

    @Override
    public String getKeyword() {
        return "minItems";
    }

    @Override
    protected IValidator compile(int minItems, CompileContext compileContext, ISchemaLocator schemaLocation) {
        checkIt(minItems >= 0, schemaLocation, "The %s keyword value must be non-negative. Actual: %s", getKeyword(), minItems);
        return (node, instancePtr, context) -> {

            var id = ValidationResultFactory.createId(schemaLocation, instancePtr);

            if(node.isArray() && node.size() < minItems) {
                return ValidationError.create(id, EErrorType.MIN_ITEMS, minItems, node.size())
                        .publish();
            }
            return ValidationResultFactory.createOk(id).publish();
        };
    }
}
