package org.gasoft.json_schema.compilers;

import com.fasterxml.jackson.core.JsonPointer;
import com.fasterxml.jackson.databind.JsonNode;
import org.gasoft.json_schema.dialects.Defaults;
import org.gasoft.json_schema.results.IValidationResult;
import org.jspecify.annotations.Nullable;

import java.net.URI;
import java.util.stream.Stream;

public class DefinitionsCompiler implements INamedCompiler {
    @Override
    public String getKeyword() {
        return "definitions";
    }

    @Override
    public Stream<URI> getVocabularies() {
        return Stream.of(Defaults.DRAFT_07_CORE);
    }

    @Override
    public @Nullable IValidator compile(JsonNode schemaNode, CompileContext compileContext, IValidationResult.ISchemaLocator schemaLocator) {
        return null;
    }

    @Override
    public void preprocess(IPreprocessorMediator mediator, String keyword, JsonNode node, JsonPointer pointer) {
        if(mediator.getDialect().equals(Defaults.DIALECT_07) && node.isObject()) {
            node.propertyStream().forEach(entry ->
                    mediator.process(pointer.appendProperty(entry.getKey()))
            );
        }
    }
}
