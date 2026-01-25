package org.gasoft.json_schema.compilers;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.base.MoreObjects;
import com.google.common.base.Predicates;
import org.gasoft.json_schema.TestUtils;

import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

public class JsonSchemaTestDataProvider {

    static final Path testDirectory = Path.of(System.getProperty("user.dir") +"/test_sources/tests/draft2020-12/");

    static Stream<TestFile> getTestFiles() {
        return TestUtils.getPathsFromDir(testDirectory, Predicates.alwaysTrue(), Integer.MAX_VALUE)
                .stream()
                .map(path -> new TestFile(
                        path,
                        testDirectory.relativize(path),
                        toSchema(path).toList(),
                        testDirectory.relativize(path)
                ));
    }

    static TestFile toTestFile(Path parent, Path path) {
        return new TestFile(path, parent.relativize(path), toSchema(path).toList(), testDirectory.relativize(path));
    }

    static List<TestUtils.IFile> getFilesHierarchy() {
        return TestUtils.getPathsHierarchy(testDirectory, Predicates.alwaysTrue());
    }

    public static Stream<Schema> toSchema(Path path) {
        return TestUtils.loadJson(path.toFile())
                    .valueStream()
                    .map(schema -> {
                        var tests = schema.get("tests")
                                .valueStream()
                                .map(test -> new Test(
                                        test.get("description").asText(),
                                        test.get("data"),
                                        test.get("valid").asBoolean()
                                ))
                                .toList();
                        return new Schema(
                                schema.get("schema"),
                                schema.get("description").asText(),
                                tests
                        );
                    });
    }

    record Test(String description, JsonNode value, boolean expected){
        @Override
        public String toString() {
            return MoreObjects.toStringHelper("Test")
                    .add("description", description)
                    .add("expected", expected)
                    .toString();
        }
    }

    record Schema(JsonNode schemaValue, String description, Collection<Test> tests){

        @Override
        public String toString() {
            return MoreObjects.toStringHelper(this)
                    .add("description", description)
                    .toString();
        }
    }

    record TestFile(Path file, Path relativePath, Collection<Schema> schemas, Path rootRelativePath) {
        @Override
        public String toString() {
            return relativePath.toString();
        }
    }

}
