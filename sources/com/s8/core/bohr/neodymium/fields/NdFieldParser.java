package com.s8.core.bohr.neodymium.fields;

import java.io.IOException;

import com.s8.api.bytes.ByteInflow;
import com.s8.core.bohr.atom.protocol.BOHR_Properties;


/**
 * 
 *
 * @author Pierre Convert
 * Copyright (C) 2022, Pierre Convert. All rights reserved.
 * 
 */
public abstract class NdFieldParser {
	
	
	public abstract NdField getField();
	
	
	/**
	 * 
	 * @param inflow
	 * @return
	 * @throws IOException 
	 */
	public abstract NdFieldDelta deserializeDelta(ByteInflow inflow) throws IOException;
	

	
	public static boolean isNonNull(int props) {
		return (props & BOHR_Properties.IS_NON_NULL_PROPERTIES_BIT) == BOHR_Properties.IS_NON_NULL_PROPERTIES_BIT;
	}
}
