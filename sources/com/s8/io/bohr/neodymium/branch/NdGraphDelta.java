package com.s8.io.bohr.neodymium.branch;

import static com.s8.api.bohr.BOHR_Keywords.CLOSE_JUMP;
import static com.s8.api.bohr.BOHR_Keywords.DEFINE_JUMP_AUTHOR;
import static com.s8.api.bohr.BOHR_Keywords.DEFINE_JUMP_COMMENT;
import static com.s8.api.bohr.BOHR_Keywords.DEFINE_JUMP_TIMESTAMP;
import static com.s8.api.bohr.BOHR_Keywords.OPEN_JUMP;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.s8.api.bytes.ByteOutflow;
import com.s8.api.bytes.MemoryFootprint;
import com.s8.io.bohr.neodymium.branch.endpoint.NdOutbound;
import com.s8.io.bohr.neodymium.exceptions.NdIOException;
import com.s8.io.bohr.neodymium.object.NdObjectDelta;
import com.s8.io.bohr.neodymium.type.BuildScope;


/**
 * 
 *
 * @author Pierre Convert
 * Copyright (C) 2022, Pierre Convert. All rights reserved.
 * 
 */
public class NdGraphDelta {




	/**
	 * Mandatory version info, i.e. the version of the graph AFTER having applied this delta
	 */
	public final long version;



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
	 * the last assigned index for objects created during this delta
	 */
	public long lastAssignedIndex = -1;


	/**
	 * 
	 */
	public NdGraphDelta(long version) {
		super();
		this.version = version;
	}

	
	
	
	
	
	/**
	 * 
	 * @param timestamp
	 */
	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}
	
	
	/**
	 * 
	 * @param author
	 */
	public void setAuthor(String author) {
		this.author = author;
	}
	
	
	/**
	 * 
	 * @param comment
	 */
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
	
		/* <update objects/exposure> */
		BuildScope scope = graph.createBuildContext();
		for(NdObjectDelta objectDelta : objectDeltas) { 
			objectDelta.consume(graph, scope);
		}
		scope.resolve();
		/* </update objects/exposure> */
		
		
		/* <version> */
		if(version != (graph.version + 1)) { 
			throw new NdIOException("Mismatch in versions");
		}
		graph.version++;
		/* </version> */
		
		
		/* <last-assigned-index> */
		if(lastAssignedIndex < graph.lastAssignedIndex) {
			throw new NdIOException("last assigned index cannot be smaller");
		}
		graph.lastAssignedIndex = lastAssignedIndex;
		/* </last-assigned-index> */
	}




	public void serialize(NdOutbound outbound, ByteOutflow outflow) throws IOException {

		outflow.putUInt8(OPEN_JUMP);
		
		
		/* version */
		outflow.putUInt64(version);
		
		/* last assigned index */
		outflow.putInt64(lastAssignedIndex);
		
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