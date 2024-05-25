package com.s8.core.bohr.neodymium.repository;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import com.s8.core.bohr.neodymium.branch.NdBranch;
import com.s8.core.bohr.neodymium.branch.NdBranchIOModule;




public class NdRepository {
	
	
	
	public final NdRepositoryMetadata metadata;
	
	
	
	public final Map<String, NdBranch> branches = new HashMap<>();
	
	
	/**
	 * 
	 */
	public Path nIO_path;
	
	
	
	/**
	 * 
	 */
	public NdRepository(NdRepositoryMetadata metadata) {
		super();
		this.metadata = metadata;
	}
	
	/**
	 * 
	 * @return
	 */
	public String getAddress() {
		return metadata.address;
	}

	
	/**
	 * 
	 * @param branchId
	 * @return
	 * @throws IOException
	 */
	public NdBranch getBranch(String branchId) throws IOException {
		NdBranch branch = branches.get(branchId);
		if(branch == null) {
			 branch = NdBranchIOModule.read(null, nIO_path, branchId, false);
			 branches.put(branchId, branch);
		}
		return branch;
		
	}
	

	
}