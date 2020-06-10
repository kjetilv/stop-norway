package stopnorway.in;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stopnorway.database.Entity;

import java.time.Instant;
import java.util.Collection;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public final class Progress {

    private static final Logger log = LoggerFactory.getLogger(Progress.class);

    static final int K = 1024;

    private static final int MB = K * K;

    private final Collection<Enum<?>> enumsProcessed = new CopyOnWriteArraySet<>();

    private final int sources;

    private final long length;

    private final int enums;

    private final long start;

    private final AtomicInteger sourcesProcessed = new AtomicInteger();

    private final AtomicInteger entities = new AtomicInteger();

    private final AtomicLong bytesProcessed = new AtomicLong();

    private final AtomicBoolean done = new AtomicBoolean();

    private final long mbs;

    public Progress(Collection<OperatorSource> sources, Collection<? extends Enum<?>> enums, Instant start) {
        this.sources = sources.size();
        this.length = sources.stream().mapToLong(OperatorSource::getLength).sum();
        this.enums = enums.size();
        this.start = start.getEpochSecond();
        mbs = length / MB;
    }

    public boolean live() {
        return !done.get();
    }

    public Collection<Entity> recorded(OperatorSource source, Collection<Entity> entities) {
        enumsProcessed.add(source.getEnum());
        sourcesProcessed.incrementAndGet();
        long bytesCount = bytesProcessed.addAndGet(source.getLength());
        this.entities.addAndGet(entities.size());
        if (bytesCount == length) {
            if (done.compareAndSet(false, true)) {
                logSummary(Instant.now());
            }
        }
        return entities;
    }

    public void
    summary(Instant now) {
        if (!done.get()) {
            done.set(logSummary(now));
        }
    }

    private boolean logSummary(Instant now) {
        long bytesCount = bytesProcessed.get();
        int enityCount = entities.get();
        int enumCount = enumsProcessed.size();
        int sourcesCount = sourcesProcessed.get();

        int bytesPerc = (int) (100.0d * bytesCount / length);
        long secs = now.getEpochSecond() - start;
        long bytesHz = bytesCount / secs;
        log.info(
                "{}% {}/{} sources, {}/{}mb /{}mb/s, {}K entities /{}/s <- {}/{} operators",
                bytesPerc,
                sourcesCount,
                sources,
                bytesCount / MB,
                mbs,
                bytesHz / MB,
                enityCount / K,
                enityCount / secs,
                enumCount,
                enums);
        return bytesCount == length;
    }
}
