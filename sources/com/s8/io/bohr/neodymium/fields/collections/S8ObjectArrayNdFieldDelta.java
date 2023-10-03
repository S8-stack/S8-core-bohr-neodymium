package com.s8.io.bohr.neodymium.fields.collections;

import java.lang.reflect.Array;

import com.s8.api.bytes.MemoryFootprint;
import com.s8.api.objects.repo.RepoS8Object;
import com.s8.io.bohr.neodymium.exceptions.NdIOException;
import com.s8.io.bohr.neodymium.fields.NdField;
import com.s8.io.bohr.neodymium.fields.NdFieldDelta;
import com.s8.io.bohr.neodymium.type.BuildScope;


/**
 * 
 *
 * @author Pierre Convert
 * Copyright (C) 2022, Pierre Convert. All rights reserved.
 * 
 */
public class S8ObjectArrayNdFieldDelta extends NdFieldDelta {

	
	
	public final S8ObjectArrayNdField field;
	
	public final String[] itemIdentifiers;

	/**
	 * 
	 * @param field
	 * @param indices
	 */
	public S8ObjectArrayNdFieldDelta(S8ObjectArrayNdField field, String[] indices) {
		super();
		this.field = field;
		this.itemIdentifiers = indices;
	}
	

	@Override
	public NdField getField() { 
		return field;
	}

	
	@Override
	public void consume(RepoS8Object object, BuildScope scope) throws NdIOException {

		if(itemIdentifiers!=null) {
			
			scope.appendBinding(new BuildScope.Binding() {
				@Override
				public void resolve(BuildScope scope) throws NdIOException {
					int n = itemIdentifiers.length;
					Object array = Array.newInstance(field.componentType, n);
					for(int index = 0; index < n; index++) {
						String id = itemIdentifiers[index];
						Array.set(array, index, id != null ? scope.retrieveObject(id) : null);
					}
					field.handler.set(object, array);
				}
			});	
			
		}
		else {
			field.handler.set(object, null);
		}
	}


	@Override
	public void computeFootprint(MemoryFootprint weight) {
		if(itemIdentifiers!=null) {
			weight.reportInstance();
			weight.reportReferences(itemIdentifiers.length);	
		}
	}


}
