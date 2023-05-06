package com.s8.io.bohr.neodymium.branch;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import com.s8.io.bohr.neodymium.codebase.NdCodebase;
import com.s8.io.bohr.neodymium.object.NdObject;
import com.s8.io.bohr.neodymium.object.NdVertex;
import com.s8.io.bohr.neodymium.type.GraphCrawler;
import com.s8.io.bohr.neodymium.type.NdType;

public class RemapModule {
	
	
	public final NdCodebase codebase;
	
	
	public final IdGenerator idGenerator;
	
	
	
	public RemapModule(NdCodebase codebase, IdGenerator idGenerator) {
		super();
		this.codebase = codebase;
		this.idGenerator = idGenerator;
	}



	/**
	 * 
	 * @param version
	 * @param objects
	 * @throws IOException 
	 */
	public NdGraph remap(long version, NdObject[] objects) throws IOException {
		
		
		if(objects == null) {
			throw new IOException("Must defined objects");
		}
		
		if(objects.length > NdBranch.EXPOSURE_RANGE) {
			throw new IOException("Cannot exceed exposure range");
		}
		
		
		
		Map<String, NdVertex> vertices = new HashMap<>();
		
	
		
	
		Queue<NdVertex> queue = new LinkedList<NdVertex>();
		List<NdObject> rollback = new ArrayList<NdObject>();
		

		GraphCrawler crawler = new GraphCrawler() {
			
			@Override
			public void accept(NdObject object) {
				if(!object.S8_spin) {
					
					/* retrieve type */
					NdType type = codebase.getType(object);
					
					String id = object.S8_id;

					/* create vertex */
					NdVertex vertex = new NdVertex(type);

					/* assign object to vertex */
					vertex.object = object;
					object.S8_vertex = vertex;
					

					// else, object already known from update branch base
					if(id == null) {

						/* index */
						id = idGenerator.generateId();

						/* index */
						object.S8_id = id;
					}

					/* append vertex */
					vertices.put(id, vertex);
					
					queue.add((NdVertex) object.S8_vertex);
					rollback.add(object);
					object.S8_spin = true;	
				}
			}
		};


		/* prepare initial set */
		int range = objects.length;
		for(int port = 0; port < range; port ++) {
			NdObject object = objects[port];
			if(object!=null) {
				crawler.accept(object);
			}
		}
		
		/* iterative sweeping */
		NdVertex vertex;
		while((vertex = queue.poll()) != null) { vertex.sweep(crawler); }
		
		// rollback
		rollback.forEach(object -> { object.S8_spin = false; });
		
		
		return new NdGraph(version, vertices, objects);
	}
}
