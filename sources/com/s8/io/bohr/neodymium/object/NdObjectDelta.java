package com.s8.io.bohr.neodymium.object;

import java.io.IOException;

import com.s8.api.bytes.ByteOutflow;
import com.s8.api.bytes.MemoryFootprint;
import com.s8.io.bohr.neodymium.branch.NdGraph;
import com.s8.io.bohr.neodymium.branch.endpoint.NdOutbound;
import com.s8.io.bohr.neodymium.exceptions.NdIOException;
import com.s8.io.bohr.neodymium.type.BuildScope;


/**
 * 
 * 
 * @author Pierre Convert
 * Copyright (C) 2022, Pierre Convert. All rights reserved.
 * 
 */
public abstract class NdObjectDelta {

	public final String index;



	public NdObjectDelta(String index) {
		super();
		this.index = index;
	}


	/**
	 * 
	 * @param shell
	 * @return
	 * @throws NdIOException 
	 * @throws IOException 
	 */
	public abstract void consume(NdGraph graph, BuildScope scope) throws NdIOException;

	
	/**
	 * 
	 * @param outbound
	 * @param outflow
	 * @throws IOException
	 */
	public abstract void serialize(NdOutbound outbound, ByteOutflow outflow) throws IOException;

	
	/**
	 * 
	 * @param weight
	 */
	public abstract void computeFootprint(MemoryFootprint weight);
}
