package stopnorway.database;

import stopnorway.geo.Box;
import stopnorway.util.MostlyOnce;

import java.util.Optional;
import java.util.function.Supplier;

public abstract class AbstractBoxed extends AbstractIdentified implements Boxed {

    private final Supplier<Box> box;

    protected AbstractBoxed(Id id) {
        super(id);
        this.box = MostlyOnce.get(() -> computeBox()
                .orElseThrow(() ->
                        new IllegalStateException(this + " could not compute box")));
    }

    public Box getBox() {
        return box.get();
    }

    protected abstract Optional<Box> computeBox();
}
