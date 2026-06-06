package net.minecraft;

import com.mojang.jtracy.TracyClient;
import com.mojang.jtracy.Zone;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

public record TracingExecutor(ExecutorService service) implements Executor {
    public Executor forName(String p_372983_) {
        if (SharedConstants.IS_RUNNING_IN_IDE) {
            return p_372951_ -> this.service.execute(() -> {
                    Thread thread = Thread.currentThread();
                    String s = thread.getName();
                    thread.setName(p_372983_);

                    try (Zone zone = TracyClient.beginZone(p_372983_, SharedConstants.IS_RUNNING_IN_IDE)) {
                        p_372951_.run();
                    } finally {
                        thread.setName(s);
                    }
                });
        } else {
            return (Executor)(TracyClient.isAvailable() ? (Executor) p_372837_ -> this.service.execute(() -> {
                    try (Zone zone = TracyClient.beginZone(p_372983_, SharedConstants.IS_RUNNING_IN_IDE)) {
                        p_372837_.run();
                    }
                }) : this.service);
        }
    }

    @Override
    public void execute(Runnable p_373097_) {
        this.service.execute(wrapUnnamed(p_373097_));
    }

    public void shutdownAndAwait(long p_372802_, TimeUnit p_373037_) {
        this.service.shutdown();

        boolean flag;
        try {
            flag = this.service.awaitTermination(p_372802_, p_373037_);
        } catch (InterruptedException interruptedexception) {
            flag = false;
        }

        if (!flag) {
            this.service.shutdownNow();
        }
    }

    private static Runnable wrapUnnamed(Runnable p_372957_) {
        return !TracyClient.isAvailable() ? p_372957_ : () -> {
            try (Zone zone = TracyClient.beginZone("task", SharedConstants.IS_RUNNING_IN_IDE)) {
                p_372957_.run();
            }
        };
    }
}
