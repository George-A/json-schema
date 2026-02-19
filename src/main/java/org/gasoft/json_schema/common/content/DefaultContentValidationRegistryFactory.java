package org.gasoft.json_schema.common.content;

import org.gasoft.json_schema.common.JsonUtils;

public class DefaultContentValidationRegistryFactory {

    private static final SimpleContentValidationRegistry INSTANCE  = new SimpleContentValidationRegistry();

    public static IContentValidationRegistry getDefault() {
        return INSTANCE;
    }

    private DefaultContentValidationRegistryFactory() {
    }

    static {
        INSTANCE.addContentEncodingValidator("7bit", ContentUtils::check7BitEncoding);
        INSTANCE.addContentEncodingValidator("base64", ContentUtils::checkBase64);
        INSTANCE.addContentEncodingValidator("quoted-printable", ContentUtils::checkQuotedPrintable);
    }

    static {
        INSTANCE.addContentTypeValidator(new MimeTypeValidator(
                MimeType::hasJsonContent,
                JsonUtils::parse
        ));
    }
}
