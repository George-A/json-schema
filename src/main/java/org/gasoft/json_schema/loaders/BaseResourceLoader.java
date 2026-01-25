package org.gasoft.json_schema.loaders;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.Lists;
import org.gasoft.json_schema.common.SchemaCompileException;

import java.net.URI;
import java.util.List;
import java.util.stream.Stream;

import static com.google.common.base.Preconditions.checkState;

public class BaseResourceLoader implements IResourceLoader {

    private final List<IResourceLoader> loaders = Lists.newArrayList();

    public BaseResourceLoader() {
        this
                .addLoader(new HttpLoader())
                .addLoader(new FileLoader())
                .addLoader(new ClasspathLoader());
    }

    public BaseResourceLoader addLoader(IResourceLoader loader) {
        loaders.addFirst(loader);
        return this;
    }

    @Override
    public Stream<String> getSupportedSchemes() {
        return loaders.stream()
                .flatMap(IResourceLoader::getSupportedSchemes);
    }

    @Override
    public JsonNode loadResource(URI byUri) {
        checkState(byUri.isAbsolute(), "The uri %s is not absolute", byUri);
        String scheme = byUri.getScheme();
        return loaders.stream()
                .filter(loader -> loader.getSupportedSchemes().anyMatch(scheme::equals))
                .findFirst()
                .orElseThrow(() -> SchemaCompileException.create("Can`t find loader for schema %s from uri %s", scheme, byUri))
                .loadResource(byUri);
    }
}
