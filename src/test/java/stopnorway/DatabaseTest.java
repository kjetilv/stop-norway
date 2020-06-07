package stopnorway;

import org.junit.jupiter.api.Test;
import stopnorway.data.Operator;
import stopnorway.data.TripDefinition;
import stopnorway.geo.Distance;
import stopnorway.geo.Point;
import stopnorway.geo.Points;
import stopnorway.geo.Unit;
import stopnorway.in.TestData;

import java.util.Collection;

import static org.assertj.core.api.Assertions.assertThat;

class DatabaseTest {

    @Test
    void test_national() {
        Database database = TestData.getDatabase(Operator.RUT);

        Point point = Points.point(59.914632, 10.732630);
        Distance accuracy = Distance.of(10, Unit.M);
        Collection<TripDefinition> tripDefinitions = database.getTripDefinitions(point.squareBox(accuracy));
        assertThat(tripDefinitions).isNotEmpty();
    }

}
