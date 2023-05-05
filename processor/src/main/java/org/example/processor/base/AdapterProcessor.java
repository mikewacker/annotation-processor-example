package org.example.processor.base;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Set;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;

/**
 * {@link Processor} that is an adapter for a {@link LiteProcessor}.
 *
 * <p>The {@link LiteProcessor} is created via the abstract method
 * {@link #createLiteProcessor(ProcessingEnvironment)}.</p>
 *
 * <p>It also handles uncaught exceptions in the {@link LiteProcessor}.</p>
 */
public abstract class AdapterProcessor implements Processor {

    private LiteProcessor liteProcessor;
    private Messager messager;

    @Override
    public final void init(ProcessingEnvironment processingEnv) {
        liteProcessor = createLiteProcessor(processingEnv);
        messager = processingEnv.getMessager();
    }

    /** Creates a {@link LiteProcessor} from the {@link ProcessingEnvironment}. */
    protected abstract LiteProcessor createLiteProcessor(ProcessingEnvironment processingEnv);

    @Override
    public final boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        if (annotations.isEmpty()) {
            return false;
        }

        try {
            liteProcessor.process(annotations, roundEnv);
        } catch (Exception e) {
            String message = createUncaughtExceptionMessage(e);
            messager.printMessage(Diagnostic.Kind.ERROR, message);
        }
        return false;
    }

    /** Create the error message for an uncaught exception. */
    private String createUncaughtExceptionMessage(Exception e) {
        StringWriter messageWriter = new StringWriter();
        PrintWriter writer = new PrintWriter(messageWriter);
        String processorName = getClass().getCanonicalName();
        writer.format("Uncaught exception processing annotations in %s:", processorName)
                .println();
        e.printStackTrace(writer);
        return messageWriter.toString();
    }
}
