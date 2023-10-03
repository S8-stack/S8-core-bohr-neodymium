package com.s8.io.bohr.neodymium.fields.objects;

import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Queue;

import com.s8.api.bohr.BOHR_Properties;
import com.s8.api.bohr.BOHR_Types;
import com.s8.api.bytes.ByteInflow;
import com.s8.api.bytes.ByteOutflow;
import com.s8.api.bytes.MemoryFootprint;
import com.s8.api.exceptions.S8IOException;
import com.s8.api.objects.annotations.S8Field;
import com.s8.api.objects.annotations.S8Getter;
import com.s8.api.objects.annotations.S8Setter;
import com.s8.api.objects.repo.RepoS8Object;
import com.s8.api.objects.serial.BohrSerializable;
import com.s8.io.bohr.atom.serial.BohrSerialUtilities;
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
public class S8SerializableNdField<T extends BohrSerializable> extends NdField {



	public final static NdFieldPrototype PROTOTYPE = new NdFieldPrototype() {


		@Override
		public NdFieldProperties captureField(Field field) throws NdBuildException {
			Class<?> fieldType = field.getType();
			if(BohrSerializable.class.isAssignableFrom(fieldType)){
				S8Field annotation = field.getAnnotation(S8Field.class);
				if(annotation != null) {
					NdFieldProperties properties = new NdFieldProperties(this, NdHandlerType.FIELD, fieldType);
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
				if(BohrSerializable.class.isAssignableFrom(baseType)) {
					NdFieldProperties properties = new NdFieldProperties(this, NdHandlerType.GETTER_SETTER_PAIR, baseType);
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
				if(BohrSerializable.class.isAssignableFrom(baseType)){
					NdFieldProperties properties = new NdFieldProperties(this, NdHandlerType.GETTER_SETTER_PAIR, baseType);
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
			return new S8SerializableNdField<>(ordinal, properties, handler);
		}
	}



	private BohrSerializable.BohrSerialPrototype<T> deserializer;



	/**
	 * 
	 * @param properties
	 * @param handler
	 * @throws NdBuildException 
	 */
	public S8SerializableNdField(int ordinal, NdFieldProperties properties, NdHandler handler) throws NdBuildException {
		super(ordinal, properties, handler);
		Class<?> baseType = properties.getBaseType();
		try {
			deserializer = BohrSerialUtilities.getDeserializer(baseType);
		} 
		catch (S8IOException e) {
			e.printStackTrace();
			throw new NdBuildException("Failed to build the S8Serizalizable GphField due to  "+e.getMessage() , baseType);
		}
	}





	@Override
	public void sweep(RepoS8Object object, GraphCrawler crawler) {
		// no sweep
	}


	@Override
	public void collectReferencedBlocks(RepoS8Object object, Queue<String> references) {
		// No ext references
	}





	@Override
	public void computeFootprint(RepoS8Object object, MemoryFootprint weight) throws NdIOException {
		BohrSerializable value = (BohrSerializable) handler.get(object);
		if(value!=null) {
			weight.reportInstance();
			weight.reportBytes(value.computeFootprint());	
		}
	}

	@Override
	public void deepClone(RepoS8Object origin, RepoS8Object clone, BuildScope scope) throws NdIOException {
		BohrSerializable value = (BohrSerializable) handler.get(origin);
		handler.set(clone, value.deepClone());
	}


	@Override
	public void DEBUG_print(String indent) {
		System.out.println(indent+name+": (ByteSerializableFieldHandler)");
	}


	@Override
	public boolean hasDiff(RepoS8Object base, RepoS8Object update) throws NdIOException {
		
		@SuppressWarnings("unchecked")
		T left = (T) handler.get(base);
		
		@SuppressWarnings("unchecked")
		T right = (T) handler.get(update);
		
		if(left != null && right !=null) {
			return deserializer.hasDelta(left, right);
		}
		else if((left != null && right == null) || (left == null && right != null)) {
			return true;
		}
		else {
			return false;
		}
	}


	@Override
	public NdFieldDelta produceDiff(RepoS8Object object) throws NdIOException {
		return new S8SerializableNdFieldDelta<>(S8SerializableNdField.this, (BohrSerializable) handler.get(object));
	}


	@Override
	protected void printValue(RepoS8Object object, Writer writer) throws IOException {
		Object value = handler.get(object);
		if(value!=null) {
			writer.write("(");
			writer.write(value.getClass().getCanonicalName());
			writer.write("): ");
			writer.write(value.toString());	
		}
		else {
			writer.write("null");
		}
	}

	@Override
	public String printType() {
		return "S8Object";
	}



	@Override
	public boolean isValueResolved(RepoS8Object object) {
		return true; // always resolved at resolve step in shell
	}




	/* <IO-inflow-section> */

	@Override
	public NdFieldParser createParser(ByteInflow inflow) throws IOException {
		int code = inflow.getUInt8();
		if(code != BOHR_Types.SERIAL) {
			throw new IOException("only array accepted");
		}
		// in fine, create parser
		return new Parser();
	}


	private class Parser extends NdFieldParser {

		@Override
		public S8SerializableNdField<T> getField() {
			return S8SerializableNdField.this;
		}

		
		@Override
		public NdFieldDelta deserializeDelta(ByteInflow inflow) throws IOException {
			return new S8SerializableNdFieldDelta<>(S8SerializableNdField.this, deserialize(inflow));
		}

		private BohrSerializable deserialize(ByteInflow inflow) throws IOException {
			int props = inflow.getUInt8();
			if(isNonNull(props)) {
				return deserializer.deserialize(inflow);
			}
			else {
				return null;
			}
		}

	}

	/* </IO-inflow-section> */



	/* <IO-outflow-section> */

	@Override
	public NdFieldComposer createComposer(int code) throws NdIOException {
		switch(exportFormat) {

		case DEFAULT_FLOW_TAG: case "serial" : return new Outflow(code);

		default : throw new NdIOException("Impossible to match IO type for flow: "+exportFormat);
		}
	}


	private class Outflow extends NdFieldComposer {

		public Outflow(int code) { super(code); }

		@Override
		public NdField getField() {
			return S8SerializableNdField.this;
		}

		@Override
		public void publishFlowEncoding(ByteOutflow outflow) throws IOException {
			outflow.putUInt8(BOHR_Types.SERIAL);
		}

		@Override
		public void composeValue(RepoS8Object object, ByteOutflow outflow) throws IOException {
			BohrSerializable value = (BohrSerializable) handler.get(object);
			if(value != null) {
				outflow.putUInt8(BOHR_Properties.IS_NON_NULL_PROPERTIES_BIT);
				value.serialize(outflow);
			}
			else {
				outflow.putUInt8(0x00);
			}
		}

		@Override
		public void publishValue(NdFieldDelta delta, ByteOutflow outflow) throws IOException {
			@SuppressWarnings("unchecked")
			BohrSerializable value = ((S8SerializableNdFieldDelta<T>) delta).value;
			if(value != null) {
				outflow.putUInt8(BOHR_Properties.IS_NON_NULL_PROPERTIES_BIT);
				value.serialize(outflow);
			}
			else {
				outflow.putUInt8(0x00);
			}
		}
	}
	/* </IO-outflow-section> */


}
