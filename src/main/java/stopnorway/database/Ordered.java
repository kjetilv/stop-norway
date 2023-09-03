package stopnorway.database;

public interface Ordered extends Comparable<Ordered> {

    int getOrder();

    @Override
    default int compareTo(Ordered ordered) {
        return Integer.compare(getOrder(), ordered.getOrder());
    }
}
