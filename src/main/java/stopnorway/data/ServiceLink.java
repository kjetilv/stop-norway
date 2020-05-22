package stopnorway.data;

import stopnorway.database.Entity;
import stopnorway.database.Id;

import java.util.Objects;
import java.util.function.Consumer;

public class ServiceLink extends Entity {

    private final Id fromPoint;

    private final Id toPoint;

    private final String distance;

    public ServiceLink(Id id, Id fromPoint, Id toPoint, String distance) {
        super(id);
        this.fromPoint = Objects.requireNonNull(fromPoint, "fromPoint");
        this.toPoint = Objects.requireNonNull(toPoint, "toPoint");
        this.distance = distance;
    }

    @Override
    protected StringBuilder withStringBody(StringBuilder sb) {
        return super.withStringBody(sb).append(fromPoint).append("->").append(toPoint);
    }

    @Override
    public void hashTo(Consumer<byte[]> h) {
        super.hashTo(h);
        hash(h, fromPoint, toPoint);
    }
}
