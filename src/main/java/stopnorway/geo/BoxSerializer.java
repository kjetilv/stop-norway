package stopnorway.geo;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

public final class BoxSerializer extends Serializer<Box> {

    @Override
    public void write(Kryo kryo, Output output, Box object) {
        writePoint(kryo, output, object.min());
        writePoint(kryo, output, object.max());
    }

    @Override
    public Box read(Kryo kryo, Input input, Class<? extends Box> type) {
        Point min = readPoint(kryo, input);
        Point max = readPoint(kryo, input);
        return min.box(max);
    }

    private Point readPoint(Kryo kryo, Input input) {
        return input.readBoolean()
                ? kryo.readObject(input, CodedPoint.class)
                : kryo.readObject(input, DoublePoint.class);
    }

    private void writePoint(Kryo kryo, Output output, Point min) {
        output.writeBoolean(min instanceof CodedPoint);
        kryo.writeObject(output, min);
    }
}
