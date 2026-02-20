package io.github.georgeakulov.json_schema.common.uritemplate;


public interface URITemplateParserListener {

    default void onTextFragment(String text) {}

    default void onVariable(URITemplateVariable var){}

    default void onCompleted(){}

    default void onSyntaxError(String value, int position) {
        throw new URITemplateSyntaxException(value + " (" + position + ")");
    }
}