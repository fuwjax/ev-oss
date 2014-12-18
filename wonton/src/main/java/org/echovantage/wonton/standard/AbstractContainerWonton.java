package org.echovantage.wonton.standard;

import org.echovantage.wonton.Wonton;

abstract class AbstractContainerWonton extends AbstractWonton {
	@Override
	public final void accept(final Visitor visitor) {
		assert visitor != null;
		acceptShallow(new ShallowVisitor(visitor));
	}

	protected static final class ShallowVisitor {
		private final Visitor visitor;

		protected ShallowVisitor(final Visitor visitor) {
			this.visitor = visitor;
		}

		protected void visit(final String key, final Wonton value) {
			final Path root = new StandardPath(key);
			visitor.visit(root, value);
			value.accept((path, wonton) -> visitor.visit(root.append(path), wonton));
		}
	}

	protected abstract void acceptShallow(ShallowVisitor visitor);
}
