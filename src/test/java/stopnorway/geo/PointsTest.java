package stopnorway.geo;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PointsTest {

    @Test
    void parseSequence() {
        List<Point> sequence = Points.sequence("54.3 10.71 54.2 10.76 55.123123123 10.789789789789");

        assertThat(sequence).containsExactly(
                Points.point(54.3, 10.71),
                Points.point(54.2, 10.76),
                Points.point(55.123123, 10.789790));
    }

}
