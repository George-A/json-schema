package io.github.georgeakulov.json_schema.compilers;

import io.github.georgeakulov.json_schema.compilers.base.BaseReferenceCompiler;
import io.github.georgeakulov.json_schema.dialects.Defaults;
import io.github.georgeakulov.json_schema.loaders.IReferenceResolver.IResolutionResult;
import io.github.georgeakulov.json_schema.results.IValidationResult.ISchemaLocator;

import java.net.URI;
import java.util.stream.Stream;

public class DynamicRefCompiler extends BaseReferenceCompiler {

    @Override
    public String getKeyword() {
        return "$dynamicRef";
    }

    @Override
    public Stream<URI> getVocabularies() {
        return Stream.of(Defaults.DRAFT_2020_12_CORE);
    }


    @Override
    protected IResolutionResult resolveRef(CompileContext context, String textValue, ISchemaLocator schemaLocator) {
        return context.resolveDynamicRef(textValue, schemaLocator);
    }
}
