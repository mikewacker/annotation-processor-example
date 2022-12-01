package org.example.immutable.processor.modeler;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.testing.compile.Compilation;
import javax.annotation.processing.Filer;
import javax.inject.Inject;
import javax.lang.model.element.TypeElement;
import org.example.immutable.processor.base.ImmutableBaseLiteProcessor;
import org.example.immutable.processor.base.ProcessorScope;
import org.example.immutable.processor.model.ImmutableImpl;
import org.example.immutable.processor.test.TestCompiler;
import org.example.immutable.processor.test.TestImmutableImpls;
import org.example.immutable.processor.test.TestResources;
import org.junit.jupiter.api.Test;

public final class ImmutableImplsTest {

    @Test
    public void create_Empty() throws Exception {
        create("test/Empty.java", TestImmutableImpls.empty());
    }

    private void create(String sourcePath, ImmutableImpl expectedImpl) throws Exception {
        Compilation compilation = TestCompiler.create(TestLiteProcessor.class).compile(sourcePath);
        ImmutableImpl impl = TestResources.loadObjectForSource(compilation, sourcePath, new TypeReference<>() {});
        assertThat(impl).isEqualTo(expectedImpl);
    }

    @ProcessorScope
    public static final class TestLiteProcessor extends ImmutableBaseLiteProcessor {

        private final ImmutableImpls implFactory;
        private final Filer filer;

        @Inject
        TestLiteProcessor(ImmutableImpls implFactory, Filer filer) {
            this.implFactory = implFactory;
            this.filer = filer;
        }

        @Override
        protected void process(TypeElement typeElement) {
            implFactory.create(typeElement).ifPresent(impl -> TestResources.saveObject(filer, typeElement, impl));
        }
    }
}
