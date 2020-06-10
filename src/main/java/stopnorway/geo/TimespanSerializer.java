package stopnorway.geo;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import stopnorway.database.AbstractSerializer;

import java.time.LocalTime;

public final class TimespanSerializer extends AbstractSerializer<Timespan> {

    @Override
    public void write(
            Kryo kryo, Output output, Timespan object
    ) {
        kryo.writeObject(output, object.getStart());
        output.writeVarInt(object.getStartOffset(), true);
        kryo.writeObject(output, object.getEnd());
        output.writeVarInt(object.getEndOffset(), true);
    }

    @Override
    public Timespan read(
            Kryo kryo, Input input, Class<? extends Timespan> type
    ) {
        return new Timespan(
                kryo.readObject(input, LocalTime.class),
                input.readVarInt(true),
                kryo.readObject(input, LocalTime.class),
                input.readVarInt(true));
    }
}
