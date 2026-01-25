package org.gasoft.json_schema.compilers;

import org.gasoft.json_schema.results.EErrorType;
import org.gasoft.json_schema.results.IValidationResult.ISchemaLocator;
import org.gasoft.json_schema.results.ValidationError;
import org.gasoft.json_schema.results.ValidationResultFactory;

import static org.gasoft.json_schema.common.LocatedSchemaCompileException.checkIt;

public class MaxItemsCompiler extends BaseIntegerCompiler {

    @Override
    public String getKeyword() {
        return "maxItems";
    }

    @Override
    protected IValidator compile(int maxItems, CompileContext compileContext, ISchemaLocator schemaLocation) {
        checkIt(maxItems >= 0, schemaLocation,
                "The %s keyword value must be non-negative. Actual: %s", getKeyword(), maxItems);
        return (instance, instancePtr,context) -> {
            var id = ValidationResultFactory.createId(schemaLocation, instancePtr);

            if(instance.isArray() && instance.size() > maxItems) {
                return ValidationError.create(id, EErrorType.MAX_ITEMS, getKeyword(), maxItems, instance.size())
                        .publish();
            }
            return ValidationResultFactory.createOk(id).publish();
        };
    }
}
