package com.s8.core.bohr.neodymium.demos.repo2;

import com.s8.api.annotations.S8ObjectType;



/**
 * 
 * 
 * 
 * 
 * @author Pierre Convert
 * Copyright (C) 2022, Pierre Convert. All rights reserved.
 *
 */
@S8ObjectType(name = "empty-floor")
public class MyEmptyFloor extends MyFloor {

	public MyEmptyFloor() {
		super();
	}

	public static MyFloor create() {
		MyEmptyFloor floor = new MyEmptyFloor();
		return floor;
	}

	@Override
	protected void init() {
		
	}

	@Override
	protected void variate() {
		
	}

}
