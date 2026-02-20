package io.github.georgeakulov.json_schema.compilers;

import com.fasterxml.jackson.databind.JsonNode;
import io.github.georgeakulov.json_schema.TestUtils;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

public class JsonSchemaTestDataProvider {

    static final Path testDirectory = Path.of(System.getProperty("user.dir") +"/test_sources/tests/");

    static TestFile toTestFile(Path parent, TestUtils.IFile iFile) {
        var testFile = new TestFile(iFile, iFile.path(), parent.relativize(iFile.path()), new ArrayList<>(), testDirectory.relativize(iFile.path()));
        toSchema(testFile, iFile.path()).forEach(testFile.schemas::add);
        return testFile;
    }

    static List<TestUtils.IFile> getFilesHierarchy() {
        return TestUtils.getPathsHierarchy(testDirectory, ignore -> true);
    }

    public static Stream<Schema> toSchema(TestFile testFile, Path path) {
        return TestUtils.loadJson(path.toFile())
                    .valueStream()
                    .map(schema -> {
                        var schemaObj = new Schema(
                                testFile,
                                schema.get("schema"),
                                schema.get("description").asText(),
                                new ArrayList<>()
                        );
                        schema.get("tests")
                                .valueStream()
                                .map(test -> new Test(
                                        schemaObj,
                                        test.get("description").asText(),
                                        test.get("data"),
                                        test.get("valid").asBoolean()
                                ))
                                .forEach(schemaObj.tests::add);
                        return schemaObj;
                    });
    }

    record Test(Schema schema, String description, JsonNode value, boolean expected){

        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder("Test{");
            sb.append("description='").append(description).append('\'');
            sb.append(", expected=").append(expected);
            sb.append('}');
            return sb.toString();
        }
    }

    public record Schema(TestFile testFile, JsonNode schemaValue, String description, Collection<Test> tests){

        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder("Schema{");
            sb.append(", description='").append(description).append('\'');
            sb.append('}');
            return sb.toString();
        }
    }

    public record TestFile(TestUtils.IFile iFile, Path file, Path relativePath, Collection<Schema> schemas, Path rootRelativePath) {
        @Override
        public String toString() {
            return relativePath.toString();
        }
    }
}
