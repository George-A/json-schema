package io.github.georgeakulov.json_schema.loaders;

import com.fasterxml.jackson.databind.JsonNode;

import java.net.URI;
import java.util.stream.Stream;

public interface IResourceLoader {

    Stream<String> getSupportedSchemes();
    JsonNode loadResource(URI byUri);
}
