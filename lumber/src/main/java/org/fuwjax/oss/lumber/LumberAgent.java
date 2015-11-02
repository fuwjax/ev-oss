package org.fuwjax.oss.lumber;

import static java.lang.ClassLoader.getSystemResourceAsStream;
import static java.util.Optional.ofNullable;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.instrument.Instrumentation;
import java.nio.charset.StandardCharsets;
import java.security.ProtectionDomain;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;

public class LumberAgent implements ClassFileTransformer {
	public static final LumberAgent LOGGER = new LumberAgent();
	private static final Predicate<CtMethod> TRUE = m -> true;

	public static void premain(final String agentArgs, final Instrumentation inst) throws Exception {
		LOGGER.configure(agentArgs);
		inst.addTransformer(LOGGER, false);
	}

	public static void agentmain(final String agentArgs, final Instrumentation inst) throws Exception {
		LOGGER.configure(agentArgs);
		inst.addTransformer(LOGGER, true);
		inst.retransformClasses(Arrays.asList(inst.getAllLoadedClasses()).stream().filter(inst::isModifiableClass)
				.filter(LOGGER::isTransformedClass).collect(Collectors.toList()).toArray(new Class<?>[0]));
	}

	private final ClassPool pool = ClassPool.getDefault();
	private final List<LumberFilter> filters = load(LumberFilter.class, () -> new SignatureFilter());
	private final List<LumberAppender> appenders = load(LumberAppender.class, () -> new SysoutAppender());
	private final List<LumberNormalizer> normalizers = load(LumberNormalizer.class, () -> new ToStringConverter());
	private final Json json = new Json(normalizers);

	private LumberAgent() {
		Runtime.getRuntime().addShutdownHook(new Thread(this::close));
	}

	private void configure(final String agentArgs) throws Exception {
		final Map<String, Object> config = loadConfig(agentArgs);
		configure(filters, config);
		configure(appenders, config);
		configure(normalizers, config);
	}

	private void configure(final List<? extends LumberStrategy> list, final Map<String, Object> config) {
		list.forEach(element -> element.safeConfigure(config));
	}

	private static Map<String, Object> loadConfig(final String agentArgs) throws IOException {
		final Json configParser = new Json();
		final String configFile = ofNullable(agentArgs).orElse("lumber.json");
		try (InputStream in = getSystemResourceAsStream(configFile);
				BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
			return (Map<String, Object>) configParser.toObject(reader.lines().collect(Collectors.joining("\n")));
		}
	}

	private static <T> List<T> load(final Class<T> cls, final Supplier<T> defaultMember) {
		final List<T> list = new ArrayList<>();
		final ServiceLoader<T> services = ServiceLoader.load(cls);
		services.forEach(list::add);
		if (list.isEmpty()) {
			list.add(defaultMember.get());
		}
		return list;
	}

	public void close() {
		close(filters);
		close(appenders);
		close(normalizers);
	}

	private void close(final List<?> list) {
		list.stream().filter(f -> f instanceof AutoCloseable).map(f -> (AutoCloseable) f).forEach(this::close);
	}

	private void close(final AutoCloseable closer) {
		try {
			closer.close();
		} catch (final Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public byte[] transform(final ClassLoader loader, final String className, final Class<?> classBeingRedefined,
			final ProtectionDomain protectionDomain, final byte[] classfileBuffer) throws IllegalClassFormatException {
		final Predicate<CtMethod> applicable = filters.stream().filter(filter -> filter.mightTransform(className))
				.map(LumberFilter::transformTest).reduce(TRUE, Predicate::or);
		if (applicable.equals(TRUE)) {
			return classfileBuffer;
		}
		try {
			final CtClass cls = pool.makeClass(new ByteArrayInputStream(classfileBuffer));
			Arrays.asList(cls.getDeclaredMethods()).stream().filter(applicable).forEach(this::addLogging);
			return cls.toBytecode();
		} catch (final Exception e) {
			e.printStackTrace();
			return classfileBuffer;
		}
	}

	private boolean isTransformedClass(final Class<?> cls) {
		return filters.stream().anyMatch(filter -> filter.mightTransform(cls.getName()));
	}

	private static final ThreadLocal<Deque<LogLine>> logLines = new ThreadLocal<Deque<LogLine>>() {
		@Override
		protected Deque<LogLine> initialValue() {
			return new ArrayDeque<>();
		}
	};

	private final class LogLine {
		private long entered;
		private final StringBuilder line;

		public LogLine(final Class<?> cls, final String methodName, final Object... args) {
			line = new StringBuilder("{\"cls\":\"").append(cls.getCanonicalName()).append("\",\"method\":\"")
					.append(methodName).append("\",\"args\":[")
					.append(Arrays.asList(args).stream().map(json::toString).collect(Collectors.joining(",")))
					.append("],\"entered\":").append(System.currentTimeMillis());
		}

		public void stamp() {
			entered = System.nanoTime();
		}

		public CharSequence leave(final long stop, final Object result) {
			final long delta = stop - entered;
			return line.append(",\"delta\":").append(delta).append(",\"result\":").append(json.toString(result))
					.append('}');
		}

		public CharSequence fail(final long stop, final Throwable cause) {
			final long delta = stop - entered;
			return line.append(",\"delta\":").append(delta).append(",\"cause\":").append(json.toString(cause))
					.append('}');
		}

		public CharSequence leave(final long stop) {
			final long delta = stop - entered;
			return line.append(",\"delta\":").append(delta).append('}');
		}
	}

	public void enter(final Class<?> cls, final String methodName, final Object... args) {
		final LogLine l = new LogLine(cls, methodName, args);
		logLines.get().push(l);
		l.stamp();
	}

	public void leave(final Object result) {
		final long stop = System.nanoTime();
		log(logLines.get().pop().leave(stop, result));
	}

	public void leave() {
		final long stop = System.nanoTime();
		log(logLines.get().pop().leave(stop));
	}

	public void fail(final Throwable cause) {
		final long stop = System.nanoTime();
		log(logLines.get().pop().fail(stop, cause));
	}

	private void log(final CharSequence line) {
		appenders.forEach(appender -> appender.safeAppend(line));
	}

	private void addLogging(final CtMethod method) {
		try {
			method.addCatch("{ org.fuwjax.oss.lumber.LumberAgent.LOGGER.fail($e); throw $e; }",
					pool.get(Throwable.class.getCanonicalName()), "$e");
			if (method.getReturnType().equals(CtClass.voidType)) {
				method.insertAfter("org.fuwjax.oss.lumber.LumberAgent.LOGGER.leave();");
			} else {
				method.insertAfter("org.fuwjax.oss.lumber.LumberAgent.LOGGER.leave($_);");
			}
			method.insertBefore(
					"org.fuwjax.oss.lumber.LumberAgent.LOGGER.enter($class, \"" + method.getName() + "\", $args);");
		} catch (final Exception e) {
			throw new RuntimeException(e);
		}
	}
}
