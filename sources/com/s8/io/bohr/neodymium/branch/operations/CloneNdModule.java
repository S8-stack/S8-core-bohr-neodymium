package com.s8.io.bohr.neodymium.branch.operations;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.s8.io.bohr.atom.S8ShellStructureException;
import com.s8.io.bohr.neodymium.branch.NdGraph;
import com.s8.io.bohr.neodymium.object.NdObject;
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
	public static NdGraph deepClone(NdGraph base) throws IOException, S8ShellStructureException {
		
		
		
		/* <vertices> */
		
		Map<String, NdVertex> cloneVertices = new HashMap<>();
		BuildScope scope = BuildScope.fromVertices(cloneVertices);
		
		base.vertices.forEach((id, vertex) -> {
			try {
				NdObject objectClone = vertex.type.deepClone(vertex.object, scope);
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
		NdObject[] baseExposure = base.exposure;
		int range = baseExposure.length;
		NdObject[] cloneExposure = new NdObject[range];
		NdObject exposed;
		for(int slot = 0; slot < range; slot++) {
			if((exposed = baseExposure[slot]) != null) {
				cloneExposure[slot] = cloneVertices.get(exposed.S8_id).object;
			}
		}
		/* </exposure> */
		
		/**
		 * 
		 */
		return new NdGraph(base.version, cloneVertices, cloneExposure);
	}

}
