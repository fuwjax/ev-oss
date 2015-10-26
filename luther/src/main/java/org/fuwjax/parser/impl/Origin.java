package org.fuwjax.parser.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class Origin {
	private final int index;
	private Transition mark;
	private final List<Transition> awaiting = new ArrayList<>();

	public Origin(final int index) {
		this.index = index;
	}

	/*
	 * This is technically not sufficient to determine equality for the origin,
	 * as there is one origin per symbol per index. This definition of equals is
	 * for the benefit of Transition which is already per symbol.
	 */
	@Override
	public boolean equals(final Object obj) {
		try {
			final Origin o = (Origin) obj;
			return getClass().equals(o.getClass()) && index == o.index;
		} catch (final Exception e) {
			return false;
		}
	}

	@Override
	public String toString() {
		return Integer.toString(index);
	}

	@Override
	public int hashCode() {
		return index;
	}

	public void setMark(final Transition item) {
		if (awaiting.size() == 1) {
			this.mark = item.mark();
		}
	}

	public void addAwaiting(final Transition item) {
		awaiting.add(item);
	}

	public void complete(final Consumer<Transition> onComplete) {
		if (mark != null) {
			onComplete.accept(mark);
		} else {
			awaiting.forEach(onComplete);
		}
	}

	public Transition markOf(final Transition transition) {
		return mark == null ? transition : mark.markOf(transition);
	}

	public void triggerTransform() {
		if(mark != null){
			mark.transformChildren();
		}else if(awaiting.size() == 1){
			awaiting.get(0).transformChildren();
		}
	}
}
