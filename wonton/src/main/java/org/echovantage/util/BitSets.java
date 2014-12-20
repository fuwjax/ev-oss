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
}
