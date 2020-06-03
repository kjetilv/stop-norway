package stopnorway.data;

import stopnorway.geo.Point;
import stopnorway.geo.Points;

class CodePointTest extends PointTestCase {

    @Override
    protected Point p(double lat, double lon) {
        return Points.NORWAY_CODER.coded(lat, lon);
    }
}
