package io.heavenland.mebot;

import jakarta.annotation.Resource;
import jakarta.enterprise.concurrent.ManagedExecutorService;

import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public abstract class BackgroundTaskProcessor {

    @Resource(name = "concurrent/default-managed-executor-service")
    protected ManagedExecutorService managedExecutorService;

    protected Future<?> future = null;

    protected boolean isDone = true;

    public boolean isRunning() {
        return !isDone;
    }

    public ExecutorService getExecutorService() {
        ExecutorService service;
        service = Objects.requireNonNullElseGet(managedExecutorService, () -> Executors.newFixedThreadPool(10));
        return service;
    }

    public abstract Runnable createRunnable();

    public final boolean start() {

        if (isRunning()) {
            return false;
        }

        isDone = false;
        var service = getExecutorService();
        future = service.submit(createRunnable());
        return true;
    }
}
