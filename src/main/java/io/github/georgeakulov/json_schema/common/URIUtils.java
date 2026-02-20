package io.github.georgeakulov.json_schema.common;

import java.net.URI;
import java.net.URISyntaxException;

public class URIUtils {

    public static URI clearEmptyFragments(URI fromUri) {
        if(fromUri.getFragment() != null && fromUri.getFragment().isBlank()) {
            try {
                fromUri = new URI(
                        fromUri.getScheme(),
                        fromUri.getSchemeSpecificPart(),
                        null
                );
            }
            catch(URISyntaxException use) {
                throw SchemaCompileException.create("Error on cleanup fragment from uri {0}",  fromUri, use);
            }
        }
        return fromUri;
    }
}
