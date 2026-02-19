package org.gasoft.json_schema.common.content;

import java.util.function.Predicate;

public record MimeTypeValidator(Predicate<MimeType> predicate, IContentValidationRegistry.ExceptionableCons validator) {
}
