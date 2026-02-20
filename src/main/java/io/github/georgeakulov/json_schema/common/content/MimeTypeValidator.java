package io.github.georgeakulov.json_schema.common.content;

import java.util.function.Predicate;

public record MimeTypeValidator(Predicate<MimeType> predicate, IContentValidationRegistry.ExceptionableCons validator) {
}
