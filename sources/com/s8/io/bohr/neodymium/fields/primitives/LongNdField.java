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
public class LongNdField extends PrimitiveNdField {


	public final static PrimitiveNdField.Prototype PROTOTYPE = new Prototype(long.class){

		@Override
		public PrimitiveNdField.Builder createFieldBuilder(NdFieldProperties properties, NdHandler handler) {
			return new LongNdField.Builder(properties, handler);
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
			return new LongNdField(ordinal, properties, handler);
		}		
	}

	
	/**
	 * 
	 * @param outboundTypeName
	 * @param handler
	 * @throws NdBuildException 
	 */
	public LongNdField(int ordinal, NdFieldProperties properties, NdHandler handler) throws NdBuildException{
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
		long value = handler.getLong(origin);
		handler.setLong(clone, value);
	}

	@Override
	public boolean hasDiff(RepoS8Object base, RepoS8Object update) throws NdIOException {
		long baseValue = handler.getLong(base);
		long updateValue = handler.getLong(update);
		return baseValue != updateValue;
	}

	@Override
	public NdFieldDelta produceDiff(RepoS8Object object) throws NdIOException {
		return new LongNdFieldDelta(this, handler.getLong(object));
	}


	@Override
	public void DEBUG_print(String indent) {
		System.out.println(indent+name+": (long)");
	}



	@Override
	protected void printValue(RepoS8Object object, Writer writer) throws IOException {
		writer.write(Long.toString(handler.getLong(object)));
	}




	/* <IO-inflow-section> */


	@Override
	public NdFieldParser createParser(ByteInflow inflow) throws IOException {
		int code = inflow.getUInt8();
		switch(code) {

		case BOHR_Types.UINT8 : return new UInt8Parser();
		case BOHR_Types.UINT16 : return new UInt16Parser();
		case BOHR_Types.UINT32 : return new UInt32Parser();
		case BOHR_Types.UINT64 : return new UInt64Parser();

		case BOHR_Types.INT8 : return new IntParser();
		case BOHR_Types.INT16 : return new Int16Parser();
		case BOHR_Types.INT32 : return new Int32Parser();
		case BOHR_Types.INT64 : return new Int64Parser();

		default : throw new NdIOException("Failed to find field-inflow for code: "+Integer.toHexString(code));
		}
	}



	private abstract class BaseParser extends NdFieldParser {

		@Override
		public LongNdField getField() {
			return LongNdField.this;
		}

		@Override
		public NdFieldDelta deserializeDelta(ByteInflow inflow) throws IOException {
			return new LongNdFieldDelta(LongNdField.this, deserialize(inflow));
		}

		public abstract long deserialize(ByteInflow inflow) throws IOException;

	}

	private class UInt8Parser extends BaseParser {
		public @Override long deserialize(ByteInflow inflow) throws IOException {
			return (long) inflow.getUInt8();
		}
	}

	private class UInt16Parser extends BaseParser {
		public @Override long deserialize(ByteInflow inflow) throws IOException {
			return (long) inflow.getUInt16();
		}
	}
	private class UInt32Parser extends BaseParser {
		public @Override long deserialize(ByteInflow inflow) throws IOException {
			return (long) inflow.getUInt8();
		}
	}

	private class UInt64Parser extends BaseParser {
		public @Override long deserialize(ByteInflow inflow) throws IOException {
			return (long) inflow.getUInt16();
		}
	}

	private class IntParser extends BaseParser {
		public @Override long deserialize(ByteInflow inflow) throws IOException {
			return (long) inflow.getInt8();
		}
	}

	private class Int16Parser extends BaseParser {
		public @Override long deserialize(ByteInflow inflow) throws IOException {
			return (long) inflow.getInt16();
		}
	}
	private class Int32Parser extends BaseParser {
		public @Override long deserialize(ByteInflow inflow) throws IOException {
			return (long) inflow.getInt8();
		}
	}

