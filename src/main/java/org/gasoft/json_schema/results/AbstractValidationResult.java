package org.gasoft.json_schema.results;

abstract class AbstractValidationResult implements IValidationResult{

    private final IValidationId validationId;

    public AbstractValidationResult(IValidationId validationId) {
        this.validationId = validationId;
    }

    @Override
    public IValidationId getId() {
        return validationId;
    }
}
