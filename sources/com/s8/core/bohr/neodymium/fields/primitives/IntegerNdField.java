package com.s8.core.bohr.neodymium.fields.primitives;

import java.io.IOException;
import java.io.Writer;

import com.s8.api.bytes.ByteInflow;
import com.s8.api.bytes.ByteOutflow;
import com.s8.api.bytes.MemoryFootprint;
import com.s8.api.flow.repository.objects.RepoS8Object;
import com.s8.core.bohr.atom.protocol.BOHR_Types;
import com.s8.core.bohr.neodymium.exceptions.NdBuildException;
import com.s8.core.bohr.neodymium.exceptions.NdIOException;
import com.s8.core.bohr.neodymium.fields.NdField;
import com.s8.core.bohr.neodymium.fields.NdFieldComposer;
import com.s8.core.bohr.neodymium.fields.NdFieldDelta;
import com.s8.core.bohr.neodymium.fields.NdFieldParser;
import com.s8.core.bohr.neodymium.fields.NdFieldPrototype;
import com.s8.core.bohr.neodymium.handlers.NdHandler;
import com.s8.core.bohr.neodymium.properties.NdFieldProperties;
import com.s8.core.bohr.neodymium.type.BuildScope;

/**
 * 
 *
 * @author Pierre Convert
 * Copyright (C) 2022, Pierre Convert. All rights reserved.
 * 
 */
public class IntegerNdField extends PrimitiveNdField {

	public final static PrimitiveNdField.Prototype PROTOTYPE = new Prototype(int.class){

		@Override
		public PrimitiveNdField.Builder createFieldBuilder(NdFieldProperties properties, NdHandler handler) {
			return new IntegerNdField.Builder(properties, handler);
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
			return new IntegerNdField(ordinal, properties, handler);
		}		
	}

	/**
	 * 
	 * @param outboundTypeName
	 * @param handler
	 * @throws NdBuildException 
	 */
	public IntegerNdField(int ordinal, NdFieldProperties properties, NdHandler handler) throws NdBuildException{
		super(ordinal, properties, handler);
	}

	@Override
	public Prototype getPrototype() {
		return PROTOTYPE;
	}

	@Override
	public void computeFootprint(RepoS8Object object, MemoryFootprint weight) {
		weight.reportBytes(4);
	}


	@Override
	public void deepClone(RepoS8Object origin, RepoS8Object clone, BuildScope scope) throws NdIOException {
		int value = handler.getInteger(origin);
		handler.setInteger(clone, value);
	}

	@Override
	public boolean hasDiff(RepoS8Object base, RepoS8Object update) throws NdIOException {
		int baseValue = handler.getInteger(base);
		int updateValue = handler.getInteger(update);
		return baseValue != updateValue;
	}

	@Override
	public NdFieldDelta produceDiff(RepoS8Object object) throws NdIOException {
		return new IntegerNdFieldDelta(this, handler.getInteger(object));
	}


	@Override
	public void DEBUG_print(String indent) {
		System.out.println(indent+name+": (long)");
	}



	@Override
	protected void printValue(RepoS8Object object, Writer writer) throws IOException {
		writer.write(Integer.toString(handler.getInteger(object)));
	}


	/* <IO-inflow-section> */


	@Override
	public NdFieldParser createParser(ByteInflow inflow) throws IOException {
		int code = inflow.getUInt8();
		switch(code) {

		case BOHR_Types.UINT8 : return new UInt8Parser();
		case BOHR_Types.UINT16 : return new UInt16Parser();
		case BOHR_Types.UINT32 : return new UInt32Parser();

		case BOHR_Types.INT8 : return new Int8Parser();
		case BOHR_Types.INT16 : return new Int16Parser();
		case BOHR_Types.INT32 : return new Int32Parser();

		default : throw new NdIOException("Failed to find field-inflow for code: "+Integer.toHexString(code));
		}
	}



	private abstract class BaseParser extends NdFieldParser {

		@Override
		public IntegerNdField getField() {
			return IntegerNdField.this;
		}

		@Override
		public NdFieldDelta deserializeDelta(ByteInflow inflow) throws IOException {
			return new IntegerNdFieldDelta(IntegerNdField.this, deserialize(inflow));
		}

		public abstract int deserialize(ByteInflow inflow) throws IOException;

	}

	private class UInt8Parser extends BaseParser {
		public @Override int deserialize(ByteInflow inflow) throws IOException {
			return (int) inflow.getUInt8();
		}
	}

