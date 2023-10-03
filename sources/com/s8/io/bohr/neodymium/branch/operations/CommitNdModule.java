package com.s8.io.bohr.neodymium.branch.operations;

import java.io.IOException;
import java.util.List;

import com.s8.api.objects.repo.RepoS8Object;
import com.s8.io.bohr.atom.S8ShellStructureException;
import com.s8.io.bohr.neodymium.branch.NdGraphDelta;
import com.s8.io.bohr.neodymium.branch.NdGraph;
import com.s8.io.bohr.neodymium.exceptions.NdIOException;
import com.s8.io.bohr.neodymium.object.ExposeNdObjectDelta;
import com.s8.io.bohr.neodymium.object.NdObjectDelta;
import com.s8.io.bohr.neodymium.object.NdVertex;

/**
 * 
 *
 * @author Pierre Convert
 * Copyright (C) 2022, Pierre Convert. All rights reserved.
 * 
 */
public class CommitNdModule {

	/**
	 * Update
	 * 
	 * @throws IOException 
	 * @throws S8ShellStructureException 
	 * 
	 */
	public static NdGraphDelta generateDelta(NdGraph origin, NdGraph target) throws IOException, S8ShellStructureException {


		/* create new delta */
		NdGraphDelta delta = new NdGraphDelta(origin.version + 1);

		/* extract object deltas */
		List<NdObjectDelta> objectDeltas = delta.objectDeltas;

		/* <vertices> */
		target.vertices.forEach((id, vertex) -> {
			try {
				/* update case */
				NdVertex baseVertex;
				if((baseVertex = origin.vertices.get(id)) != null) {
					vertex.publishUpdate(baseVertex, objectDeltas);
				}
				/* create case  */
				else {
					vertex.publishCreate(objectDeltas);
				}
			} catch (NdIOException e) {
				e.printStackTrace();
			}
		});
		/* <vertices> */



		/* <exposure> */
		RepoS8Object[] baseExposure = origin.exposure;
		int baseRange = baseExposure.length;
		RepoS8Object[] exposure = target.exposure;
		int range = exposure.length;
		int r = baseRange < range ? baseRange : range;


		/* common part of range */
		for(int slot = 0; slot < r; slot++) {
			RepoS8Object exposed = exposure[slot];
			RepoS8Object b = baseExposure[slot];
			if(exposed != null && 
					(b == null || !b.S8_id.equals(exposed.S8_id))) {
				objectDeltas.add(new ExposeNdObjectDelta(exposed.S8_id, slot));
			}
		}

		/* range extension */
		for(int slot = r; slot < range; slot++) {
			RepoS8Object exposed = exposure[slot];
			objectDeltas.add(new ExposeNdObjectDelta(exposed.S8_id, slot));
		}

		/* <exposure> */


		delta.lastAssignedIndex = target.lastAssignedIndex;

		return delta;
	}


}
