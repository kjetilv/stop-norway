package stopnorway.entur;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import stopnorway.database.AbstractSerializer;

public final class JourneyPatternSerializer extends AbstractSerializer<JourneyPattern> {

    @Override
    public void write(Kryo kryo, Output output, JourneyPattern object) {
        writeId(kryo, output, object);
        writeString(output, object.getName());
        writeId(kryo, output, object.getRouteRef());
        writeList(kryo, output, object.getPointsInSequence());
        writeList(kryo, output, object.getLinksInSequence());
    }

    @Override
    public JourneyPattern read(Kryo kryo, Input input, Class<? extends JourneyPattern> type) {
        return new JourneyPattern(
                readId(kryo, input),
                readString(input),
                readId(kryo, input),
                readList(kryo, input, StopPointInJourneyPattern.class),
                readList(kryo, input, ServiceLinkInJourneyPattern.class));
    }
}
