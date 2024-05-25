package com.s8.core.bohr.neodymium.branch;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import com.s8.api.bytes.ByteInflow;
import com.s8.core.bohr.neodymium.branch.endpoint.NdInbound;
import com.s8.core.bohr.neodymium.branch.endpoint.NdOutbound;
import com.s8.core.bohr.neodymium.codebase.NdCodebase;
import com.s8.core.io.bytes.linked.LinkedByteInflow;
import com.s8.core.io.bytes.linked.LinkedByteOutflow;
import com.s8.core.io.bytes.linked.LinkedBytes;
import com.s8.core.io.bytes.linked.LinkedBytesIO;

/**
 * 
 */
public class NdBranchIOModule {


	/**
	 * 
	 */
	public final static String BRANCH_DATA_PATHNAME = "branch-data.nd";




	/**
	 * 
	 * @param codebase
	 * @param repoPath
	 * @param id
	 * @param isVerbose
	 * @return
	 * @throws IOException
	 */
	public static NdBranch read(NdCodebase codebase, Path repoPath, String id, boolean isVerbose) throws IOException {

		/* read from disk */
		Path path = repoPath.resolve(id + BRANCH_DATA_PATHNAME);

		LinkedBytes head = LinkedBytesIO.read(path, isVerbose);

		/* build inflow */
		ByteInflow inflow = new LinkedByteInflow(head);

		/* build inbound session */
		NdInbound inbound = new NdInbound(codebase);

		/* build branch */
		NdBranch branch = new NdBranch(codebase, id);

		/* load branch */
		inbound.pullFrame(inflow, delta -> branch.appendDelta(delta));

		return branch;
	}



	/**
	 * 
	 * @param codebase
	 * @param branch
	 * @param repoPath
	 * @throws IOException
	 */
	public static void write(NdCodebase codebase, NdBranch branch, Path repoPath) throws IOException {


		/* build inflow */
		LinkedByteOutflow outflow = new LinkedByteOutflow();

		/* build outbound session */
		NdOutbound outbound = new NdOutbound(codebase);

		/* push branch */
		outbound.pushFrame(outflow, branch.getSequence());

		/* write to disk */

		String id = branch.id;

		Files.createDirectories(repoPath);

		Path path = repoPath.resolve(id + BRANCH_DATA_PATHNAME);

		LinkedBytesIO.write(outflow.getHead(), path, true);
	}



}
