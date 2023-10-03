package com.s8.io.bohr.neodymium.object;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

import com.s8.api.bytes.MemoryFootprint;
import com.s8.api.objects.repo.RepoS8Object;
import com.s8.api.objects.repo.RepoS8Vertex;
import com.s8.io.bohr.atom.S8ShellStructureException;
import com.s8.io.bohr.neodymium.exceptions.NdIOException;
import com.s8.io.bohr.neodymium.fields.NdField;
import com.s8.io.bohr.neodymium.fields.NdFieldDelta;
import com.s8.io.bohr.neodymium.type.GraphCrawler;
import com.s8.io.bohr.neodymium.type.NdType;


/**
 * <h1>Node for sweepable graph</h1>
 * <p>Node encompass in a unified interface two types of cases:</p>
 * <ul>
 * <li>On the fly type resolution (S8Struct)</li>
 * <li>Compiled type resolution, stored in S8Vertex extension (like LiVertex) (S8Object)</li>
 * </ul>
 * <p>This is the building block for using sweep on graph</p>
 *
 *
 * @author Pierre Convert
 * Copyright (C) 2022, Pierre Convert. All rights reserved.
 * 
 */
public class NdVertex extends RepoS8Vertex {



	/**
	 * 
	 */
	//public final NdBranch branch;

	/**
	 * 
	 */
	public final NdType type;


	public RepoS8Object object;





	/**
	 * 
	 */
	public long event;


	/**
	 * 
	 * @param branch
	 * @param type
	 */
	public NdVertex(NdType type) {
		super();
		this.type = type;
	}


	public String getIndex() {
		return object.S8_id;
	}


	/**
	 * 
	 * @param front
	 * @throws IOException
	 * @throws S8ShellStructureException 
	 */
	public void sweep(GraphCrawler crawler) throws IOException {
		type.sweep(object, crawler);
	}



	/**
	 * 
	 * @param references
	 * @throws IOException
	 */
	public void sweepReferences(Queue<String> references) {
		type.collectReferencedBlocks(object, references);	
	}


	public void getByteCount(MemoryFootprint footprint) {

		type.computeFootprint(object, footprint);

	}


	
	public void advertise(long event) {
		this.event |= event;
	}


	
	public RepoS8Object getObject() {
		return object;
	}

	/**
	 * 
	 * @param objectDeltas
	 * @throws NdIOException
	 */
	public void publishCreate(List<NdObjectDelta> objectDeltas) throws NdIOException {
		
		String index = object.S8_id;
		
		List<NdFieldDelta> fieldDeltas = new ArrayList<NdFieldDelta>();

		NdField[] fields = type.fields;
		int n = fields.length;
		for(int i=0; i<n; i++) {
			fieldDeltas.add(fields[i].produceDiff(object));	
		}

		objectDeltas.add(new CreateNdObjectDelta(index, type, fieldDeltas));
	}
	
	

	/**
	 * 
	 * @param base
	 * @param objectDeltas
	 * @throws NdIOException
	 */
	public void publishUpdate(NdVertex base, List<NdObjectDelta> objectDeltas) throws NdIOException {
		
		RepoS8Object baseObject = base.object;
		boolean hasDelta = false;
		List<NdFieldDelta> fieldDeltas = null;

		NdField[] fields = type.fields;
		int n = fields.length;
		NdField field;
		for(int i=0; i<n; i++) {
			if((field = fields[i]).hasDiff(baseObject, object)) {
				if(!hasDelta) {
					hasDelta = true;
					fieldDeltas = new ArrayList<NdFieldDelta>();
				}
				fieldDeltas.add(field.produceDiff(object));
			}
		}
		
		/* publish only if has deltas */
		if(hasDelta) {
			objectDeltas.add(new UpdateNdObjectDelta(object.S8_id, type, fieldDeltas));
		}
	}
}
