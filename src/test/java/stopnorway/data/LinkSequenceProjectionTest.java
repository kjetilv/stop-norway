package stopnorway.data;

import org.junit.jupiter.api.Test;
import stopnorway.database.*;
import stopnorway.entur.LinkSequenceProjection;
import stopnorway.geo.Points;

import static org.assertj.core.api.Assertions.assertThat;

class LinkSequenceProjectionTest {

    @Test
    void test_box() {
        LinkSequenceProjection projection = new LinkSequenceProjection(
                new Id(Operator.RUT, LinkSequenceProjection.class, "213", 1),
                Points.point(59.1, 10.1),
                Points.point(59.0, 10.2),
                Points.point(58.9, 10.3));
        assertThat(projection.getBox()).hasValue(
                Points.point(58.9, 10.1).box(Points.point(59.1, 10.3)));
    }

    @Test
    void test_start() {
        LinkSequenceProjection projection = new LinkSequenceProjection(
                new Id(Operator.RUT, LinkSequenceProjection.class, "213", 1),
                Points.point(59.1, 10.1),
                Points.point(59.0, 10.2),
                Points.point(58.9, 10.3));
        assertThat(projection.getStart()).hasValue(Points.point(59.1, 10.1));
    }

    @Test
    void test_end() {
        LinkSequenceProjection projection = new LinkSequenceProjection(
                new Id(Operator.RUT, LinkSequenceProjection.class, "213", 1),
                Points.point(59.1, 10.1),
                Points.point(59.0, 10.2),
                Points.point(58.9, 10.3));
        assertThat(projection.getEnd()).hasValue(Points.point(58.9, 10.3));
    }
}
