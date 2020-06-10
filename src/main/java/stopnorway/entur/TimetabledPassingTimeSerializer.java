package stopnorway.entur;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import stopnorway.database.AbstractSerializer;

public final class TimetabledPassingTimeSerializer extends AbstractSerializer<TimetabledPassingTime> {

    @Override
    public void write(Kryo kryo, Output output, TimetabledPassingTime object) {
        writeId(kryo, output, object);
        writeNullableId(kryo, output, object.getStopPointInJourneyPatternRef());
        writeString(output, object.getArrivalTime());
        output.writeVarInt(object.getArrivalDayOffset(), true);
        writeString(output, object.getDepartureTime());
        output.writeVarInt(object.getDepartureDayOffset(), true);
    }

    @Override
    public TimetabledPassingTime read(Kryo kryo, Input input, Class<? extends TimetabledPassingTime> type) {
        return new TimetabledPassingTime(
                readId(kryo, input),
                readNullableId(kryo, input),
                readString(input),
                input.readVarInt(true),
                readString(input),
                input.readVarInt(true));
    }
}
