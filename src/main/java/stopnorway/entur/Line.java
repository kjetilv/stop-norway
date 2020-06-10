package stopnorway.entur;

import stopnorway.database.Entity;
import stopnorway.database.Id;

import java.util.function.Consumer;

public final class Line extends Entity {

    private final String name;

    private final String transportMode;

    public Line(Id id, String name, String transportMode) {
        super(id);
        this.name = name;
        this.transportMode = transportMode;
    }

    @Override
    public void hashTo(Consumer<byte[]> h) {
        super.hashTo(h);
        hash(h, name, transportMode);
    }

    public String getName() {
        return name;
    }

    public String getTransportMode() {
        return transportMode;
    }

    @Override
    protected StringBuilder withStringBody(StringBuilder sb) {
        return super.withStringBody(sb)
                .append(name).append("(").append(transportMode).append(")");
    }
}
