package com.s8.io.bohr.neodymium.demos.repo0;

import java.util.List;

import com.s8.api.exceptions.S8IOException;
import com.s8.api.objects.annotations.S8Field;
import com.s8.api.objects.annotations.S8ObjectType;
import com.s8.api.objects.repo.RepoS8Object;
import com.s8.io.bohr.neodymium.object.NdRef;


/**
 * 
 * @author pc
 * 
 * 
 * 
 */
@S8ObjectType(name = "Dclib/MyTechModel")
public class MyTechModel extends RepoS8Object {


	/**
	 * 
	 */
	private @S8Field(name = "myPurchaser") NdRef<MyTechModel> myPurchaser;
	
	
	private @S8Field(name = "projects") List<MyProject> projects;

	/**
	 * 
	 */
	private @S8Field(name="param#2") int jdgjg;

	
	/**
	 * 
	 * @param vx
	 */
	public MyTechModel() {
		super();
	}

	
	/**
	 * 
	 * @param value
	 * @throws S8IOException
	 */
	public void expand2(double value) throws S8IOException {
		projects.add(new MyProject());
	}
	
	
	/**
	 * 
	 * @throws S8IOException
	 */
	public void expand() throws S8IOException {
		projects.add(new MyProject());
	}

	/**
	 * 
	 */
	public void getPhotos(String val){
		
	}
	

	
	/**
	 * 
	 */
	
	/*
	public void getPhotos2(S8AsyncFlow async, S8Vars scope) { async.
		_do(() -> { 
			//S8Request request = scope.getRequest();
			//f.setL2Var("a", f.getArg(0)); // 0 = arg0
			//scope.set("b", request.getFloat(1)); // 1 = arg1
			//scope.set("${target}", request.<MyTechModel>getReference(2));
			
			async.immediately().forRefDo(myPurchaser, target -> {
				target.getPhotos(scope.get("myParamName"));
			});
		});
	};
	*/
	
}
