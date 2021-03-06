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

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;

import org.fuwjax.oss.util.io.ReadOnlyPath;

public class RecursiveTransformer implements Transformer {
	private final Transformer transformer;

	public RecursiveTransformer(final Transformer transformer) {
		this.transformer = transformer;
	}

	@Override
	public void transform(final ReadOnlyPath src, final Path dest) throws IOException {
		if(src.isDirectory()) {
			Files.createDirectories(dest);
			try(DirectoryStream<ReadOnlyPath> dir = src.newDirectoryStream()) {
				for(final ReadOnlyPath sub : dir) {
					transform(sub, dest.resolve(sub.getFileName()));
				}
			}
		} else if(src.exists()) {
			transformer.transform(src, dest);
		}
	}
}
