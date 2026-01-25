package org.gasoft.json_schema.compilers;

import com.fasterxml.jackson.databind.JsonNode;
import org.gasoft.json_schema.results.EErrorType;
import org.gasoft.json_schema.results.IValidationResult.ISchemaLocator;
import org.gasoft.json_schema.results.ValidationError;
import org.gasoft.json_schema.results.ValidationResultFactory;
import org.jspecify.annotations.Nullable;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.gasoft.json_schema.common.LocatedSchemaCompileException.checkIt;
import static org.gasoft.json_schema.common.LocatedSchemaCompileException.create;

public class DependentRequiredCompiler implements INamedCompiler {

    @Override
    public String getKeyword() {
        return "dependentRequired";
    }

    @Override
    public @Nullable IValidator compile(JsonNode schemaNode, CompileContext compileContext, ISchemaLocator schemaLocator) {
        checkIt(schemaNode.isObject(), schemaLocator, "The % keyword value must be an object. Actual: %s", getKeyword(), schemaNode.getNodeType());
        List<DependentRule> rules = schemaNode.propertyStream()
                .map(entry -> new DependentRule(entry.getKey(), parse(schemaLocator, entry.getKey(), entry.getValue())))
                .toList();
        return (instance, instancePtr, context) -> {

            var id = ValidationResultFactory.createId(schemaLocator, instancePtr);

            if(instance.isObject()) {
                return Flux.fromIterable(rules)
                                .filter(rule -> instance.has(rule.prop()))
                                .filter(rule -> !rule.dependent().stream()
                                        .allMatch(instance::has))
                                .collectList()
                                .map(invRules -> {
                                    if(invRules.isEmpty()) {
                                        return ValidationResultFactory.createOk(id);
                                    }
                                    else {
                                        return ValidationError.create(id,
                                                EErrorType.DEPENDENT_REQUIRED,
                                                invRules.stream().map(rule -> rule.prop).collect(Collectors.joining(","))
                                        );
                                    }
                                });
            }
            return ValidationResultFactory.createOk(id).publish();
        };
    }

    private Set<String> parse(ISchemaLocator locator, String propertyName, JsonNode value) {
        checkIt(value.isArray(), locator, "The %s keyword, must contains an string array. Actual type: %s", getKeyword(), value.getNodeType());
        if(!value.valueStream().allMatch(JsonNode::isTextual)) {
            throw create(
                    locator,
                    "The %s keyword array items values, must be an string. But was found types: %s",
                    getKeyword(),
                    value.valueStream()
                            .filter(node -> !node.isTextual())
                            .map(node -> node.getNodeType().toString())
                            .distinct()
                            .collect(Collectors.joining(","))
            );
        }
        var props = value.valueStream().map(JsonNode::asText).collect(Collectors.toSet());
        checkIt(props.size() == value.size(), locator, "The %s keyword values array, must contains UNIQUE items");
        return props;
    }

    private record DependentRule(String prop, Set<String> dependent) {}

}
