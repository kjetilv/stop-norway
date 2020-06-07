package stopnorway.entur;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import stopnorway.database.AbstractSerializer;

public final class RoutePointSerializer extends AbstractSerializer<RoutePoint> {

    @Override
    public void write(Kryo kryo, Output output, RoutePoint object) {
        writeId(kryo, output, object);
        writeList(kryo, output, object.getProjections());
    }

    @Override
    public RoutePoint read(Kryo kryo, Input input, Class<? extends RoutePoint> type) {
        return new RoutePoint(
                readId(kryo, input),
                readList(kryo, input, PointProjection.class));
    }
}
