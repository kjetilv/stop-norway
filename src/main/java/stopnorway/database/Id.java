package stopnorway.database;

import stopnorway.hash.AbstractHashable;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public class Id extends AbstractHashable {

    public static Map<UUID, Id> ids = new ConcurrentHashMap<>();

    private final int version;

    private final Operator operator;

    private final String type;

    private final String id;

    public Id(Operator operator, String type, String id, int version) {

        this.operator = Objects.requireNonNull(operator, "operator");
        this.type = Objects.requireNonNull(type, "type");
        this.id = Objects.requireNonNull(id, "id");
        this.version = version;
    }

    public int getVersion() {
        return version;
    }

    public Operator getOperator() {
        return operator;
    }

    public String getType() {
        return type;
    }

    public String getId() {
        return id;
    }

    public static Id intern(Id id) {
        return ids.computeIfAbsent(id.getUuid(), uuid -> id);
    }

    @Override
    public void hashTo(Consumer<byte[]> h) {
        hash(h, operator);
        hash(h, id, type);
        hash(h, version);
    }

    @Override
    protected StringBuilder withStringBody(StringBuilder sb) {
        return sb.append(operator).append(":").append(type).append(":").append(id);
    }
}
