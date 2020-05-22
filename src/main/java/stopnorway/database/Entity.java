package stopnorway.database;

import stopnorway.hash.AbstractHashable;

import java.util.function.Consumer;

public class Entity extends AbstractHashable {

    private final Id id;

    protected Entity(Id id) {

        this.id = id;
    }

    @Override
    protected StringBuilder withStringBody(StringBuilder sb) {
        return sb.append(id);
    }

    @Override
    public void hashTo(Consumer<byte[]> h) {
        hash(h, id);
    }

    public Id getId() {
        return id;
    }
}
