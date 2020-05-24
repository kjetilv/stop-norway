package stopnorway.database;

import org.junit.jupiter.api.Test;
import stopnorway.data.LinkSequenceProjection;
import stopnorway.data.ScheduledStopPoint;
import stopnorway.data.ServiceLink;

import static org.assertj.core.api.Assertions.assertThat;

class ServiceLegTest {

    @Test
    void test_overlap() {
        Id fromId = new Id(Operator.RUT, ScheduledStopPoint.class, "123", 1);
        Id toId = new Id(Operator.RUT, ScheduledStopPoint.class, "234", 1);
        LinkSequenceProjection linkSequenceProjection = new LinkSequenceProjection(
                new Id(Operator.RUT, LinkSequenceProjection.class, "456", 1),
                new Point(2, 1),
                new Point(3, 2),
                new Point(1, 3));
        ServiceLeg serviceLeg = new ServiceLeg(
                new ScheduledStopPoint(fromId, "Foo"),
                new ScheduledStopPoint(toId, "Bar"),
                new ServiceLink(
                        new Id(Operator.RUT, ServiceLink.class, "345", 1),
                        fromId,
                        toId,
                        "100.0",
                        linkSequenceProjection));

        assertThat(linkSequenceProjection.getBox()).hasValue(
                new Box(
                        new Point(1, 1),
                        new Point(3, 3)));

        Box box = new Box(
                new Point(2, 0),
                new Point(4, 4));

        assertThat(linkSequenceProjection.getBox()).hasValueSatisfying(linkSequenceProjectionBox ->
                assertThat(linkSequenceProjectionBox.overlaps(box)).isTrue());

        assertThat(serviceLeg.overlaps(box)).isTrue();
    }

}
