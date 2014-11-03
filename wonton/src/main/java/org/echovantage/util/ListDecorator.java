package org.echovantage.util;

import java.util.AbstractList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.function.Function;

public class ListDecorator<E, T> extends AbstractList<T> {
	public static <E, T> ListDecorator<E, T> listOf(final List<? extends E> list, final Function<? super T, ? extends E> decoder, final Function<? super E, ? extends T> encoder) {
		return new ListDecorator<>(list, decoder, encoder);
	}

	private final class DecoratedListIterator implements ListIterator<T> {
		private final ListIterator<? extends E> iter;

		public DecoratedListIterator(final ListIterator<? extends E> iter) {
			this.iter = iter;
		}

		@Override
		public boolean hasNext() {
			return iter.hasNext();
		}

		@Override
		public T next() {
			return encode(iter.next());
		}

		@Override
		public boolean hasPrevious() {
			return iter.hasPrevious();
		}

		@Override
		public T previous() {
			return encode(iter.previous());
		}

		@Override
		public int nextIndex() {
			return iter.nextIndex();
		}

		@Override
		public int previousIndex() {
			return iter.previousIndex();
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}

		@Override
		public void set(final T e) {
			throw new UnsupportedOperationException();
		}

		@Override
		public void add(final T e) {
			throw new UnsupportedOperationException();
		}
	}

	private final List<? extends E> list;
	private final Function<? super T, ? extends E> decoder;
	private final Function<? super E, ? extends T> encoder;

	public ListDecorator(final List<? extends E> list, final Function<? super T, ? extends E> decoder, final Function<? super E, ? extends T> encoder) {
		this.list = list;
		this.decoder = decoder;
		this.encoder = encoder;
	}

	protected E decode(final T value) {
		return decoder.apply(value);
	}

	protected T encode(final E value) {
		return encoder.apply(value);
	}

	@Override
	public int size() {
		return list.size();
	}

	@Override
	public boolean isEmpty() {
		return list.isEmpty();
	}

	@Override
	public boolean contains(final Object o) {
		return list.contains(decode((T)o));
	}

	@Override
	public Iterator<T> iterator() {
		final Iterator<? extends E> iter = list.iterator();
		return new Iterator<T>() {
			@Override
			public boolean hasNext() {
				return iter.hasNext();
			}

			@Override
			public T next() {
				return encode(iter.next());
			}
		};
	}

	@Override
	public void clear() {
		list.clear();
	}

	@Override
	public T get(final int index) {
		return encode(list.get(index));
	}

	@Override
	public int indexOf(final Object o) {
		return list.indexOf(decode((T)o));
	}

	@Override
	public int lastIndexOf(final Object o) {
		return list.lastIndexOf(decode((T)o));
	}

	@Override
	public ListIterator<T> listIterator() {
		return new DecoratedListIterator(list.listIterator());
	}

	@Override
	public ListIterator<T> listIterator(final int index) {
		return new DecoratedListIterator(list.listIterator(index));
	}

	@Override
	public List<T> subList(final int fromIndex, final int toIndex) {
		return new ListDecorator<>(list.subList(fromIndex, toIndex), decoder, encoder);
	}
}
