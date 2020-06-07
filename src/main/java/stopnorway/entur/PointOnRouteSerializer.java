package stopnorway.entur;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import stopnorway.database.AbstractSerializer;

public final class PointOnRouteSerializer extends AbstractSerializer<PointOnRoute> {

    @Override
    public void write(Kryo kryo, Output output, PointOnRoute object) {
        writeId(kryo, output, object);
        writeNullableId(kryo, output, object.getRoutePoint());
    }

    @Override
    public PointOnRoute read(Kryo kryo, Input input, Class<? extends PointOnRoute> type) {
        return new PointOnRoute(
                readId(kryo, input),
                readNullableId(kryo, input));
    }
}
