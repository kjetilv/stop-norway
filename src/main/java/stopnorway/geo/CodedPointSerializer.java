package stopnorway.geo;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

public final class CodedPointSerializer extends Serializer<CodedPoint> {

    @Override
    public void write(Kryo kryo, Output output, CodedPoint object) {
        output.writeInt(object.intLat(), true);
        output.writeInt(object.intLon(), true);
    }

    @Override
    public CodedPoint read(Kryo kryo, Input input, Class<? extends CodedPoint> type) {
        return new CodedPoint(
                input.readInt(true),
                input.readInt(true));
    }
}
