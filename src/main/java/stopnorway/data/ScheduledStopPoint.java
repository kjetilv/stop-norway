package stopnorway.data;

import stopnorway.database.Entity;
import stopnorway.database.Id;

import java.util.function.Consumer;

public class ScheduledStopPoint extends Entity {

    private final String name;

    public ScheduledStopPoint(Id id, String name) {
        super(id);
        this.name = name;
    }

    @Override
    protected StringBuilder withStringBody(StringBuilder sb) {
        return super.withStringBody(sb).append(":").append(name);
    }

    @Override
    public void hashTo(Consumer<byte[]> h) {
        super.hashTo(h);
        hash(h, name);
    }
}