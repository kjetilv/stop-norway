package stopnorway.entur;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import stopnorway.database.AbstractSerializer;

public class StopPointInJourneyPatternSerializer extends AbstractSerializer<StopPointInJourneyPattern> {

    @Override
    public void write(Kryo kryo, Output output, StopPointInJourneyPattern object) {
        writeId(kryo, output, object);
        output.writeInt(object.getOrder());
        writeId(kryo, output, object.getScheduledStopPointRef());
    }

    @Override
    public StopPointInJourneyPattern read(Kryo kryo, Input input, Class<? extends StopPointInJourneyPattern> type) {
        return new StopPointInJourneyPattern(
                readId(kryo, input),
                input.readInt(),
                readId(kryo, input));
    }
}
