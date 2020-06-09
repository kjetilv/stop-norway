package stopnorway.in;

import stopnorway.database.Id;

import java.util.Collection;

public interface EntityData {

    Id getId();

    Id getId(Field field);

    String getContent(Field field);

    String getAttribute(Attr attr);

    int getIntAttribute(Attr attr);

    int getIntContent(Field field);

    Collection<?> getSublist(Sublist sublist);
}
