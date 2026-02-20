package io.github.georgeakulov.json_schema.dialects;

import java.net.URI;
import java.util.List;

public class Defaults {

    // Draft 2020-12
    public static final URI DIALECT_2020_12 = URI.create("https://json-schema.org/draft/2020-12/schema");

    public static final URI DRAFT_2020_12_CORE = URI.create("https://json-schema.org/draft/2020-12/vocab/core");
    public static final URI DRAFT_2020_12_UNEVALUATED = URI.create("https://json-schema.org/draft/2020-12/vocab/unevaluated");
    public static final URI DRAFT_2020_12_APPLICATOR = URI.create("https://json-schema.org/draft/2020-12/vocab/applicator");
    public static final URI DRAFT_2020_12_VALIDATION = URI.create("https://json-schema.org/draft/2020-12/vocab/validation");
    public static final URI DRAFT_2020_12_META_DATA = URI.create("https://json-schema.org/draft/2020-12/vocab/meta-data");
    public static final URI DRAFT_2020_12_FORMAT_ANNOTATION = URI.create("https://json-schema.org/draft/2020-12/vocab/format-annotation");
    public static final URI DRAFT_2020_12_FORMAT_ASSERTION = URI.create("https://json-schema.org/draft/2020-12/vocab/format-assertion");
    public static final URI DRAFT_2020_12_CONTENT = URI.create("https://json-schema.org/draft/2020-12/vocab/content");

    // Draft 2019-09
    public static final URI DIALECT_2019_09 = URI.create("https://json-schema.org/draft/2019-09/schema");

    public static final URI DRAFT_2019_09_CORE = URI.create("https://json-schema.org/draft/2019-09/vocab/core");
    public static final URI DRAFT_2019_09_APPLICATOR = URI.create("https://json-schema.org/draft/2019-09/vocab/applicator");
    public static final URI DRAFT_2019_09_VALIDATION = URI.create("https://json-schema.org/draft/2019-09/vocab/validation");
    public static final URI DRAFT_2019_09_META_DATA = URI.create("https://json-schema.org/draft/2019-09/vocab/meta-data");
    public static final URI DRAFT_2019_09_FORMAT = URI.create("https://json-schema.org/draft/2019-09/vocab/format");
    public static final URI DRAFT_2019_09_CONTENT = URI.create("https://json-schema.org/draft/2019-09/vocab/content");

    // Draft 07
    public static final URI DIALECT_07 = URI.create("http://json-schema.org/draft-07/schema");

    public static final URI DRAFT_07_CORE = URI.create("https://json-schema.org/draft-07/vocab/core");

    public static boolean isDialectLaterThan(URI current, URI later) {
        if(current.equals(later)) {
            return false;
        }
        URI firstFound = DIALECT_ORDERS.reversed()
                .stream()
                .filter(d -> d.equals(later) || d.equals(current))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Dialect not found"));
        return firstFound.equals(current);
    }

    private static final List<URI> DIALECT_ORDERS = List.of(
            DIALECT_07,
            DIALECT_2019_09,
            DIALECT_2020_12
    );
}
