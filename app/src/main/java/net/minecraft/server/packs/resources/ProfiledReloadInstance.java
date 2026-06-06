package net.minecraft.server.packs.resources;

import com.google.common.base.Stopwatch;
import com.mojang.logging.LogUtils;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import net.minecraft.Util;
import net.minecraft.util.Unit;
import net.minecraft.util.profiling.Profiler;
import net.minecraft.util.profiling.ProfilerFiller;
import org.slf4j.Logger;

public class ProfiledReloadInstance extends SimpleReloadInstance<ProfiledReloadInstance.State> {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final Stopwatch total = Stopwatch.createUnstarted();

    public ProfiledReloadInstance(
        ResourceManager p_10649_, List<PreparableReloadListener> p_10650_, Executor p_10651_, Executor p_10652_, CompletableFuture<Unit> p_10653_
    ) {
        super(
            p_10651_,
            p_10652_,
            p_10649_,
            p_10650_,
            (p_372674_, p_372675_, p_372676_, p_372677_, p_372678_) -> {
                AtomicLong atomiclong = new AtomicLong();
                AtomicLong atomiclong1 = new AtomicLong();
                CompletableFuture<Void> completablefuture = p_372676_.reload(
                    p_372674_,
                    p_372675_,
                    profiledExecutor(p_372677_, atomiclong, p_372676_.getName()),
                    profiledExecutor(p_372678_, atomiclong1, p_372676_.getName())
                );
                return completablefuture.thenApplyAsync(p_372682_ -> {
                    LOGGER.debug("Finished reloading {}", p_372676_.getName());
                    return new ProfiledReloadInstance.State(p_372676_.getName(), atomiclong, atomiclong1);
                }, p_10652_);
            },
            p_10653_
        );
        this.total.start();
        this.allDone = this.allDone.thenApplyAsync(this::finish, p_10652_);
    }

    private static Executor profiledExecutor(Executor p_373101_, AtomicLong p_373001_, String p_372795_) {
        return p_372689_ -> p_373101_.execute(() -> {
                ProfilerFiller profilerfiller = Profiler.get();
                profilerfiller.push(p_372795_);
                long i = Util.getNanos();
                p_372689_.run();
                p_373001_.addAndGet(Util.getNanos() - i);
                profilerfiller.pop();
            });
    }

    private List<ProfiledReloadInstance.State> finish(List<ProfiledReloadInstance.State> p_215484_) {
        this.total.stop();
        long i = 0L;
        LOGGER.info("Resource reload finished after {} ms", this.total.elapsed(TimeUnit.MILLISECONDS));

        for (ProfiledReloadInstance.State profiledreloadinstance$state : p_215484_) {
            long j = TimeUnit.NANOSECONDS.toMillis(profiledreloadinstance$state.preparationNanos.get());
            long k = TimeUnit.NANOSECONDS.toMillis(profiledreloadinstance$state.reloadNanos.get());
            long l = j + k;
            String s = profiledreloadinstance$state.name;
            LOGGER.info("{} took approximately {} ms ({} ms preparing, {} ms applying)", s, l, j, k);
            i += k;
        }

        LOGGER.info("Total blocking time: {} ms", i);
        return p_215484_;
    }

    public static record State(String name, AtomicLong preparationNanos, AtomicLong reloadNanos) {
    }
}
