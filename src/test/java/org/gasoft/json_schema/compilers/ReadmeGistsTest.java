package org.gasoft.json_schema.compilers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.json.JsonMapper;
import org.gasoft.json_schema.Schema;
import org.gasoft.json_schema.SchemaBuilder;
import org.gasoft.json_schema.results.IValidationResult;
import org.junit.jupiter.api.Test;

import java.net.URI;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class ReadmeGistsTest {

    @Test
    void simpleTest() {
        String schema = """
                {
                    "$schema": "https://json-schema.org/draft/2020-12/schema",
                    "type": "array",
                    "items": {
                        "type": "integer"
                    }
                }
                """;
        String json = "[1,2,3,4,5,6,7,8]";

        // For config schema compilation
        SchemaBuilder builder = SchemaBuilder.create();

        // Compiled schema. The schema is immutable.
        Schema compiledSchema = builder.compile(schema);

        // Validation result.
        IValidationResult result = compiledSchema.apply(json);
        assertTrue(result.isOk());

        // More simple fluent invocation
        result = SchemaBuilder.create()
                .compile(schema)
                .apply(json);

        assertTrue(result.isOk());
    }

    @Test
    void testResourceLoader() throws Exception{
        String schema = """
                {
                    "$schema": "https://json-schema.org/draft/2020-12/schema",
                    "type": "array",
                    "items": {
                        "$ref": "urn:uuid:fd823a01-2ef5-4091-b36a-a117ecfa8827"
                    }
                }
                """;
        String schemaRef = """
                {
                    "type": "integer"
                }
                """;
        JsonNode schemaRefJson = new JsonMapper().readTree(schemaRef);
        String json = "[1,2,3,4,5,6,7,8]";

        IValidationResult result = SchemaBuilder.create()
                .addResourceLoader("urn", uri -> schemaRefJson)
                .compile(schema)
                .apply(json);

        assertTrue(result.isOk());
    }

    @Test
    void testIdToSchema() {
        String schema = """
                {
                    "$schema": "https://json-schema.org/draft/2020-12/schema",
                    "type": "array",
                    "items": {
                        "$ref": "someIdentifier"
                    }
                }
                """;
        String schemaRef = """
                {
                    "type": "integer"
                }
                """;
        String json = "[1,2,3,4,5,6,7,8]";

        IValidationResult result = SchemaBuilder.create()
                .addMappingIdToSchema("someIdentifier", schemaRef)
                .compile(schema)
                .apply(json);

        assertTrue(result.isOk());
    }

    @Test
    void testIdToUri() throws Exception{

        String schema = """
                {
                    "$schema": "https://json-schema.org/draft/2020-12/schema",
                    "type": "array",
                    "items": {
                        "$ref": "someRef"
                    }
                }
                """;
        String schemaRef = """
                {
                    "type": "integer"
                }
                """;
        JsonNode schemaRefJson = new JsonMapper().readTree(schemaRef);
        String json = "[1,2,3,4,5,6,7,8]";

        URI middleUri = URI.create("urn:uuid:fd823a01-2ef5-4091-b36a-a117ecfa8827");
        IValidationResult result = SchemaBuilder.create()
                .addMappingIdToURI("someRef", middleUri)
                .addResourceLoader("urn", uri -> {
                    if(uri.equals(middleUri)) {
                        return schemaRefJson;
                    }
                    return null;
                })
                .compile(schema)
                .apply(json);

        assertTrue(result.isOk());
    }
}
