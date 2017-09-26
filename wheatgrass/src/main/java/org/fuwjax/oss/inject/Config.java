package org.fuwjax.oss.inject;

import java.nio.file.Path;
import java.nio.file.Paths;

public class Config implements ObjectFactory {
	private Path path;

	public Config(String path) {
		this.path = Paths.get(path);
	}

	@Override
	public Object get(BindConstraint constraint) throws ReflectiveOperationException {
		return null;
	}

	@Override
	public void inject(BindConstraint constraint, Object target) throws ReflectiveOperationException {
		// do nothing
	}
}
