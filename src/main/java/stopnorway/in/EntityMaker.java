package stopnorway.in;

import stopnorway.database.Entity;

public interface EntityMaker<E extends Entity> {

    E entity(EntityData data);
}
