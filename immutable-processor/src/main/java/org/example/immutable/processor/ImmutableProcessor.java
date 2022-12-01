package org.example.immutable.processor;

import com.google.auto.service.AutoService;
import dagger.BindsInstance;
import dagger.Component;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import org.example.immutable.Immutable;
import org.example.immutable.processor.base.ImmutableBaseProcessor;
import org.example.immutable.processor.base.LiteProcessor;
import org.example.immutable.processor.base.ProcessorModule;
import org.example.immutable.processor.base.ProcessorScope;

/** Processes interfaces annotated with {@link Immutable}. */
@AutoService(Processor.class)
public final class ImmutableProcessor extends ImmutableBaseProcessor {

    @Override
    protected LiteProcessor initLiteProcessor(ProcessingEnvironment processingEnv) {
        ProcessorComponent processorComponent = ProcessorComponent.of(processingEnv);
        return processorComponent.liteProcessor();
    }

    @Component(modules = ProcessorModule.class)
    @ProcessorScope
    interface ProcessorComponent {

        static ProcessorComponent of(ProcessingEnvironment processingEnv) {
            return DaggerImmutableProcessor_ProcessorComponent.factory().create(processingEnv);
        }

        ImmutableLiteProcessor liteProcessor();

        @Component.Factory
        interface Factory {

            ProcessorComponent create(@BindsInstance ProcessingEnvironment processingEnv);
        }
    }
}
