package org.fuwjax.oss.type;

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Type;
import java.util.Objects;

public class RichGenericArrayType implements GenericArrayType{
	private Type component;

	public RichGenericArrayType(Type component){
		this.component = component;
	}
	
    public boolean equals(Object obj) {
        if(obj instanceof GenericArrayType) {
        	GenericArrayType o = (GenericArrayType)obj;
            return Objects.equals(component, o.getGenericComponentType());
        }
        return false;
    }

    public int hashCode() {
        return Objects.hashCode(component);
    }

	@Override
	public Type getGenericComponentType() {
		return component;
	}
}
