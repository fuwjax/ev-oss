package org.echovantage.gild.transform;

import java.nio.charset.Charset;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class SortTransform extends LinesTransform {
	private final Comparator<String> comparator;

	public SortTransform(final Charset charset) {
		this(charset, Comparator.naturalOrder());
	}

	public SortTransform(final Charset charset, final Comparator<String> comparator) {
		super(charset);
		this.comparator = comparator;
	}

	@Override
	protected List<String> transform(final List<String> lines) {
		Collections.sort(lines, comparator);
		return lines;
	}
}
