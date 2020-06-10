package stopnorway;

import stopnorway.data.Journey;
import stopnorway.data.JourneySpecification;
import stopnorway.geo.Box;
import stopnorway.geo.Scale;
import stopnorway.geo.Timespan;

import java.time.temporal.TemporalAmount;
import java.util.Arrays;
import java.util.Collection;

public interface Database {

    Box getBox();

    Scale getScale();

    TemporalAmount getTimescale();

    default Collection<JourneySpecification> getJourneySpecifications(Box... boxes) {
        return getJourneySpecifications(Arrays.asList(boxes));
    }

    Collection<JourneySpecification> getJourneySpecifications(Collection<Box> boxes);

    default Collection<Journey> getJourneys(Timespan... boxes) {
        return getJourneys(Arrays.asList(boxes));
    }

    Collection<Journey> getJourneys(Collection<Timespan> boxes);

    int getSize();
}
