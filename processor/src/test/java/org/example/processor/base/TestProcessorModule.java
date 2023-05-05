package org.example.processor.base;

import dagger.Binds;
import dagger.Component;
import dagger.MapKey;
import dagger.Module;
import dagger.Provides;
import dagger.multibindings.IntoMap;
import java.util.Map;
import java.util.Objects;
import javax.annotation.processing.Processor;

/**
 * Provides all test implementations of {@link LiteProcessor}
 * as well as the selected implementation, using the {@link LiteProcessor} class as a key.
 *
 * <p>The design of this module is slightly convoluted,
 * but it saves us from writing a separate {@link Component} and {@link Processor}
 * for each test implementation of {@link LiteProcessor}.</p>
 */
@Module
interface TestProcessorModule {

    /** Selects a {@link LiteProcessor} from all available test implementations.  */
    @Provides
    @ProcessorScope
    static LiteProcessor provideLiteProcessor(
            Map<Class<? extends LiteProcessor>, LiteProcessor> liteProcessors, Class<? extends LiteProcessor> key) {
        LiteProcessor liteProcessor = liteProcessors.get(key);
        Objects.requireNonNull(liteProcessor);
        return Objects.requireNonNull(liteProcessor);
    }

    @MapKey
    @interface LiteProcessorClassKey {

        Class<? extends LiteProcessor> value();
    }

    /*
     * Add test implementations of LiteProcessor below (in import order).
     */

    @Binds
    @ProcessorScope
    @IntoMap
    @LiteProcessorClassKey(AdapterProcessorTest.ErrorLiteProcessor.class)
    LiteProcessor bindAdapterProcessorTest_ErrorLiteProcessor(AdapterProcessorTest.ErrorLiteProcessor liteProcessor);

    @Binds
    @ProcessorScope
    @IntoMap
    @LiteProcessorClassKey(AdapterProcessorTest.TestLiteProcessor.class)
    LiteProcessor bindAdapterProcessorTest_TestLiteProcessor(AdapterProcessorTest.TestLiteProcessor liteProcessor);

    @Binds
    @ProcessorScope
    @IntoMap
    @LiteProcessorClassKey(IsolatingLiteProcessorTest.TestLiteProcessor.class)
    LiteProcessor bindIsolatingLiteProcessorTest_TestLiteProcessor(
            IsolatingLiteProcessorTest.TestLiteProcessor liteProcessor);
}
