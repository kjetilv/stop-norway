package stopnorway.geo;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

public final class DoublePointSerializer extends Serializer<DoublePoint> {

    @Override
    public void write(Kryo kryo, Output output, DoublePoint object) {
        output.writeDouble(object.lat());
        output.writeDouble(object.lon());
    }

    @Override
    public DoublePoint read(Kryo kryo, Input input, Class<? extends DoublePoint> type) {
        return new DoublePoint(
                input.readDouble(),
                input.readDouble());
    }
}
