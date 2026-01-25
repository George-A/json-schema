package org.gasoft.json_schema.common.unicode;

import com.google.common.collect.RangeSet;
import com.google.common.collect.TreeRangeSet;

public class Unicode {


    private static final RangeSet<Integer> VIRAMA = preloadUnicodeData();

    public static boolean isVirama(int codePoint) {
        return VIRAMA.contains(codePoint);
    }

    private static RangeSet<Integer> preloadUnicodeData() {
        RangeSet<Integer> rangeSet = TreeRangeSet.create();
        ParseUtils.forEachLine(
                "UnicodeDataShort.txt",
                line -> rangeSet.addAll(ParseUtils.parseNumbers(line))
        );
        return rangeSet;
    }
}
