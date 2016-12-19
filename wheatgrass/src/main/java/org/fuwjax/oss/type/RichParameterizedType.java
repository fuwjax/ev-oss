package org.fuwjax.oss.type;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Objects;

public class RichParameterizedType implements ParameterizedType{
	private Type owner;
	private Type raw;
	private Type[] args;

	public RichParameterizedType(Type owner, Type raw, Type... actualTypeArguments) {
		this.owner = owner;
		this.raw = raw;
		this.args = actualTypeArguments;
	}

	@Override
	public Type[] getActualTypeArguments() {
		return args;
	}

	@Override
	public Type getRawType() {
		return raw;
	}

	@Override
	public Type getOwnerType() {
		return owner;
	}

    @Override
	public boolean equals(Object obj) {
        if(obj instanceof ParameterizedType) {
            ParameterizedType o = (ParameterizedType)obj;
            return Objects.equals(owner, o.getOwnerType()) && Objects.equals(raw, o.getRawType()) && Arrays.equals(args, o.getActualTypeArguments());
        }
        return false;
    }

    @Override
	public int hashCode() {
        return Arrays.hashCode(args) ^ Objects.hashCode(owner) ^ Objects.hashCode(raw);
    }
}
