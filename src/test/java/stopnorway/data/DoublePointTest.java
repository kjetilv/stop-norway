package stopnorway.data;

import stopnorway.geo.Point;

import static stopnorway.geo.Points.point;

class DoublePointTest extends PointTestCase {

    @Override
    protected Point p(double v, double v2) {
        return point(v, v2);
    }
}
