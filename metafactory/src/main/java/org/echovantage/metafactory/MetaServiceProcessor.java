package org.echovantage.metafactory;

import static org.echovantage.util.Elements.areEqual;
import static org.echovantage.util.Elements.isService;
import static org.echovantage.util.Elements.typeOf;

import java.io.IOException;

import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;

import org.echovantage.metafactory.MetaInfContext.MetaInfContract;

public class MetaServiceProcessor implements AnnotationProcessor {
	@Override
	public boolean process(final TypeElement annotation, final MetaInfContext context) throws IOException {
		if(!areEqual(annotation, MetaService.class)) {
			return false;
		}
		for(final Element service : context.getElementsAnnotatedWith(annotation)) {
			process(context, (TypeElement)service);
		}
		return true;
	}

	private void process(final MetaInfContext context, final TypeElement service) {
		if(!isService(service)) {
			throw new IllegalArgumentException(String.format("Not a valid service %s", service));
		}
		final MetaService anno = service.getAnnotation(MetaService.class);
		TypeElement contract = context.typeOf(anno::value);
		if(contract == null) {
			contract = inferContract(service);
		}
		final MetaInfContract meta = context.getContract(contract);
		meta.watch(service);
		meta.add(service);
	}

	private static TypeElement inferContract(final TypeElement service) {
		final TypeElement superclass = typeOf(service.getSuperclass());
		if(areEqual(superclass, Object.class)) {
			if(service.getInterfaces().size() != 1) {
				throw new IllegalArgumentException("Needed exactly one interface on service to infer contract");
			}
			return typeOf(service.getInterfaces().get(0));
		}
		if(!service.getInterfaces().isEmpty()) {
			throw new IllegalArgumentException("Could not choose between superclass and interface for contract");
		}
		return superclass;
	}
}
