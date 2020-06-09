package stopnorway.in;

import stopnorway.database.Entity;

public interface EntityMaker<E extends Entity> {

    Entity entity(ParseState<E> data);
}
