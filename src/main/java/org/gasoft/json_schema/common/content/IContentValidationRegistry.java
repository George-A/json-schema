package org.gasoft.json_schema.common.content;

import org.jspecify.annotations.Nullable;

public interface IContentValidationRegistry {

    @Nullable
    ExceptionableOp getContentEncodingFn(String contentEncoding);

    @Nullable
    ExceptionableCons getContentTypeFn(MimeType mimeType);

    interface ExceptionableOp {
        @Nullable String apply(String s) throws Exception;
    }

    interface ExceptionableCons {
        void accept(String s) throws Exception;
    }}
