package com.s8.io.bohr.neodymium.fields.primitives;

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
public class IntegerNdFieldDelta  extends NdFieldDelta {

	public final IntegerNdField field;
	public final int value;

	public IntegerNdFieldDelta(IntegerNdField field, int value) {
		super();
		this.field = field;
		this.value = value;
	}


	@Override
	public NdField getField() { 
		return field; 
	}

	@Override
	public void consume(RepoS8Object object, BuildScope scope) throws NdIOException {
		field.handler.setInteger(object, value);
	}

	@Override
	public void computeFootprint(MemoryFootprint weight) {
		weight.reportBytes(4);
	}
}
