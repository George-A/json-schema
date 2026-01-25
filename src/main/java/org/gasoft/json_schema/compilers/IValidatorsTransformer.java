package org.gasoft.json_schema.compilers;

import java.util.Map;

public interface IValidatorsTransformer {

    // The inplace applicator (if-the-else) must be transformed before than unevaluatedProperties
    default int getOrder() {
        return 0;
    }
    void transform(Map<String, ICompiler.IValidatorAction> validators, CompileContext compileContext);
}
