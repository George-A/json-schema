package org.gasoft.json_schema.dialects;

import com.google.common.collect.Maps;
import org.jspecify.annotations.Nullable;

import java.net.URI;
import java.util.Map;

public class VocabularyRegistry {

    private final Map<URI, Vocabulary> vocabularies = Maps.newConcurrentMap();

    public VocabularyRegistry() {
        addVocabulary(Vocabulary.Defaults.DRAFT_2020_12_CORE);
        addVocabulary(Vocabulary.Defaults.DRAFT_2020_12_APPLICATOR);
        addVocabulary(Vocabulary.Defaults.DRAFT_2020_12_UNEVALUATED);
        addVocabulary(Vocabulary.Defaults.DRAFT_2020_12_VALIDATION);
        addVocabulary(Vocabulary.Defaults.DRAFT_2020_12_META_DATA);
        addVocabulary(Vocabulary.Defaults.DRAFT_2020_12_FORMAT_ANNOTATION);
        addVocabulary(Vocabulary.Defaults.DRAFT_2020_12_CONTENT_SCHEMA);
        addVocabulary(Vocabulary.Defaults.DRAFT_2020_12_FORMAT_ASSERTION);
    }

    public void addVocabulary(Vocabulary vocabulary) {
        vocabularies.put(vocabulary.getUri(), vocabulary);
    }

    public @Nullable Vocabulary optVocabulary(URI uri) {
        return vocabularies.get(uri);
    }
}
