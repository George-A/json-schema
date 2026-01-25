package org.gasoft.json_schema.compilers;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.google.common.base.Preconditions.checkState;

public class CompilerRegistry {

    private final Map<String, ICompiler> compilers = Maps.newHashMap();
    private final Map<String, ICompilerFactory> compilerFactories = Maps.newHashMap();
    private final List<IValidatorsTransformer> validatorTransformers = Lists.newArrayList();
    CompilerRegistry() {
        // Add all compilers to registry
    }

    CompilerRegistry addCompiler(String name, ICompiler compiler) {
        Objects.requireNonNull(compiler);
        checkState(this.compilers.put(name, compiler) == null, "The compiler with name %s already exists", name);
        if(compiler instanceof IValidatorsTransformer) {
            addValidatorsTransformer((IValidatorsTransformer) compiler);
        }
        return this;
    }

    CompilerRegistry addCompiler(ICompilerFactory compilerFactory) {
        Objects.requireNonNull(compilerFactory);
        compilerFactory.getSupportedKeywords()
                .forEach(keyword -> compilerFactories.put(keyword, compilerFactory));
        if(compilerFactory instanceof IValidatorsTransformer) {
            addValidatorsTransformer((IValidatorsTransformer) compilerFactory);
        }
        return this;
    }

    CompilerRegistry addValidatorsTransformer(IValidatorsTransformer validatorsTransformer) {
        validatorTransformers.add(validatorsTransformer);
        return this;
    }

    CompilerRegistry addCompiler(INamedCompiler namedCompiler) {
        Objects.requireNonNull(namedCompiler);
        return addCompiler(namedCompiler.getKeyword(), namedCompiler);
    }

    public List<IValidatorsTransformer> getTransformers() {
        return validatorTransformers;
    }

    @Nullable
    public ICompiler optCompiler(String keyword) {
        ICompiler compiler = compilers.get(keyword);
        if(compiler != null) {
            return compiler;
        }
        ICompilerFactory factory = compilerFactories.get(keyword);
        if(factory != null) {
            return factory.getCompiler(keyword);
        }

        return null;
    }

}
