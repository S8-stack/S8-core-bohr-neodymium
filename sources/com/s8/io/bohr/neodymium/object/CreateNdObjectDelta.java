package com.s8.io.bohr.neodymium.object;

import java.io.IOException;
import java.util.List;

import com.s8.api.bohr.BOHR_Keywords;
import com.s8.api.bytes.ByteOutflow;
import com.s8.api.bytes.MemoryFootprint;
import com.s8.api.objects.repo.RepoS8Object;
import com.s8.io.bohr.neodymium.branch.NdGraph;
import com.s8.io.bohr.neodymium.branch.endpoint.NdOutbound;
import com.s8.io.bohr.neodymium.exceptions.NdIOException;
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
public class CreateNdObjectDelta extends NdObjectDelta {

	public List<NdFieldDelta> deltas;


	public final NdType type;

	

	public CreateNdObjectDelta(String index, NdType type, List<NdFieldDelta> deltas) {
		super(index);

		this.type = type;
		
		// deltas
		this.deltas = deltas;

	}

	@Override
	public void serialize(NdOutbound outbound, ByteOutflow outflow) throws IOException {

		NdTypeComposer composer = outbound.getComposer(type.getRuntimeName());
		
		/*  advertise diff type: publish a create node */
		composer.publish_CREATE_NODE(outflow, index);

		/* serialize field deltas */
		// produce all diffs
		for(NdFieldDelta delta : deltas) {
			int ordinal = delta.getField().ordinal;
			composer.fieldComposers[ordinal].publish(delta, outflow);
		}

		/* Close node */
		outflow.putUInt8(BOHR_Keywords.CLOSE_NODE);
	}



	@Override
	public void consume(NdGraph graph, BuildScope scope) throws NdIOException {

		// create object
		RepoS8Object object = type.createNewInstance();

		/* clear spin */
		object.S8_spin = false;

		/* assign object id */
		object.S8_id = index;

		/* consume diff */
		type.consumeDiff(object, deltas, scope);


		/* retrieve vertex */
		NdVertex vertex = graph.vertices.get(index);
		if(vertex == null) { 
			/* this case happens when object creation does not come from I/O */
			vertex = new NdVertex(type);
			graph.vertices.put(index, vertex);
		}

		vertex.object = object;
	}


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

