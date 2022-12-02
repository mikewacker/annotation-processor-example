package test.method;

import org.example.immutable.Immutable;
import java.util.List;

@Immutable
public interface TypeWildcard {

    List<?> member();
}
