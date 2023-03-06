package com.bawnorton.randoassistant.thread;

import com.bawnorton.randoassistant.RandoAssistant;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayDeque;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public class FailableSerialExecutor implements Executor {
    private final Queue<FailableRunnable> tasks;
    private final Executor executor;
    private FailableRunnable active;

    public FailableSerialExecutor() {
        executor = new DirectExecutor();
        tasks = new ArrayDeque<>();
    }

    public synchronized void execute(@NotNull Runnable command, @NotNull Runnable onSuccess, @NotNull Runnable onFailure) {
        tasks.add(new FailableRunnable(command, onSuccess, onFailure));
        if (active == null) {
            scheduleNext();
        }
    }

    protected synchronized void scheduleNext() {
        if ((active = tasks.poll()) != null) {
            executor.execute(active);
        }
    }

    @Override
    public synchronized void execute(@NotNull Runnable command) {
        throw new UnsupportedOperationException("Use execute(Runnable, Runnable, Runnable) instead");
    }

    private class FailableRunnable implements Runnable {
        private final Runnable runnable;
        private final Runnable onSuccess;
        private final Runnable onFailure;

        private FailableRunnable(Runnable runnable, Runnable onSuccess, Runnable onFailure) {
            this.runnable = runnable;
            this.onSuccess = onSuccess;
            this.onFailure = onFailure;
        }

        @Override
        public synchronized void run() {
            CompletableFuture.runAsync(runnable)
                    .thenRun(() -> {
                        onSuccess.run();
                        scheduleNext();
                    })
                    .exceptionally((e) -> {
                        onFailure.run();
                        RandoAssistant.LOGGER.error("Failed to execute task", e);
                        scheduleNext();
                        return null;
                    });
        }
    }
}
