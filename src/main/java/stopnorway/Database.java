package stopnorway;

import stopnorway.data.Journey;
import stopnorway.data.JourneySpecification;
import stopnorway.geo.Box;
import stopnorway.geo.Scale;

import java.util.Arrays;
import java.util.Collection;

public interface Database {

    Box getBox();

    Scale getScale();

    default Collection<JourneySpecification> getTripDefinitions(Box... boxes) {
        return getTripDefinitions(Arrays.asList(boxes));
    }

    Collection<JourneySpecification> getTripDefinitions(Collection<Box> boxes);

    default Collection<Journey> getJourneys(Box... boxes) {
        return getJourneys(Arrays.asList(boxes));
    }

    Collection<Journey> getJourneys(Collection<Box> boxes);

    int getSize();
}
