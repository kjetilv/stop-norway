package stopnorway.entur;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import stopnorway.database.AbstractSerializer;

public final class ScheduledStopPointSerializer extends AbstractSerializer<ScheduledStopPoint> {

    @Override
    public void write(Kryo kryo, Output output, ScheduledStopPoint object) {
        writeId(kryo, output, object);
        writeString(output, object.getName());
    }

    @Override
    public ScheduledStopPoint read(Kryo kryo, Input input, Class<? extends ScheduledStopPoint> type) {
        return new ScheduledStopPoint(
                readId(kryo, input),
                readString(input));
    }
}
