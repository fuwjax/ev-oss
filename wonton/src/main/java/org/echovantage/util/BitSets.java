/*******************************************************************************
 * Copyright (c) 2011 Michael Doberenz. All rights reserved. This program and
 * the accompanying materials are made available under the terms of the Eclipse
 * Public License v1.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html Contributors: Michael Doberenz -
 * initial API and implementation
 ******************************************************************************/
package org.echovantage.util;

import java.util.BitSet;

public class BitSets {
	public static BitSet bitSetOf(int... ints){
		BitSet bits = new BitSet();
		for(int i: ints){
			bits.set(i);
		}
		return bits;
	}

    public static void main(String... args){
        BitSet part = new BitSet();
        for(int i=Character.MIN_CODE_POINT; i<= Character.MAX_CODE_POINT; i++){
            if(!Character.isJavaIdentifierStart(i) && Character.isJavaIdentifierPart(i) && !Character.isIdentifierIgnorable(i)){
                part.set(i);
            }
        }
        int p = part.nextSetBit(0);
        while(p > -1){
            int p1 = part.nextClearBit(p);
            if(p +1 == p1){
                System.out.println(String.format("\"\\u%04x\",",p));
            }else{
                System.out.println(String.format("\"\\u%04x\"-\"\\u%04x\",",p, p1-1));
            }
            p = part.nextSetBit(p1);
        }
    }
}
