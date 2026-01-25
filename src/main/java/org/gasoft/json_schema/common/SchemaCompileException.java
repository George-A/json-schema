package org.gasoft.json_schema.common;

import com.google.common.base.Strings;
import org.jspecify.annotations.NonNull;

public class SchemaCompileException extends RuntimeException {

    protected SchemaCompileException(String message) {
        super(message);
    }

    protected SchemaCompileException(String message, Throwable cause) {
        super(message, cause);
    }

    public static SchemaCompileException create(String msg, Object ... args) {
        return new SchemaCompileException(Strings.lenientFormat(msg, args));
    }


    public static SchemaCompileException create(Throwable thr, String msg, Object ... args) {
        return new SchemaCompileException(Strings.lenientFormat(msg, args), thr);
    }

    public static void checkIt(boolean value, String msg, Object ... args) {
        if(!value) {
            throw new SchemaCompileException(Strings.lenientFormat(msg, args));
        }
    }

    public static <T> @NonNull T checkNonNull(T obj, String msg, Object ... args) {
        if(obj == null) {
            throw new SchemaCompileException(Strings.lenientFormat(msg, args));
        }
        return obj;
    }
}
