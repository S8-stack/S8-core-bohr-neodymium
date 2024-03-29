package com.s8.core.bohr.neodymium.type;

import com.s8.api.flow.repository.objects.RepoS8Object;
import com.s8.core.bohr.neodymium.exceptions.NdIOException;

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
