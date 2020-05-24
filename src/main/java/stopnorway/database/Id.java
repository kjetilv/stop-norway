package stopnorway.database;

import stopnorway.hash.AbstractHashable;

import java.util.Objects;
import java.util.function.Consumer;

public final class Id extends AbstractHashable {

    private final int version;

    private final Operator operator;

    private final String type;

    private final String id;

    public Id(Operator operator, Class<? extends Entity> type, String id, int version) {
        this(operator, Objects.requireNonNull(type, "type").getSimpleName(), id, version);
    }

    public Id(Operator operator, String type, String id, int version) {

        this.operator = Objects.requireNonNull(operator, "operator");
        this.type = Objects.requireNonNull(type, "type");
        this.id = Objects.requireNonNull(id, "id");
        this.version = version;
    }

    public static Id id(Operator operator, String type, String id, int version) {
        return new Id(operator, type, id, version);
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
