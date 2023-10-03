package com.s8.io.bohr.neodymium.object;

import java.io.IOException;
import java.util.List;

import com.s8.api.bohr.BOHR_Keywords;
import com.s8.api.bytes.ByteOutflow;
import com.s8.api.bytes.MemoryFootprint;
import com.s8.api.exceptions.S8IOException;
import com.s8.api.objects.repo.RepoS8Object;
import com.s8.io.bohr.neodymium.branch.NdGraph;
import com.s8.io.bohr.neodymium.branch.endpoint.NdOutbound;
import com.s8.io.bohr.neodymium.fields.NdFieldDelta;
import com.s8.io.bohr.neodymium.type.BuildScope;
import com.s8.io.bohr.neodymium.type.NdType;
import com.s8.io.bohr.neodymium.type.NdTypeComposer;


/**
 * 
 * 
 * @author Pierre Convert
 * Copyright (C) 2022, Pierre Convert. All rights reserved.
 * 
 */
public class UpdateNdObjectDelta extends NdObjectDelta {

	
	public final NdType type;

	public final List<NdFieldDelta> deltas;
	
	
	/**
	 * 
	 * @param index
	 * @param type
	 * @param deltas
	 */
	public UpdateNdObjectDelta(String index, NdType type, List<NdFieldDelta> deltas) {
		super(index);
		

		// type
		this.type = type;
		
		// deltas
		this.deltas = deltas;
		
	}


	@Override
	public void serialize(NdOutbound outbound, ByteOutflow outflow) throws IOException {

		
		NdTypeComposer composer = outbound.getComposer(type.getRuntimeName());
		
		/*  advertise diff type: publish a create node */

		/* pass flag */
		outflow.putUInt8(BOHR_Keywords.UPDATE_NODE);

		/* pass index */
		outflow.putStringUTF8(index);

		// produce all diffs
		for(NdFieldDelta delta : deltas) {
			int ordinal = delta.getField().ordinal;
			composer.fieldComposers[ordinal].publish(delta, outflow);
		}

		/* Close node */
		outflow.putUInt8(BOHR_Keywords.CLOSE_NODE);
	}



	@Override
	public void consume(NdGraph graph, BuildScope scope) {

		try {
			// retrieve vertex
			NdVertex vertex = graph.vertices.get(index);

			if(vertex==null) {
				throw new S8IOException("failed to retrieve vertex for index: "+index);
			}

			// retrieve object
			RepoS8Object object = vertex.object;
			
			// retrieve type
			NdType type = vertex.type;

			// consume diff
			type.consumeDiff(object, deltas, scope);	

		}
		catch (Exception e) {
			e.printStackTrace();
		}	
	}


	/*
	public static UpdateNdObjectDelta deserialize(LithCodebaseIO codebaseIO, ByteInflow inflow) {
		UpdateNdObjectDelta objectDelta = new UpdateNdObjectDelta();
		objectDelta.deserializeBody(codebaseIO, inflow);
		return objectDelta;
	}
	 */


	@Override
	public void computeFootprint(MemoryFootprint weight) {

		weight.reportInstance();

		// fields
		if(deltas!=null) {
			for(NdFieldDelta delta : deltas) {
				delta.computeFootprint(weight);
			}
		}
	}
}
