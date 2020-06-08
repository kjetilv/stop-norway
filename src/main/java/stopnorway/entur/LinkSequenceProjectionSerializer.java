package stopnorway.entur;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import stopnorway.database.AbstractSerializer;
import stopnorway.database.Id;
import stopnorway.geo.CodedPoint;
import stopnorway.geo.CodedPointSerializer;
import stopnorway.geo.Point;

import java.util.ArrayList;
import java.util.Collection;

public class LinkSequenceProjectionSerializer
        extends AbstractSerializer<LinkSequenceProjection> {

    private final Serializer<CodedPoint> pointSerializer;

    public LinkSequenceProjectionSerializer(int dimension) {
        this.pointSerializer = new CodedPointSerializer();
    }

    @Override
    public void write(Kryo kryo, Output output, LinkSequenceProjection object) {
        writeId(kryo, output, object.getId());
        Collection<? extends Point> points = object.getTrajectory();
        int size = points.size();
        output.writeVarInt(size, true);
        for (Point point: points) {
            kryo.writeObject(output, point, pointSerializer);
        }
    }

    @Override
    public LinkSequenceProjection read(Kryo kryo, Input input, Class<? extends LinkSequenceProjection> type) {
        Id id = readId(kryo, input);
        int size = input.readInt(true);
        Collection<Point> points = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            points.add(kryo.readObject(
                    input,
                    CodedPoint.class,
                    pointSerializer));
        }
        return new LinkSequenceProjection(id, points);
    }
}
