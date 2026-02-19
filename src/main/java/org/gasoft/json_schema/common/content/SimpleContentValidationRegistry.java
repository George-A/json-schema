package org.gasoft.json_schema.common.content;

import org.jspecify.annotations.Nullable;

import java.util.*;

public class SimpleContentValidationRegistry implements IContentValidationRegistry {

    private final Map<String, ExceptionableOp> contentEncoding = new HashMap<>();
    private final List<MimeTypeValidator> contentTypeValidators = new ArrayList<>();

    @Override
    public @Nullable ExceptionableOp getContentEncodingFn(String contentEncoding) {
        return this.contentEncoding.get(contentEncoding);
    }

    @Override
    public @Nullable ExceptionableCons getContentTypeFn(MimeType mimeType) {
        return contentTypeValidators.stream()
                .filter(val -> val.predicate().test(mimeType))
                .map(MimeTypeValidator::validator)
                .findAny()
                .orElse(null);
    }

    public void addAllContentEncodingValidator(Map<String, ExceptionableOp> validators) {
        this.contentEncoding.putAll(validators);
    }

    public void addContentTypeValidator(MimeTypeValidator validator) {
        this.contentTypeValidators.addFirst(validator);
    }

    public void addContentEncodingValidator(String encoding, ExceptionableOp op) {
        this.contentEncoding.put(encoding, op);
    }
}
