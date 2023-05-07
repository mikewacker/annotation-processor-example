package org.example.immutable.processor.generator;

import dagger.BindsInstance;
import dagger.Component;
import javax.annotation.processing.Filer;
import javax.inject.Inject;
import javax.lang.model.element.TypeElement;
import org.example.immutable.processor.model.ImmutableImpl;
import org.example.processor.base.ProcessorScope;
import org.example.processor.source.IsolatingSourceFileGenerator;
import org.example.processor.source.SourceGenerator;
import org.example.processor.type.ImportableType;

/** Generates source files from {@link ImmutableImpl}'s. */
@ProcessorScope
public final class ImmutableGenerator extends IsolatingSourceFileGenerator<ImmutableImpl, TypeElement> {

    @Inject
    ImmutableGenerator(Filer filer) {
        super(filer);
    }

    @Override
    protected String getSourceName(ImmutableImpl impl) {
        return impl.type().qualifiedName();
    }

    @Override
    protected SourceGenerator<ImmutableImpl> createSourceGenerator(ImmutableImpl impl) {
        SourceComponent sourceComponent = SourceComponent.of(impl.importManager());
        return sourceComponent.sourceGenerator();
    }

    @Component(modules = SourceModule.class)
    @SourceScope
    interface SourceComponent {

        static SourceComponent of(SourceGenerator<ImportableType> namer) {
            return DaggerImmutableGenerator_SourceComponent.factory().create(namer);
        }

        SourceGenerator<ImmutableImpl> sourceGenerator();

        @Component.Factory
        interface Factory {

            SourceComponent create(@BindsInstance SourceGenerator<ImportableType> namer);
        }
    }
}
