package org.fuwjax.oss.console;

import static org.fuwjax.oss.util.pipe.StandardPipes.DEVNULL;
import static org.fuwjax.oss.util.pipe.StandardPipes.STDERR;
import static org.fuwjax.oss.util.pipe.StandardPipes.STDIN;
import static org.fuwjax.oss.util.pipe.StandardPipes.STDOUT;
import static org.fuwjax.oss.util.pipe.StandardPipes.captureStdErr;
import static org.fuwjax.oss.util.pipe.StandardPipes.captureStdIn;
import static org.fuwjax.oss.util.pipe.StandardPipes.captureStdOut;
import static org.fuwjax.oss.util.pipe.StandardPipes.pipe;
import static org.fuwjax.oss.util.pipe.StandardPipes.sink;
import static org.fuwjax.oss.util.pipe.StandardPipes.source;
import static org.fuwjax.oss.util.pipe.StandardPipes.transform;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.fuwjax.oss.util.CompositeException;
import org.fuwjax.oss.util.pipe.Pipe;
import org.fuwjax.oss.util.pipe.PipeReader;
import org.junit.rules.MethodRule;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;

public class TestConsole implements MethodRule {
	private static final String inputPrefix = "> ";

	@Override
	public Statement apply(Statement statement, FrameworkMethod method, Object target) {
		ConsoleLog log = method.getAnnotation(ConsoleLog.class);
		Path test = Paths.get(log.resource(), log.value());
		Path temp = Paths.get(log.target(), log.value());
		return new Statement() {
			@Override
			public void evaluate() throws Throwable {
				if (Files.exists(test)) {
					test(test, temp, log.echo(), statement);
				} else {
					record(test, statement);
				}
			}
		};
	}

	private void test(Path test, Path target, boolean echo, Statement statement) throws Throwable {
		Files.createDirectories(target.getParent());
		CompositeException error = new CompositeException("Failed running script " + test);
		try (Pipe log = pipe().to(sink(target));
				Pipe console = pipe().teeTo(captureStdIn(), transform(l -> inputPrefix + l, log));
				PipeReader monitor = new PipeReader();
				Pipe capture = pipe().teeTo(log, monitor).mergeFrom(captureStdOut(), captureStdErr());
				Pipe feed = pipe().from(source(test)).teeTo(line -> assertOutput(line, monitor, console, error),
						echo ? STDOUT : DEVNULL)) {
			statement.evaluate();
		}
		error.throwIf(AssertionError.class).throwIfCaused();
	}

	public void assertOutput(String line, PipeReader monitor, Pipe console, CompositeException error) {
		if (line.startsWith(inputPrefix)) {
			console.writeLine(line.substring(2));
		} else {
			String actual = monitor.readLine();
			if (line.startsWith("/ ")) {
				error.attempt(() -> assertTrue(actual.matches(line.substring(2))));
			}
			error.attempt(() -> assertEquals(line, actual));
		}
	}

	private static void record(Path test, Statement statement) throws Throwable {
		Files.createDirectories(test.getParent());
		try (Pipe log = pipe().to(sink(test));
				Pipe stdin = pipe().teeTo(transform(l -> inputPrefix + l, log), captureStdIn()).from(STDIN);
				Pipe stdout = pipe().teeTo(log, STDOUT).from(captureStdOut());
				Pipe stderr = pipe().teeTo(log, STDERR).from(captureStdErr())) {
			statement.evaluate();
		}
	}
}
