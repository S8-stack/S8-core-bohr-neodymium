package com.s8.io.bohr.neodymium.branch;


import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import com.s8.io.bohr.atom.S8ShellStructureException;
import com.s8.io.bohr.neodymium.branch.operations.CloneNdModule;
import com.s8.io.bohr.neodymium.branch.operations.CommitNdModule;
import com.s8.io.bohr.neodymium.branch.operations.CompareNdModule;
import com.s8.io.bohr.neodymium.codebase.NdCodebase;
import com.s8.io.bohr.neodymium.exceptions.NdIOException;
import com.s8.io.bohr.neodymium.object.NdObject;
import com.s8.io.bytes.base64.Base64Generator;




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
	 * highest index
	 */
	long highestIndex;



	/**
	 * 
	 */
	private final List<NdBranchDelta> deltas = new ArrayList<>();


	

	/**
	 * last state
	 */
	private NdGraph head = new NdGraph();
	


	private RemapModule remapModule;
	
	
	
	private final Base64Generator idxGen;



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

		
		idxGen = new Base64Generator(id+':');
		
		remapModule = new RemapModule(codebase, new IdGenerator() {
			@Override
			public String generateId() {
				return idxGen.generate(highestIndex++);
			}
		});
	}
	
	
	



	/**
	 * 
	 * @param objects
	 * @throws  
	 * @throws IOException 
	 */
	public void appendDelta(NdBranchDelta delta) throws IOException {
		
		// add delta
		deltas.add(delta);
		
		/* run delta on head */
		delta.consume(head);
	}
	
	
	/**
	 * 
	 * @return
	 */
	public NdBranchDelta[] getSequence(){
		int nDeltas = deltas.size();
		NdBranchDelta[] sequence = new NdBranchDelta[nDeltas];
		for(int i = 0; i<nDeltas; i++) { sequence[i] = deltas.get(i); }
		return sequence;
	}



	/**
	 * 
	 * @return
	 */
	public String createNewIndex() {
		return idxGen.generate(++highestIndex);
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
		NdGraph clone = new NdGraph();
		int index = 0;
		while(clone.version < version) {
			deltas.get(index++).consume(clone);
		}
		return clone;
	}


	/**
	 * 
	 * @param branch
	 * @throws IOException
	 * @throws S8ShellStructureException
	 */
	public long commit(NdObject[] objects) throws IOException, S8ShellStructureException {
		
		
		long version = head.version + 1;
		
		/* build graph from exposure */
		NdGraph next = remapModule.remap(version, objects);
		
		/* commit changes */
		NdBranchDelta delta = CommitNdModule.generateDelta(head, next);
		
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
