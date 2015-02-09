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
