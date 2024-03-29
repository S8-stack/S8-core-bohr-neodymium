package com.s8.core.bohr.neodymium.demos.repo0;

import com.s8.api.annotations.S8Field;
import com.s8.api.annotations.S8ObjectType;
import com.s8.api.flow.repository.objects.RepoS8Object;

@S8ObjectType(name = "project-payload", 
sub= {
		PlanePayload.class,
		CarPayload.class
})
public abstract class MyProjectPayload extends RepoS8Object {

	public @S8Field(name="viscosity") double visc;
	
	public @S8Field(name="message") String specializedName;

	public @S8Field(name = "field1") int counter;
	
	
	
	public MyProjectPayload() {
		super();
	}
	
	
	/* <view01> */
}
