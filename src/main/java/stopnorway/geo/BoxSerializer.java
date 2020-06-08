package stopnorway.geo;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

public final class BoxSerializer extends Serializer<Box> {

    @Override
    public void write(Kryo kryo, Output output, Box object) {
        kryo.writeObject(output, object.min());
        kryo.writeObject(output, object.max());
    }

    @Override
    public Box read(Kryo kryo, Input input, Class<? extends Box> type) {
        Point min = kryo.readObject(input, CodedPoint.class);
        Point max = kryo.readObject(input, CodedPoint.class);
        return min.box(max);
    }
}
