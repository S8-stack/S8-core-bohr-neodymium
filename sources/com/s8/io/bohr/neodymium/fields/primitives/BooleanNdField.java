package com.s8.io.bohr.neodymium.fields.primitives;

import java.io.IOException;
import java.io.Writer;

import com.s8.api.bohr.BOHR_Types;
import com.s8.api.bytes.ByteInflow;
import com.s8.api.bytes.ByteOutflow;
import com.s8.api.bytes.MemoryFootprint;
import com.s8.api.exceptions.S8BuildException;
import com.s8.api.objects.repo.RepoS8Object;
import com.s8.io.bohr.neodymium.exceptions.NdBuildException;
import com.s8.io.bohr.neodymium.exceptions.NdIOException;
import com.s8.io.bohr.neodymium.fields.NdField;
import com.s8.io.bohr.neodymium.fields.NdFieldComposer;
import com.s8.io.bohr.neodymium.fields.NdFieldDelta;
import com.s8.io.bohr.neodymium.fields.NdFieldParser;
import com.s8.io.bohr.neodymium.fields.NdFieldPrototype;
import com.s8.io.bohr.neodymium.handlers.NdHandler;
import com.s8.io.bohr.neodymium.properties.NdFieldProperties;
import com.s8.io.bohr.neodymium.type.BuildScope;

/**
 * 
 *
 * @author Pierre Convert
 * Copyright (C) 2022, Pierre Convert. All rights reserved.
 * 
 */
public class BooleanNdField extends PrimitiveNdField {

	public final static PrimitiveNdField.Prototype PROTOTYPE = new Prototype(boolean.class){

		@Override
		public PrimitiveNdField.Builder createFieldBuilder(NdFieldProperties properties, NdHandler handler) {
			return new BooleanNdField.Builder(properties, handler);
		}
	};


	private static class Builder extends PrimitiveNdField.Builder {

		public Builder(NdFieldProperties properties, NdHandler handler) {
			super(properties, handler);
		}

		@Override
		public NdFieldPrototype getPrototype() {
			return PROTOTYPE;
		}

		@Override
		public NdField build(int ordinal) throws NdBuildException {
			return new BooleanNdField(ordinal, properties, handler);
		}
	}



	/**
	 * 
	 * @param outboundTypeName
	 * @param handler
	 * @throws S8BuildException 
	 */
	public BooleanNdField(int ordinal, NdFieldProperties properties, NdHandler handler) throws NdBuildException {
		super(ordinal, properties, handler);
	}

	@Override
	public Prototype getPrototype() {
		return PROTOTYPE;
	}


	@Override
	public void computeFootprint(RepoS8Object object, MemoryFootprint weight) {
		weight.reportBytes(1);
	}

	@Override
	public void deepClone(RepoS8Object origin, RepoS8Object clone, BuildScope scope) throws NdIOException {
		boolean value = handler.getBoolean(origin);
		handler.setBoolean(clone, value);
	}

	@Override
	public void DEBUG_print(String indent) {
		System.out.println(indent + name + ": (boolean)");
	}


	@Override
	public boolean hasDiff(RepoS8Object base, RepoS8Object update) throws NdIOException {
		boolean baseValue = handler.getBoolean(base);
		boolean updateValue = handler.getBoolean(update);
		return baseValue != updateValue;
	}


	@Override
	public NdFieldDelta produceDiff(RepoS8Object object) throws NdIOException {
		return new BooleanNdFieldDelta(this, handler.getBoolean(object));
	}


	@Override
	protected void printValue(RepoS8Object object, Writer writer) throws IOException {
		writer.write(Boolean.toString(handler.getBoolean(object)));
	}

	

	



	/* <IO-inflow-section> */


	@Override
	public NdFieldParser createParser(ByteInflow inflow) throws IOException {
		int code = inflow.getUInt8();
		switch(code) {

		case BOHR_Types.BOOL8 : return new Bool8_Inflow();

		default : throw new NdIOException("Failed to find field-inflow for code: "+Integer.toHexString(code));
		}
	}



	private class Bool8_Inflow extends NdFieldParser {

		@Override
		public BooleanNdField getField() {
			return BooleanNdField.this;
		}

		@Override
		public NdFieldDelta deserializeDelta(ByteInflow inflow) throws IOException {
			return new BooleanNdFieldDelta(BooleanNdField.this, inflow.getBool8());
		}
	}

	/* </IO-inflow-section> */


	/* <IO-outflow-section> */



	@Override
	public NdFieldComposer createComposer(int code) throws NdIOException {
		switch(exportFormat) {
		case DEFAULT_FLOW_TAG: case "bool8" : return new Bool8Composer(code);
		default : throw new NdIOException("Failed to find field-outflow for encoding: "+exportFormat);
		}	
	}
	
	
	private class Bool8Composer extends NdFieldComposer {

		public Bool8Composer(int code) { super(code); }

		@Override
		public BooleanNdField getField() {
			return BooleanNdField.this;
		}

		@Override
		public void publishFlowEncoding(ByteOutflow outflow) throws IOException {
			outflow.putUInt8(BOHR_Types.BOOL8);
		}

		@Override
		public void composeValue(RepoS8Object object, ByteOutflow outflow) throws IOException {
			outflow.putBool8(handler.getBoolean(object));
		}
		
		@Override
		public void publishValue(NdFieldDelta delta, ByteOutflow outflow) throws IOException {
			outflow.putBool8(((BooleanNdFieldDelta) delta).value);
		}
	}

	/* <IO-outflow-section> */	


}
