package io.github.georgeakulov.json_schema.compilers;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.SimpleFileServer;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.function.Supplier;

public class TestServer {

    private HttpServer server;

    void upWithPath(int port, File path) {
        down();
        server = SimpleFileServer.createFileServer(new InetSocketAddress("localhost", port), path.toPath(), SimpleFileServer.OutputLevel.VERBOSE);
        server.start();
    }


    void upWithContent(int port, String relativePath, Supplier<String> content) {
        down();
        try {
            server = HttpServer.create(
                    new InetSocketAddress("localhost", port),
                    0,
                    relativePath,
                    exchange -> {

                        String contentStr = content.get();
                        exchange.sendResponseHeaders(200, contentStr.length());
                        try (var os = exchange.getResponseBody()) {
                            os.write(contentStr.getBytes(StandardCharsets.UTF_8));
                        }
                    }
            );
            server.start();
        }
        catch(IOException ex) {
            throw new RuntimeException("Can`t create server", ex);
        }
    }

    void down() {
        if(server != null) {
            server.stop(0);
        }
    }
}
