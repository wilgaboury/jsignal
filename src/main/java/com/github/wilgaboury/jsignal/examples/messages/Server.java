package com.github.wilgaboury.jsignal.examples.messages;

import com.github.wilgaboury.jsignal.EffectHandle;
import com.github.wilgaboury.jsignal.interfaces.Signal;
import com.github.wilgaboury.jsignal.state.UserConnection;

import java.io.IOException;
import java.net.ServerSocket;

import static com.github.wilgaboury.jsignal.ReactiveUtil.*;

/**
 * Reactive port, server automatically restarts whenever port is changed
 */
public class Server {
    private final Signal<Integer> port;
    private final EffectHandle serverStartEffect;

    public Server(int port) {
        this.port = createAtomicSignal(port);
        this.serverStartEffect = createServerStartEffect();
    }

    public void setPort(int port) {
        this.port.accept(port);
    }

    public EffectHandle createServerStartEffect() {
        return createAsyncEffect(() -> {
            ServerSocket socket = createServerSocket();
            onCleanup(() -> closeServerSocket(socket));

            Thread thread = new Thread(() -> serverLoop(socket));
            thread.start();
        });
    }

    private void serverLoop(ServerSocket serverSocket) {
        try {
            while (true) {
                UserConnection.dispatch(serverSocket.accept());
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private ServerSocket createServerSocket() {
        try {
            return new ServerSocket(port.get());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void closeServerSocket(ServerSocket socket) {
        try {
            socket.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
