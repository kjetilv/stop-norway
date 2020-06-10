package stopnorway;

import org.junit.jupiter.api.Test;
import stopnorway.data.Operator;
import stopnorway.data.JourneySpecification;
import stopnorway.geo.Distance;
import stopnorway.geo.Point;
import stopnorway.geo.Points;
import stopnorway.geo.Unit;
import stopnorway.in.TestData;

import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

class DatabaseTest {

    @Test
    void test_national() {
        Database database = TestData.getDatabase(Operator.RUT);

        Point[] points = {
                Points.point(59.913916, 10.734865),
                Points.point(59.913470, 10.736641),
                Points.point(59.912921, 10.738953)
        };
        Distance accuracy = Distance.of(10, Unit.M);
        Collection<JourneySpecification> tripDefinitions = database.getJourneySpecifications(
                Arrays.stream(points)
                        .map(point -> point.squareBox(accuracy))
                        .collect(Collectors.toList()));
        assertThat(tripDefinitions).isNotEmpty();
    }
}
