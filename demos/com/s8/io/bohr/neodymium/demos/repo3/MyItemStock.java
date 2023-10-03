package com.s8.io.bohr.neodymium.demos.repo3;

import com.s8.api.objects.annotations.S8Field;
import com.s8.api.objects.annotations.S8ObjectType;
import com.s8.api.objects.repo.RepoS8Object;

/**
 * 
 *
 * @author Pierre Convert
 * Copyright (C) 2022, Pierre Convert. All rights reserved.
 * 
 */
@S8ObjectType(name = "item-stock")
public class MyItemStock extends RepoS8Object {


	public @S8Field(name = "n-items") int nItemsCurrentlyInWarehouse;
		
	public MyItemStock() {
		super();
	}

}
