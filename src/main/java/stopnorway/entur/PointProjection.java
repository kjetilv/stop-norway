package stopnorway.entur;

import stopnorway.database.Entity;
import stopnorway.database.Id;
import stopnorway.in.Field;

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

    public Id getProjectedPointRef() {
        return projectedPointRef;
    }

    @Override
    protected StringBuilder withStringBody(StringBuilder sb) {
        return super.withStringBody(sb).append(Field.ProjectedPointRef).append(": ").append(projectedPointRef);
    }
}
