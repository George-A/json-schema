package org.gasoft.json_schema.common;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.Comparator;
import java.util.Map;

public final class JsonNodeComparator {

    public static final Comparator<JsonNode> JSON_NODE_COMPARATOR = Comparator.nullsFirst(
            Comparator.comparing(JsonNode::getNodeType)
                    .thenComparing(new UpdatedComparator())
            );

    private static class UpdatedComparator implements Comparator<JsonNode> {

        @Override
        public int compare(JsonNode o1, JsonNode o2) {
            return switch(o1.getNodeType()) {
                case NUMBER -> compareNumber(o1, o2);
                case ARRAY -> compareArray(o1, o2);
                case OBJECT -> compareObject(o1, o2);
                case STRING -> o1.textValue().compareTo(o2.textValue());
                case BOOLEAN -> Boolean.compare(o1.booleanValue(), o2.booleanValue());
                case NULL -> 0;
                default -> o1.equals(o2) ? 0 : 1;
            };
        }

        private int compareObject(JsonNode o1, JsonNode o2) {

            var obj1 = (ObjectNode)o1;
            var obj2 = (ObjectNode)o2;

            if(obj1.size() != obj2.size()) {
                return obj1.size() - obj2.size();
            }

            for (Map.Entry<String, JsonNode> property : obj1.properties()) {
                int result = JSON_NODE_COMPARATOR.compare(property.getValue(), o2.get(property.getKey()));
                if(result != 0) {
                    return result;
                }
            }
            return 0;
        }

        private int compareArray(JsonNode o1, JsonNode o2) {

            var arr1 = (ArrayNode)o1;
            var arr2 = (ArrayNode)o2;
            if(arr1.size() != arr2.size()) {
                return arr1.size() - arr2.size();
            }
            for(int i = 0; i < arr1.size(); i++) {
                int result = JSON_NODE_COMPARATOR.compare(arr1.get(i), arr2.get(i));
                if(result != 0) {
                    return result;
                }
            }
            return 0;
        }

        private int compareNumber(JsonNode o1, JsonNode o2) {
            return o1.decimalValue().compareTo(o2.decimalValue());
        }
    }
}
