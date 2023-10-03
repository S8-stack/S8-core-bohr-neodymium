package com.s8.io.bohr.neodymium.fields.objects;

import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Queue;

import com.s8.api.bohr.BOHR_Types;
import com.s8.api.bytes.ByteInflow;
import com.s8.api.bytes.ByteOutflow;
import com.s8.api.bytes.MemoryFootprint;
import com.s8.api.objects.annotations.S8Field;
import com.s8.api.objects.annotations.S8Getter;
import com.s8.api.objects.annotations.S8Setter;
import com.s8.api.objects.repo.RepoS8Object;
import com.s8.io.bohr.neodymium.exceptions.NdBuildException;
import com.s8.io.bohr.neodymium.exceptions.NdIOException;
import com.s8.io.bohr.neodymium.fields.NdField;
import com.s8.io.bohr.neodymium.fields.NdFieldBuilder;
import com.s8.io.bohr.neodymium.fields.NdFieldComposer;
import com.s8.io.bohr.neodymium.fields.NdFieldDelta;
import com.s8.io.bohr.neodymium.fields.NdFieldParser;
import com.s8.io.bohr.neodymium.fields.NdFieldPrototype;
import com.s8.io.bohr.neodymium.handlers.NdHandler;
import com.s8.io.bohr.neodymium.handlers.NdHandlerType;
import com.s8.io.bohr.neodymium.properties.NdFieldProperties;
import com.s8.io.bohr.neodymium.type.BuildScope;
import com.s8.io.bohr.neodymium.type.GraphCrawler;


/**
 * 
 *
 * @author Pierre Convert
 * Copyright (C) 2022, Pierre Convert. All rights reserved.
 * 
 */
public class EnumNdField extends NdField {



	public final static NdFieldPrototype PROTOTYPE = new NdFieldPrototype() {


		@Override
		public NdFieldProperties captureField(Field field) throws NdBuildException {
			Class<?> baseType = field.getType();
			if(baseType.isEnum()) {
				S8Field annotation = field.getAnnotation(S8Field.class);
				if(annotation != null) {
					NdFieldProperties properties = new NdFieldProperties(this, NdHandlerType.FIELD, baseType);
					properties.setFieldAnnotation(annotation);
					return properties;	
				}
				else { return null; }
			}
			else { return null; }
		}


		@Override
		public NdFieldProperties captureSetter(Method method) throws NdBuildException {
			Class<?> baseType = method.getParameterTypes()[0];
			S8Setter annotation = method.getAnnotation(S8Setter.class);
			if(annotation != null) {
				if(baseType.isEnum()) {
					NdFieldProperties properties = 
							new NdFieldProperties(this, NdHandlerType.GETTER_SETTER_PAIR, baseType);
					properties.setSetterAnnotation(annotation);
					return properties;
				}
				else {
					throw new NdBuildException("S8Annotated field of type List must have its "
							+"parameterized type inheriting from S8Object", method);
				}
			}
			else { return null; }
		}

		@Override
		public NdFieldProperties captureGetter(Method method) throws NdBuildException {
			Class<?> baseType = method.getReturnType();

			S8Getter annotation = method.getAnnotation(S8Getter.class);
			if(annotation != null) {
				if(baseType.isEnum()) {
					NdFieldProperties properties = 
							new NdFieldProperties(this, NdHandlerType.GETTER_SETTER_PAIR, baseType);
					properties.setGetterAnnotation(annotation);
					return properties;
				}
				else {
					throw new NdBuildException("S8Annotated field of type List must have its "
							+"parameterized type inheriting from S8Object", method);

				}
			}
			else { return null; }
		}


		@Override
		public NdFieldBuilder createFieldBuilder(NdFieldProperties properties, NdHandler handler) {
			return new Builder(properties, handler);
		}
	};



	private static class Builder extends NdFieldBuilder {

		public Builder(NdFieldProperties properties, NdHandler handler) {
			super(properties, handler);
		}

		@Override
		public NdFieldPrototype getPrototype() {
			return PROTOTYPE;
		}

		@Override
		public NdField build(int ordinal) throws NdBuildException {
			return new EnumNdField(ordinal, properties, handler);
		}
	}



	private Class<?> enumType;

	private Object[] values;


	/**
	 * 
	 * @param ordinal
	 * @param properties
	 * @param handler
	 * @throws NdIOException
	 */
	public EnumNdField(int ordinal, NdFieldProperties properties, NdHandler handler) {
		super(ordinal, properties, handler);
		this.enumType = properties.getBaseType();
		this.values = enumType.getEnumConstants();
	}


	@Override
	public void computeFootprint(RepoS8Object object, MemoryFootprint weight) {
		weight.reportInstance();
		weight.reportBytes(4); // int ordinal
	}


	@Override
	public void collectReferencedBlocks(RepoS8Object object, Queue<String> references) {
		//no blocks to collect
	}


	@Override
	public void sweep(RepoS8Object object, GraphCrawler crawler) {
		// nothing to collect
	}


