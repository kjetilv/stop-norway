package stopnorway.geo;

import stopnorway.util.Accept;

import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;

public final class Trajectory {

    private final Collection<Sample> samples;

    public Trajectory(Sample... samples) {
        this(Arrays.asList(samples));
    }

    public Trajectory(Collection<Sample> samples) {
        this.samples = Accept.list(samples);
    }

    public Collection<Box> getBoxes() {
        return samples.stream().map(Sample::getBox).collect(Collectors.toList());
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[samples:" + samples.size() + "]";
    }
}
