package org.gasoft.json_schema.compilers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.json.JsonMapper;
import org.gasoft.json_schema.IContentProcessing;
import org.gasoft.json_schema.Schema;
import org.gasoft.json_schema.SchemaBuilder;
import org.gasoft.json_schema.results.IValidationResult;
import org.junit.jupiter.api.Test;

import java.net.URI;

import static org.junit.jupiter.api.Assertions.assertFalse;
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

    @Test
    void testAddFormatValidator() {
        String schema = """
                {
                    "$schema": "https://json-schema.org/draft/2020-12/schema",
                    "format": "thousandNumber"
                }
                """;
        Schema schemaInst = SchemaBuilder.create()
                .setFormatAssertionsEnabled(true)
                .addFormatValidator("thousandNumber", str -> str.equals("1000"))
                .compile(schema);

        assertTrue(schemaInst.apply("\"1000\"").isOk());

        assertFalse(schemaInst.apply("\"1001\"").isOk());
    }

    @Test
    void testContentLevel() {
        String schema = """
                {
                "$schema": "https://json-schema.org/draft/2020-12/schema",
                "contentEncoding": "base64",
                "contentMediaType": "application/json",
                "contentSchema": {
                        "$schema": "https://json-schema.org/draft/2020-12/schema",
                        "type": "string"
                    }
                }
                """;
        String invalidEncoding = "\"MQ!==\""; // Symbol ! not allowed
        String validEncoding = "\"MQ==\""; // base64 encoded integer 1
        String validEncodingAndSchema = "\"IjEi\""; // base64 encoded string "1"

        // Behavior for disabled validations and invalid data
        IValidationResult result = SchemaBuilder.create()
                .setContentVocabularyBehavior(IContentProcessing.ContentValidationLevel.DISABLE)
                .compile(schema)
                .apply(validEncoding);
        // Validations do not apply
        assertTrue(result.isOk());

        // Behavior for validating contentEncoding and contentMediaType
        Schema compiledSchema = SchemaBuilder.create()
                .setContentVocabularyBehavior(IContentProcessing.ContentValidationLevel.ENCODING)
                .compile(schema);

        // The base64 encoding is invalid
        result = compiledSchema.apply(invalidEncoding);
        assertFalse(result.isOk());

        // The base64 encoding is valid
        result = compiledSchema.apply(validEncoding);
        assertTrue(result.isOk());

        // Behavior for validating contentEncoding and contentMediaType and contentSchema too
        compiledSchema = SchemaBuilder.create()
                .setContentVocabularyBehavior(IContentProcessing.ContentValidationLevel.ENCODING_AND_SCHEMA)
                .compile(schema);

        // Valid base64 encoding but value not conform to contentSchema
        result = compiledSchema.apply(validEncoding);
        assertFalse(result.isOk());

        // Valid base64 encoding and  value is conform to contentSchema
        result = compiledSchema.apply(validEncodingAndSchema);
        assertTrue(result.isOk());
    }
}
