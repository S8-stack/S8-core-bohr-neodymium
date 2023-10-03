package com.s8.io.bohr.neodymium.object;

import java.io.IOException;

import com.s8.api.bohr.BOHR_Keywords;
import com.s8.api.bytes.ByteOutflow;
import com.s8.api.bytes.MemoryFootprint;
import com.s8.io.bohr.neodymium.branch.NdGraph;
import com.s8.io.bohr.neodymium.branch.endpoint.NdOutbound;
import com.s8.io.bohr.neodymium.exceptions.NdIOException;
import com.s8.io.bohr.neodymium.type.BuildScope;


/**
 * 
 * 
 * 
 * @author Pierre Convert
 * Copyright (C) 2022, Pierre Convert. All rights reserved.
 * 
 */
public class ExposeNdObjectDelta extends NdObjectDelta {
	
	public final int slot;


	public ExposeNdObjectDelta(String index, int slot) {
		super(index);
		this.slot = slot;
	}

	@Override
	public void serialize(NdOutbound outbound, ByteOutflow outflow) throws IOException {

		outflow.putUInt8(BOHR_Keywords.EXPOSE_NODE);

		/* define index */
		outflow.putStringUTF8(index);

		/* define slot */
		outflow.putUInt8(slot);
	}



	@Override
	public void consume(NdGraph graph, BuildScope scope) throws NdIOException {
		
		/* retrieve vertex */
		NdVertex vertex = graph.vertices.get(index);
		
		graph.expose(slot, vertex.object);
	}
	

	@Override
	public void computeFootprint(MemoryFootprint weight) {

		weight.reportInstance();
	}

}

