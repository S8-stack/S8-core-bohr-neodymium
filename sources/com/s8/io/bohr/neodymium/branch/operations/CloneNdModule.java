package com.s8.io.bohr.neodymium.branch.operations;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.s8.api.objects.repo.RepoS8Object;
import com.s8.io.bohr.atom.S8ShellStructureException;
import com.s8.io.bohr.neodymium.branch.NdGraph;
import com.s8.io.bohr.neodymium.object.NdVertex;
import com.s8.io.bohr.neodymium.type.BuildScope;


/**
 * 
 * 
 * @author Pierre Convert
 * Copyright (C) 2022, Pierre Convert. All rights reserved.
 * 
 */
public class CloneNdModule {
	
	


	/**
	 * 
	 * @throws S8ShellStructureException 
	 * @throws IOException 
	 */
	public static NdGraph deepClone(NdGraph origin) throws IOException, S8ShellStructureException {
		
		
		
		/* <vertices> */
		
		Map<String, NdVertex> cloneVertices = new HashMap<>();
		BuildScope scope = BuildScope.fromVertices(cloneVertices);
		
		origin.vertices.forEach((id, vertex) -> {
			try {
				RepoS8Object objectClone = vertex.type.deepClone(vertex.object, scope);
				objectClone.S8_id = id;
				NdVertex vertexClone = new NdVertex(vertex.type);
				vertexClone.object = objectClone;
				
				cloneVertices.put(id, vertexClone);
			}
			catch (IOException e) {
				e.printStackTrace();
			}
		});

		// resolve
		scope.resolve();
		
		/* </vertices> */
		
		
		/* <exposure> */
		RepoS8Object[] baseExposure = origin.exposure;
		int range = baseExposure.length;
		RepoS8Object[] cloneExposure = new RepoS8Object[range];
		RepoS8Object exposed;
		for(int slot = 0; slot < range; slot++) {
			if((exposed = baseExposure[slot]) != null) {
				cloneExposure[slot] = cloneVertices.get(exposed.S8_id).object;
			}
		}
		/* </exposure> */
		
		
		/** graph */
		return new NdGraph(origin.version, cloneVertices, cloneExposure, origin.lastAssignedIndex);
	}

}
