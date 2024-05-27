package com.s8.core.bohr.neodymium.repository;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

import com.s8.api.flow.repository.objects.S8BranchMetadata;
import com.s8.api.flow.repository.objects.S8RepositoryMetadata;
import com.s8.core.bohr.neodymium.branch.NdBranchMetadata;
import com.s8.core.io.json.JSON_Field;
import com.s8.core.io.json.JSON_Type;

@JSON_Type(name = "MgRepositoryMetadata")
public class NdRepositoryMetadata implements S8RepositoryMetadata {


	@JSON_Field(name = "name") 
	public String name;
	
	@JSON_Field(name = "address") 
	public String address;
	
	@JSON_Field(name = "creationDate") 
	public long creationDate;
	
	@JSON_Field(name = "owner") 
	public String owner;
	
	@JSON_Field(name = "info") 
	public String info;


	@JSON_Field(name = "branches")
	public Map<String, NdBranchMetadata> branches;
	
	/*
	 * I/O module : true if changes have not been persisted to the Disk
	 */
	public boolean nIO_hasUnsavedChanges;

	
	/**
	 * 
	 * @return
	 */
	public NdRepositoryMetadata deepClone() {
		NdRepositoryMetadata clone = new NdRepositoryMetadata();
		clone.name = name;
		clone.address = address;
		clone.creationDate = creationDate;
		clone.owner = owner;
		clone.info = info;
		
		HashMap<String, NdBranchMetadata> cloneBranches = new HashMap<>();
		branches.forEach((id, bMetadata) -> cloneBranches.put(id, bMetadata.deepClone()));
		clone.branches = cloneBranches;
		
		return clone;
	}
	
	/**
	 * 
	 * @return
	 */
	public NdRepositoryMetadata shallowClone() {
		NdRepositoryMetadata clone = new NdRepositoryMetadata();
		clone.name = name;
		clone.address = address;
		clone.creationDate = creationDate;
		clone.owner = owner;
		clone.info = info;
		
		return clone;
	}



	@Override
	public String getName() {
		return name;
	}

	
	@Override
	public String getAddress() {
		return address;
	}


	@Override
	public long getCreationDate() {
		return creationDate;
	}


	@Override
	public String getOwner() {
		return owner;
	}


	@Override
	public String getInfo() {
		return info;
	}


	@Override
	public int getNbBranches() {
		return branches.size();
	}

	@Override
	public void crawlBranches(BiConsumer<String, S8BranchMetadata> consumer) {
		branches.forEach(consumer);
	}	
}
