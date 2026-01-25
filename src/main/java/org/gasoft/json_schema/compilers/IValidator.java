package org.gasoft.json_schema.compilers;

import com.fasterxml.jackson.core.JsonPointer;
import com.fasterxml.jackson.databind.JsonNode;
import org.gasoft.json_schema.results.IValidationResult;
import org.reactivestreams.Publisher;

@FunctionalInterface
public interface IValidator {

    Publisher<IValidationResult> validate(JsonNode instance, JsonPointer instanceLocation, IValidationContext context);
}
