package com.s8.io.bohr.neodymium.branch;


import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import com.s8.api.objects.repo.RepoS8Object;
import com.s8.io.bohr.atom.S8ShellStructureException;
import com.s8.io.bohr.neodymium.branch.operations.CloneNdModule;
import com.s8.io.bohr.neodymium.branch.operations.CommitNdModule;
import com.s8.io.bohr.neodymium.branch.operations.CompareNdModule;
import com.s8.io.bohr.neodymium.codebase.NdCodebase;
import com.s8.io.bohr.neodymium.exceptions.NdIOException;




/**
 * <ul>
 * <li>PULL: Retrieve the branch deltas from inflow. No objects is constructed at this point </li>
 * <li>ROLL: move construction up to a specific version.</li>
 * <li>CLONE: Create a new Object by deep cloning current state (this clone is not connected to vertices)</li>
 * <li>COMMIT: (ONLY possible upon rolled to HEAD) create new delta from HEAD to the objects passed as argument</li>
 * <li>PUSH: release delta to outbound.</li>
 * Build head by playing all delta -> At this point </li>
 * <li>
 * 
 * @author pierreconvert
 *
 *
 *
 * <ul>
 * <li><b>PULL</b>: Read from I/O, .</li>
 * <li><b>
 * 
 * 
 * @author Pierre Convert
 * Copyright (C) 2022, Pierre Convert. All rights reserved.
 * 
 */
public class NdBranch {
	
	

	/**
	 * 
	 */
	public final static int EXPOSURE_RANGE = 8;

	/**
	 * codebase
	 */
	public final NdCodebase codebase;


	/**
	 * like hy.fr.com/main-ref/orc/project0273
	 */
	//public final String address;
	
	
	/**
	 * branch id (like 'master', 'main', 'dev', 'bug-fix-ticket08909808')
	 */
	public final String id;




	/**
	 * 
	 */
	private final List<NdGraphDelta> deltas = new ArrayList<>();


	

	/**
	 * last state
	 */
	private NdGraph head = NdGraph.createZeroStart();
	


	private MappingModule remapModule;
	




	/**
	 * 
	 * @param branchId
	 * @param graph
	 * @param deltas
	 * @throws IOException 
	 */
	public NdBranch(NdCodebase codebase, String id) {
		super();
		this.codebase = codebase;
		this.id = id;

		remapModule = new MappingModule(codebase);
	}
	
	
	



	/**
	 * 
	 * @param objects
	 * @throws  
	 * @throws IOException 
	 */
	public void appendDelta(NdGraphDelta delta) throws IOException {
		
		// add delta
		deltas.add(delta);
		
		/* run delta on head */
		delta.operate(head);
	}
	
	
	/**
	 * 
	 * @return
	 */
	public NdGraphDelta[] getSequence(){
		int nDeltas = deltas.size();
		NdGraphDelta[] sequence = new NdGraphDelta[nDeltas];
		for(int i = 0; i<nDeltas; i++) { sequence[i] = deltas.get(i); }
		return sequence;
	}



	
	
	/**
	 * 
	 * @return
	 * @throws IOException
	 * @throws S8ShellStructureException
	 */
	public NdGraph cloneHead() throws IOException, S8ShellStructureException {
		return CloneNdModule.deepClone(head);
	}
	
	
	
	/**
	 * 
	 * @param version
	 * @return
	 * @throws NdIOException
	 */
	public NdGraph cloneVersion(long version) throws NdIOException {
		NdGraph clone = NdGraph.createZeroStart();
		int index = 0;
		while(clone.version < version) {
			deltas.get(index++).operate(clone);
		}
		return clone;
	}

	
	public long commit(RepoS8Object[] objects) throws IOException, S8ShellStructureException {
		return commit(objects, -1L, null, null);
	}

	/**
	 * 
	 * @param branch
	 * @throws IOException
	 * @throws S8ShellStructureException
	 */
	public long commit(RepoS8Object[] objects, long timestamp, String author, String comment) throws IOException, S8ShellStructureException {
		
		
		long version = head.version + 1;
		
		/* build graph from exposure */
		NdGraph next = remapModule.remap(version, objects, id, head.lastAssignedIndex);
		
		/* commit changes */
		NdGraphDelta delta = CommitNdModule.generateDelta(head, next);
		
		/* metadatas */
		if(timestamp>=0) { delta.setTimestamp(timestamp); }
		if(author != null) { delta.setAuthor(author); }
		if(comment != null) { delta.setComment(comment); }
		
		
		/* submit delta */
		appendDelta(delta);
		
		return version;
	}
	
	
	
	/**
	 * 
	 * @param graph
	 * @param writer
	 * @throws IOException
	 * @throws S8ShellStructureException
	 */
	public void compareHead(NdGraph graph, Writer writer) throws IOException, S8ShellStructureException {
		CompareNdModule.deepCompare(head, graph, writer);
	}






	public long getHeadVersion() {
		return head.version;
	}


	
	
}
