package org.gasoft.json_schema.compilers;

import com.google.common.base.Strings;
import org.gasoft.json_schema.Schema;
import org.gasoft.json_schema.SchemaBuilder;
import org.gasoft.json_schema.TestUtils;
import org.gasoft.json_schema.results.ValidationResultFactory;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.gasoft.json_schema.compilers.JsonSchemaTestDataProvider.*;

@Disabled
@Execution(ExecutionMode.CONCURRENT)
public class JsonSchemaOrgTests {

    static TestServer server = new TestServer();

    @BeforeAll
    static void beforeAll() {
        server.upWithPath(1234, new File(System.getProperty("user.dir") + "/test_sources/remotes"));
    }

    @AfterAll
    static void afterAll() {
        server.down();
    }

    @TestFactory()
    Stream<DynamicNode> testDrafts() {
        return JsonSchemaTestDataProvider.getFilesHierarchy()
                .stream()
                .map(file -> forFile(testDirectory, file))
                .filter(Objects::nonNull);
    }

    Stream<DynamicNode> flatTests(DynamicNode node) {
        if(node instanceof DynamicContainer) {
            return ((DynamicContainer) node).getChildren()
                    .flatMap(this::flatTests);
        }
        return Stream.of(node);
    }

    @Disabled
    @TestFactory
    Stream<DynamicNode> stress() {
        var testList = JsonSchemaTestDataProvider.getFilesHierarchy()
                .stream()
                .map(file -> forFile(testDirectory, file))
                .filter(Objects::nonNull)
                .flatMap(this::flatTests)
                .filter(Objects::nonNull)
                .filter(node -> node instanceof DynamicTest)
                .collect(Collectors.toList());
        return IntStream.range(0, 10)
                .mapToObj(ignore -> testList)
                .flatMap(List::stream);
    }

    DynamicNode forFile(Path parent, TestUtils.IFile file) {

        if(file.isDirectory()) {
            return DynamicContainer.dynamicContainer(
                    "\uD83D\uDCC1: " + parent.relativize(file.path()),
                    file.childs().stream()
                            .map(inFile -> forFile(file.path(), inFile))
                            .filter(Objects::nonNull)
                            .toList()
            );
        }
        else {
            JsonSchemaTestDataProvider.TestFile testFile = toTestFile(parent, file.path());
            return fileToTest(testFile);
        }
    }

    DynamicNode fileToTest(TestFile testFile) {
        return DynamicContainer.dynamicContainer(
                testFile.toString(),
                testFile.schemas()
                        .stream()
                        .map(schema -> {
                            try {
                                var compiledSchema = SchemaBuilder.create()
                                        .setDraft202012DefaultDialect()
                                        .setFormatAssertionsEnabled(
                                                testFile.rootRelativePath().toString().startsWith("optional/format")
                                                        || testFile.relativePath().toString().startsWith("optional/format-assertion.json")
                                        )
                                        .compile(schema.schemaValue());

                                return DynamicContainer.dynamicContainer(
                                        schema.description(),
                                        schema.tests().stream()
                                                //                                                                .filter(test -> test.description().startsWith("a valid host name"))
                                                .map(test -> DynamicTest.dynamicTest(
                                                        test.description(),
                                                        toExecutable(compiledSchema, schema, test)
                                                ))
                                );
                            }
                            catch(Exception ex) {
                                return DynamicTest.dynamicTest(
                                        schema.description(),
                                        () -> Assertions.fail("Schema compilation error", ex)
                                );
                            }
                        })
                        .filter(Objects::nonNull)
        );
    }

    Executable toExecutable(Schema compiledSchema, JsonSchemaTestDataProvider.Schema schema, JsonSchemaTestDataProvider.Test test){
        return () -> {

            var result = compiledSchema.apply(test.value());

            Assertions.assertEquals(test.expected(), result.isOk(), () -> Strings.lenientFormat(
                "The schema '%s' and test '%s' has non expected result %s, with msg: \"%s\"",
                schema.description(),
                test.description(),
                result.isOk(),
                ValidationResultFactory.hierarchyFormat(result)
            ));
        };
    }
}
