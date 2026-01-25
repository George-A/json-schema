package org.gasoft.json_schema.common;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.time.format.SignStyle;
import java.time.temporal.*;

public class DateTimeFormatValidation {

    public static boolean validateDate(String value) {

        try {
            var accessor = new DateTimeFormatterBuilder()
                    .appendValue(ChronoField.YEAR, 4, 4, SignStyle.NOT_NEGATIVE)
                    .appendLiteral('-')
                    .appendValue(ChronoField.MONTH_OF_YEAR, 2, 2, SignStyle.NOT_NEGATIVE)
                    .appendLiteral('-')
                    .appendValue(ChronoField.DAY_OF_MONTH, 2, 2, SignStyle.NOT_NEGATIVE)
                    .toFormatter()
                    .withResolverStyle(ResolverStyle.STRICT)
                    .parse(value);
        }
        catch(DateTimeParseException e) {
            return false;
        }
        return true;
    }

    public static boolean validateTime(String value) {

            int position = 0;
            var hour = parsePart(value, position, 23);
            if(hour == null) {
                return false;
            }
            position += 3;
            var minutes = parsePart(value, position, 59);
            if(minutes == null) {
                return false;
            }
            position += 3;

            var res = parseDec(value, position);
            if(res == null) {
                return false;
            }

            position += res.length;
            long seconds = res.value.longValue();

            if(seconds < 0 || seconds > 60) {
                return false;
            }

            if(value.length() <= position) {
                return false;
            }

            var ch = value.charAt(position);
            position ++;

            Integer oh = 0, om = 0;

            if(ch == '+' || ch == '-') {
                oh = parsePart(value, position, 23);
                if(oh == null) {
                    return false;
                }
                position += 3;
                om = parseAndCheck(value, position, 59);
                if(om == null) {
                    return false;
                }
                position += 2;
            }
            else if(!(ch == 'Z' || ch == 'z')) {
                return false;
            }

            if(seconds == 60) {
                var time = hour * 60 + minutes;
                var offset = oh * 60 + om;
                int calc;
                if(ch == '-') {
                    calc = time + offset;
                }
                else if(ch == '+') {
                    calc = time - offset;
                }
                else {
                    calc = time;
                }
                if(calc < 0) {
                    calc += 1440;
                }

                if(! (calc / 60 == 23 && calc % 60 == 59)) {
                    return false;
                }
            }
            return value.length() == position;
    }

    public static boolean validateDateTime(String value) {
        var parts = value.split("[Tt]");
        return parts.length == 2 && validateDate(parts[0]) & validateTime(parts[1]);
    }

    public static boolean validateDuration(String value) {

        var parts = value.split("T", -1);
        return switch(parts.length) {
            case 1 -> validateDurationDate(parts[0]) && parts[0].length() > 1;
            case 2 -> validateDurationDate(parts[0]) && validateDurationTime(parts[1]) && value.length() > 2;
            default -> false;
        };
    }

    private static boolean validateDurationTime(String value) {

        if(value.isEmpty()) {
            return false;
        }

        int position = 0;
        int bits = 0;
        while(position < value.length()) {

            var len = length(value, position);
            if(len == 0 || position + len >= value.length()) {
                return false;
            }
            var type = value.charAt(position + len);

            try {
                var bit = switch (type) {
                    case 'H' -> {
                        Long.parseLong(value, position, position + len, 10);
                        yield 1;
                    }
                    case 'M' -> {
                        Long.parseLong(value, position, position + len, 10);
                        yield  2;
                    }
                    case 'S' -> {
                        new BigDecimal(value.toCharArray(), position, len);
                        yield  4;
                    }
                    default -> 0;
                };

                if(bit == 0) {
                    return false;
                }
                if(bit <= bits) {
                    return false;
                }

                bits |= bit;
            }
            catch(NumberFormatException ignore) {
                return false;
            }

            position += len + 1;
        }
        return true;
    }

    private static boolean validateDurationDate(String value) {

        if(value.isEmpty() || value.charAt(0) != 'P') {
            return false;
        }

        int position = 1;
        int bits = 0;
        while(position < value.length()) {

            var seq = parseSeqInt(value, position);
            if(seq == null) {
                return false;
            }

            var bit = switch(seq.type) {
                case 'Y' -> 1;
                case 'M' -> 2;
                case 'W' -> bits > 0 ? 0 : 8;
                case 'D' -> 4;
                default -> 0;
            };

            if(bit == 0 || bit < bits) {
                return false;
            }

            bits |= bit;

            position += seq.length;
        }

        return true;
    }

    private static Seq parseSeqInt(String value, int pos) {
        int p = pos;
        var seq = parseInt(value, p);
        if(seq == null) {
            return null;
        }
        p += seq.length;
        if(p < value.length()) {
            return new Seq(value.charAt(p), p - pos + 1);
        }
        return null;
    }

    static SeqInt parseInt(String value, int pos) {
        int p = pos;
        int result = 0;
        while(p < value.length()) {
            var ch = value.codePointAt(p);
            if(ch < '0' || ch > '9') {
                break;
            }
            if(p - pos > 0 && result == 0) {
                return null;
            }
            result *= 10;
            result += (ch - '0');
            p++;
        }
        return new SeqInt(result, p - pos);
    }

    private static SeqDec parseDec(String value, int pos) {
        int len = length(value, pos);

        try {
            var decimal = new BigDecimal(value.toCharArray(), pos, len);
            return new SeqDec(decimal, len);
        }
        catch(NumberFormatException ignore) {
            return null;
        }
    }

    private static Integer parsePart(String from, int position, int max) {
        var result = parseAndCheck(from, position, max);
        if(result == null) {
            return null;
        }
        position += 2;
        if(from.length() <= position || from.charAt(position) != ':') {
            return null;
        }
        return result;
    }

    private static int length(String value, int pos) {
        int start = pos;
        for(; pos < value.length(); pos++) {
            var ch = value.codePointAt(pos);
            if(!((ch >= '0' && ch <= '9') || ch == '.')) {
                break;
            }
        }
        return pos - start;
    }

    private static Integer parseAndCheck(String from, int position, int max) {
        int value = 0;
        if(from.length() < position + 2) {
            return null;
        }
        for(int i = 0; i < 2; i++) {
            int ch = from.codePointAt(position + i);
            if(ch < '0' || ch > '9') {
                return null;
            }
            value *= 10;
            value += (ch - '0');
        }
        if(value <= max) {
            return value;
        }
        return null;
    }

    record SeqInt(int value, int length){}
    private record SeqDec(BigDecimal value, int length){}
    private record Seq(char type, int length){}
}
