package stopnorway.geo;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class BoxTest {

    @Test
    void test_build() {
        Box box = Points.point(1d, 200d).box(Points.point(2d, 100d));
        assertThat(box.min()).isEqualTo(Points.point(1d, 100d));
        assertThat(box.max()).isEqualTo(Points.point(2d, 200d));
    }

    @Test
    void test_size() {
        Box box = Points.point(0.0001, .0).box(Points.point(0.0002, .0));
        assertThat(box.areaSqMeters() > 0);
    }

    @Test
    void test_height() {
        Point p1 = Points.point(59.00, 10.00);
        Point p2 = Points.point(59.01, 10.00);
        assertThat(p1.box(p2).height())
                .isEqualTo(Distance.of(1_111.950, Unit.M));
    }

    @Test
    void test_width() {
        Point p1 = Points.point(59.00, 10.00);
        Point p2 = Points.point(59.00, 10.02);
        assertThat(p1.box(p2).width())
                .isEqualTo(Distance.of(1_145.393, Unit.M));
    }

    @Test
    void test_area() {
        Box box = Points.point(1, 200).box(Points.point(2, 100));
        double expected = box.height().toMeters() * box.width().toMeters();
        assertThat(box.areaSqMeters()).isEqualTo(expected);
    }

    @Test
    void scale() {
        Box box = Points.point(59.134, 10.123).box(Points.point(60.234, 12.234));
        Box scaled = box.scaledTo(Scale.of(100, 50));
        assertThat(scaled).isEqualTo(
                Points.point(59.13, 10.12).box(Points.point(60.24, 12.24)));
    }

    @Test
    void test_overlap() {
        Box box1 = Points.point(1d, 100d).box(Points.point(2d, 200d));
        Box box2 = Points.point(1.5d, 50d).box(Points.point(2.5d, 250d));

        asserOverlap(box1, box2);
    }

    @Test
    void test_overlap_again() {
        Box box1 = Points.point(1d, 1d).box(Points.point(3d, 3d));
        Box box2 = Points.point(2d, 0d).box(Points.point(4d, 4d));

        asserOverlap(box1, box2);
    }

    private void asserOverlap(Box box1, Box box2) {
        assertThat(box1.overlaps(box2)).isTrue();
        assertThat(box2.overlaps(box1)).isTrue();
    }

    @Test
    void test_overlap_yet_again() {

        Box box1 = Points.point(59.912, 10.720).box(Points.point(59.915, 10.732));
        Box box2 = Points.point(59.914, 10.729).box(Points.point(59.915, 10.733));

        //
        //               +----o 59.915,10.733
        //  +---------------- o 59.915,10.732
        //  |            |    |
        //  |            o 59.914/10.729
        //  |                 |
        //  |                 |
        //  o 59.912/10720----+
        asserOverlap(box1, box2);
    }

    @Test
    void test_no_overlap() {
        Box box1 = Points.point(1, 1).box(Points.point(2, 2));
        Box box2 = Points.point(3, 3).box(Points.point(4, 4));

        assertThat(box1.overlaps(box2)).isFalse();
        assertThat(box2.overlaps(box1)).isFalse();
    }

    @Test
    void test_combine() {
        Box box1 = Points.point(1d, 100d).box(Points.point(2d, 200d));
        Box box2 = Points.point(1.5d, 50d).box(Points.point(2.5d, 250d));

        assertThat(box1.combined(box2)).isEqualTo(
                Points.point(1d, 50d).box(Points.point(2.5d, 250d)));
    }

    @Test
    void test_combine2() {
        Box box1 = Points.point(1d, 1d).box(Points.point(2d, 2d));
        Box box2 = Points.point(3d, 3d).box(Points.point(5d, 5d));

        assertThat(box1.combined(box2)).isEqualTo(
                Points.point(1d, 1d).box(Points.point(5d, 5d)));
    }
}
