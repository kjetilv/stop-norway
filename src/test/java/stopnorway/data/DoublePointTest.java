package stopnorway.data;

import org.junit.jupiter.api.Test;
import stopnorway.geo.*;

import static org.assertj.core.api.Assertions.assertThat;

class DoublePointTest {

    @Test
    void dist2() {
        Point p1 = Points.point(59.00, 10.00);
        Point p2 = Points.point(59.01, 10.00);
        assertThat(p1.distanceTo(p2)).isEqualTo(Distance.of(1111.9492664453662, Unit.M));
    }

    @Test
    void dist3() {
        Point p1 = Points.point(59.00, 10.00);
        Point p2 = Points.point(59.00, 10.02);
        assertThat(p1.distanceTo(p2)).isEqualTo(Distance.of(1145.3924149029722, Unit.M));
    }

    @Test
    void dist4() {
        Point p1 = Points.point(59.00, 10.00);
        Point p2 = Points.point(59.01, 10.02);
        assertThat(p1.distanceTo(p2)).isEqualTo(Distance.of(1596.2374088420825, Unit.M));
    }

    @Test
    void dist() {
        Point p1 = Points.point(59.9131303, 10.7375788);
        Point p2 = Points.point(59.9163249, 10.7285773);

        Distance dist = p1.distanceTo(p2);
        assertThat(dist.toMeters()).isEqualTo(614.765);
    }

    @Test
    void scalebox() {
        Point p1 = Points.point(59.9131303, 10.7375788);

        assertThat(p1.scaledBox(new Scale(100, 100))).isEqualTo(
                Points.point(59.91, 10.73).box(Points.point(59.92, 10.74)));
    }

    @Test
    void down() {
        Point p1 = Points.point(59.9161303, 10.7375788);

        assertThat(p1.downTo(new Scale(100, 50))).isEqualTo(Points.point(59.91, 10.72));
        assertThat(p1.downTo(new Scale(100, 100))).isEqualTo(Points.point(59.91, 10.73));
        assertThat(p1.downTo(new Scale(10, 100))).isEqualTo(Points.point(59.9, 10.73));
    }

    @Test
    void up() {
        Point p1 = Points.point(59.9361303, 10.7235788);

        assertThat(p1.upTo(new Scale(100, 50))).isEqualTo(Points.point(59.94, 10.74));
        assertThat(p1.upTo(new Scale(100, 100))).isEqualTo(Points.point(59.94, 10.73));
        assertThat(p1.upTo(new Scale(100, 10))).isEqualTo(Points.point(59.94, 10.8));
        assertThat(p1.upTo(new Scale(50, 50))).isEqualTo(Points.point(59.94, 10.74));
        assertThat(p1.upTo(new Scale(10, 100))).isEqualTo(Points.point(60.0, 10.73));
    }

}
