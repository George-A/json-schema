package io.github.georgeakulov.json_schema.compilers;

import com.fasterxml.jackson.core.JsonPointer;
import com.fasterxml.jackson.databind.JsonNode;
import io.github.georgeakulov.json_schema.IContentProcessing.ContentValidationLevel;
import io.github.georgeakulov.json_schema.common.content.ContentUtils;
import io.github.georgeakulov.json_schema.common.LocatedSchemaCompileException;
import io.github.georgeakulov.json_schema.common.content.ContentUtils.ValidationItem;
import io.github.georgeakulov.json_schema.common.content.MimeType;
import io.github.georgeakulov.json_schema.compilers.ICompiler.ICompileAction;
import io.github.georgeakulov.json_schema.compilers.ICompiler.IValidatorAction;
import io.github.georgeakulov.json_schema.dialects.Defaults;
import io.github.georgeakulov.json_schema.dialects.Vocabulary;
import io.github.georgeakulov.json_schema.results.IValidationResult;
import io.github.georgeakulov.json_schema.results.IValidationResult.ISchemaLocator;
import org.jspecify.annotations.Nullable;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static io.github.georgeakulov.json_schema.results.ValidationResultFactory.createId;
import static io.github.georgeakulov.json_schema.results.ValidationResultFactory.createOk;

public class ContentTypesCompilerFactory implements ICompilerFactory, IValidatorsTransformer {

    @Override
    public Stream<IVocabularySupport> getSupportedKeywords() {
        return Stream.of(
                CompilerRegistry.VocabularySupport.of(Defaults.DRAFT_07_CORE, "contentMediaType", "contentEncoding"),
                CompilerRegistry.VocabularySupport.of(Defaults.DRAFT_2019_09_CONTENT, "contentMediaType", "contentEncoding", "contentSchema"),
                CompilerRegistry.VocabularySupport.of(Defaults.DRAFT_2020_12_CONTENT, "contentMediaType", "contentEncoding", "contentSchema")
        );
    }

    @Override
    public @Nullable ICompiler getCompiler(String keyword, Vocabulary vocabulary) {
        return switch(keyword) {
            case "contentMediaType" -> new ContentMediaTypeCompiler();
            case "contentEncoding" -> new ContentEncodingCompiler();
            case "contentSchema" -> new ContentSchemaCompiler();
            default -> null;
        };
    }

    private static abstract class BaseContentCompiler implements ICompiler{

        @Override
        public @Nullable IValidator compile(JsonNode schemaNode, CompileContext compileContext, ISchemaLocator schemaLocator) {
            URI dialect = compileContext.getDialect(schemaLocator).getURI();
            var level = compileContext.getConfig().getContentValidationLevel();
            if(level == ContentValidationLevel.DISABLE) {
                return null;
            }
            if(level == ContentValidationLevel.DEFAULT && Defaults.isDialectLaterThan(dialect, Defaults.DIALECT_07)) {
                return null;
            }
            return compileImpl(schemaNode, compileContext, schemaLocator);
        }

        protected abstract IValidator compileImpl(JsonNode schemaNode, CompileContext compileContext, ISchemaLocator schemaLocator);
    }

    private static class ContentMediaTypeCompiler extends BaseContentCompiler{
        private MimeType contentMediaType;
        @Override
        public @Nullable IValidator compileImpl(JsonNode schemaNode, CompileContext compileContext, ISchemaLocator schemaLocator) {
            LocatedSchemaCompileException.checkIt(schemaNode.isTextual(), "The contentMediaType value must be a string. Actual {0}", schemaNode);
            contentMediaType = MimeType.create(schemaNode.asText());
            return (instance, instanceLocation, context) ->
                    createOk(createId(schemaLocator, instanceLocation))
                            .publish();
        }
    }

    private static class ContentEncodingCompiler extends BaseContentCompiler {

        private String contentEncoding;

