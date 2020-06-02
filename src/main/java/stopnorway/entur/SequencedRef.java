package stopnorway.entur;

import stopnorway.database.Entity;
import stopnorway.database.Id;
import stopnorway.database.Ordered;

import java.util.function.Consumer;

abstract class SequencedRef extends Entity implements Ordered {

    private final int order;
    private final Id ref;

    public SequencedRef(Id id, int order, Id ref) {
        super(id);
        this.order = order;
        this.ref = ref;
    }

    @Override
    public final void hashTo(Consumer<byte[]> h) {
        super.hashTo(h);
        hash(h, order);
        hash(h, ref);
    }

    @Override
    protected final StringBuilder withStringBody(StringBuilder sb) {
        return super.withStringBody(sb).append("#").append(order).append(": ").append(ref);
    }

    @Override
    public final int getOrder() {
        return order;
    }

    protected final Id getRef() {
        return ref;
    }
}
