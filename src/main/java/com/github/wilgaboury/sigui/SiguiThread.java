package com.github.wilgaboury.sigui;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.lwjgl.glfw.GLFW.*;

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

    public void loop() {
        while (!serviceSupport.isShutdown()) {
            runQueue(MAX_QUEUE_EXEC_SEC);
            render();
            glfwPollEvents();
        }
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

    private void render() {

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
