package org.gasoft.json_schema;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.google.common.collect.Lists;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class TestUtils {

    static JsonMapper mapper = new JsonMapper();

    public static List<Path> getPathsFromDir(Path folderPath, Predicate<Path> filter, int depth) {

        List<Path> paths = Lists.newArrayList();
        getPathsFromDirCb(folderPath, depth, path -> {
            File file = path.toFile();
            if(!file.isDirectory() && filter.test(path)) {
                paths.add(path);
            }
        });
        return paths;
    }

    public static List<IFile> getPathsHierarchy(Path folderPath, Predicate<Path> fileFilter) {
        List<IFile> files = Lists.newArrayList();
        getPathsFromDirCb(folderPath, 1, path -> {
            if(folderPath.equals(path)) {
                return;
            }
            File file = path.toFile();
            if(file.isDirectory()) {
                files.add(new IntDirectory(path, getPathsHierarchy(path, fileFilter)));
            }
            else {
                if(fileFilter.test(path)) {
                    files.add(new IntFile(path));
                }
            }
        });
        return files;
    }

    public static void getPathsFromDirCb(Path folderPath, int depth, Consumer<Path> cb) {
        if(!folderPath.toFile().exists()) {
            throw new IllegalArgumentException("The path " + folderPath + " not exists");
        }

        try(Stream<Path> files = Files.find(
                folderPath,
                depth,
                (path, attrs) -> true)) {
            files.forEach(cb);
        }
        catch(IOException e) {
            throw new RuntimeException("Error on search files in folder: " + folderPath, e);
        }
    }

    public static JsonNode loadJson(File file) {
        try (InputStream inStream = Files.newInputStream(file.toPath())) {
            return mapper.configure(JsonParser.Feature.ALLOW_COMMENTS, true).reader().readTree(inStream);
        }
        catch(IOException e) {
            throw new RuntimeException("Error load json: " + file, e);
        }
    }

    public static JsonNode fromString(String str) {
        try {
            return mapper.reader().readTree(str);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public interface IFile {
        Path path();
        List<IFile> childs();
        boolean isDirectory();
    }

    record IntFile(Path path) implements IFile {
        @Override
        public List<IFile> childs() {
            return List.of();
        }

        @Override
        public boolean isDirectory() {
            return false;
        }
    }
    record IntDirectory(Path path, List<IFile> childs) implements IFile {
        @Override
        public boolean isDirectory() {
            return true;
        }
    }
}
