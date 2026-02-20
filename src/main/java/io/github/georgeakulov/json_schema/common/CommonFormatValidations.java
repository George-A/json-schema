package io.github.georgeakulov.json_schema.common;

import java.net.URI;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Pattern;

public class CommonFormatValidations {

    private static final String UUID_REGEX = "(([0-9A-F]{8,8})-([0-9A-F]{4,4})-([0-9A-F]{4,4})-([0-9A-F]{4,4})-([0-9A-F]{12,12}))";
    private static final String URI_NONASCII_REGEX = "^([\\x00-\\x7f]+)$";
    private static final String IP4_REGEX = "^((25[0-5]|(2[0-4]|1\\d|[1-9]|)\\d)\\.?\\b){4}$";

    public static Predicate<String> getUUIDFormatValidator() {
        return new PatternPredicate(UUID_REGEX, Pattern.CASE_INSENSITIVE);
    }

    public static Predicate<String> getIpv6Validator() {
        return new Ipv6Validator();
    }

    private static Predicate<String> onlyAscii() {
        return Pattern.compile(URI_NONASCII_REGEX).asMatchPredicate();
    }

    public static Predicate<String> getURIValidator() {
        return onlyAscii()
                .and(str -> {
                    try {
                        return URI.create(str).isAbsolute();
                    }
                    catch(Exception ignore) {
                        return false;
                    }
                });
    }

    private static <T> Function<T, Boolean> toFunc(Predicate<T> func) {
        return func::test;
    }

    public static Predicate<String> getIriValidator() {
        return toUri()
                .andThen(
                        toFunc(
                                Predicate.<URI>not(Objects::isNull)
                                        .and(getIriRefPredicate())
                                        .and(URI::isAbsolute)
                        )
                )
                ::apply;
    }

    public static Predicate<String> getIriReferenceValidator() {
        return toUri()
                .andThen(
                        toFunc(
                            Predicate.<URI>not(Objects::isNull)
                                    .and(getIriRefPredicate())
                        )
                )
                ::apply;
    }

    private static Function<String, URI> toUri() {
        return str -> {
            try {
                return URI.create(str);
            } catch (Exception e) {
                return null;
            }
        };
    }

    private static Predicate<URI> getIriRefPredicate() {
        final var ipv6 = getIpv6Validator();
        return uri -> {
            var auth = uri.getAuthority();
            if(auth != null) {
                if(ipv6.test(auth)) {
                    // Ipv6 must be in square brackets
                    return auth.startsWith("[") && auth.endsWith("]");
                }
            }
            return true;
        };
    }

    public static Predicate<String> getIpv4Validator() {
        return Pattern.compile(IP4_REGEX).asPredicate();
    }

    public static Predicate<String> getURIReferenceValidator() {
        Predicate<String> predicate = Pattern.compile(URI_NONASCII_REGEX, Pattern.CASE_INSENSITIVE).asPredicate();
        return str -> {
            try {
                if (predicate.test(str)) {
                    var ignore = URI.create(str);
                    return true;
                }
                return false;
            }
            catch(Exception e) {
                return false;
            }
        };
    }

    public static Predicate<String> getJsonPointerValidator() {
        return str -> {
            try {
                return checkJsonPointer(str);
            }
            catch(IllegalArgumentException ignore) {
                return false;
            }
        };
    }

    public static Predicate<String> getRelativeJsonPointerValidator() {
        return str -> {
            try {
                return checkRelativeJsonPointer(str);
            }
            catch(Exception e) {
                return false;
            }
        };
    }

    private static class PatternPredicate implements Predicate<String> {

        private final Predicate<String> pattern;

        public PatternPredicate(String pattern, int flags) {
            this.pattern = Pattern.compile(pattern, flags).asMatchPredicate();
        }

        @Override
        public boolean test(String s) {
            return pattern.test(s);
        }
    }

    private static boolean checkRelativeJsonPointer(String value) {
        if(value.isEmpty()) {
            return false;
        }
        var pos = 0;
        var seq = DateTimeFormatValidation.parseInt(value, 0);
        if(seq == null || seq.length() == 0) {
            return false;
        }
        pos += seq.length();
        if(value.endsWith("#")) {
            return checkJsonPointer(value, pos, value.length() - 1);
        }
        else {
            return checkJsonPointer(value, pos, value.length());
        }
    }

    private static boolean checkJsonPointer(String value) {
        return checkJsonPointer(value, 0, value.length());
    }

    private static boolean checkJsonPointer(String value, int from, int to) {
        if(from == to) {
            return true;
        }
        int pos = from;
        while(pos < to) {
            var parsedLength = parsePart(value, pos, to);
            if(parsedLength == null) {
                return false;
            }
            pos += parsedLength;
        }
        return true;
    }

    private static Integer parsePart(String from, int pos, int to) {
        int start = pos;
        if(from.charAt(pos++) != '/') {
            return null;
        }

        while(pos < to) {

            var ch = from.charAt(pos);
            if(ch == '/') {
                break;
            }

            pos ++;
            if(ch == '~') {

                if(pos >= to) {
                    return null;
                }

                var next = from.charAt(pos++);
                if(!(next == '1' || next == '0')) {
                    return null;
                }
            }
        }
        return pos - start;
    }
}
