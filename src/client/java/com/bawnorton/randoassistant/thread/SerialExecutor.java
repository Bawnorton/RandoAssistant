package com.bawnorton.randoassistant.thread;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayDeque;
import java.util.Queue;
import java.util.concurrent.Executor;
import java.util.concurrent.RejectedExecutionException;

public class SerialExecutor implements Executor {
    private final Queue<Runnable> tasks;
    private final Executor executor;
    private Runnable active;

    public SerialExecutor() {
        executor = new DirectExecutor();
        tasks = new ArrayDeque<>();
    }

    @Override
    public synchronized void execute(@NotNull Runnable command) {
        if(tasks.size() >= 5000) {
            tasks.clear();
            throw new RejectedExecutionException("Too many tasks queued");
        }
        tasks.add(() -> {
            try {
                command.run();
            } finally {
                scheduleNext();
            }
        });
        if (active == null) {
            scheduleNext();
        }
    }

    protected synchronized void scheduleNext() {
        if ((active = tasks.poll()) != null) {
            executor.execute(active);
        }
    }
}
