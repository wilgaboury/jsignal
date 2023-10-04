package com.github.wilgaboury.jsignal.examples.messages;

import com.github.wilgaboury.jsignal.Effect;
import com.github.wilgaboury.jsignal.interfaces.Signal;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.SocketException;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.github.wilgaboury.jsignal.ReactiveUtil.*;

/**
 * Reactive port, server automatically restarts whenever port is changed
 */
public class Server {
    private static final Logger logger = Logger.getLogger(Server.class.getName());

    private final Signal<Integer> port;
    private final Effect serverStartEffect;

    public Server(int port) {
        this.port = createAtomicSignal(port);
        this.serverStartEffect = createServerStartEffect();
    }

    public void setPort(int port) {
        this.port.accept(port);
    }

    public Effect createServerStartEffect() {
        return createAsyncEffect(withAsyncExecutor(() -> {
            ServerSocket socket = createServerSocket();
            onCleanup(() -> {
                logger.log(Level.INFO, "running socket cleanup");
                closeServerSocket(socket);
            });

            Thread thread = new Thread(() -> serverLoop(socket));
            thread.start();
        }));
    }

    private void serverLoop(ServerSocket serverSocket) {
        try {
            while (true) {
                UserConnection.dispatch(serverSocket.accept());
            }
        } catch (SocketException e) {
            logger.log(Level.INFO, "socket closed");
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
