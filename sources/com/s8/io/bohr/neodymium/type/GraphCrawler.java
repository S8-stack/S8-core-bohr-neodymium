package com.s8.io.bohr.neodymium.type;

import com.s8.api.objects.repo.RepoS8Object;
import com.s8.io.bohr.neodymium.exceptions.NdIOException;

/**
 * 
 *
 * @author Pierre Convert
 * Copyright (C) 2022, Pierre Convert. All rights reserved.
 * 
 */
public interface GraphCrawler {

	
	/**
	 * 
	 * @param object
	 * @throws NdIOException
	 */
	public void accept(RepoS8Object object) throws NdIOException;
	
	
}
