package stopnorway.data;

import org.junit.jupiter.api.Test;
import stopnorway.database.Box;
import stopnorway.database.DoublePoint;
import stopnorway.database.Point;
import stopnorway.database.Scale;

import static org.assertj.core.api.Assertions.assertThat;

class BoxTest {

    @Test
    void test_build() {
        Box box = new Box(new DoublePoint(1, 200), new DoublePoint(2, 100));
        assertThat(box.min()).isEqualTo(new DoublePoint(1, 100));
        assertThat(box.max()).isEqualTo(new DoublePoint(2, 200));
    }

    @Test
    void test_size() {
        Box box = new Box(
                new DoublePoint(0.0001, .0), new DoublePoint(0.0002, .0));
    }

    @Test
    void test_height() {
        Point p1 = new DoublePoint(59.00, 10.00);
        Point p2 = new DoublePoint(59.01, 10.00);
        assertThat(new Box(p1, p2).heightMeters()).isEqualTo(1111.9492664453662);
    }

    @Test
    void test_width() {
        Point p1 = new DoublePoint(59.00, 10.00);
        Point p2 = new DoublePoint(59.00, 10.02);
        assertThat(new Box(p1, p2).widthMeters()).isEqualTo(1145.3924149029722);
    }

    @Test
    void test_area() {
        Box box = new Box(new DoublePoint(1, 200), new DoublePoint(2, 100));
        assertThat(box.areaSqMeters()).isEqualTo(1.2361740275036929E12);
    }

    @Test
    void scale() {
        Box box = new Box(
                new DoublePoint(59.134, 10.123),
                new DoublePoint(60.234, 12.234));
        Box scaled = box.scaledTo(new Scale(100, 50));
        assertThat(scaled).isEqualTo(new Box(
                new DoublePoint(59.13, 10.12),
                new DoublePoint(60.24, 12.24)));
    }

    @Test
    void test_overlap() {
        Box box1 = new Box(new DoublePoint(1, 100), new DoublePoint(2, 200));
        Box box2 = new Box(new DoublePoint(1.5, 50), new DoublePoint(2.5, 250));

        assertThat(box1.overlaps(box2)).isTrue();
        assertThat(box2.overlaps(box1)).isTrue();
    }

    @Test
    void test_overlap_again() {
        Box box1 = new Box(new DoublePoint(1, 1), new DoublePoint(3, 3));
        Box box2 = new Box(new DoublePoint(2, 0), new DoublePoint(4, 4));
    }

    @Test
    void test_overlap_yet_again() {

        Box box1 = new Box(new DoublePoint(59.912, 10.720), new DoublePoint(59.915, 10.732));
        Box box2 = new Box(new DoublePoint(59.914, 10.729), new DoublePoint(59.915, 10.733));

        //
        //               +----o 59.915,10.733
        //  +---------------- o 59.915,10.732
        //  |            |    |
        //  |            o 59.914/10.729
        //  |                 |
        //  |                 |
        //  o 59.912/10720----+
        assertThat(box1.overlaps(box2)).isTrue();
        assertThat(box2.overlaps(box1)).isTrue();
    }

    @Test
    void test_no_overlap() {
        Box box1 = new Box(new DoublePoint(1, 1), new DoublePoint(2, 2));
        Box box2 = new Box(new DoublePoint(3, 3), new DoublePoint(4, 4));

        assertThat(box1.overlaps(box2)).isFalse();
        assertThat(box2.overlaps(box1)).isFalse();
    }

    @Test
    void test_combine() {
        Box box1 = new Box(new DoublePoint(1, 100), new DoublePoint(2, 200));
        Box box2 = new Box(new DoublePoint(1.5, 50), new DoublePoint(2.5, 250));

        assertThat(box1.combined(box2)).isEqualTo(
                new Box(new DoublePoint(1, 50), new DoublePoint(2.5, 250)));
    }

    @Test
    void test_combine2() {
        Box box1 = new Box(new DoublePoint(1, 1), new DoublePoint(2, 2));
        Box box2 = new Box(new DoublePoint(3, 3), new DoublePoint(5, 5));

        assertThat(box1.combined(box2)).isEqualTo(
                new Box(new DoublePoint(1, 1), new DoublePoint(5, 5)));
    }
}
