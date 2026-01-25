package org.gasoft.json_schema.results;

public enum EErrorType {

    CONST("Value %s not equals to const %s"),
    CONTAINS_MIN("The array must contains at least %s valid items. Actual: %s"),
    CONTAINS_MAX("The array can contains no more than %s valid items. Actual: %s"),
    DEPENDENCIES("The dependencies for [%s] are not satisfied"),
    DEPENDENT_REQUIRED("The required dependencies for [%s] are not satisfied"),
    ENUM("The value %s not declared in enum %s"),
    EXCLUSIVE_MAXIMUM("Value %s greater than %s"),
    EXCLUSIVE_MINIMUM("Value %s less or equal than %s"),
    FORMAT("Value %s not conform to format %s"),
    MAXIMUM("Value %s greater than %s"),
    MINIMUM("Value %s less than %s"),
    MAX_ITEMS("Required max array size %s, Actual size: %s"),
    MIN_ITEMS("Required minimum items amount is %s, Actual size: %s"),
    MAX_PROPERTIES("Maximum allowed properties is%s. Actual: %s"),
    MIN_PROPERTIES("Require min properties count is %s. Actual: %s"),
    MULTIPLE_OF("The node value of %s not conform to multipleOf value %s"),
    NOT("The subschema validation was successfully. Result will be inverter"),
    ONE_OF_EMPTY("None of the results were successful."),
    ONE_OF_MORE_THAN_ONE("More than 1 successful results."),
    ANY_OF("None of the variants were successful."),
    PATTERN("The value %s not conform to pattern: %s"),
    REQUIRED("Some Required properties %s are missing"),
    FALSE_SCHEMA("Because schema is false"),
    TYPE("The node value %s not conform to type %s"),
    UNIQUE_ITEMS("At least one item %s not unique"),
    MAX_LENGTH("The length of %s must be less than or equal to %s. Actual: %s"),
    MIN_LENGTH("The length of %s must be greater or equal than  %s. Actual: %s")

    ;
    private final String defaultErrorMsg;

    EErrorType(String s) {
        defaultErrorMsg = s;
    }

    public String getDefaultErrorMsg() {
        return defaultErrorMsg;
    }
}
