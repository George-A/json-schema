package org.gasoft.json_schema.dialects;

import com.fasterxml.jackson.databind.JsonNode;
import org.jspecify.annotations.Nullable;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.AbstractMap;
import java.util.Map;
import java.util.function.Function;

import static org.gasoft.json_schema.common.SchemaCompileException.checkIt;

public class DialectResolver {

    private final DialectRegistry dialectRegistry;
    private final VocabularyRegistry vocabularyRegistry;

    public DialectResolver(DialectRegistry dialectRegistry, VocabularyRegistry vocabularyRegistry) {
        this.dialectRegistry = dialectRegistry;
        this.vocabularyRegistry = vocabularyRegistry;
    }

    public Dialect optDefaultDialect(URI uri) {
        var result = dialectRegistry.optDialect(uri);
        if(result != null) {
            return Dialect.create(result, vocabularyRegistry::optVocabulary);
        }
        return null;
    }

    @Nullable
    public Dialect resolveDialect(JsonNode schema, Function<URI, JsonNode> resourceLoader) {

        if(!schema.has("$schema")) {
            return null;
        }

        Map.Entry<JsonNode, DialectInfo> info = new AbstractMap.SimpleEntry<>(schema, null);

        info.setValue(resolveExistingDialect(schema));
        if(info.getValue() == null) {
            info = resolveUnknownDialect(schema, resourceLoader);
            if(info.getKey() == null || info.getValue() == null) {
                // Can`t resolve dialect
                return null;
            }
        }

        return tryApplyVocabularies(info.getValue(), info.getKey());
    }

    private Dialect tryApplyVocabularies(DialectInfo dialectInfo, JsonNode schema) {
        JsonNode vocabularies = schema.path("$vocabulary");
        if(vocabularies.isMissingNode()) {
            return Dialect.create(dialectInfo, vocabularyRegistry::optVocabulary);
        }

        checkIt(vocabularies.isObject(), "The $vocabulary must be an object");
        dialectInfo.clearVocabularies();
        vocabularies.propertyStream()
                .forEach(entry -> {
                    URI vocabulary = cleanUpFragment(URI.create(entry.getKey()));
                    checkIt(entry.getValue().isBoolean(), "The values of $vocabulary keyword properties must be boolean");
                    var used = entry.getValue().asBoolean();
                    if(used) {
                        checkVocabulary(vocabulary);
                    }
                    dialectInfo.addVocabulary(vocabulary, used);
                });
        return Dialect.create(dialectInfo, vocabularyRegistry::optVocabulary);
    }

    private void checkVocabulary(URI vocabulary) {
        var existing = vocabularyRegistry.optVocabulary(vocabulary);
        if(existing == null) {
            throw new RuntimeException("The unknown vocabulary " + vocabulary);
        }
    }

    private URI cleanUpFragment(URI from) {
        try {
            return new URI(
                    from.getScheme(),
                    from.getUserInfo(),
                    from.getHost(),
                    from.getPort(),
                    from.getPath(),
                    from.getQuery(),
                    null
            ).normalize();
        } catch (URISyntaxException e) {
            throw new RuntimeException("Error to cleanup URI:" + from, e);
        }
    }

    private Map.Entry<JsonNode, DialectInfo> resolveUnknownDialect(JsonNode schema, Function<URI, JsonNode> resourceLoader) {
        JsonNode unknownSchema = resourceLoader.apply(URI.create(schema.get("$schema").textValue()));
        return new AbstractMap.SimpleEntry<>(unknownSchema, resolveExistingDialect(unknownSchema));
    }


    private @Nullable DialectInfo resolveExistingDialect(JsonNode schema) {
        var node = schema.path("$schema");
        if(node.isMissingNode()) {
            return null;
        }
        URI uri = URI.create(node.textValue());
        return  dialectRegistry.optDialect(uri);
    }

}