	@Override
	public void DEBUG_print(String indent) {
		System.out.println(indent+name+": ("+enumType.getName()+")");
	}



	@Override
	public void deepClone(RepoS8Object origin, RepoS8Object clone, BuildScope scope) throws NdIOException {
		Object value = handler.get(origin);
		handler.set(clone, value);
	}


	@Override
	public boolean hasDiff(RepoS8Object base, RepoS8Object update) throws NdIOException {
		Object baseValue = handler.get(base);
		Object updateValue = handler.get(update);
		return (baseValue!=null && !baseValue.equals(updateValue)) 
				|| (baseValue==null && updateValue!=null);
	}


	@Override
	public NdFieldDelta produceDiff(RepoS8Object object) throws NdIOException {
		return new EnumNdFieldDelta(this, handler.get(object));
	}

	@Override
	protected void printValue(RepoS8Object object, Writer writer) throws IOException {
		Object value = handler.get(object);
		if(value!=null) {
			Enum<?> enumValue = (Enum<?>) value;
			writer.write(enumValue.name());	
		}
		else {
			writer.write("null");
		}
	}


	@Override
	public String printType() {
		return enumType.getCanonicalName();
	}


	@Override
	public boolean isValueResolved(RepoS8Object object) {
		return true; // always resolved
	}



	/* <IO-inflow-section> */

	@Override
	public NdFieldParser createParser(ByteInflow inflow) throws IOException {
		int code = inflow.getUInt8();
		switch(code) {
		case BOHR_Types.UINT8 : return new UInt8Parser();
		case BOHR_Types.UINT16 : return new UInt16Parser();
		case BOHR_Types.UINT32 : return new UInt32Parser();
		default : throw new NdIOException("Failed to find field-inflow for code: "+Integer.toHexString(code));
		}
	}


	private abstract class BaseParser extends NdFieldParser {

		@Override
		public EnumNdField getField() {
			return EnumNdField.this;
		}

		@Override
		public NdFieldDelta deserializeDelta(ByteInflow inflow) throws IOException {
			return new EnumNdFieldDelta(EnumNdField.this, deserialize(inflow));
		}

		public abstract Object deserialize(ByteInflow inflow) throws IOException;

	}

	private class UInt8Parser extends BaseParser {
		public @Override Object deserialize(ByteInflow inflow) throws IOException {
			return values[(int) inflow.getUInt8()];
		}
	}

	private class UInt16Parser extends BaseParser {
		public @Override Object deserialize(ByteInflow inflow) throws IOException {
			return values[inflow.getUInt16()];
		}
	}

	private class UInt32Parser extends BaseParser {
		public @Override Object deserialize(ByteInflow inflow) throws IOException {
			return values[inflow.getUInt32()];
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
		case DEFAULT_FLOW_TAG: 
			if(values.length<=0xff) {
				return new UInt8Composer(code);
			}
			else if(values.length<=0xffff) {
				return new UInt16Composer(code);
			}
			else {
				return new UInt32Composer(code);
			}
		default : throw new NdIOException("Failed to find field-outflow for encoding: "+exportFormat);
		}
	}


	private abstract class BaseComposer extends NdFieldComposer {

		public BaseComposer(int code) {
			super(code);
		}


		@Override
		public EnumNdField getField() {
			return EnumNdField.this;
		}


		@Override
		public void composeValue(RepoS8Object object, ByteOutflow outflow) throws IOException {
			serialize(outflow, handler.get(object));
		}
		
		@Override
		public void publishValue(NdFieldDelta delta, ByteOutflow outflow) throws IOException {
			serialize(outflow, ((EnumNdFieldDelta) delta).value);
		}

		public abstract void serialize(ByteOutflow outflow, Object value) throws IOException;
	}


	private class UInt8Composer extends BaseComposer {
		public UInt8Composer(int code) {
			super(code);
		}
		public @Override void publishFlowEncoding(ByteOutflow outflow) throws IOException {
			outflow.putUInt8(BOHR_Types.UINT8);
		}
		public @Override void serialize(ByteOutflow outflow, Object value) throws IOException {
			int code = ((Enum<?>) value).ordinal();
			outflow.putUInt8(code);
		}
	}

	private class UInt16Composer extends BaseComposer {
		public UInt16Composer(int code) {
			super(code);
		}
		public @Override void publishFlowEncoding(ByteOutflow outflow) throws IOException {
			outflow.putUInt8(BOHR_Types.UINT16);
		}
		public @Override void serialize(ByteOutflow outflow, Object value) throws IOException {
			int code = ((Enum<?>) value).ordinal();
			outflow.putUInt16(code);
		}
	}

	private class UInt32Composer extends BaseComposer {
		public UInt32Composer(int code) {
			super(code);
		}
		public @Override void publishFlowEncoding(ByteOutflow outflow) throws IOException {
			outflow.putUInt8(BOHR_Types.UINT32);
		}
		public @Override void serialize(ByteOutflow outflow, Object value) throws IOException {
			int code = ((Enum<?>) value).ordinal();
			outflow.putUInt32(code);
		}
	}

	/* <IO-outflow-section> */
}
