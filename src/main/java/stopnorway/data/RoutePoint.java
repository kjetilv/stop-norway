package stopnorway.data;

import stopnorway.database.Entity;
import stopnorway.database.Id;

import java.util.Collection;
import java.util.function.Consumer;

public final class RoutePoint extends Entity {

    private final Collection<PointProjection> projections;

    public RoutePoint(Id id, Collection<PointProjection> projections) {
        super(id);
        this.projections = projections;
    }

    @Override
    public void hashTo(Consumer<byte[]> h) {
        super.hashTo(h);
        hash(h, projections);
    }

    @Override
    protected StringBuilder withStringBody(StringBuilder sb) {
        return super.withStringBody(sb).append(Sublist.projections).append(": ").append(
                projections.size() > 1 ? "[" + projections.size() + "]" : projections);
    }
}
