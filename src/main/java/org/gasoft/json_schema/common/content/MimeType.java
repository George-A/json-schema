package org.gasoft.json_schema.common.content;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class MimeType {

    private final String type;
    private final String subType;
    private final Map<String, String> params;

    public static MimeType create(String value) {
        return new MimeType(value);
    }

    /**
     * @return type part of mime name. For "application/json" returns "application"
     */
    public String getType() {
        return type;
    }


    /**
     * @return subtype part of mime name. For "application/json" returns "json"
     */
    public String getSubType() {
        return subType;
    }

    /**
     * @return all additional params "charset", "boundary" and etc.
     */
    public Map<String, String> getParams() {
        return params;
    }

    public boolean isApplication() {
        return type.equals("application");
    }

    public boolean isText() {
        return type.equals("text");
    }

    public boolean hasJsonContent() {
        return isApplication() && (subType.endsWith("json") || subType.equals("jwt"));
    }

    private MimeType(String fromString) {
        String[] parts = fromString.split(";");
        String mime = parts[0];
        int idx = mime.indexOf("/");
        if (idx < 0) {
            type = mime.trim().toLowerCase();
            subType = "";
        } else {
            type = mime.substring(0, idx).trim().toLowerCase();
            subType = mime.substring(idx + 1).trim().toLowerCase();
        }

        Map<String, String> params = new HashMap<>();
        for (int i = 1; i < parts.length; i++) {
            String part = parts[i];
            int assignIdx = part.indexOf('=');
            if (assignIdx < 0) {
                params.put(part.trim().toLowerCase(), null);
            } else {
                String key = part.substring(0, assignIdx).trim().toLowerCase();
                String value = part.substring(assignIdx + 1).trim().toLowerCase();
                if (value.startsWith("\"") && value.endsWith("\"")) {
                    value = value.substring(1, value.length() - 1);
                }
                params.put(key, value);
            }
        }
        this.params = Collections.unmodifiableMap(params);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("MimeType{");
        sb.append("type='").append(type).append('\'');
        sb.append(", subType='").append(subType).append('\'');
        sb.append(", params=").append(params);
        sb.append('}');
        return sb.toString();
    }
}
