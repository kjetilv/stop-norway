package stopnorway.data;

import stopnorway.database.Entity;
import stopnorway.database.Id;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

public final class ServiceLink extends Entity {

    private final Id fromPoint;

    private final Id toPoint;

    private final String distance;

    private final Collection<LinkSequenceProjection> projections;

    public ServiceLink(Id id, Id fromPoint, Id toPoint, String distance, Collection<LinkSequenceProjection> projections) {
        super(id);
        this.fromPoint = Objects.requireNonNull(fromPoint, "fromPoint");
        this.toPoint = Objects.requireNonNull(toPoint, "toPoint");
        this.distance = distance;
        this.projections = projections == null || projections.isEmpty() ? Collections.emptyList() : projections;
    }

    @Override
    public void hashTo(Consumer<byte[]> h) {
        super.hashTo(h);
        hash(h, fromPoint, toPoint);
    }

    @Override
    protected StringBuilder withStringBody(StringBuilder sb) {
        return super.withStringBody(sb).append(fromPoint).append("->").append(toPoint);
    }

    public Id getFromPoint() {
        return fromPoint;
    }

    public Id getToPoint() {
        return toPoint;
    }

    public String getDistance() {
        return distance;
    }

    public Collection<LinkSequenceProjection> getProjections() {
        return projections;
    }
}
