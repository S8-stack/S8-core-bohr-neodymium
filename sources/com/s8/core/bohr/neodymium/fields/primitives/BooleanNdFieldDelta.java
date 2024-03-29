package com.s8.core.bohr.neodymium.fields.primitives;

import com.s8.api.bytes.MemoryFootprint;
import com.s8.api.flow.repository.objects.RepoS8Object;
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
public class BooleanNdFieldDelta extends NdFieldDelta {


	public final BooleanNdField field;

	public final boolean value;

	public BooleanNdFieldDelta(BooleanNdField field, boolean value) {
		super();
		this.field = field;
		this.value = value;
	}


	@Override
	public void consume(RepoS8Object object, BuildScope scope) throws NdIOException {
		field.handler.setBoolean(object, value);
	}


	@Override
	public void computeFootprint(MemoryFootprint weight) {
		weight.reportBytes(1);
	}
	
	
	@Override
	public NdField getField() { 
		return field;
	}

}