	private class UInt16Parser extends BaseParser {
		public @Override int deserialize(ByteInflow inflow) throws IOException {
			return inflow.getUInt16();
		}
	}

	private class UInt32Parser extends BaseParser {
		public @Override int deserialize(ByteInflow inflow) throws IOException {
			return inflow.getUInt8();
		}
	}

	private class Int8Parser extends BaseParser {
		public @Override int deserialize(ByteInflow inflow) throws IOException {
			return inflow.getInt8();
		}
	}

	private class Int16Parser extends BaseParser {
		public @Override int deserialize(ByteInflow inflow) throws IOException {
			return inflow.getInt16();
		}
	}

	private class Int32Parser extends BaseParser {
		public @Override int deserialize(ByteInflow inflow) throws IOException {
			return inflow.getInt32();
		}
	}

	/* </IO-inflow-section> */


	/* <IO-outflow-section> */

	@Override
	public NdFieldComposer createComposer(int code) throws NdIOException {
		switch(exportFormat) {

		case "uint8" : return new UInt8Composer(code);
		case "uint16" : return new UInt16Composer(code);
		case "uint32" : return new UInt32Composer(code);

		case "int8" : return new Int8Composer(code);
		case "int16" : return new Int16Composer(code);
		case DEFAULT_FLOW_TAG: case "int32" : return new Int32Composer(code);

		default : throw new NdIOException("Failed to find field-outflow for encoding: "+exportFormat);
		}
	}


	private abstract class BaseComposer extends NdFieldComposer {

		public BaseComposer(int code) {
			super(code);
		}


		@Override
		public IntegerNdField getField() {
			return IntegerNdField.this;
		}


		@Override
		public void composeValue(RepoS8Object object, ByteOutflow outflow) throws IOException {
			serialize(outflow, handler.getInteger(object));
		}

		@Override
		public void publishValue(NdFieldDelta delta, ByteOutflow outflow) throws IOException {
			serialize(outflow, ((IntegerNdFieldDelta) delta).value);
		}

		public abstract void serialize(ByteOutflow outflow, int value) throws IOException;
	}


	private class UInt8Composer extends BaseComposer {
		public UInt8Composer(int code) { super(code); }
		public @Override void publishFlowEncoding(ByteOutflow outflow) throws IOException {
			outflow.putUInt8(BOHR_Types.UINT8);
		}
		public @Override void serialize(ByteOutflow outflow, int value) throws IOException {
			outflow.putUInt8(value);
		}
	}

	private class UInt16Composer extends BaseComposer {
		public UInt16Composer(int code) { super(code); }
		public @Override void publishFlowEncoding(ByteOutflow outflow) throws IOException {
			outflow.putUInt8(BOHR_Types.UINT16);
		}
		public @Override void serialize(ByteOutflow outflow, int value) throws IOException {
			outflow.putUInt16(value);
		}
	}

	private class UInt32Composer extends BaseComposer {
		public UInt32Composer(int code) { super(code); }
		public @Override void publishFlowEncoding(ByteOutflow outflow) throws IOException {
			outflow.putUInt8(BOHR_Types.UINT32);
		}
		public @Override void serialize(ByteOutflow outflow, int value) throws IOException {
			outflow.putUInt32(value);
		}
	}


	private class Int8Composer extends BaseComposer {
		public Int8Composer(int code) { super(code); }
		public @Override void publishFlowEncoding(ByteOutflow outflow) throws IOException {
			outflow.putUInt8(BOHR_Types.INT8);
		}
		public @Override void serialize(ByteOutflow outflow, int value) throws IOException {
			outflow.putInt8((byte) value);
		}
	}

	private class Int16Composer extends BaseComposer {
		public Int16Composer(int code) { super(code); }
		public @Override void publishFlowEncoding(ByteOutflow outflow) throws IOException {
			outflow.putUInt8(BOHR_Types.INT16);
		}
		public @Override void serialize(ByteOutflow outflow, int value) throws IOException {
			outflow.putInt16((short) value);
		}
	}

	private class Int32Composer extends BaseComposer {
		
		public Int32Composer(int code) { super(code); }
		
		public @Override void publishFlowEncoding(ByteOutflow outflow) throws IOException {
			outflow.putUInt8(BOHR_Types.INT32);
		}
		
		public @Override void serialize(ByteOutflow outflow, int value) throws IOException {
			outflow.putInt32(value);
		}
	}	
	/* <IO-outflow-section> */

}
