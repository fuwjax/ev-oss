package org.fuwjax.oss.util;


import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

import org.fuwjax.oss.util.RunWrapException;

public interface Log {
	interface Level{
		String name();
	}
	enum StandardLevel implements Level{
		ERROR,
		WARN,
		INFO
	}

	Log SYSTEM = System.out::println;
	
	class Tee implements Log{
		private List<Log> logs;

		public Tee(Log... logs){
			this.logs = Arrays.asList(logs);
		}
		
		@Override
		public void log(String message) {
			logs.forEach(log -> log.log(message));
		}
	}
	
	static String stackTrace(Throwable x){
		StringWriter out = new StringWriter();
		x.printStackTrace(new PrintWriter(out));
		return out.toString();
	}
	
	static Level level(String name){
		return () -> name;
	}

	default void error(Throwable cause, String pattern, Object... args){
		log(StandardLevel.ERROR, cause, pattern, args);
	}
	
	default void error(String pattern, Object... args){
		log(StandardLevel.ERROR, pattern, args);
	}
	
	default void warning(Throwable cause, String pattern, Object... args){
		log(StandardLevel.WARN, cause, pattern, args);
	}
	
	default void warning(String pattern, Object... args){
		log(StandardLevel.WARN, pattern, args);
	}
	
	default void info(String pattern, Object... args){
		log(StandardLevel.INFO, pattern, args);
	}
	
	default void debug(String pattern, Object... args){
		// ignore
	}
	
	default void log(Level level, Throwable cause, String pattern, Object... args){
		log(message(level, cause, pattern, args));
	}

	default void log(Level level, String pattern, Object... args){
		log(message(level, pattern, args));
	}
	
	default void log(String pattern, Object... args){
		log(message(pattern, args));
	}
	
	void log(String message);

	default String message(String pattern, Object...args){
		return String.format(pattern, args);
	}

	default String message(Level level,String pattern,Object... args){
		return String.format("%s: %s: %s", time(LocalDateTime.now()), level.name(), message(pattern, args));
	}
	
	default String message(Level level,Throwable cause,String pattern,Object... args){
		if(cause == null){
			return message(level, pattern, args);
		}
		return String.format("%s: %s: %s:: %s: %s\n%s", time(LocalDateTime.now()), level.name(), message(pattern, args), cause.getClass().getCanonicalName(), cause.getLocalizedMessage(), stackTrace(cause));
	}
	
	default String time(LocalDateTime time){
		return String.format("%tF %<tT.%<tL", time);
	}
	
	static PrintStream toPrintStream(Log log){
		try{
			return new PrintStream(new OutputStream(){
				@Override
				public void write(int b) throws IOException {
					write(new byte[]{(byte)b});
				}
				
				@Override
				public void write(byte[] b) throws IOException {
					write(b, 0, b.length);
				}
				
				@Override
				public void write(byte[] b, int off, int len) throws IOException {
					log.log(new String(b, off, len, StandardCharsets.UTF_8));
				}
			}, true, "UTF-8");
		}catch(UnsupportedEncodingException e){
			throw new RunWrapException(e, "Impossible: UTF-8 required by Java");
		}
	}

	static Object defer(Supplier<?> toString){
		return new Object(){
			@Override
			public String toString() {
				return String.valueOf(toString.get());
			}
		};
	}
}
