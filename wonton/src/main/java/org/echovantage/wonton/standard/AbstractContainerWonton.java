package org.echovantage.wonton.standard;

import org.echovantage.wonton.Wonton;

abstract class AbstractContainerWonton extends AbstractWonton {
	@Override
	public final Wonton get(final Path path) {
		assert path != null;
		final Wonton elm = get(path.key());
		if(elm == null) {
			throw new NoSuchPathException(path);
		}
		return path.tail().isEmpty() ? elm : elm.get(path.tail());
	}

	protected abstract Wonton get(String key);

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

	protected Mutable set(final Path path, final Wonton value) {
		assert path != null && !path.isEmpty();
		assert value != null;
		if(path.tail().isEmpty()) {
			return set(path.key(), value);
		}
		return mutable(path).set(path.tail(), value);
	}

	private Mutable mutable(final Path path) {
		Wonton child = get(path.key());
		if(child == null) {
			if(path.tail().isEmpty() || path.tail().key().equals("0")) {
				child = new ListWonton();
			} else {
				child = new MapWonton();
			}
			set(path.key(), child);
		} else if(!(child instanceof Mutable)) {
			throw new IllegalStateException("Cannot set " + path + ", wonton is not mutable");
		}
		return (Mutable)child;
	}

	protected Mutable append(final Path path, final Wonton value) {
		assert path != null;
		assert value != null;
		if(path.isEmpty()) {
			return append(value);
		}
		return mutable(path).append(path.tail(), value);
	}

	protected Mutable append(final Wonton value) {
		throw new UnsupportedOperationException();
	}

	protected Mutable set(final String key, final Wonton value) {
		throw new UnsupportedOperationException();
	}

	protected Wonton build() {
		return this;
	}
}
