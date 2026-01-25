package org.gasoft.json_schema;

import org.jspecify.annotations.NonNull;

import java.util.function.Predicate;

/**
 * Regexp factory
 */
public interface IRegexPredicateFactory {

    /**
     * Compile {@code pattern} regexp and return it as predicate
     * @param pattern regular expression pattern
     * @return predicate for testing strings
     */
    @NonNull Predicate<String> compile(String pattern);
}
