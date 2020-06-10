package stopnorway.geo;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;

class TimespanTest {

    @Test
    void combine() {
        Timespan ts1 = new Timespan(
                LocalTime.NOON.plus(Duration.ofMinutes(30)),
                LocalTime.NOON.plus(Duration.ofMinutes(30)).plus(Duration.ofMinutes(15)));

        Timespan ts2 = new Timespan(
                LocalTime.MIDNIGHT.plus(Duration.ofHours(9)),
                LocalTime.MIDNIGHT.plus(Duration.ofHours(9).plusMinutes(30)));

        assertThat(ts1.combined(ts2)).isEqualTo(
                new Timespan(
                        LocalTime.MIDNIGHT.plus(Duration.ofHours(9)),
                        LocalTime.NOON.plus(Duration.ofMinutes(30)).plus(Duration.ofMinutes(15))));
    }

    @Test
    void test_earliest() {
        Timespan ts1 = new Timespan(
                LocalTime.NOON.plus(Duration.ofHours(22).plusMinutes(30)),
                LocalTime.NOON.plus(Duration.ofHours(23)));

        Timespan ts2 = new Timespan(
                LocalTime.NOON.plus(Duration.ofHours(23).plusMinutes(30)),
                LocalTime.MIDNIGHT.plus(Duration.ofMinutes(30)),
                1);

        Timespan ts3 = new Timespan(
                LocalTime.MIDNIGHT.plus(Duration.ofHours(1)),
                1,
                LocalTime.MIDNIGHT.plus(Duration.ofHours(1).plusMinutes(30)),
                1);

        assertThat(ts1.earliest(ts2)).isSameAs(ts1);
        assertThat(ts1.earliest(ts3)).isSameAs(ts1);
        assertThat(ts2.earliest(ts3)).isSameAs(ts2);

        assertThat(ts2.earliest(ts1)).isSameAs(ts1);
        assertThat(ts3.earliest(ts1)).isSameAs(ts1);
        assertThat(ts3.earliest(ts2)).isSameAs(ts2);
    }

    @Test
    void test_latst() {
        Timespan ts1 = new Timespan(
                LocalTime.NOON.plus(Duration.ofHours(22).plusMinutes(30)),
                LocalTime.NOON.plus(Duration.ofHours(23)));

        Timespan ts2 = new Timespan(
                LocalTime.NOON.plus(Duration.ofHours(23).plusMinutes(30)),
                LocalTime.MIDNIGHT.plus(Duration.ofMinutes(30)),
                1);

        Timespan ts3 = new Timespan(
                LocalTime.MIDNIGHT.plus(Duration.ofHours(1)),
                1,
                LocalTime.MIDNIGHT.plus(Duration.ofHours(1).plusMinutes(30)),
                1);

        assertThat(ts1.latest(ts2)).isSameAs(ts2);
        assertThat(ts1.latest(ts3)).isSameAs(ts3);
        assertThat(ts2.latest(ts3)).isSameAs(ts3);

        assertThat(ts2.latest(ts1)).isSameAs(ts2);
        assertThat(ts3.latest(ts1)).isSameAs(ts3);
        assertThat(ts3.latest(ts2)).isSameAs(ts3);
    }

    @Test
    void combine_offset() {
        Timespan ts1 = new Timespan(
                LocalTime.NOON.plus(Duration.ofHours(22).plusMinutes(30)),
                LocalTime.NOON.plus(Duration.ofHours(23)));

        Timespan ts2 = new Timespan(
                LocalTime.MIDNIGHT.plus(Duration.ofHours(1)),
                1,
                LocalTime.MIDNIGHT.plus(Duration.ofHours(1).plusMinutes(30)),
                1);

        assertThat(ts1.combined(ts2)).isEqualTo(
                new Timespan(
                        LocalTime.NOON.plus(Duration.ofHours(22).plusMinutes(30)),
                        LocalTime.MIDNIGHT.plus(Duration.ofHours(1).plusMinutes(30)),
                        1));
    }

    @Test
    void test_scaling_simple() {
        LocalTime halfPastNoon = LocalTime.NOON.plus(Duration.ofMinutes(30));
        LocalTime quarterToOne = halfPastNoon.plus(Duration.ofMinutes(15));
        Timespan ts = new Timespan(halfPastNoon, quarterToOne);

        assertThat(ts.timespans(Duration.ofHours(1))).hasSize(1);
        assertThat(ts.timespans(Duration.ofHours(1)).findFirst())
                .hasValueSatisfying(scaledBox -> {
                    assertThat(scaledBox.getStart()).isEqualTo(LocalTime.NOON);
                    assertThat(scaledBox.getEnd()).isEqualTo(LocalTime.NOON.plusHours(1));
                });
    }

    @Test
    void test_scaling_multi() {
        LocalTime noon = LocalTime.NOON;
        LocalTime halfPastNoon = noon.plus(Duration.ofMinutes(30));
        LocalTime quarterToOne = halfPastNoon.plus(Duration.ofHours(3));
        Timespan ts = new Timespan(halfPastNoon, quarterToOne);

        assertThat(ts.timespans(Duration.ofHours(1))).hasSize(4);
        assertThat(ts.timespans(Duration.ofHours(1)).findFirst())
                .hasValueSatisfying(scaledBox -> {
                    assertThat(scaledBox.getStart()).isEqualTo(noon);
                    assertThat(scaledBox.getEnd()).isEqualTo(noon.plusHours(1));
                });
        assertThat(ts.timespans(Duration.ofHours(1)).skip(3).findFirst())
                .hasValueSatisfying(timespan -> {
                    assertThat(timespan.getStart()).isEqualTo(noon.plusHours(3));
                    assertThat(timespan.getEnd()).isEqualTo(noon.plusHours(4));
                });
    }

    @Test
    void test_scaling_multi_offset() {
        LocalTime halfPastNoon = LocalTime.MIDNIGHT.plus(Duration.ofHours(23).plusMinutes(30));
        LocalTime quarterToOne = LocalTime.MIDNIGHT.plus(Duration.ofHours(3).plusMinutes(15));
        Timespan ts = new Timespan(halfPastNoon, quarterToOne, 1);

        assertThat(ts.timespans(Duration.ofHours(1))).hasSize(5);

        assertThat(ts.timespans(Duration.ofHours(1)).findFirst()).hasValue(
                new Timespan(LocalTime.MIDNIGHT.plusHours(23), LocalTime.MIDNIGHT, 1));
        assertThat(ts.timespans(Duration.ofHours(1)).skip(1).findFirst()).hasValue(
                new Timespan(LocalTime.MIDNIGHT, 1, LocalTime.MIDNIGHT.plusHours(1), 1));
        assertThat(ts.timespans(Duration.ofHours(1)).skip(2).findFirst()).hasValue(
                new Timespan(LocalTime.MIDNIGHT.plusHours(1), 1, LocalTime.MIDNIGHT.plusHours(2), 1));
        assertThat(ts.timespans(Duration.ofHours(1)).skip(3).findFirst()).hasValue(
                new Timespan(LocalTime.MIDNIGHT.plusHours(2), 1, LocalTime.MIDNIGHT.plusHours(3), 1));
        assertThat(ts.timespans(Duration.ofHours(1)).skip(4).findFirst()).hasValue(
                new Timespan(LocalTime.MIDNIGHT.plusHours(3), 1, LocalTime.MIDNIGHT.plusHours(4), 1));
    }

}
