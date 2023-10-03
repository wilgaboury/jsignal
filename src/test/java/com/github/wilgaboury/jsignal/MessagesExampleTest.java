package com.github.wilgaboury.jsignal;

import com.github.wilgaboury.jsignal.examples.messages.Server;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;

public class MessagesExampleTest {
    @Test
    public void testMessagesServer() throws IOException, ExecutionException, InterruptedException {
        Server server = new Server(7888);

        try (Socket socket = new Socket( "localhost", 7888)) {
            socket.getOutputStream().write(0);
        }

        Executors.newSingleThreadExecutor().submit(() -> server.setPort(7889)).get();
    }
}
