package org.gasoft.json_schema.compilers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import com.fasterxml.jackson.databind.node.NumericNode;
import org.gasoft.json_schema.results.IValidationResult.ISchemaLocator;

import java.math.BigDecimal;

import static org.gasoft.json_schema.common.LocatedSchemaCompileException.checkIt;

public class Utils {

    public static boolean checkEquality(JsonNode o1, JsonNode o2) {

        if(o1.equals(o2)) {
            return true;
        }

        if(o1.getNodeType() == JsonNodeType.NUMBER && o1.getNodeType() == o2.getNodeType()) {
            return o1.decimalValue().compareTo(o2.decimalValue()) == 0;
        }

        return false;
    }

    public static Integer tryCastInteger(NumericNode node) {
        BigDecimal decimal = node.decimalValue();
        if(decimal.remainder(BigDecimal.ONE).compareTo(BigDecimal.ZERO) == 0) {
            return decimal.intValueExact();
        }
        return null;
    }

    public static int getCheckedInteger(ISchemaLocator locator, JsonNode node, String message, Object ... args) {
        checkIt(node.isNumber(), locator, message, args);
        Integer integer = tryCastInteger((NumericNode)  node);
        checkIt(integer != null, locator, message, args);
        return integer;
    }
}
