package io.github.georgeakulov.json_schema.compilers;

import io.github.georgeakulov.json_schema.compilers.base.BaseIntegerCompiler;
import io.github.georgeakulov.json_schema.dialects.Defaults;
import io.github.georgeakulov.json_schema.results.EErrorType;
import io.github.georgeakulov.json_schema.results.IValidationResult.ISchemaLocator;
import io.github.georgeakulov.json_schema.results.ValidationError;
import io.github.georgeakulov.json_schema.results.ValidationResultFactory;

import java.net.URI;
import java.util.stream.Stream;

import static io.github.georgeakulov.json_schema.common.LocatedSchemaCompileException.checkIt;

public class MinItemsCompiler extends BaseIntegerCompiler {

    @Override
    public String getKeyword() {
        return "minItems";
    }

    @Override
    public Stream<URI> getVocabularies() {
        return Stream.of(
                Defaults.DRAFT_2020_12_VALIDATION,
                Defaults.DRAFT_2019_09_VALIDATION,
                Defaults.DRAFT_07_CORE
        );
    }

    @Override
    protected IValidator compile(int minItems, CompileContext compileContext, ISchemaLocator schemaLocation) {
        checkIt(minItems >= 0, schemaLocation, "The {0} keyword value must be non-negative. Actual: {1}", getKeyword(), minItems);
        return (node, instancePtr, context) -> {

            var id = ValidationResultFactory.createId(schemaLocation, instancePtr);

            if(node.isArray() && node.size() < minItems) {
                return ValidationError.create(id, EErrorType.MIN_ITEMS, minItems, node.size())
                        .publish();
            }
            return ValidationResultFactory.createOk(id).publish();
        };
    }
}
