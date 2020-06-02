package stopnorway.database;

import java.util.Objects;

public class AbstractIdentified {

    private final Id id;

    protected AbstractIdentified(Id id) {
        this.id = Objects.requireNonNull(id, "id");
    }

    public final Id getId() {
        return id;
    }
}
