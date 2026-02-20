package io.github.georgeakulov.json_schema.compilers;

import io.github.georgeakulov.json_schema.compilers.ICompiler.IValidatorAction;
import io.github.georgeakulov.json_schema.results.IValidationResult;

import java.util.Map;

public interface IValidatorsTransformer {

    // The inplace applicator (if-the-else) must be transformed before than unevaluatedProperties
    default int getOrder() {
        return 0;
    }
    void transform(Map<String, IValidatorAction> validators, CompileContext compileContext, IValidationResult.ISchemaLocator locator);
}
