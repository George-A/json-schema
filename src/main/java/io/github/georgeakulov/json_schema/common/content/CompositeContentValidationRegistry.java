package io.github.georgeakulov.json_schema.common.content;

import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class CompositeContentValidationRegistry implements IContentValidationRegistry {

    private final List<IContentValidationRegistry> registryList = new ArrayList<>();

    public CompositeContentValidationRegistry(IContentValidationRegistry contentValidationRegistry) {
        this.registryList.add(contentValidationRegistry);
    }

    public void addFirst(IContentValidationRegistry registry) {
        this.registryList.addFirst(registry);
    }

    public void addLast(IContentValidationRegistry registry) {
        this.registryList.addLast(registry);
    }

    @Override
    public @Nullable ExceptionableOp getContentEncodingFn(String contentEncoding) {
        return registryList.stream()
                .map(registry -> registry.getContentEncodingFn(contentEncoding))
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(null);
    }

    @Override
    public @Nullable ExceptionableCons getContentTypeFn(MimeType mimeType) {
        return registryList.stream()
                .map(registry -> registry.getContentTypeFn(mimeType))
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(null);
    }
}
