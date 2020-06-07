package stopnorway.entur;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import stopnorway.database.AbstractSerializer;

public final class PointProjectionSerializer extends AbstractSerializer<PointProjection> {

    @Override
    public void write(Kryo kryo, Output output, PointProjection object) {
        writeId(kryo, output, object);
        writeId(kryo, output, object.getProjectedPointRef());
    }

    @Override
    public PointProjection read(Kryo kryo, Input input, Class<? extends PointProjection> type) {
        return new PointProjection(
                readId(kryo, input),
                readId(kryo, input));
    }
}
