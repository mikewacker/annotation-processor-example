package test.method;

import org.example.immutable.Immutable;
import java.util.Map;

@Immutable
public interface TypeDeclaredGeneric {

    Map<String, String> member();
}
