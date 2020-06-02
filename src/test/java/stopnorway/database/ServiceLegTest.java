package stopnorway.database;

import org.junit.jupiter.api.Test;
import stopnorway.data.Operator;
import stopnorway.data.ServiceLeg;
import stopnorway.entur.LinkSequenceProjection;
import stopnorway.entur.ScheduledStopPoint;
import stopnorway.entur.ServiceLink;
import stopnorway.geo.Box;
import stopnorway.geo.Points;

import static org.assertj.core.api.Assertions.assertThat;

class ServiceLegTest {

    @Test
    void test_overlap() {
        Id fromId = new Id(Operator.RUT, ScheduledStopPoint.class, "123");
        Id toId = new Id(Operator.RUT, ScheduledStopPoint.class, "234");
        LinkSequenceProjection linkSequenceProjection = new LinkSequenceProjection(
                new Id(Operator.RUT, LinkSequenceProjection.class, "456"),
                Points.point(2, 1),
                Points.point(3, 2),
                Points.point(1, 3));
        ServiceLeg serviceLeg = new ServiceLeg(
                new Id(Operator.RUT, ServiceLink.class, "234"),
                new ScheduledStopPoint(fromId, "Foo"),
                new ScheduledStopPoint(toId, "Bar"),
                new ServiceLink(
                        new Id(Operator.RUT, ServiceLink.class, "345"),
                        fromId,
                        toId,
                        "100.0",
                        linkSequenceProjection));

        assertThat(linkSequenceProjection.getBox()).hasValue(
                Points.point(1, 1).box(Points.point(3, 3)));

        Box box = Points.point(2, 0).box(Points.point(4, 4));

        assertThat(linkSequenceProjection.getBox()).hasValueSatisfying(linkSequenceProjectionBox ->
                assertThat(linkSequenceProjectionBox.overlaps(box)).isTrue());

        assertThat(serviceLeg.overlaps(box)).isTrue();
    }

}
