package com.etrandafir.asyncapi.inventoryservice;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;

import io.github.springwolf.asyncapi.v3.jackson.DefaultAsyncApiSerializerService;
import io.github.springwolf.asyncapi.v3.model.AsyncAPI;
import io.github.springwolf.core.standalone.DefaultStandaloneApplication;

class StandaloneTest {

    @Test
    void asyncApiStandaloneArtifactTest() throws IOException {
        // given
        var standaloneApplication =
                DefaultStandaloneApplication.builder().buildAndStart();

        // when
        AsyncAPI asyncApi = standaloneApplication.getAsyncApiService().getAsyncAPI();
        String actual = new DefaultAsyncApiSerializerService().toJsonString(asyncApi);

        // then
        // writing the actual file can be useful for debugging (remember: gitignore)
        Files.writeString(Path.of("src", "test", "resources", "asyncapi.standalone.json"), actual);

        // then
        InputStream s = this.getClass().getResourceAsStream("/asyncapi.json");
        String expected = new String(s.readAllBytes(), StandardCharsets.UTF_8).trim();
        assertEquals(expected, actual);
    }
}