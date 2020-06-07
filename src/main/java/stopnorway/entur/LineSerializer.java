package stopnorway.entur;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import stopnorway.database.AbstractSerializer;

public class LineSerializer extends AbstractSerializer<Line> {

    @Override
    public void write(Kryo kryo, Output output, Line object) {
        writeId(kryo, output, object);
        String name = object.getName();
        writeString(output, name);
        writeString(output, object.getTransportMode());
    }

    @Override
    public Line read(Kryo kryo, Input input, Class<? extends Line> type) {
        return new Line(
                readId(kryo, input),
                readString(input),
                readString(input));
    }
}
