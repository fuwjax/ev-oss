package org.fuwjax.parser;

import org.fuwjax.parser.impl.Char;

public interface Node {
	StringBuilder match(StringBuilder builder);

	default String match() {
		return match(new StringBuilder()).toString();
	}

	int length();

	Object value();

	Node result();

	static Node codepoint(final int ch) {
		return new Char(ch);
	}
	
	default String nestedString(){
		return String.valueOf(value());
	}
}