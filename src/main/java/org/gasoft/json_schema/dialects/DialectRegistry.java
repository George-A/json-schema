package org.gasoft.json_schema.dialects;

import com.google.common.collect.Maps;
import org.jspecify.annotations.Nullable;

import java.net.URI;
import java.util.Map;

import static org.gasoft.json_schema.dialects.Vocabulary.Defaults.*;

public class DialectRegistry {

    public static final URI DIALECT_2020_12 = URI.create("https://json-schema.org/draft/2020-12/schema");

    private static final DialectRegistry INSTANCE = new DialectRegistry();

    private final Map<URI, DialectInfo> predefined = Maps.newHashMap();

    public static  DialectRegistry getInstance() {
        return INSTANCE;
    }

    private DialectRegistry() {
        _addDialect(new DialectInfo(DIALECT_2020_12)
                .addVocabulary(DRAFT_2020_12_CORE.getUri(), true)
                .addVocabulary(DRAFT_2020_12_APPLICATOR.getUri(), true)
                .addVocabulary(DRAFT_2020_12_UNEVALUATED.getUri(), true)
                .addVocabulary(DRAFT_2020_12_VALIDATION.getUri(), true)
                .addVocabulary(DRAFT_2020_12_FORMAT_ANNOTATION.getUri(), true)
                .addVocabulary(DRAFT_2020_12_META_DATA.getUri(), true)
                .addVocabulary(DRAFT_2020_12_CONTENT_SCHEMA.getUri(), true)
        );
    }

    private DialectRegistry _addDialect(DialectInfo dialectInfo) {
        this.predefined.put(dialectInfo.getUri(), dialectInfo);
        return this;
    }

    @Nullable DialectInfo optDialect(URI dialectUri) {
        DialectInfo info = predefined.get(dialectUri);
        if(info != null) {
            return info.copy();
        }
        return null;
    }
}
