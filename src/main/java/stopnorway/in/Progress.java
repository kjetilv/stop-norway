package stopnorway.in;

import stopnorway.database.Entity;

import java.time.Instant;
import java.util.Collection;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public final class Progress {

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

    public Progress(Collection<OperatorSource> sources, Collection<Enum<?>> enums, Instant start) {
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
        bytesProcessed.addAndGet(source.getLength());
        this.entities.addAndGet(entities.size());
        return entities;
    }

    public String summary(Instant now) {
        long bytesCount = bytesProcessed.get();
        int enityCount = entities.get();
        int enumCount = enumsProcessed.size();
        int sourcesCount = sourcesProcessed.get();

        int bytesPerc = (int) (100.0d * bytesCount / length);
        if (bytesPerc >= 100) {
            done.set(true);
        }
        long secs = now.getEpochSecond() - start;
        long bytesHz = bytesCount / secs;
        return getClass().getSimpleName() + "[" + bytesPerc + "% " +
                sourcesCount + "/" + sources + " sources, " +
                bytesCount / MB + "/" + mbs + "mb/" + bytesHz / MB + "mb/s, " +
                enityCount / K + "K entities/" + enityCount / secs + "/s <- " +
                enumCount + "/" + enums + " operators]";
    }
}
