package org.gasoft.json_schema.common.unicode;

import com.google.common.collect.Range;
import com.google.common.collect.RangeSet;
import com.google.common.collect.TreeRangeSet;
import com.google.common.io.LineReader;
import org.jspecify.annotations.NonNull;

import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Objects;
import java.util.function.Consumer;

class ParseUtils {

    static void forEachLine(String resourceName, Consumer<String> lineConsumer) {
        try {

            var cl = Thread.currentThread().getContextClassLoader();
            try(var is = new InputStreamReader(Objects.requireNonNull(cl.getResourceAsStream(resourceName)))){
                var lr = new LineReader(is);
                // Skip first line as title
                var line = lr.readLine();
                while(line != null) {
                    //parse line
                    lineConsumer.accept(line);
                    line = lr.readLine();
                }
            }
        }
        catch(Exception e) {
            throw new RuntimeException(e);
        }
    }

    static NamedRange parseLine(String line) {
        if(line.isBlank()){
            return null;
        }
        int sep = line.indexOf(";");
        return new NamedRange(
                line.substring(0, sep),
                parseNumbers(line.substring(sep + 1))
        );
    }

    static RangeSet<@NonNull Integer> parseNumbers(String line) {
        RangeSet<@NonNull Integer> rangeSet = TreeRangeSet.create();
        Arrays.stream(line.split(","))
                .map(ParseUtils::parseItem)
                .forEach(rangeSet::add);
        return rangeSet;
    }

    private static Range<@NonNull Integer> parseItem(String item) {
        var idx = item.indexOf("-");
        if(idx < 0) {
            return Range.singleton(Integer.parseInt(item, 16));
        }
        return Range.closed(
                Integer.parseInt(item, 0, idx, 16),
                Integer.parseInt(item, idx + 1, item.length(), 16)
        );
    }

    record NamedRange(String name, RangeSet<@NonNull Integer> rangeSet) {}
}
