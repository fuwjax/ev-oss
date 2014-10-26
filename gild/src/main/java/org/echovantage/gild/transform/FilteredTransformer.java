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
