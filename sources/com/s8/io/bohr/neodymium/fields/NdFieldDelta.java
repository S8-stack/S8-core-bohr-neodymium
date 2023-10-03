package com.s8.io.bohr.neodymium.fields;

import com.s8.api.bytes.MemoryFootprint;
import com.s8.api.objects.repo.RepoS8Object;
import com.s8.io.bohr.neodymium.exceptions.NdIOException;
import com.s8.io.bohr.neodymium.type.BuildScope;


/**
 * <p><code>NdFieldDelta</code> are immutable!</p>
 *
 * @author Pierre Convert
 * Copyright (C) 2022, Pierre Convert. All rights reserved.
 * 
 */
public abstract class NdFieldDelta {
		
	
	public NdFieldDelta() {
		super();
	}
	
	/**
	 * 
	 * @param object
	 * @throws LthSerialException 
	 */
	public abstract void consume(RepoS8Object object, BuildScope scope) throws NdIOException;
	
	public abstract void computeFootprint(MemoryFootprint weight);
	
	
	public abstract NdField getField();
}
