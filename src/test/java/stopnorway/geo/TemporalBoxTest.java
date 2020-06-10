package stopnorway.geo;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static stopnorway.geo.Points.point;

class TemporalBoxTest {

    @Test
    void test_scaling() {
        LocalTime halfPastNoon = LocalTime.NOON.plus(Duration.ofMinutes(30));
        LocalTime quarterToOne = halfPastNoon.plus(Duration.ofMinutes(15));
        TemporalBox box = new TemporalBox(point(54.10, 10.2).box(54.12, 10.4), halfPastNoon, quarterToOne);

        TemporalBox scaledBox = box.scaledBox(Duration.ofHours(1));
        assertThat(scaledBox.getStart()).isEqualTo(LocalTime.NOON);
        assertThat(scaledBox.getEnd()).isEqualTo(LocalTime.NOON.plusHours(1));

    }

}
