package com.s8.core.bohr.neodymium.fields.objects;

import com.s8.api.bytes.MemoryFootprint;
import com.s8.api.flow.repository.objects.RepoS8Object;
import com.s8.api.serial.S8Serializable;
import com.s8.core.bohr.neodymium.exceptions.NdIOException;
import com.s8.core.bohr.neodymium.fields.NdField;
import com.s8.core.bohr.neodymium.fields.NdFieldDelta;
import com.s8.core.bohr.neodymium.type.BuildScope;


/**
 * 
 *
 * @author Pierre Convert
 * Copyright (C) 2022, Pierre Convert. All rights reserved.
 * 
 */
public class S8SerializableNdFieldDelta<T extends S8Serializable> extends NdFieldDelta {
	
	
	public final S8SerializableNdField<T> field;
	
	public final S8Serializable value;

	
	
	public S8SerializableNdFieldDelta(S8SerializableNdField<T> field, S8Serializable value) {
		super();
		this.field = field;
		this.value = value;
	}

	@Override
	public void consume(RepoS8Object object, BuildScope scope) throws NdIOException {
		field.handler.set(object, value);
	}
	
	
	@Override
	public NdField getField() { 
		return field;
	}
	
	
	@Override
	public void computeFootprint(MemoryFootprint weight) {
		if(value!=null) {
			weight.reportInstance();
			weight.reportBytes(value.computeFootprint());	
		}
	}

}
