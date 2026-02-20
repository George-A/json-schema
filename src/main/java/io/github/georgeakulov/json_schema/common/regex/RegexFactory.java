package io.github.georgeakulov.json_schema.common.regex;


import io.github.georgeakulov.json_schema.IRegexPredicateFactory;
import org.jspecify.annotations.NonNull;

import java.util.function.Predicate;
import java.util.regex.Pattern;

public class RegexFactory {

    public static IRegexPredicateFactory jdk(){
        return new JdkRegexPredicateFactory();
    }

    private static class JdkRegexPredicateFactory implements IRegexPredicateFactory {

        @Override
        public @NonNull Predicate<String> compile(String value) {
            Pattern pattern = Pattern.compile(value, Pattern.UNICODE_CASE | Pattern.UNICODE_CHARACTER_CLASS);
            return pattern.asPredicate();
        }
    }
}
