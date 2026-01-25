package org.gasoft.json_schema.loaders;

import com.fasterxml.jackson.core.JsonPointer;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.Maps;
import org.gasoft.json_schema.dialects.Dialect;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.net.URI;
import java.util.Map;
import java.util.UUID;

public class SchemaInfo {

    private final Dialect dialect;
    private final UUID uuid;
    @Nullable private final URI id;
    @Nullable private final URI origin;
    @NonNull private final JsonPointer pointerOfRoot;
    @NonNull private final JsonNode content;

    private final Map<String, JsonPointer> anchors;
    private final Map<String, JsonPointer> dynamicAnchors;
    private final Map<String, UUID> subschemas;

    SchemaInfo(Dialect dialect, UUID uuid, @Nullable URI id, @Nullable URI origin,
               @NonNull JsonPointer pointerOfRoot, @NonNull JsonNode content,
               Map<String, JsonPointer> anchors, Map<String, JsonPointer> dynamicAnchors, Map<String, UUID> subschemas) {
        this.uuid = uuid;
        this.id = id;
        this.origin = origin;
        this.pointerOfRoot = pointerOfRoot;
        this.content = content;
        this.anchors = anchors;
        this.dynamicAnchors = dynamicAnchors;
        this.dialect = dialect;
        this.subschemas = subschemas;
    }

    public Dialect getDialect() {
        return dialect;
    }

    public UUID getUuid() {
        return uuid;
    }

    public @NonNull JsonPointer getPointerOfRoot() {
        return pointerOfRoot;
    }

    public JsonPointer optAnchor(String anchorValue) {
        return anchors.get(anchorValue);
    }

    public JsonPointer optDynamicAnchor(String anchorValue) {
        return dynamicAnchors.get(anchorValue);
    }

    public JsonNode getContent() {
        return content;
    }

    public @Nullable URI getId(){
        return id;
    }

    public @Nullable URI getOrigin() {
        return origin;
    }

    public @Nullable UUID resolveSubSchema(JsonPointer pointer) {
        return subschemas.get(pointer.toString());
    }

    public record SubSchemaInfo(UUID uuid, URI id, JsonPointer absolutePointer, JsonNode schema, Map<String, JsonPointer> anchors,
                                Map<String, JsonPointer> dynamicAnchors, Map<String, SubSchemaInfo> subschemas) {
        public static SubSchemaInfo emptyAnchors(UUID uuid, URI id, JsonPointer absolutePointer, JsonNode schema){
            return new SubSchemaInfo(uuid, id, absolutePointer, schema, Maps.newHashMap(), Maps.newHashMap(), Maps.newHashMap());
        }
    }
}
