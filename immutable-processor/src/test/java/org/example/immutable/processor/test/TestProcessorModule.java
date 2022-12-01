package org.example.immutable.processor.test;

import dagger.Binds;
import dagger.Component;
import dagger.MapKey;
import dagger.Module;
import dagger.Provides;
import dagger.multibindings.IntoMap;
import java.util.Map;
import java.util.Objects;
import javax.annotation.processing.Processor;
import org.example.immutable.processor.base.ImmutableBaseLiteProcessorTest;
import org.example.immutable.processor.base.LiteProcessor;
import org.example.immutable.processor.base.ProcessorScope;
import org.example.immutable.processor.generator.ImmutableGeneratorTest;
import org.example.immutable.processor.modeler.ElementNavigatorTest;
import org.example.immutable.processor.modeler.ImmutableImplsTest;
import org.example.immutable.processor.modeler.ImmutableTypesTest;
import org.example.immutable.processor.modeler.TopLevelTypesTest;

/**
 * Provides all test implementations of {@link LiteProcessor}
 * as well as the selected implementation, using the {@link LiteProcessor} class as a key.
 *
 * <p>The design of this module is slightly convoluted,
 * but it saves us from writing a separate {@link Component} and {@link Processor}
 * for each test implementation of {@link LiteProcessor}.</p>
 */
@Module
public interface TestProcessorModule {

    /** Selects a {@link LiteProcessor} from all available test implementations.  */
    @Provides
    @ProcessorScope
    static LiteProcessor provideLiteProcessor(
            Map<Class<? extends LiteProcessor>, LiteProcessor> liteProcessors, Class<? extends LiteProcessor> key) {
        LiteProcessor liteProcessor = liteProcessors.get(key);
        return Objects.requireNonNull(liteProcessor);
    }

    @MapKey
    @interface LiteProcessorClassKey {

        Class<? extends LiteProcessor> value();
    }

    /*
     * Add test implementations of LiteProcessor below (in import order).
     */

    // org.example.immutable.processor.base

    @Binds
    @ProcessorScope
    @IntoMap
    @LiteProcessorClassKey(ImmutableBaseLiteProcessorTest.TestLiteProcessor.class)
    LiteProcessor bindImmutableBaseLiteProcessorTestLiteProcessor(
            ImmutableBaseLiteProcessorTest.TestLiteProcessor liteProcessor);

    // org.example.immutable.processor.generator

    @Binds
    @ProcessorScope
    @IntoMap
    @LiteProcessorClassKey(ImmutableGeneratorTest.TestLiteProcessor.class)
    LiteProcessor bindImmutableGeneratorTestLiteProcessor(ImmutableGeneratorTest.TestLiteProcessor liteProcessor);

    // org.example.immutable.processor.modeler

    @Binds
    @ProcessorScope
    @IntoMap
    @LiteProcessorClassKey(ElementNavigatorTest.TestLiteProcessor.class)
    LiteProcessor bindElementNavigatorTestLiteProcessor(ElementNavigatorTest.TestLiteProcessor liteProcessor);

    @Binds
    @ProcessorScope
    @IntoMap
    @LiteProcessorClassKey(ImmutableImplsTest.TestLiteProcessor.class)
    LiteProcessor bindImmutableImplsTestLiteProcessor(ImmutableImplsTest.TestLiteProcessor liteProcessor);

    @Binds
    @ProcessorScope
    @IntoMap
    @LiteProcessorClassKey(ImmutableTypesTest.TestLiteProcessor.class)
    LiteProcessor bindImmutableTypesTestLiteProcessor(ImmutableTypesTest.TestLiteProcessor liteProcessor);

    @Binds
    @ProcessorScope
    @IntoMap
    @LiteProcessorClassKey(TopLevelTypesTest.TestLiteProcessor.class)
    LiteProcessor bindTopLevelTypesTestLiteProcessor(TopLevelTypesTest.TestLiteProcessor liteProcessor);
}
