package stopnorway.entur;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import stopnorway.database.AbstractSerializer;
import stopnorway.geo.Point;

public class LinkSequenceProjectionSerializer
        extends AbstractSerializer<LinkSequenceProjection> {

    private final Class<? extends Point> pointClass;

    public LinkSequenceProjectionSerializer(Class<? extends Point> pointClass) {
        this.pointClass = pointClass;
    }

    @Override
    public void write(Kryo kryo, Output output, LinkSequenceProjection object) {
        writeId(kryo, output, object.getId());
        writeList(kryo, output, object.getTrajectory());
    }

    @Override
    public LinkSequenceProjection read(Kryo kryo, Input input, Class<? extends LinkSequenceProjection> type) {
        return new LinkSequenceProjection(
                readId(kryo, input),
                readList(kryo, input, this.pointClass));
    }
}
