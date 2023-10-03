package com.s8.io.bohr.neodymium.branch.endpoint;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.s8.api.bohr.BOHR_Keywords;
import com.s8.api.bytes.ByteOutflow;
import com.s8.io.bohr.neodymium.branch.NdGraphDelta;
import com.s8.io.bohr.neodymium.codebase.NdCodebase;
import com.s8.io.bohr.neodymium.exceptions.NdBuildException;
import com.s8.io.bohr.neodymium.exceptions.NdIOException;
import com.s8.io.bohr.neodymium.type.NdType;
import com.s8.io.bohr.neodymium.type.NdTypeComposer;

/**
 * 
 *
 * @author Pierre Convert
 * Copyright (C) 2022, Pierre Convert. All rights reserved.
 * 
 */
public class NdOutbound {


	private final NdCodebase codebase;

	private long typeCode = 0;

	/**
	 * 
	 */
	private final Map<String, NdTypeComposer> composers;


	public NdOutbound(NdCodebase codebase) {
		super();
		this.codebase = codebase;
		this.composers = new HashMap<>();
	}



	/**
	 * 
	 * @param type
	 * @return
	 * @throws NdIOException
	 */
	public NdTypeComposer getComposer(Class<?> type) throws NdIOException {
		String runtimeName = type.getName();
		return getComposer(runtimeName);
	}

	/**
	 * 
	 * @param type
	 * @return
	 * @throws NdIOException
	 */
	public NdTypeComposer getComposer(String runtimeTypeName) throws NdIOException {
		NdTypeComposer composer = composers.computeIfAbsent(runtimeTypeName, name -> {
			NdType nType = codebase.getTypeByRuntimeName(name);
			try {
				return new NdTypeComposer(nType, ++typeCode);
			} 
			catch (NdBuildException e) {
				e.printStackTrace();
				return null;
			}
		});
		if(composer != null) { return composer; }
		else {
			throw new NdIOException("failed to build composer");
		}
	}



	/**
	 * 
	 * @param outflow
	 * @param deltas
	 * @throws IOException
	 */
	private void composeSequence(ByteOutflow outflow, NdGraphDelta[] deltas) throws IOException {
		outflow.putUInt8(BOHR_Keywords.OPEN_SEQUENCE);
		for(NdGraphDelta delta : deltas){
			delta.serialize(this, outflow);
		}
		outflow.putUInt8(BOHR_Keywords.CLOSE_SEQUENCE);
	}
	
	

	/**
	 * 
	 * @param outflow
	 * @throws IOException
	 */
	public void pushFrame(ByteOutflow outflow, NdGraphDelta[] deltas) throws IOException {
		outflow.putByteArray(BOHR_Keywords.FRAME_HEADER);
		composeSequence(outflow, deltas);
		outflow.putByteArray(BOHR_Keywords.FRAME_FOOTER);
	}

}
