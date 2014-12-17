package org.echovantage.metafactory;

import java.io.IOException;

import javax.lang.model.element.TypeElement;

public interface AnnotationProcessor {
	boolean process(final TypeElement annotation, final MetaInfContext context) throws IOException;
}
