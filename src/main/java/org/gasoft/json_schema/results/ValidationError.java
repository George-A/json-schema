package org.gasoft.json_schema.results;

import com.google.common.base.MoreObjects;
import com.google.common.base.Strings;
import org.gasoft.json_schema.results.IValidationResult.IValidationResultError;
import org.jspecify.annotations.NonNull;

public class ValidationError extends AbstractValidationResult implements IValidationResultError {

    private final EErrorType errorType;
    private final Object[]  args;

    public static ValidationError create(IValidationId validationId, EErrorType errorType) {
        return new ValidationError(validationId, errorType, null);
    }

    public static ValidationError create(IValidationId validationId, EErrorType errorType, Object ... args) {
        return new ValidationError(validationId, errorType, args);
    }

    private ValidationError(IValidationId validationId, EErrorType errorType, Object[] args) {
        super(validationId);
        this.errorType = errorType;
        this.args = args;
    }

    @Override
    public String getError() {
        if(args == null) {
            return errorType.getDefaultErrorMsg();
        }
        return Strings.lenientFormat(errorType.getDefaultErrorMsg(), args);
    }

    @Override
    public @NonNull String toString() {
        return MoreObjects.toStringHelper(this)
                .add("id", getId())
                .add("msg", getError())
                .toString();
    }
}
