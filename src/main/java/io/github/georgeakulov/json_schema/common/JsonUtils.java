package io.github.georgeakulov.json_schema.common;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.io.InputStream;

public class JsonUtils {

    private final static JsonMapper JSON_MAPPER = new JsonMapper();

    public static JsonNode parse(String jsonString) {
        try {
            return JSON_MAPPER.reader().readTree(jsonString);
        }
        catch(IOException e) {
            throw new IllegalArgumentException("Error on parse json", e);
        }
    }

    public static JsonNode parse(InputStream is) {
        try {
            return JSON_MAPPER.reader().readTree(is);
        }
        catch(IOException e) {
            throw new IllegalArgumentException("Error on parse json", e);
        }
    }
}
