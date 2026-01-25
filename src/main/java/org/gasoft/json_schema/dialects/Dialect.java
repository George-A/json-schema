package org.gasoft.json_schema.dialects;

import org.gasoft.json_schema.common.SchemaCompileException;
import org.gasoft.json_schema.compilers.CommonCompilersFactory;
import org.gasoft.json_schema.compilers.ICompiler;
import org.jspecify.annotations.Nullable;

import java.net.URI;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.gasoft.json_schema.common.SchemaCompileException.checkNonNull;

public class Dialect {

    private final DialectInfo dialectInfo;
    private final Set<String> keywords;

    private Dialect(DialectInfo dialectInfo, Set<String> keywords) {
        this.dialectInfo = dialectInfo;
        this.keywords = keywords;
    }

    static Dialect create(DialectInfo dialectInfo, Function<URI, Vocabulary> vocabularyResolver) {
        return new Dialect(dialectInfo,
                dialectInfo.asStream()
                        .map(state -> {
                            var cod = vocabularyResolver.apply(state.vocabulary());
                            if(cod == null) {
                                if(state.state()) {
                                    throw SchemaCompileException.create("The vocabulary %s is required but not registered", state.vocabulary());
                                }
                            }
                            return cod;
                        })
                        .filter(Objects::nonNull)
                        .flatMap(Vocabulary::keywordStream)
                        .collect(Collectors.toSet())
        );
    }

    public boolean isAssertionRequired() {
        return dialectInfo.getVocabularyState(Vocabulary.Defaults.DRAFT_2020_12_FORMAT_ASSERTION.getUri())
                .map(DialectInfo.VocabularyState::state)
                .orElse(false);
    }

    public boolean hasKeyword(String keyword) {
        return keywords.contains(keyword);
    }

    public @Nullable ICompiler optCompiler(String keyword) {
        if(hasKeyword(keyword)) {
            return checkNonNull(CommonCompilersFactory.getCompilerRegistry().optCompiler(keyword), "Can`t find compiler for keyword %s", keyword);
        }
        return null;
    }
}
