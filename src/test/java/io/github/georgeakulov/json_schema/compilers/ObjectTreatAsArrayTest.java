package io.github.georgeakulov.json_schema.compilers;

import io.github.georgeakulov.json_schema.SchemaBuilder;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static io.github.georgeakulov.json_schema.TestUtils.fromString;


public class ObjectTreatAsArrayTest {

    @Test
    void treatAsArray() {
        String schema = """
                {
                    "type": "array"
                }
                """;
        String data = """
                {
                    "some": true
                }
                """;
        var result = SchemaBuilder.create()
                .setDraft202012DefaultDialect()
                .setTryCastToArray(true)
                .compile(fromString(schema))
                .apply(fromString(data));
        Assertions.assertTrue(result.isOk(), () -> "Some errors found " + result);
    }
}
