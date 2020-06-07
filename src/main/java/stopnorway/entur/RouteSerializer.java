package stopnorway.entur;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import stopnorway.database.AbstractSerializer;

public final class RouteSerializer extends AbstractSerializer<Route> {

    @Override
    public void write(Kryo kryo, Output output, Route object) {
        writeId(kryo, output, object);
        writeString(output, object.getName());
        writeString(output, object.getShortName());
        writeString(output, object.getDirectionType());
        writeList(kryo, output, object.getPointsInSequence());
    }

    @Override
    public Route read(Kryo kryo, Input input, Class<? extends Route> type) {
        return new Route(
                readId(kryo, input),
                readString(input),
                readString(input),
                readString(input),
                readList(kryo, input, PointOnRoute.class));
    }

}
