package com.github.wilgaboury.sigui;

import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.opengl.GL;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.glClearColor;

public class SiguiThread {
    private static final Logger logger = Logger.getLogger(SiguiThread.class.getName());

    private static final SiguiThread INSTANCE = new SiguiThread();

    private static final double MAX_QUEUE_EXEC_SEC = 1d / 60d;

    private final ConcurrentLinkedQueue<Runnable> queue;
    private final ServiceSupport serviceSupport;
    private final Thread thread;

    public SiguiThread() {
        queue = new ConcurrentLinkedQueue<>();
        serviceSupport = new ServiceSupport(this::innerStart, this::innerShutdown);
        thread = new Thread(this::loop);

    }

    public static SiguiThread getInstance() {
        return INSTANCE;
    }

    public void loop() {
        init();

        while (!serviceSupport.isShutdown()) {
            runQueue(MAX_QUEUE_EXEC_SEC);
            layout();
            render();
            glfwPollEvents();
        }
    }

    private void init() {
        GLFWErrorCallback.createPrint();
        if (!glfwInit())
            throw new IllegalStateException("Unable to initialize GLFW");

        // Enable v-sync
        glfwSwapInterval(1);
    }

    private void runQueue(double maxTime) {
        double start = glfwGetTime();
        while (!queue.isEmpty() && glfwGetTime() - start < maxTime) {
            try {
                queue.poll().run();
            } catch (Exception e) {
                logger.log(Level.SEVERE, "uncaught exception in ui thread", e);
            }
        }
    }

    private void layout() {
        for (var window : Window.windows) {
            window.layout();
        }
    }

    private void render() {
        for (var window : Window.windows) {
            window.render();
        }
    }

    private void innerStart() {
        thread.start();
    }

    private void innerShutdown() {
        // no-op
    }

    public void start() {
        serviceSupport.start();
    }

    public void shutdown() {
        serviceSupport.shutdown();
    }

    public static void invokeLater(Runnable runnable) {
        INSTANCE.queue.add(runnable);
    }
}
