package org.gasoft.json_schema.compilers;

import org.jspecify.annotations.Nullable;

import java.util.stream.Stream;

public interface ICompilerFactory {

    Stream<String> getSupportedKeywords();

    @Nullable ICompiler getCompiler(String keyword);
}
