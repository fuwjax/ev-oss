/*
 * Copyright (C) 2015 EchoVantage (info@echovantage.com)
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
package org.echovantage.gild.transform;

import java.io.IOException;
import java.nio.file.DirectoryStream.Filter;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.echovantage.util.ReadOnlyPath;

public class FilteredTransformer implements Transformer {
	private static class FilteredTransform extends StreamTransformer {
		private final Filter<ReadOnlyPath> filter;

		public FilteredTransform(final Filter<ReadOnlyPath> filter, final StreamTransform transform) {
			super(transform);
			this.filter = filter;
		}

		public boolean transformed(final ReadOnlyPath src, final Path dest) throws IOException {
			if(!filter.accept(src)) {
				return false;
			}
			transform(src, dest);
			return true;
		}
	}

	private final List<FilteredTransform> transforms = new ArrayList<>();

	public FilteredTransformer with(final Filter<ReadOnlyPath> filter, final StreamTransform transform) {
		transforms.add(new FilteredTransform(filter, transform));
		return this;
	}

	@Override
	public void transform(final ReadOnlyPath src, final Path dest) throws IOException {
		for(final FilteredTransform xform : transforms) {
			if(xform.transformed(src, dest)) {
				return;
			}
		}
		src.copyTo(dest);
	}
}
