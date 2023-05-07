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
import org.example.immutable.processor.modeler.ElementNavigatorTest;
import org.example.immutable.processor.modeler.ImmutableImplsTest;
import org.example.immutable.processor.modeler.ImmutableMembersTest;
import org.example.immutable.processor.modeler.ImmutableTypesTest;
import org.example.immutable.processor.modeler.MemberTypesTest;
import org.example.processor.base.LiteProcessor;
import org.example.processor.base.ProcessorScope;

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
    @LiteProcessorClassKey(ElementNavigatorTest.TestLiteProcessor.class)
    LiteProcessor bindElementNavigatorTest_TestLiteProcessor(ElementNavigatorTest.TestLiteProcessor liteProcessor);

    @Binds
    @ProcessorScope
    @IntoMap
    @LiteProcessorClassKey(ImmutableImplsTest.TestLiteProcessor.class)
    LiteProcessor bindImmutableImplsTest_TestLiteProcessor(ImmutableImplsTest.TestLiteProcessor liteProcessor);

    @Binds
    @ProcessorScope
    @IntoMap
    @LiteProcessorClassKey(ImmutableMembersTest.TestLiteProcessor.class)
    LiteProcessor bindImmutableMembersTest_TestLiteProcessor(ImmutableMembersTest.TestLiteProcessor liteProcessor);

    @Binds
    @ProcessorScope
    @IntoMap
    @LiteProcessorClassKey(ImmutableTypesTest.TestLiteProcessor.class)
    LiteProcessor bindImmutableTypesTest_TestLiteProcessor(ImmutableTypesTest.TestLiteProcessor liteProcessor);

    @Binds
    @ProcessorScope
    @IntoMap
    @LiteProcessorClassKey(MemberTypesTest.TestLiteProcessor.class)
    LiteProcessor bindMemberTypesTest_TestLiteProcessor(MemberTypesTest.TestLiteProcessor liteProcessor);
}
