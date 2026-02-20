package io.github.georgeakulov.json_schema.common.uritemplate;

import java.util.*;

import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toList;
import static io.github.georgeakulov.json_schema.common.uritemplate.URITemplateOperator.*;

public class URITemplateVariable {

    private final URITemplateOperator modifier;

    private final List<URIVarComponent> components;

    public static URITemplateVariable parse(String template) {
        if ((template.charAt(0) == '{') && (template.charAt(template.length() - 1) == '}')) {
            template = template.substring(1, template.length() - 1);
        }
        if (template.isEmpty()) {
            throw new URITemplateSyntaxException("URI template cannot be empty: {}");
        }
        Optional<URITemplateOperator> modifier = URITemplateOperator.valueOf(template.charAt(0));
        String specs = template;
        if (modifier.isPresent()) {
            specs = template.substring(1);
            if (specs.isEmpty()) {
                throw new URITemplateSyntaxException("Name not specified: {" + modifier.get().operatorChar() + "}");
            }
        }
        List<URIVarComponent> components = stream(specs.split(","))
                .map(URIVarComponent::parse)
                .collect(toList());
        return new URITemplateVariable(modifier.orElse(NONE), components);
    }

    private URITemplateVariable(URITemplateOperator modifier, List<URIVarComponent> components) {
        this.modifier = modifier;
        if (components == null || components.isEmpty()) {
            throw new URITemplateSyntaxException("At least one component required for template variable");
        }
        this.components = Collections.unmodifiableList(components);
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        URITemplateVariable variable = (URITemplateVariable) o;
        if (modifier != variable.modifier) return false;
        if (components.size() != variable.components.size()) return false;
        for (int i = 0; i < components.size(); i++) {
            if (!components.get(i).equals(variable.components.get(i))) return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        return Objects.hash(modifier, components);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder("{");
        if (modifier != null && modifier.operatorChar() != null) {
            builder.append(modifier.operatorChar());
        }
        boolean first = true;
        for (URIVarComponent component : components) {
            if (!first) {
                builder.append(',');
            } else {
                first = false;
            }
            builder.append(component);
        }
        return builder.append('}').toString();
    }

}