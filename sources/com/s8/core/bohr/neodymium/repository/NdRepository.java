package com.s8.core.bohr.neodymium.repository;

import java.util.HashMap;
import java.util.Map;

import com.s8.core.bohr.neodymium.branch.NdBranch;




public class NdRepository {



	public final NdRepositoryMetadata metadata;

	


	public final Map<String, NdBranch> branches = new HashMap<>();



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


	



}