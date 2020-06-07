package stopnorway.entur;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import stopnorway.database.AbstractSerializer;

public class TimetabledPassingTimeSerializer extends AbstractSerializer<TimetabledPassingTime> {

    @Override
    public void write(Kryo kryo, Output output, TimetabledPassingTime object) {
        writeId(kryo, output, object);
        writeNullableId(kryo, output, object.getStopPointInJourneyPatternRef());
        writeString(output, object.getDepartureTime());
    }

    @Override
    public TimetabledPassingTime read(Kryo kryo, Input input, Class<? extends TimetabledPassingTime> type) {
        return new TimetabledPassingTime(
                readId(kryo, input),
                readNullableId(kryo, input),
                readString(input));
    }
}
