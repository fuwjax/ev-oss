package org.fuwjax.parser.builder;

import java.util.BitSet;
import java.util.function.IntPredicate;

/**
 * Created by fuwjax on 1/12/15.
 */
public class Codepoints {
	private static final BitSet NOTHING = new BitSet();
	private final BitSet bits;
	private final boolean negate;
	private final String rep;

	public Codepoints() {
		this(NOTHING, false, "");
	}

	private Codepoints(final BitSet bits, final boolean negate, final String rep) {
		this.bits = bits;
		this.negate = negate;
		this.rep = rep;
	}

	public Codepoints add(final int... codepoints) {
		final BitSet set = (BitSet) bits.clone();
		final StringBuilder builder = new StringBuilder(rep);
		for (final int codepoint : codepoints) {
			set.set(codepoint, true);
			cp(builder, codepoint);
		}
		return new Codepoints(set, negate, builder.toString());
	}

	private static StringBuilder cp(final StringBuilder builder, final int codepoint) {
		switch (codepoint) {
		case '\n':
			builder.append("\\n");
			break;
		case '\r':
			builder.append("\\r");
			break;
		case '\t':
			builder.append("\\t");
			break;
		default:
			builder.appendCodePoint(codepoint);
		}
		return builder;
	}
	
	public boolean get(int ch){
		return bits.get(ch) ^ negate;
	}
	
	public Codepoints negate() {
		return new Codepoints(bits, !negate, rep);
	}

	public Codepoints range(final int start, final int end) {
		final BitSet set = (BitSet) bits.clone();
		set.set(start, end + 1, true);
		return new Codepoints(set, negate, cp(cp(new StringBuilder(rep), start).append('-'), end).toString());
	}

	@Override
	public int hashCode() {
		return negate ? ~bits.hashCode() : bits.hashCode();
	}

	@Override
	public boolean equals(final Object obj) {
		try {
			final Codepoints o = (Codepoints) obj;
			return getClass().equals(o.getClass()) && bits.equals(o.bits) && negate == o.negate;
		} catch (final Exception e) {
			return false;
		}
	}
	
	@Override
	public String toString() {
		return "[" + (negate ? "^" : "") + rep + "]";
	}

	public IntPredicate toPredicate() {
		return this::get;
	}
}
