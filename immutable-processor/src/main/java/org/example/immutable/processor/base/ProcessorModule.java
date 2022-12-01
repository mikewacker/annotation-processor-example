package org.example.immutable.processor.base;

import dagger.Module;
import dagger.Provides;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

/** Provides objects that are part of the {@link ProcessingEnvironment}. */
@Module
public interface ProcessorModule {

    @Provides
    @ProcessorScope
    static Elements provideElements(ProcessingEnvironment processingEnv) {
        return processingEnv.getElementUtils();
    }

    @Provides
    @ProcessorScope
    static Types provideTypes(ProcessingEnvironment processingEnv) {
        return processingEnv.getTypeUtils();
    }

    @Provides
    @ProcessorScope
    static Messager provideMessager(ProcessingEnvironment processingEnv) {
        return processingEnv.getMessager();
    }

    @Provides
    @ProcessorScope
    static Filer provideFiler(ProcessingEnvironment processingEnv) {
        return processingEnv.getFiler();
    }
}
