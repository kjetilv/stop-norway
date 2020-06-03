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

    @Override
    public boolean equals(Object o) {
        return this == o || o != null && getClass() == o.getClass() &&
                Objects.equals(id, ((AbstractIdentified) o).id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
