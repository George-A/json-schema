package io.github.georgeakulov.json_schema;

import com.fasterxml.jackson.databind.JsonNode;
import io.github.georgeakulov.json_schema.common.JsonUtils;
import io.github.georgeakulov.json_schema.results.IValidationResult;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Mono;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

/**
 * Compiled schema<br/>
 * immutable and thread safe
 */
public class Schema implements Function<JsonNode, IValidationResult> {

    private final Function<JsonNode, Publisher<IValidationResult>> validationFunction;

    Schema(Function<JsonNode, Publisher<IValidationResult>> validationFunction) {
        this.validationFunction = Objects.requireNonNull(validationFunction);
    }
    /**
     * Non blocking validation call
     * @param node json data instance to validate
     * @return {@link CompletableFuture}
     * @throws NullPointerException if {@code node} is null
     */
    public CompletableFuture<IValidationResult> asFuture(JsonNode node) {
        return Mono.from(map(node)).toFuture();
    }

    /**
     * Non blocking validation call
     * @param jsonString json data string representation to validate
     * @return {@link CompletableFuture}
     * @throws NullPointerException if {@code node} is null
     * @throws IllegalArgumentException if {jsonString} is no valid json
     */
    public CompletableFuture<IValidationResult> asFuture(String jsonString) {
        return Mono.from(map(jsonString)).toFuture();
    }

    /**
     * Map validation process to {@link Publisher}
     * @param node json data instance to validate
     * @return {@link Publisher<IValidationResult>}
     * @throws NullPointerException if {@code node} is null
     */
    public Publisher<IValidationResult> map(JsonNode node) {
        Objects.requireNonNull(node, "The node instance is null");
        return validationFunction.apply(node);
    }

    /**
     * Map validation process to {@link Publisher}
     * @param jsonString json data instance to validate
     * @return {@link Publisher<IValidationResult>}
     * @throws NullPointerException if {@code jsonString} is null
     * @throws IllegalArgumentException if (@code jsonString) is not valid json
     */
    public Publisher<IValidationResult> map(String jsonString) {
        Objects.requireNonNull(jsonString, "The jsonString is null");
        return validationFunction.apply(JsonUtils.parse(jsonString));
    }

    /**
     * Blocking call of validation process
     * @return {@link IValidationResult} result of validation
     * @throws NullPointerException if {@code node} is null
     */
    @Override
    public IValidationResult apply(JsonNode node) {
        return Mono.from(map(node)).block();
    }

    /**
     * Blocking call of validation process
     * @return {@link IValidationResult} result of validation
     * @throws NullPointerException if {@code jsonString} is null
     * @throws  IllegalArgumentException if {@code jsonString} is not valid json
     */
    public IValidationResult apply(String jsonString) {
        return Mono.from(map(jsonString)).block();
    }
}