        @Override
        public @Nullable IValidator compileImpl(JsonNode schemaNode, CompileContext compileContext, ISchemaLocator schemaLocator) {
            LocatedSchemaCompileException.checkIt(schemaNode.isTextual(), "The contentEncoding value must be a string. Actual {0}", schemaNode);
            contentEncoding = schemaNode.asText();
            return (instance, instanceLocation, context) ->
                    createOk(createId(schemaLocator, instanceLocation))
                            .publish();
        }
    }

    private static class ContentSchemaCompiler extends BaseContentCompiler {

        private ContentMediaTypeCompiler contentMediaTypeCompiler;
            private Function<JsonNode, Publisher<IValidationResult>> func;

        @Override
        public @Nullable IValidator compileImpl(JsonNode schemaNode, CompileContext compileContext, ISchemaLocator schemaLocator) {

            if(contentMediaTypeCompiler != null && contentMediaTypeCompiler.contentMediaType.hasJsonContent()) {
                var value = compileContext.compileRoot(schemaNode);
                if(compileContext.getConfig().getContentValidationLevel() == ContentValidationLevel.ENCODING_AND_SCHEMA) {
                    func = value;
                    return (instance, instanceLocation, context) ->
                            createOk(createId(schemaLocator, instanceLocation))
                            .publish();
                }
            }
            return null;
        }

        @Override
        public void resolveCompilationOrder(List<ICompileAction> current, CompileContext compileContext, ISchemaLocator schemaLocator) {
            current.stream()
                    .filter(action -> action.keyword().equals("contentMediaType"))
                    .findAny()
                    .ifPresent(action -> contentMediaTypeCompiler = ((ContentMediaTypeCompiler)action.compiler()));

            IntStream.range(0, current.size())
                    .filter(idx -> current.get(idx).keyword().equals("contentSchema"))
                    .findAny()
                    .ifPresent(idx -> current.add(current.remove(idx)));
        }
    }

    @Override
    public void transform(Map<String, IValidatorAction> validators, CompileContext compileContext, ISchemaLocator locator) {
        IValidatorAction encoding = validators.remove("contentEncoding");
        IValidatorAction mediaType = validators.remove("contentMediaType");
        IValidatorAction contentSchema = validators.remove("contentSchema");
        if(mediaType != null || encoding != null) {
            validators.put("contentMediaType", new ContentValidator(compileContext, mediaType, encoding, contentSchema));
        }
    }

    private static class ContentValidator implements IValidatorAction, IValidator {
        private final IValidatorAction mediaType;
        private final IValidatorAction encoding;
        private final BiFunction<JsonPointer, JsonNode, Iterable<IValidationResult>> validator;

        public ContentValidator(CompileContext compileContext, IValidatorAction mediaType, IValidatorAction encoding, IValidatorAction contentSchema) {
            this.mediaType = mediaType;
            this.encoding = encoding;
            this.validator = ContentUtils.prepareValidation(
                    compileContext.getConfig().getContentValidationRegistry(),
                    encoding == null ? null : (new ValidationItem<>(encoding, ((ContentEncodingCompiler)encoding.compileAction().compiler()).contentEncoding)),
                    mediaType == null ? null : new ValidationItem<>(mediaType, ((ContentMediaTypeCompiler)mediaType.compileAction().compiler()).contentMediaType),
                    contentSchema == null ? null : new ValidationItem<>(contentSchema, ((ContentSchemaCompiler)contentSchema.compileAction().compiler()).func)
            );
        }

        @Override
        public IValidator validator() {
            return this;
        }

        @Override
        public Publisher<IValidationResult> validate(JsonNode instance, JsonPointer instanceLocation, IValidationContext context) {
            return Flux.fromIterable(validator.apply(instanceLocation, instance));
        }

        private IValidatorAction firstNonNull() {
            return mediaType == null ? encoding : mediaType;
        }

        @Override
        public ICompileAction compileAction() {
            return firstNonNull().compileAction();
        }
    }
}
