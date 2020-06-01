package stopnorway.in;

import org.junit.jupiter.api.Test;
import stopnorway.Database;
import stopnorway.database.Operator;
import stopnorway.geo.Box;
import stopnorway.geo.Point;
import stopnorway.geo.Scale;

import static org.assertj.core.api.Assertions.assertThat;
import static stopnorway.geo.Points.point;

class ParserTest extends ParserTestCase {

    @Test
    public void serviceLinks() {
        Database run = run(Operator.FLB);

        Box surroundingBox = point(60.86, 7.11).box(point(60.87, 7.12));

        assertThat(run.getServiceLegs(
                surroundingBox)).hasSize(11);

        Point flaamStasjon = point(60.86307, 7.11378);

        assertThat(run.getServiceLegs(flaamStasjon.scaledBox(Scale.DEFAULT))).hasSize(11);
    }

}
