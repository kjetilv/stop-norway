package stopnorway.geo;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

public final class CodedPointSerializer extends Serializer<CodedPoint> {

    @Override
    public void write(Kryo kryo, Output output, CodedPoint object) {
        if (object.dimension() == 1_000_000) {
            output.writeBoolean(true);
        } else {
            output.writeBoolean(false);
            output.writeVarInt((int) Math.log10(object.dimension()), true);
        }
        output.writeInt(object.intLat(), true);
        output.writeInt(object.intLon(), true);
    }

    @Override
    public CodedPoint read(Kryo kryo, Input input, Class<? extends CodedPoint> type) {
        int dimension = input.readBoolean() ? 1_000_000 : (int) Math.pow(10, input.readVarInt(true));
        return new CodedPoint(
                dimension,
                input.readInt(true),
                input.readInt(true));
    }
}
