package stopnorway.data;

import stopnorway.database.Entity;
import stopnorway.database.Id;

import java.util.function.Consumer;

public final class PointProjection extends Entity {

    private final Id projectedPointRef;

    public PointProjection(Id id, Id projectedPointRef) {
        super(id);
        this.projectedPointRef = projectedPointRef;
    }

    @Override
    public void hashTo(Consumer<byte[]> h) {
        super.hashTo(h);
        hash(h, projectedPointRef);
    }

    @Override
    protected StringBuilder withStringBody(StringBuilder sb) {
        return super.withStringBody(sb).append(Field.ProjectedPointRef).append(": ").append(projectedPointRef);
    }
}
