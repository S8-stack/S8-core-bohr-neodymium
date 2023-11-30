package com.s8.core.bohr.neodymium.demos.repo0;

import com.s8.api.annotations.S8Field;
import com.s8.api.annotations.S8ObjectType;
import com.s8.api.flow.repository.objects.RepoS8Object;
import com.s8.core.bohr.neodymium.object.NdRef;

/**
 * 
 * @author pierreconvert
 *
 */
@S8ObjectType(name = "Dclib/MyProject")
public class MyProject extends RepoS8Object {


	public MyProject() {
		super();
	}


	public @S8Field(name = "field1") int specialField;

	/**
	 * the visco
	 */
	public @S8Field(name = "viscosity") double visc;


	/** 
	 * the text for the data
	 */
	public @S8Field(name = "message") String txt;

	public @S8Field(name = "payload") MyProjectPayload payload;

	public @S8Field(name = "last") NdRef<MyProject> last;



	public void setViscosity(double val) {
		visc = val;
	}


	public void setPayload(int choice) {
		payload = new CarPayload();
	}


}
