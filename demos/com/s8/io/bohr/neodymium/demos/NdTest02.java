package com.s8.io.bohr.neodymium.demos;

import java.io.IOException;
import java.io.OutputStreamWriter;

import com.s8.io.bohr.atom.S8BuildException;
import com.s8.io.bohr.neodymium.branch.NdBranch;
import com.s8.io.bohr.neodymium.branch.NdGraph;
import com.s8.io.bohr.neodymium.branch.endpoint.NdInbound;
import com.s8.io.bohr.neodymium.branch.endpoint.NdOutbound;
import com.s8.io.bohr.neodymium.codebase.NdCodebase;
import com.s8.io.bohr.neodymium.codebase.NdCodebaseBuilder;
import com.s8.io.bohr.neodymium.codebase.NdCodebaseBuilder.UpperLevel;
import com.s8.io.bohr.neodymium.demos.repo2.MyBuilding;
import com.s8.io.bohr.neodymium.exceptions.NdBuildException;
import com.s8.io.bohr.neodymium.object.NdObject;
import com.s8.io.bytes.linked.LinkedByteInflow;
import com.s8.io.bytes.linked.LinkedByteOutflow;

public class NdTest02 {
	
	
	private static OutputStreamWriter writer;
	private static NdCodebase codebase;

	
	/**
	 * 
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		
		initialize(MyBuilding.class, false);
		
		
		NdBranch originBranch = new NdBranch(codebase, "com.toto.123.098", "master");
		

		MyBuilding building = MyBuilding.create();
		originBranch.commit(new NdObject[] { null, building});
		
		building.variate();
		originBranch.commit(new NdObject[] { null, building});
		
		building.variate();
		originBranch.commit(new NdObject[] { null, building});
		
		
		// test copy
		NdGraph testCopy = originBranch.cloneHead();
		originBranch.compareHead(testCopy, writer);
		
		
		LinkedByteOutflow outflow = new LinkedByteOutflow(1024);
		NdOutbound outbound = new NdOutbound(codebase);
		outbound.pushFrame(outflow, originBranch.getSequence());
		
		LinkedByteInflow inflow = new LinkedByteInflow(outflow.getHead());
		NdInbound inbound = new NdInbound(codebase);
		NdBranch branchClone = new NdBranch(codebase, "com.toto.123.098", "master");
		inbound.pullFrame(inflow, delta -> {
			try {
				branchClone.appendDelta(delta);
			} catch (IOException e) {
				e.printStackTrace();
			}
		});
		
		originBranch.compareHead(branchClone.cloneHead(), writer);
		
		//rBranch.print(new OutputStreamWriter(System.out));
		
		
		terminate();
	}

	
	
	/**
	 * 
	 * @param rootClass
	 * @param isVerbose
	 * @throws NdBuildException
	 */
	private static void initialize(Class<?> rootClass, boolean isVerbose) throws NdBuildException {
		UpperLevel upperLevel = new UpperLevel() {
			public @Override void pushRowType(Class<?> type) throws S8BuildException { }
		};
		NdCodebaseBuilder codebaseBuilder = new NdCodebaseBuilder(upperLevel, false);
		codebaseBuilder.pushObjectType(MyBuilding.class);
		codebase = codebaseBuilder.build();
		
		if(isVerbose) {
			codebase.DEBUG_print();	
		}
		
		writer = new OutputStreamWriter(System.out);
	}
	
	
	private static void terminate() throws IOException {
		writer.close();
	}
	
	
	
}
