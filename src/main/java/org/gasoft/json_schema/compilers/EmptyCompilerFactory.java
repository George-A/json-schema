package org.gasoft.json_schema.compilers;

import com.fasterxml.jackson.databind.JsonNode;
import org.gasoft.json_schema.results.IValidationResult;
import org.jspecify.annotations.Nullable;

import java.util.stream.Stream;

public class EmptyCompilerFactory implements ICompilerFactory{

    private static final ICompiler EMPTY = new EmptyCompiler();

    @Override
    public Stream<String> getSupportedKeywords() {
        return Stream.of("$schema", "$comment", "$anchor", "$dynamicAnchor", "$vocabulary",
                "title", "description", "deprecated", "readOnly", "writeOnly", "examples",
                "contentEncoding", "contentMediaType", "contentSchema");
    }

    @Override
    public @Nullable ICompiler getCompiler(String keyword) {
        return EMPTY;
    }

    private static class EmptyCompiler implements ICompiler {
        @Override
        public @Nullable IValidator compile(JsonNode schemaNode, CompileContext compileContext, IValidationResult.ISchemaLocator schemaLocator) {
            return null;
        }
    }
}
