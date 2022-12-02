package test.method;

import org.example.immutable.Immutable;
import java.util.Map;

@Immutable
public interface TypeDeclaredNestedGenericStatic {

    Map.Entry<String, String> member();
}
