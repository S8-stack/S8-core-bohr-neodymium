package com.s8.io.bohr.neodymium.branch;

import static com.s8.io.bohr.atom.BOHR_Keywords.CLOSE_JUMP;
import static com.s8.io.bohr.atom.BOHR_Keywords.DEFINE_JUMP_TIMESTAMP;
import static com.s8.io.bohr.atom.BOHR_Keywords.DEFINE_JUMP_AUTHOR;
import static com.s8.io.bohr.atom.BOHR_Keywords.DEFINE_JUMP_COMMENT;
import static com.s8.io.bohr.atom.BOHR_Keywords.OPEN_JUMP;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.s8.io.bohr.neodymium.branch.endpoint.NdOutbound;
import com.s8.io.bohr.neodymium.exceptions.NdIOException;
import com.s8.io.bohr.neodymium.object.NdObjectDelta;
import com.s8.io.bohr.neodymium.type.BuildScope;
import com.s8.io.bytes.alpha.ByteOutflow;
import com.s8.io.bytes.alpha.MemoryFootprint;


/**
 * 
 *
 * @author Pierre Convert
 * Copyright (C) 2022, Pierre Convert. All rights reserved.
 * 
 */
public class NdGraphDelta {




	/**
	 * Mandatory version info
	 */
	public final long targetVersion;



	/**
	 * 
	 */
	private long timestamp = -1;


	/**
	 * 
	 */
	private String author = null;
	
	
	/**
	 * 
	 */
	private String comment = null;


	/**
	 * 
	 */
	public List<NdObjectDelta> objectDeltas = new ArrayList<>();


	/**
	 * 
	 */
	public long lastAssignedIndex = -1;


	/**
	 * 
	 */
	public NdGraphDelta(long version) {
		super();
		this.targetVersion = version;
	}

	
	
	
	
	
	public void setTimestamp(long timestamp) {
		this.setTimestamp(timestamp);
	}
	
	public void setAuthor(String author) {
		this.author = author;
	}
	
	public void setComment(String comment) {
		this.comment = comment;
	}

	
	/**
	 * 
	 * @param delta
	 */
	public void appendObjectDelta(NdObjectDelta delta) {
		objectDeltas.add(delta);
	}



	/**
	 * 
	 * @param graph
	 * @throws NdIOException
	 */
	public void operate(NdGraph graph) throws NdIOException {
		/* check version */
		if(targetVersion != (graph.version + 1)) { 
			throw new NdIOException("Mismatch in versions");
		}
		
		BuildScope scope = graph.createBuildContext();
		for(NdObjectDelta objectDelta : objectDeltas) { 
			objectDelta.consume(graph, scope); 
		}
		scope.resolve();
		
		/* increment version of graph */
		graph.version++;
	}




	public void serialize(NdOutbound outbound, ByteOutflow outflow) throws IOException {

		outflow.putUInt8(OPEN_JUMP);
		
		outflow.putUInt64(targetVersion);
		
		/* <metadatas> */
		if(timestamp >= 0) {
			outflow.putUInt8(DEFINE_JUMP_TIMESTAMP);
			outflow.putUInt64(timestamp);
		}
		
		if(author != null) {
			outflow.putUInt8(DEFINE_JUMP_AUTHOR);
			outflow.putStringUTF8(author);
		}
	
		if(comment != null) {
			outflow.putUInt8(DEFINE_JUMP_COMMENT);
			outflow.putStringUTF8(comment);
		}
		/* </metadatas> */

		// compose common database
		//codebaseIO.compose(outflow, false);
		for(NdObjectDelta objectDelta : objectDeltas) { 
		
			
			objectDelta.serialize(outbound, outflow); 
		}


		outflow.putUInt8(CLOSE_JUMP);
	}


	public void computeFootprint(MemoryFootprint weight) {
		weight.reportInstance();
		objectDeltas.forEach(delta -> delta.computeFootprint(weight));
	}
	
	
	
	/**
	 * 
	 * @return
	 */
	public long getTimestamp() {
		return timestamp;
	}
	
	/**
	 * 
	 * @return
	 */
	public String getAuthor() {
		return author;
	}
	
	/**
	 * 
	 * @return
	 */
	public String getComment() {
		return comment;
	}

}