/*
 * Copyright (C) 2015 fuwjax.org (info@fuwjax.org)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.fuwjax.oss.gild.transform;

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
