package stopnorway.geo;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PointsTest {

    @Test
    void parseSequence() {
        List<Point> sequence = Points.sequence("54.3 10.71 54.2 10.76 55.123123123 10.789789789789 54.678678678 10 54.567567567 10.0");

        assertThat(sequence).containsExactly(
                Points.point(54.3, 10.71),
                Points.point(54.2, 10.76),
                Points.point(55.123123, 10.789790),
                Points.point(54.678679, 10.0),
                Points.point(54.567568, 10.0));
    }
}
