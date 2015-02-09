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
