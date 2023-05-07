package org.example.immutable.processor.generator;

import dagger.Binds;
import dagger.Module;
import dagger.Provides;
import javax.inject.Named;
import org.example.immutable.processor.model.ImmutableImpl;
import org.example.immutable.processor.model.ImmutableMember;
import org.example.immutable.processor.model.ImmutableType;
import org.example.immutable.processor.model.MemberType;
import org.example.processor.imports.ImportGenerator;
import org.example.processor.imports.ImportManager;
import org.example.processor.source.SourceGenerator;

/** Binds {@link SourceGenerator}'s for various parts of the source code. */
@Module
interface SourceModule {

    @Binds
    @SourceScope
    SourceGenerator<ImmutableImpl> bindSourceGenerator(ImmutableImplGenerator.Source generator);

    @Provides
    @SourceScope
    static SourceGenerator<ImportManager> providePackageAndImportsGenerator() {
        return ImportGenerator.instance();
    }

    @Binds
    @SourceScope
    SourceGenerator<ImmutableType> bindTypeDeclarationGenerator(ImmutableTypeGenerator generator);

    @Binds
    @SourceScope
    @Named("field")
    SourceGenerator<ImmutableMember> bindFieldGenerator(ImmutableMemberGenerator.Field generator);

    @Binds
    @SourceScope
    @Named("constructor")
    SourceGenerator<ImmutableImpl> bindConstructorGenerator(ImmutableImplGenerator.Constructor generator);

    @Binds
    @SourceScope
    @Named("constructorArg")
    SourceGenerator<ImmutableMember> bindConstructorArgGenerator(ImmutableMemberGenerator.ConstructorArg generator);

    @Binds
    @SourceScope
    @Named("fieldInitializer")
    SourceGenerator<ImmutableMember> bindFieldInitializerGenerator(ImmutableMemberGenerator.FieldInitializer generator);

    @Binds
    @SourceScope
    @Named("method")
    SourceGenerator<ImmutableMember> bindMethodGenerator(ImmutableMemberGenerator.Method generator);

    @Binds
    @SourceScope
    SourceGenerator<MemberType> bindTypeGenerator(MemberTypeGenerator generator);
}
