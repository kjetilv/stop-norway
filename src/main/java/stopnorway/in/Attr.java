package stopnorway.in;

import javax.xml.namespace.QName;

public enum Attr implements EnumMatch {

    order,
    id,
    ref;

    private static final String URI = "http://www.netex.org.uk/netex";

    private final DataType dataType;

    private final QName qName;

    Attr() {
        this(null);
    }

    Attr(DataType dataType) {
        this.dataType = dataType != null ? dataType
                : name().endsWith("Ref") ? DataType.Ref
                : DataType.Content;
        this.qName = new QName("", name());
    }

    public DataType getDataType() {
        return dataType;
    }

    public QName qname() {
        return qName;
    }
}
