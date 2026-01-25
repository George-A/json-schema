package org.gasoft.json_schema.dialects;

import com.google.common.collect.Sets;

import java.net.URI;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Stream;

public class Vocabulary {

    private final URI vocabularyUri;
    private final Set<String> keywords = Sets.newHashSet();

    public Vocabulary(URI vocabularyUri) {
        this.vocabularyUri = vocabularyUri;
    }

    public Vocabulary(String uri) {
        this(URI.create(uri));
    }

    public URI getUri() {
        return vocabularyUri;
    }

    public Vocabulary addKeyword(String ... keyword) {
        this.keywords.addAll(Arrays.asList(keyword));
        return this;
    }

    public Stream<String> keywordStream() {
        return keywords.stream();
    }

    public static class Defaults {
        public static final Vocabulary DRAFT_2020_12_CORE = new Vocabulary("https://json-schema.org/draft/2020-12/vocab/core")
                .addKeyword("$id", "$schema", "$ref", "$anchor", "$dynamicRef",
                        "$dynamicAnchor", "$vocabulary", "$comment", "$defs");

        public static final Vocabulary DRAFT_2020_12_UNEVALUATED = new Vocabulary("https://json-schema.org/draft/2020-12/vocab/unevaluated")
                .addKeyword("unevaluatedProperties", "unevaluatedItems");

        public static final Vocabulary DRAFT_2020_12_APPLICATOR = new Vocabulary("https://json-schema.org/draft/2020-12/vocab/applicator")
                .addKeyword("prefixItems", "items", "contains", "additionalProperties",
                        "properties", "patternProperties", "dependentSchemas", "propertyNames", "if", "then", "else",
                        "allOf", "anyOf", "oneOf", "not", "dependencies");

        public static final Vocabulary DRAFT_2020_12_VALIDATION = new Vocabulary("https://json-schema.org/draft/2020-12/vocab/validation")
                .addKeyword("type", "const", "enum", "multipleOf", "maximum", "exclusiveMaximum", "minimum", "exclusiveMinimum",
                        "maxLength", "minLength", "pattern", "maxItems", "minItems", "uniqueItems", "maxContains", "minContains",
                        "maxProperties", "minProperties", "required", "dependentRequired");

        public static final Vocabulary DRAFT_2020_12_META_DATA = new Vocabulary("https://json-schema.org/draft/2020-12/vocab/meta-data")
                .addKeyword("title", "description", "deprecated", "readOnly", "writeOnly", "examples");


        public static final Vocabulary DRAFT_2020_12_FORMAT_ANNOTATION = new Vocabulary("https://json-schema.org/draft/2020-12/vocab/format-annotation")
                .addKeyword("format");

        public static final Vocabulary DRAFT_2020_12_FORMAT_ASSERTION = new Vocabulary("https://json-schema.org/draft/2020-12/vocab/format-assertion")
                .addKeyword("format");

        public static final Vocabulary DRAFT_2020_12_CONTENT_SCHEMA = new Vocabulary("https://json-schema.org/draft/2020-12/vocab/content")
                .addKeyword("contentEncoding", "contentMediaType", "contentSchema");


    }
}
