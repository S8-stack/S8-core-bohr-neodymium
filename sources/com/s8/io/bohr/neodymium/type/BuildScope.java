package com.s8.io.bohr.neodymium.type;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.s8.api.objects.repo.RepoS8Object;
import com.s8.io.bohr.neodymium.exceptions.NdIOException;
import com.s8.io.bohr.neodymium.object.NdVertex;



/**
 * 
 * 
 * @author Pierre Convert
 * Copyright (C) 2022, Pierre Convert. All rights reserved.
 * 
 */
public abstract class BuildScope {
	

	
	/**
	 * 
	 */
	List<Binding> bindings = new ArrayList<>();


	/**
	 * 
	 * @param binding
	 */
	public void appendBinding(Binding binding) {
		bindings.add(binding);
	}
	
	
	/**
	 * 
	 * @param index
	 * @return
	 */
	public abstract RepoS8Object retrieveObject(String id);
	
	/**
	 * <p><b>ALWAYS RESOLVE AFTER DESERIALIZATION</b></p>
	 * 
	 * @throws LthSerialException
	 */
	public void resolve() throws NdIOException {

		// screen all bindings
		for(Binding binding : bindings) {

			// resolve
			binding.resolve(this);
		}

		// bindings have now all been consumed, so clear
		bindings.clear();
	}


	
	/**
	 * 
	 * @author pc
	 *
	 */
	public interface Binding {


		/**
		 * Attempt to resolve a binding.
		 * @return false if the binding has been successfully resolved (nothing else to do)
		 * @throws LthSerialException
		 */
		public abstract void resolve(BuildScope scope) throws NdIOException;

	}
	
	
	public static BuildScope fromVertices(Map<String, NdVertex> vertices) {
		return new BuildScope() {

			@Override
			public RepoS8Object retrieveObject(String id) {
				NdVertex vertex = vertices.get(id);
				if(vertex == null) {
					throw new RuntimeException("Cannot find vertex");
				}
				return vertex.object;
			}
		};
	}
	
}