	private class Int64Parser extends BaseParser {
		public @Override long deserialize(ByteInflow inflow) throws IOException {
			return (long) inflow.getInt16();
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
		case "uint64" : return new UInt64Composer(code);

		case "int8" : return new Int8Composer(code);
		case "int16" : return new Int16Composer(code);
		case "int32" : return new Int32Composer(code);
		case DEFAULT_FLOW_TAG: case "int64" : return new Int64Composer(code);

		default : throw new NdIOException("Failed to find field-outflow for encoding: "+exportFormat);
		}
	}


	private abstract class BaseComposer extends NdFieldComposer {
		public BaseComposer(int code) { super(code); }

		@Override
		public LongNdField getField() {
			return LongNdField.this;
		}


		@Override
		public void composeValue(RepoS8Object object, ByteOutflow outflow) throws IOException {
			serialize(outflow, handler.getLong(object));
		}
		
		@Override
		public void publishValue(NdFieldDelta delta, ByteOutflow outflow) throws IOException {
			serialize(outflow, ((LongNdFieldDelta) delta).value);
		}

		public abstract void serialize(ByteOutflow outflow, long value) throws IOException;
	}


	private class UInt8Composer extends BaseComposer {
		public UInt8Composer(int code) { super(code); }
		public @Override void publishFlowEncoding(ByteOutflow outflow) throws IOException {
			outflow.putUInt8(BOHR_Types.UINT8);
		}
		public @Override void serialize(ByteOutflow outflow, long value) throws IOException {
			outflow.putUInt8((int) value);
		}
	}

	private class UInt16Composer extends BaseComposer {
		public UInt16Composer(int code) { super(code); }
		public @Override void publishFlowEncoding(ByteOutflow outflow) throws IOException {
			outflow.putUInt8(BOHR_Types.UINT16);
		}
		public @Override void serialize(ByteOutflow outflow, long value) throws IOException {
			outflow.putUInt16((int) value);
		}
	}

	private class UInt32Composer extends BaseComposer {
		public UInt32Composer(int code) { super(code); }
		public @Override void publishFlowEncoding(ByteOutflow outflow) throws IOException {
			outflow.putUInt8(BOHR_Types.UINT32);
		}
		public @Override void serialize(ByteOutflow outflow, long value) throws IOException {
			outflow.putUInt32((int) value);
		}
	}

	private class UInt64Composer extends BaseComposer {
		public UInt64Composer(int code) { super(code); }
		public @Override void publishFlowEncoding(ByteOutflow outflow) throws IOException {
			outflow.putUInt8(BOHR_Types.UINT64);
		}
		public @Override void serialize(ByteOutflow outflow, long value) throws IOException {
			outflow.putUInt64((int) value);
		}
	}

	private class Int8Composer extends BaseComposer {
		public Int8Composer(int code) { super(code); }
		public @Override void publishFlowEncoding(ByteOutflow outflow) throws IOException {
			outflow.putUInt8(BOHR_Types.INT8);
		}
		public @Override void serialize(ByteOutflow outflow, long value) throws IOException {
			outflow.putInt8((byte) value);
		}
	}

	private class Int16Composer extends BaseComposer {
		public Int16Composer(int code) { super(code); }
		public @Override void publishFlowEncoding(ByteOutflow outflow) throws IOException {
			outflow.putUInt8(BOHR_Types.INT16);
		}
		public @Override void serialize(ByteOutflow outflow, long value) throws IOException {
			outflow.putInt16((short) value);
		}
	}

	private class Int32Composer extends BaseComposer {
		public Int32Composer(int code) { super(code); }
		public @Override void publishFlowEncoding(ByteOutflow outflow) throws IOException {
			outflow.putUInt8(BOHR_Types.INT32);
		}
		public @Override void serialize(ByteOutflow outflow, long value) throws IOException {
			outflow.putInt32((int) value);
		}
	}

	private class Int64Composer extends BaseComposer {
		public Int64Composer(int code) { super(code); }
		public @Override void publishFlowEncoding(ByteOutflow outflow) throws IOException {
			outflow.putUInt8(BOHR_Types.INT64);
		}
		public @Override void serialize(ByteOutflow outflow, long value) throws IOException {
			outflow.putInt64((int) value);
		}
	}

	/* <IO-outflow-section> */

}
