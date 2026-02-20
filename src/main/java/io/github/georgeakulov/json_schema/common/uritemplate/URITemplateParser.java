package io.github.georgeakulov.json_schema.common.uritemplate;

public class URITemplateParser {

    public static boolean parse(String value) {
        try {
            URITemplateParser.parse(value, new URITemplateParserListener(){});
            return true;
        }
        catch(URITemplateSyntaxException ignore) {
            return false;
        }
    }

    private static void parse(String value, URITemplateParserListener listener) {
        if (value == null) {
            throw new NullPointerException("value");
        }
        StringBuilder current = new StringBuilder();
        // control flags
        boolean main = true;
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            switch (c) {
                case '{':
                    if (!main) {
                        listener.onSyntaxError(value, i);
                    }
                    listener.onTextFragment(current.toString());
                    current.setLength(0);
                    main = false;
                    break;
                case '}':
                    if (main) {
                        listener.onSyntaxError(value, i);
                    }
                    listener.onVariable(URITemplateVariable.parse(current.toString()));
                    current.setLength(0);
                    main = true;
                    break;
                default:
                    current.append(c);
            }
        }
        if (!main) {
            listener.onSyntaxError(value, value.length() - 1);
        } else {
            listener.onTextFragment(current.toString());
        }
        listener.onCompleted();
    }
}