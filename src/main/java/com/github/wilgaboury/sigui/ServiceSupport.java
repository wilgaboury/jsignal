package com.github.wilgaboury.sigui;

public class ServiceSupport {
    private final Runnable innerStart;
    private final Runnable innerShutdown;

    private boolean isStarted;
    private boolean isShutdown;

    public ServiceSupport(Runnable innerStart, Runnable innerShutdown) {
        this.innerStart = innerStart;
        this.innerShutdown = innerShutdown;
        this.isStarted = false;
        this.isShutdown = false;
    }

    public synchronized void start() {
        if (isStarted || isShutdown)
            return;

        innerStart.run();
        this.isStarted = true;
    }

    public synchronized void shutdown() {
        if (isShutdown)
            return;

        if (isStarted)
            innerShutdown.run();

        isShutdown = true;
    }

    public synchronized boolean isStarted() {
        return isStarted;
    }

    public synchronized boolean isShutdown() {
        return isShutdown;
    }
}
