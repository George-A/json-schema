package io.github.georgeakulov.json_schema.compilers;

import com.fasterxml.jackson.databind.JsonNode;
import io.github.georgeakulov.json_schema.dialects.Defaults;
import io.github.georgeakulov.json_schema.loaders.IReferenceResolver.IResolutionResult;
import io.github.georgeakulov.json_schema.results.IValidationResult.ISchemaLocator;
import org.jspecify.annotations.Nullable;

import java.net.URI;
import java.util.List;
import java.util.stream.Stream;

import static io.github.georgeakulov.json_schema.common.LocatedSchemaCompileException.checkIt;
import static io.github.georgeakulov.json_schema.compilers.IdCompiler.isSame;

public class RefCompiler implements INamedCompiler {

    @Override
    public String getKeyword() {
        return "$ref";
    }

    @Override
    public Stream<URI> getVocabularies() {
        return Stream.of(
                Defaults.DRAFT_2020_12_CORE,
                Defaults.DRAFT_2019_09_CORE,
                Defaults.DRAFT_07_CORE
        );
    }

    @Override
    public @Nullable IValidator compile(JsonNode schemaNode, CompileContext compileContext, ISchemaLocator schemaLocator) {
        checkIt(schemaNode.isTextual(), schemaLocator,"The {0} keyword value must be an string", getKeyword());

        IResolutionResult result = compileContext.resolveRef(schemaNode.textValue(), schemaLocator);
        ISchemaLocator locator = result.getResolvedLocator();
        if (isSame(schemaLocator, result.getResolvedLocator())) {
            locator = schemaLocator;
        }
        JsonNode navigatedToPtr = result.getSchema().at(result.getReferencedPtr());
        checkIt(!navigatedToPtr.isMissingNode(), schemaLocator,
                "The {0} keyword resolution result is invalid. Reference not exists in resolve result {1}",
                getKeyword(), result);

        return  compileContext.compile(navigatedToPtr, locator);
    }

    @Override
    public int resolveOperationOrderSort() {
        return -1;
    }

    @Override
    public void resolveCompilationOrder(List<ICompileAction> current, CompileContext compileContext, ISchemaLocator schemaLocator) {
        if(compileContext.getDialect(schemaLocator).getURI().equals(Defaults.DIALECT_07)) {
            ICompileAction refAction = current.stream()
                    .filter(action -> action.keyword().equals(getKeyword()))
                    .findAny()
                    .orElse(null);
            if(refAction != null) {
                current.clear();
                current.add(refAction);
            }
        }
    }
}
