package org.gasoft.json_schema.compilers;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.Maps;
import org.gasoft.json_schema.results.IValidationResult.ISchemaLocator;
import org.jspecify.annotations.NonNull;

import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

import static org.gasoft.json_schema.common.LocatedSchemaCompileException.checkIt;

public class PropertiesCompiler extends BasePropertiesCompiler implements INamedCompiler {

    @Override
    public String getKeyword() {
        return "properties";
    }

    @Override
    public @NonNull IValidator compile(JsonNode schemaNode, CompileContext compileContext, ISchemaLocator schemaLocator) {
        checkIt(schemaNode.isObject(), schemaLocator,"The %s keyword value must be an object. Actual %s", getKeyword(), schemaNode);
        final PropertiesTask propertiesTask = new PropertiesTask();
        schemaNode.propertyStream()
                .forEach(property ->
                    propertiesTask.addValidator(property.getKey(),
                            compileContext.compile(property.getValue(), schemaLocator.appendProperty(property.getKey())))
                );

        return new PropertiesValidator(schemaLocator, propertiesTask::getValidators, compileContext.getConfig());
    }

    private static class PropertiesTask {

        private final Map<String, IValidator> validators = Maps.newHashMap();

        void addValidator(String property, IValidator validator) {
            this.validators.put(property, validator);
        }

        Stream<IValidator> getValidators(String propertyName) {
            return Stream.of(validators.get(propertyName))
                    .filter(Objects::nonNull);
        }
    }
}
