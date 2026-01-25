package org.gasoft.json_schema.compilers;

import org.gasoft.json_schema.results.EErrorType;
import org.gasoft.json_schema.results.IValidationResult.ISchemaLocator;
import org.gasoft.json_schema.results.ValidationError;
import org.gasoft.json_schema.results.ValidationResultFactory;

import static org.gasoft.json_schema.common.LocatedSchemaCompileException.checkIt;

public class MinPropertiesCompiler extends BaseIntegerCompiler {

    @Override
    public String getKeyword() {
        return "minProperties";
    }

    @Override
    protected IValidator compile(int minProperties, CompileContext compileContext, ISchemaLocator schemaLocation) {

        checkIt(minProperties >= 0, schemaLocation,
                "The %s keyword value must be non negative integer. Actual: %s", getKeyword(), minProperties);

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
