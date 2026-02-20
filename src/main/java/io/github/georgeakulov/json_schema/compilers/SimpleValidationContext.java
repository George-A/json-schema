package io.github.georgeakulov.json_schema.compilers;

import com.fasterxml.jackson.core.JsonPointer;

public class SimpleValidationContext implements IValidationContext {

    public SimpleValidationContext() {
    }

    public SimpleValidationContext(SimpleValidationContext simpleValidationContext) {
    }

    @Override
    public IValidationContext recreate(JsonPointer pointer) {
        return new SimpleValidationContext(this);
    }
}
