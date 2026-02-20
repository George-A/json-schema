package io.github.georgeakulov.json_schema;

import com.fasterxml.jackson.databind.JsonNode;
import org.jspecify.annotations.Nullable;

import java.net.URI;

/**
 * The result of id resolution.
 * <br/>
 * If both methods return null, the resolution algorithm from the specification will be applied.
 */
public interface IExternalResolutionResult {

    /**
     * If not null, then this schema will be used.
     */
    default @Nullable JsonNode getSchema() {
        return null;
    }

    /**
     * If not null, then the schema will be loaded from this URI.
     * If another method returned not null, then it will be assumed that its schema
     * was loaded from this URI and the schema will not be loaded.
     */
    default @Nullable URI getAbsoluteUri() {
        return null;
    }
}
