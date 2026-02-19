package org.gasoft.json_schema.common.content;

import com.fasterxml.jackson.core.JsonPointer;
import com.fasterxml.jackson.databind.JsonNode;
import org.gasoft.json_schema.common.JsonUtils;
import org.gasoft.json_schema.common.SchemaCompileException;
import org.gasoft.json_schema.common.content.IContentValidationRegistry.ExceptionableOp;
import org.gasoft.json_schema.compilers.ICompiler.IValidatorAction;
import org.gasoft.json_schema.results.EErrorType;
import org.gasoft.json_schema.results.IValidationResult;
import org.gasoft.json_schema.results.ValidationError;
import org.gasoft.json_schema.results.ValidationResultFactory;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Mono;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;

public class ContentUtils {

    static final ExceptionableOp IDENTITY = str -> str;
    static final IContentValidationRegistry.ExceptionableCons EMPTY = str -> {};

    public static BiFunction<JsonPointer, JsonNode, Iterable<IValidationResult>> prepareValidation(
            IContentValidationRegistry registry,
            ValidationItem<String> contentEncoding,
            ValidationItem<MimeType> contentType,
            ValidationItem<Function<JsonNode, Publisher<IValidationResult>>> contentSchema) {

        ContentValidator validator = null;
        if(contentEncoding != null) {
            var func = registry.getContentEncodingFn(contentEncoding.value());
            validator = new ContentValidator(
                    contentEncoding.action(),
                    func == null ? IDENTITY : func,
                    EErrorType.CONTENT_ENCODING,
                    contentEncoding.value()
            );
        }

        if(contentType != null) {
            var cons = registry.getContentTypeFn(contentType.value());
            var type = new ContentValidator(
                    contentType.action(),
                    toOp(cons == null ? EMPTY : cons),
                    EErrorType.CONTENT_TYPE,
                    contentType.value()
            );
            if(validator == null) {
                validator = type;
            }
            else {
                validator.next = type;
            }
        }

        if(contentSchema != null) {

            Objects.requireNonNull(validator)
                    .next = new ContentValidator(
                            contentSchema.action(),
                            str -> {
                                JsonNode node = JsonUtils.parse(str);
                                IValidationResult result = Objects.requireNonNull(
                                        Mono.from(contentSchema.value().apply(node))
                                        .block()
                                );
                                if(!result.isOk()) {
                                    throw new IllegalArgumentException("Invalid content schema");
                                }
                                return str;
                            },
                            EErrorType.CONTENT_SCHEMA
                    );
        }

        ContentValidator resolved = SchemaCompileException.checkNonNull(validator, "No validators resolved");

        return (pointer, node) -> {
            var collector = new ResultCollector();
            resolved.validate(node, pointer, collector);
            return collector.validationResult;
        };
    }

    public record ValidationItem<T>(IValidatorAction action, T value){}

    private static class ResultCollector {
        private final List<IValidationResult> validationResult = new ArrayList<>(3);
        private String prevResult;
    }

    private static ExceptionableOp toOp(IContentValidationRegistry.ExceptionableCons cons) {
        return val -> {
            cons.accept(val);
            return val;
        };
    }

    private static class ContentValidator {
        private final IValidatorAction action;
        private final ExceptionableOp operation;
        private final EErrorType errType;
        private final Object[] args;
        private ContentValidator next;

        public ContentValidator(IValidatorAction action, ExceptionableOp operation, EErrorType errType, Object ... args) {
            this.action = action;
            this.operation = operation;
            this.errType = errType;
            this.args = args;
        }

        void validate(JsonNode node, JsonPointer pointer, ResultCollector resultCollector) {
            var id = action.compileAction().createId(pointer);
            if(node.isTextual()) {
                try {
                    String resolve = resultCollector.prevResult == null ? node.textValue() : resultCollector.prevResult;
                    resultCollector.prevResult = operation.apply(resolve);
                    resultCollector.validationResult.add(ValidationResultFactory.createOk(id));
                    if(next != null) {
                        next.validate(node, pointer, resultCollector);
                    }
                }
                catch(Exception e) {
                    resultCollector.validationResult.add(
                            ValidationError.create(id, errType, args)
                    );
                }
            }
            else {
                resultCollector.validationResult.add(ValidationResultFactory.createOk(id));
            }
        }
    }

    static String checkQuotedPrintable(String s) {
        var baos = new ByteArrayOutputStream();
        byte [] data = s.getBytes(StandardCharsets.US_ASCII);
        int i = 0;
        while(i < data.length) {
            int c = data[i] & 0xff;
            if(c == '=') {
                if (++i >= data.length) {
                    throw new IllegalArgumentException("Unexpected line end");
                }

                // May be \r
                int next = data[++i] & 0xff;
                if(next == '\r' || next == '\n') {
                    i++;
                    if(next == '\r' && i < data.length &&  (data[i] & 0xff) == '\n') {
                        i++;
                    }
                    continue;
                }

                if(++i < data.length) {
                    int next_more = data[i] & 0xff;
                    int h1 = Character.digit(next, 16);
                    int h2 = Character.digit(next_more, 16);
                    if(h1 < 0 || h2 < 0) {
                        throw new IllegalArgumentException(String.format("Invalid hex digit at sequence %c, %c", next, next_more));
                    }
                    baos.write(((h1 << 4) | h2) & 0xff);
                    i++;
                }
                else {
                    throw new IllegalArgumentException("Not full HEX pair");
                }
            }
            else {
                if((c >= 33 && c <= 60) || (c >= 62 && c <= 126) || c == 9 || c == 32) {
                    baos.write(c);
                    i++;
                }
                else {
                    throw new IllegalArgumentException(String.format("The symbol %c must be encoded", c));
                }
            }
        }
        return baos.toString(StandardCharsets.UTF_8);
    }

    static String check7BitEncoding(String s) {
        if(!s.chars().allMatch(c -> c < 128)) {
            throw new IllegalStateException("Unexpected character: " + s);
        }
        return s;
    }

    static String checkBase64(String content) throws IllegalArgumentException {
        byte [] data = Base64.getDecoder().decode(content);
        return new String(data, StandardCharsets.UTF_8);
    }

}
