package stopnorway.entur;

import stopnorway.database.Entity;
import stopnorway.database.Id;
import stopnorway.in.Field;

import java.util.function.Consumer;

public final class PointOnRoute extends Entity {

    private final Id routePoint;

    public PointOnRoute(Id id, Id routePoint) {
        super(id);
        this.routePoint = routePoint;
    }

    public Id getRoutePoint() {
        return routePoint;
    }

    @Override
    protected StringBuilder withStringBody(StringBuilder sb) {
        return super.withStringBody(sb).append(Field.RoutePointRef).append(": ").append(routePoint);
    }

    @Override
    public void hashTo(Consumer<byte[]> h) {
        super.hashTo(h);
        hash(h, routePoint);
    }
}
