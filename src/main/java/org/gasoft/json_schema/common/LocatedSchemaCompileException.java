package org.gasoft.json_schema.common;

import com.google.common.base.Strings;
import org.gasoft.json_schema.results.IValidationResult.ISchemaLocator;

public class LocatedSchemaCompileException extends SchemaCompileException {

    private final ISchemaLocator locator;

    private LocatedSchemaCompileException(ISchemaLocator locator, String message) {
        super(message);
        this.locator = locator;
    }

    public ISchemaLocator getLocator() {
        return locator;
    }

    private LocatedSchemaCompileException(ISchemaLocator locator, String message, Throwable cause) {
        super(message, cause);
        this.locator = locator;
    }

    public static LocatedSchemaCompileException create(ISchemaLocator locator, Throwable cause, String message, Object ... args) {
        return new LocatedSchemaCompileException(locator, Strings.lenientFormat(message, args), cause);
    }

    public static LocatedSchemaCompileException create(ISchemaLocator locator, String message, Object ... args) {
        return new LocatedSchemaCompileException(locator, Strings.lenientFormat(message, args));
    }

    public static void checkIt(boolean value, ISchemaLocator locator, String msg, Object... args) {
        if(!value) {
            throw new LocatedSchemaCompileException(locator, Strings.lenientFormat(msg, args));
        }
    }
}
