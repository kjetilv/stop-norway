package stopnorway.entur;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import stopnorway.database.AbstractSerializer;

import java.time.LocalTime;

public final class TimetabledPassingTimeSerializer extends AbstractSerializer<TimetabledPassingTime> {

    @Override
    public void write(Kryo kryo, Output output, TimetabledPassingTime object) {
        writeId(kryo, output, object);
        writeNullableId(kryo, output, object.getStopPointInJourneyPatternRef());
        kryo.writeObjectOrNull(output, object.getArrivalTime(), LocalTime.class);
        output.writeVarInt(object.getArrivalDayOffset(), true);
        kryo.writeObjectOrNull(output, object.getDepartureTime(), LocalTime.class);
        output.writeVarInt(object.getDepartureDayOffset(), true);
    }

    @Override
    public TimetabledPassingTime read(Kryo kryo, Input input, Class<? extends TimetabledPassingTime> type) {
        return new TimetabledPassingTime(
                readId(kryo, input),
                readNullableId(kryo, input),
                kryo.readObjectOrNull(input, LocalTime.class),
                input.readVarInt(true),
                kryo.readObjectOrNull(input, LocalTime.class),
                input.readVarInt(true));
    }
}
