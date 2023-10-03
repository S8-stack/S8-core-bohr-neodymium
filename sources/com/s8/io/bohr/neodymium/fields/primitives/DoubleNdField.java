package com.s8.io.bohr.neodymium.fields.primitives;

import java.io.IOException;
import java.io.Writer;

import com.s8.api.bohr.BOHR_Types;
import com.s8.api.bytes.ByteInflow;
import com.s8.api.bytes.ByteOutflow;
import com.s8.api.bytes.MemoryFootprint;
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
public class DoubleNdField extends PrimitiveNdField {


	public final static PrimitiveNdField.Prototype PROTOTYPE = new Prototype(double.class){

		@Override
		public PrimitiveNdField.Builder createFieldBuilder(NdFieldProperties properties, NdHandler handler) {
			return new DoubleNdField.Builder(properties, handler);
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
			return new DoubleNdField(ordinal, properties, handler);
		}		
	}

	/**
	 * 
	 * @param outboundTypeName
	 * @param handler
	 * @throws NdBuildException 
	 */
	public DoubleNdField(int ordinal, NdFieldProperties properties, NdHandler handler) throws NdBuildException{
		super(ordinal, properties, handler);
	}

	@Override
	public Prototype getPrototype() {
		return PROTOTYPE;
	}


	@Override
	public void computeFootprint(RepoS8Object object, MemoryFootprint weight) {
		weight.reportBytes(8);
	}


	@Override
	public void deepClone(RepoS8Object origin, RepoS8Object clone, BuildScope scope) throws NdIOException {
		double value = handler.getDouble(origin);
		handler.setDouble(clone, value);
	}


	@Override
	public boolean hasDiff(RepoS8Object base, RepoS8Object update) throws NdIOException {
		double baseValue = handler.getDouble(base);
		double updateValue = handler.getDouble(update);
		return baseValue != updateValue;
	}

	@Override
	public NdFieldDelta produceDiff(RepoS8Object object) throws NdIOException {
		return new DoubleNdFieldDelta(this, handler.getDouble(object));
	}


	@Override
	public void DEBUG_print(String indent) {
		System.out.println(indent+name+": (double)");
	}


	@Override
	protected void printValue(RepoS8Object object, Writer writer) throws IOException {
		writer.write(Double.toString(handler.getDouble(object)));
	}



	


	/* <IO-inflow-section> */


	@Override
	public NdFieldParser createParser(ByteInflow inflow) throws IOException {
		int code = inflow.getUInt8();
		switch(code) {

		case BOHR_Types.FLOAT32 : return new Float32Parser();
		case BOHR_Types.FLOAT64 : return new Float64Parser();

		default : throw new NdIOException("Failed to find field-inflow for code: "+Integer.toHexString(code));
		}
	}



	private abstract class BaseParser extends NdFieldParser {

		@Override
		public DoubleNdField getField() {
			return DoubleNdField.this;
		}

		@Override
		public NdFieldDelta deserializeDelta(ByteInflow inflow) throws IOException {
			return new DoubleNdFieldDelta(DoubleNdField.this, deserialize(inflow));
		}

		public abstract double deserialize(ByteInflow inflow) throws IOException;

	}

	private class Float32Parser extends BaseParser {
		public @Override double deserialize(ByteInflow inflow) throws IOException {
			return inflow.getFloat32();
		}
	}

	private class Float64Parser extends BaseParser {
		public @Override double deserialize(ByteInflow inflow) throws IOException {
			return inflow.getFloat64();
		}
	}
	/* </IO-inflow-section> */


	/* <IO-outflow-section> */

	@Override
	public NdFieldComposer createComposer(int code) throws NdIOException {
		switch(exportFormat) {

		case "float32" : return new Float32Composer(code);
		case DEFAULT_FLOW_TAG: case "float64" : return new Float64Composer(code);

		default : throw new NdIOException("Failed to find field-outflow for encoding: "+exportFormat);
		}
	}


	private abstract class BaseComposer extends NdFieldComposer {

		public BaseComposer(int code) {
			super(code);
		}


		@Override
		public DoubleNdField getField() {
			return DoubleNdField.this;
		}


		@Override
		public void composeValue(RepoS8Object object, ByteOutflow outflow) throws IOException {
			serialize(outflow, handler.getDouble(object));
		}
		
		@Override
		public void publishValue(NdFieldDelta delta, ByteOutflow outflow) throws IOException {
			serialize(outflow, ((DoubleNdFieldDelta) delta).value);
		}

		public abstract void serialize(ByteOutflow outflow, double value) throws IOException;
	}


	private class Float32Composer extends BaseComposer {
		public Float32Composer(int code) { super(code); }
		public @Override void publishFlowEncoding(ByteOutflow outflow) throws IOException {
			outflow.putUInt8(BOHR_Types.FLOAT32);
		}
		public @Override void serialize(ByteOutflow outflow, double value) throws IOException {
			outflow.putFloat32((float) value);
		}
	}

	private class Float64Composer extends BaseComposer {
		public Float64Composer(int code) { super(code); }
		public @Override void publishFlowEncoding(ByteOutflow outflow) throws IOException {
			outflow.putUInt8(BOHR_Types.FLOAT64);
		}
		public @Override void serialize(ByteOutflow outflow, double value) throws IOException {
			outflow.putFloat64(value);
		}
	}

	/* <IO-outflow-section> */

}
