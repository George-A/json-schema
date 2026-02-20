package io.github.georgeakulov.json_schema.compilers;

import com.fasterxml.jackson.databind.JsonNode;
import io.github.georgeakulov.json_schema.dialects.Dialect;
import io.github.georgeakulov.json_schema.loaders.IReferenceResolver;
import io.github.georgeakulov.json_schema.loaders.SchemasRegistry;
import io.github.georgeakulov.json_schema.results.IValidationResult;
import io.github.georgeakulov.json_schema.results.IValidationResult.ISchemaLocator;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.reactivestreams.Publisher;

import java.util.*;
import java.util.function.Function;

public class CompileContext implements IReferenceResolver {

    private Compiler rootCompiler;
    private final CompileConfig compileConfig;
    private SchemasRegistry schemaRegistry;

    private final Map<String, ICompiler> stageCompilers = new HashMap<>();
    private Map<ISchemaLocator, RecursionCheck<IValidator>> compileData = new TreeMap<>(Comparator.naturalOrder());


    public CompileContext(CompileConfig compileConfig) {
        this.compileConfig = compileConfig;
    }

    private CompileContext(CompileContext parent) {
        this.rootCompiler = parent.rootCompiler;
        this.schemaRegistry = parent.schemaRegistry;
        this.compileData = parent.compileData;
        this.compileConfig = parent.compileConfig;
    }

    public Dialect getDialect(ISchemaLocator schemaLocator) {
        return schemaRegistry.getDialect(schemaLocator);
    }

    public IValidator compile(JsonNode schema, ISchemaLocator schemaPointer) {
        return rootCompiler.compile(schema, this, schemaPointer);
    }

    public Function<JsonNode, Publisher<IValidationResult>> compileRoot(JsonNode schema) {
        return rootCompiler.compileSchema(schema, null, compileConfig);
    }

    public CompileContext withRegistry(SchemasRegistry registry) {
        this.schemaRegistry = registry;
        return this;
    }

    public void addEvaluatedCompilerToContext(String keyword, ICompiler compiler) {
        this.stageCompilers.put(keyword, compiler);
    }

    public ICompiler optEvaluatedCompiler(String keyword) {
        return this.stageCompilers.get(keyword);
    }

    public CompileConfig getConfig() {
        return compileConfig;
    }

    public @Nullable IValidator setCompileData(ISchemaLocator locator, IValidator validator) {
        var check = this.compileData.computeIfAbsent(locator, locIn ->
                new RecursionCheck<>(validator));
        if(check.checkRecursion(locator)) {
            return check.payload;
        }
        return null;
    }

    public CompileContext onNewSchemaObject() {
        return new CompileContext(this);
    }

    public @NonNull IResolutionResult resolveRef(@NonNull String reference, @NonNull ISchemaLocator schemaLocator) {
        return schemaRegistry.resolveRef(reference, schemaLocator);
    }

    @Override
    public @NonNull IResolutionResult resolveDynamicRef(String refValue, @NonNull ISchemaLocator schemaLocator) {
        return schemaRegistry.resolveDynamicRef(refValue, schemaLocator);
    }

    @Override
    public @NonNull IResolutionResult resolveRecursiveRef(String refValue, @NonNull ISchemaLocator schemaLocator) {
        return schemaRegistry.resolveRecursiveRef(refValue, schemaLocator);
    }

    public @NonNull ISchemaLocator resolveId(String idValue, ISchemaLocator locator) {
        return this.schemaRegistry.resolveExistingId(idValue, locator);
    }

    public CompileContext withCompiler(Compiler compiler) {
        this.rootCompiler = compiler;
        return this;
    }

    private static class RecursionCheck<T> {

        private final T payload;
        private final Set<ISchemaLocator> inboundEdges = new TreeSet<>();

        public RecursionCheck(T payload) {
            this.payload = payload;
        }

        private boolean checkRecursion(ISchemaLocator locator) {

            var prev = evalPrev1(locator);

            if (inboundEdges.contains(prev)) {
//                System.out.println("Recursion by " + prev);
                return true;
            }
            inboundEdges.add(prev);
            return false;
        }
    }

    private static ISchemaLocator evalPrev1(ISchemaLocator locator) {

        var parent = locator.getParent();
        return Objects.requireNonNullElse(parent, locator);
    }
}
