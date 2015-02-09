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
package org.echovantage.util;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

/**
 * File utility class for test-centric operations.
 *
 * @author fuwjax
 */
public class Files2 {
	/**
	 * Copies a source to a destination, works recursively on directories.
	 *
	 * @param source
	 *           the source path
	 * @param dest
	 *           the destination path
	 * @throws IOException
	 *            if the source cannot be copied to the destination
	 */
	public static void copy(final Path source, final Path dest) throws IOException {
		if(Files.exists(source)) {
			Files.walkFileTree(source, new SimpleFileVisitor<Path>() {
				@Override
				public FileVisitResult visitFile(final Path file, final BasicFileAttributes attrs) throws IOException {
					Files.copy(file, dest.resolve(source.relativize(file)));
					return super.visitFile(file, attrs);
				}

				@Override
				public FileVisitResult preVisitDirectory(final Path dir, final BasicFileAttributes attrs) throws IOException {
					Files.createDirectories(dest.resolve(source.relativize(dir)));
					return super.preVisitDirectory(dir, attrs);
				}
			});
		}
	}

	/**
	 * Deletes a path, including directories.
	 *
	 * @param target
	 *           the path to delete
	 * @throws IOException
	 *            if the path cannot be deleted
	 */
	public static void delete(final Path target) throws IOException {
		if(target != null && Files.exists(target)) {
			Files.walkFileTree(target, new SimpleFileVisitor<Path>() {
				@Override
				public FileVisitResult visitFile(final Path file, final BasicFileAttributes attrs) throws IOException {
					Files.delete(file);
					return super.visitFile(file, attrs);
				}

				@Override
				public FileVisitResult postVisitDirectory(final Path dir, final IOException exc) throws IOException {
					Files.delete(dir);
					return super.postVisitDirectory(dir, exc);
				}
			});
		}
	}
}
