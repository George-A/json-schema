package org.gasoft.json_schema.common.unicode;

import com.google.common.collect.*;
import org.jspecify.annotations.NonNull;

import java.util.Map;

public class JoinType {

    private static final Map<String, RangeSet<Integer>> JOIN_TYPES = preloadJoinTypes();

    public static boolean isNotTransparent(int code) {
        return !evalType("T", code);
    }

    public static boolean isDual(int code) {
        return evalType("D", code);
    }

    public static boolean isLeft(int code) {
        return evalType("L", code);
    }

    public static boolean isRight(int code) {
        return evalType("R", code);
    }

    private static boolean evalType(String type, int code) {
        var range = JOIN_TYPES.get(type);
        return range != null && range.contains(code);
    }

    private static Map<String, RangeSet<Integer>> preloadJoinTypes() {
        Map<String, RangeSet<@NonNull Integer>> result = Maps.newHashMap();
        ParseUtils.forEachLine("DerivedJoiningTypeShort.txt", line -> {
            var parseResult = ParseUtils.parseLine(line);
            if(parseResult != null) {
                result.put(parseResult.name(), parseResult.rangeSet());
            }
        });
        return result;
    }
}
