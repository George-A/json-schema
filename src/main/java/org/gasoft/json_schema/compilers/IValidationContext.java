package org.gasoft.json_schema.compilers;

import com.fasterxml.jackson.core.JsonPointer;

public interface IValidationContext {

    IValidationContext recreate(JsonPointer pointer);
}
