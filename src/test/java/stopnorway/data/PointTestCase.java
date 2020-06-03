package stopnorway.data;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import stopnorway.geo.Distance;
import stopnorway.geo.Point;
import stopnorway.geo.Scale;
import stopnorway.geo.Unit;

import static org.assertj.core.api.Assertions.assertThat;

public abstract class PointTestCase {
    @Test
    void dist2() {
        Point p1 = p(59.00, 10.00);
        Point p2 = p(59.01, 10.00);
        assertThat(p1.distanceTo(p2)).isEqualTo(Distance.of(1111.950, Unit.M));
    }

    @Test
    void dist3() {
        Point p1 = p(59.00, 10.00);
        Point p2 = p(59.00, 10.02);
        assertThat(p1.distanceTo(p2)).isEqualTo(Distance.of(1145.393, Unit.M));
    }

    @Test
    void dist4() {
        Point p1 = p(59.00, 10.00);
        Point p2 = p(59.01, 10.02);
        assertThat(p1.distanceTo(p2)).isEqualTo(Distance.of(1596.239, Unit.M));
    }

    @Test
    void dist() {
        Point p1 = p(59.913130, 10.737579);
        Point p2 = p(59.916325, 10.728577);

        Distance dist = p1.distanceTo(p2);
        assertThat(dist.toMeters()).isEqualTo(614.814);
    }

    @Test
    void scalebox() {
        Point p1 = p(59.9131303, 10.7375788);

        assertThat(p1.scaledBox(Scale.of(100, 100))).isEqualTo(
                p(59.91, 10.73).box(p(59.92, 10.74)));
    }

    @Test
    void down() {
        Point p1 = p(59.9161303, 10.7375788);

        Point actual = p1.downTo(Scale.of(100, 50));
        assertThat(actual).isEqualTo(p(59.91, 10.72));
        assertThat(p1.downTo(Scale.of(100, 100))).isEqualTo(p(59.91, 10.73));
        assertThat(p1.downTo(Scale.of(10, 100))).isEqualTo(p(59.9, 10.73));
    }

    @Test
    void up() {
        Point p1 = p(59.9361303, 10.7235788);

        assertThat(p1.upTo(Scale.of(100, 50))).isEqualTo(p(59.94, 10.74));
        assertThat(p1.upTo(Scale.of(100, 100))).isEqualTo(p(59.94, 10.73));
        assertThat(p1.upTo(Scale.of(100, 10))).isEqualTo(p(59.94, 10.8));
        assertThat(p1.upTo(Scale.of(50, 50))).isEqualTo(p(59.94, 10.74));
        assertThat(p1.upTo(Scale.of(10, 100))).isEqualTo(p(60.0, 10.73));
    }

    @Test
    void upInt() {
        Point p1 = p(59.9361303, 10.7235788);
        assertThat(p1.upTo(Scale.INTEGER)).isEqualTo(p(60, 11));
    }

    @Test
    void downInt() {
        Point p1 = p(59.9361303, 10.7235788);
        assertThat(p1.downTo(Scale.INTEGER)).isEqualTo(p(59, 10));
    }

    @Test
    void comp() {
        assertThat(p(3, 4)).isLessThan(p(4, 5));
        assertThat(p(3, 4)).isLessThan(p(3, 5));
        assertThat(p(3, 4)).isLessThan(p(4, 2));
    }

    protected abstract Point p(double v, double v2);
}
