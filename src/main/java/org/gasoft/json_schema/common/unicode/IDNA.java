package org.gasoft.json_schema.common.unicode;

import com.google.common.collect.Maps;
import com.google.common.collect.RangeSet;
import org.jspecify.annotations.NonNull;

import java.util.Map;

public class IDNA {

    private static final Map<String, RangeSet<Integer>> IDNA_TABLE = preloadIDNA();

    public static boolean isContexto(int ch) {
        return containsIn("CONTEXTO", ch);
    }

    public static boolean isContextj(int ch) {
        return containsIn("CONTEXTJ", ch);
    }

    public static boolean isContext(int ch) {
        return isContexto(ch) || isContextj(ch);
    }

    public static boolean isDisallowed(int ch) {
        return containsIn("DISALLOWED", ch);
    }

    public static boolean isUnassigned(int ch) {
        return !Character.isDefined(ch);
    }

    private static boolean containsIn(String name, int ch) {
        var set = IDNA_TABLE.get(name);
        if(set != null) {
            return set.contains(ch);
        }
        return false;
    }

    private static Map<String, RangeSet<@NonNull Integer>> preloadIDNA() {
        Map<String, RangeSet<@NonNull Integer>> result = Maps.newHashMap();
        ParseUtils.forEachLine("IDNA2008Short.txt", line -> {
            var parseResult = ParseUtils.parseLine(line);
            if(parseResult != null) {
                result.put(parseResult.name(), parseResult.rangeSet());
            }
        });
        return result;
    }
}
