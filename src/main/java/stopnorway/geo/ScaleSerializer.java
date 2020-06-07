package stopnorway.geo;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

public class ScaleSerializer extends Serializer<Scale> {

    @Override
    public void write(Kryo kryo, Output output, Scale object) {
        boolean def = object.equals(Scale.DEFAULT);
        output.writeBoolean(def);
        if (!def) {
            boolean integ = object.equals(Scale.INTEGER);
            output.writeBoolean(integ);
            if (!integ) {
                output.writeInt(object.getLat(), true);
                output.writeVarInt(object.getLon(), true);
            }
        }
    }

    @Override
    public Scale read(Kryo kryo, Input input, Class<? extends Scale> type) {
        if (input.readBoolean()) {
            return Scale.DEFAULT;
        }
        if (input.readBoolean()) {
            return Scale.INTEGER;
        }
        return Scale.of(input.readInt(), input.readInt());
    }
}
