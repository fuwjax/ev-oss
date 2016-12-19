package org.fuwjax.oss.type;

import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;
import java.util.Arrays;

public class RichWildcardType implements WildcardType{
	private static final Type[] NO_BOUNDS = new Type[0];
	private static final Type[] DEFAULT_UPPER = new Type[]{Object.class};
	private static final RichWildcardType WILDCARD = new RichWildcardType(NO_BOUNDS, NO_BOUNDS);
	private Type[] upper;
	private Type[] lower;

	public static RichWildcardType wildcardOf(Type exact){
		Type[] bound = new Type[]{exact};
		return new RichWildcardType(bound, bound);
	}
	
	public static RichWildcardType upperOf(Type... upper){
		return new RichWildcardType(upper, NO_BOUNDS);
	}
	
	public static RichWildcardType lowerOf(Type... lower){
		return new RichWildcardType(NO_BOUNDS, lower);
	}
	
	public static RichWildcardType wildcard(){
		return WILDCARD;
	}
	
	private RichWildcardType(Type[] upper, Type[] lower) {
		this.upper = upper.length > 0 ? upper : DEFAULT_UPPER;
		this.lower = lower;
	}
	
    @Override
    public Type[] getUpperBounds() {
        return upper;
    }

    @Override
    public Type[] getLowerBounds() {
        return lower;
    }

    @Override
	public boolean equals(Object obj) {
        if(obj instanceof WildcardType) {
            WildcardType o = (WildcardType)obj;
            return Arrays.equals(upper, o.getUpperBounds()) && Arrays.equals(lower, o.getLowerBounds());
        }
        return false;
    }

    @Override
	public int hashCode() {
        return Arrays.hashCode(lower) ^ Arrays.hashCode(upper);
    }
}
