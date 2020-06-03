package stopnorway.in;

import java.util.stream.Stream;

public interface OperatorSources {

    Stream<OperatorSource> get(Enum<?> operator);
}
