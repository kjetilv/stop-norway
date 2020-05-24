package stopnorway.database;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public final class Trajectory {

    private final Collection<Sample> samples;

    public Trajectory(Sample... samples) {
        this(Arrays.asList(samples));
    }

    public Trajectory(Collection<Sample> samples) {
        this.samples = samples == null || samples.isEmpty()
                ? Collections.emptyList()
                : List.copyOf(samples);
    }

    public Collection<Box> getBoxes() {
        return samples.stream().map(Sample::getBox).collect(Collectors.toList());
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[samples:" + samples.size() + "]";
    }
}
