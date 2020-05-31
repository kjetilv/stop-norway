package stopnorway.in;

import org.junit.jupiter.api.Test;
import stopnorway.Database;
import stopnorway.geo.*;
import stopnorway.database.*;

import static org.assertj.core.api.Assertions.assertThat;
import static stopnorway.geo.Points.point;
import static stopnorway.geo.Unit.M;

class ParserTest extends ParserTestCase {

    @Test
    public void serviceLinks() {
        Database run = run(Operator.OSC);

        Box surroundingBox = point(59.675, 10.609).box(point(59.679, 10.584));

        assertThat(run.getServiceLegs(
                surroundingBox)).hasSize(6);

        Box oscarsborgBox = point(59.67566, 10.58461).squareBox(Distance.of(10, M));

        assertThat(run.getServiceLegs(oscarsborgBox)).hasSize(2);
    }

}
