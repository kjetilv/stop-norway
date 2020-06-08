package stopnorway.in;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import stopnorway.Database;
import stopnorway.data.Operator;
import stopnorway.data.ScheduledTrip;
import stopnorway.geo.Box;
import stopnorway.geo.Point;
import stopnorway.geo.Points;
import stopnorway.geo.Scale;

import java.util.Collection;

import static org.assertj.core.api.Assertions.assertThat;
import static stopnorway.geo.Points.point;

class ParserTest extends ParserTestCase {

    @Test
    @Disabled
    public void serviceLinksRuter() {
        Database run = run(Operator.RUT);

    }

    @Test
    @Disabled
    public void serviceLinksAll() {
        Database run = run();

        assertThat(run.getBox().overlaps(Points.NORWAY_BOX));
    }

    @Test
    public void serviceLinksFlaambanen() {
        Database run = run(Operator.FLB);

        Box surroundingBox = point(60.86, 7.11).box(point(60.87, 7.12));

        assertThat(run.getTripDefinitions(
                surroundingBox)).hasSizeGreaterThan(11);

        Point flaamStasjon = point(60.86307, 7.11378);

        assertThat(run.getTripDefinitions(flaamStasjon.scaledBox(Scale.DEFAULT))).hasSizeGreaterThan(11);

        Collection<ScheduledTrip> scheduledTrips = run.getScheduledTrips(surroundingBox);
        assertThat(scheduledTrips).hasSizeGreaterThan(11);
    }
}
