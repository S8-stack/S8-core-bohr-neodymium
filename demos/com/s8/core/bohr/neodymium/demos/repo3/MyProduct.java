package com.s8.core.bohr.neodymium.demos.repo3;

import com.s8.api.annotations.S8Field;
import com.s8.api.annotations.S8ObjectType;
import com.s8.api.flow.repository.objects.RepoS8Object;


/**
 * This is PUBLIC object
 * 
 * @author pierreconvert
 *
 */
@S8ObjectType(name = "product")
public class MyProduct extends RepoS8Object {


	@S8Field(name = "description")
	public String description;
	
	
	@S8Field(name = "stock")
	public MyItemStock stock;
	

	/**
	 * 
	 * @param shell
	 */
	public MyProduct() {
		super();
	}
	
}
