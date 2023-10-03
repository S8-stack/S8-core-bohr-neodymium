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
public class InterfaceNdField extends NdField {



	public final static NdFieldPrototype PROTOTYPE = new NdFieldPrototype() {


		@Override
		public NdFieldProperties captureField(Field field) throws NdBuildException {
			Class<?> fieldType = field.getType();
			S8Field annotation = field.getAnnotation(S8Field.class);
			if(annotation != null) {
				NdFieldProperties properties = new NdFieldProperties(this, NdHandlerType.FIELD, fieldType);
				properties.setFieldAnnotation(annotation);
				return properties;	
			}
			else { return null; }
		}


		@Override
		public NdFieldProperties captureSetter(Method method) throws NdBuildException {
			Class<?> baseType = method.getParameterTypes()[0];
			S8Setter annotation = method.getAnnotation(S8Setter.class);
			if(annotation != null) {
				NdFieldProperties properties = new NdFieldProperties(this, NdHandlerType.GETTER_SETTER_PAIR, baseType);
				properties.setSetterAnnotation(annotation);
				return properties;
			}
			else { return null; }
		}

		@Override
		public NdFieldProperties captureGetter(Method method) throws NdBuildException {
			Class<?> baseType = method.getReturnType();

			S8Getter annotation = method.getAnnotation(S8Getter.class);
			if(annotation != null) {

				NdFieldProperties properties = new NdFieldProperties(this, NdHandlerType.GETTER_SETTER_PAIR, baseType);
				properties.setGetterAnnotation(annotation);
				return properties;

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
		public NdField build(int ordinal) {
			return new InterfaceNdField(ordinal, properties, handler);
		}
	}


	public InterfaceNdField(int ordinal, NdFieldProperties properties, NdHandler handler) {
		super(ordinal, properties, handler);
	}





	@Override
	public void sweep(RepoS8Object object, GraphCrawler crawler) {
		try {
			RepoS8Object fieldObject = (RepoS8Object) handler.get(object);
			if(fieldObject!=null) {
				crawler.accept(fieldObject);
			}
		} 
		catch (NdIOException cause) {
			cause.printStackTrace();
		}
		catch (ClassCastException e) {
			System.out.println("Error for field: "+handler.describe(false));
			e.printStackTrace();
		}
	}


	@Override
	public void collectReferencedBlocks(RepoS8Object object, Queue<String> references) {
		// No ext references
	}


	@Override
	public void DEBUG_print(String indent) {
		System.out.println(indent+name+": (S8Object)");
	}

	@Override
	public void computeFootprint(RepoS8Object object, MemoryFootprint weight) throws NdIOException {
		weight.reportReference();
	}


	@Override
	public void deepClone(RepoS8Object origin, RepoS8Object clone, BuildScope scope) throws NdIOException {
		RepoS8Object value = (RepoS8Object) handler.get(origin);
		if(value!=null) {
			String index = value.S8_id;

			scope.appendBinding(new BuildScope.Binding() {

				@Override
				public void resolve(BuildScope scope) throws NdIOException {

					// no need to upcast to S8Object
					RepoS8Object indexedObject = scope.retrieveObject(index);
					if(indexedObject==null) {
						throw new NdIOException("Fialed to retriev vertex");
					}
					handler.set(clone, indexedObject);
				}
			});
		}
		else {
			handler.set(clone, null);
		}
	}


	@Override
	public boolean hasDiff(RepoS8Object base, RepoS8Object update) throws NdIOException {
		RepoS8Object baseValue = (RepoS8Object) handler.get(base);
		RepoS8Object updateValue = (RepoS8Object) handler.get(update);
		if(baseValue == null && updateValue == null) {
			return false;
		}
		else if ((baseValue != null && updateValue == null) || (baseValue == null && updateValue != null)) {
			return true;
		}
		else {
			return !baseValue.S8_id.equals(updateValue.S8_id);
		}
	}



	@Override
	public NdFieldDelta produceDiff(RepoS8Object object) throws NdIOException {
		RepoS8Object value = (RepoS8Object) handler.get(object);
		if(value != null) {
			return new InterfaceNdFieldDelta(this, value.S8_id);
		}
		else {
			return new InterfaceNdFieldDelta(this, null);
		}
	}




	@Override
	protected void printValue(RepoS8Object object, Writer writer) throws IOException {
		RepoS8Object value = (RepoS8Object) handler.get(object);
		if(value!=null) {
			writer.write("(");
			writer.write(value.getClass().getCanonicalName());
			writer.write("): ");
			writer.write(value.S8_id.toString());	
		}
		else {
			writer.write("null");
		}
	}

	@Override
	public String printType() {
		return "S8Object";
	}

	public void setValue(Object object, RepoS8Object struct) throws NdIOException {
		handler.set(object, struct);
	}





	@Override
	public boolean isValueResolved(RepoS8Object object) {
		return true; // always resolved at resolve step in shell
	}




	/* <IO-inflow-section> */

	@Override
	public NdFieldParser createParser(ByteInflow inflow) throws IOException {
		int code = inflow.getUInt8();
		switch(code){
		case BOHR_Types.S8OBJECT : return new Inflow();
		default: throw new NdIOException("Unsupported code: "+Integer.toHexString(code));
		}
	}


	private class Inflow extends NdFieldParser {

		@Override
		public InterfaceNdField getField() {
			return InterfaceNdField.this;
		}

		@Override
		public NdFieldDelta deserializeDelta(ByteInflow inflow) throws IOException {
			return new InterfaceNdFieldDelta(InterfaceNdField.this, inflow.getStringUTF8());
		}
	}


	/* </IO-inflow-section> */



	/* <IO-outflow-section> */

	@Override
	public NdFieldComposer createComposer(int code) throws NdIOException {
		switch(exportFormat) {

		case DEFAULT_FLOW_TAG: case "obj[]" : return new Outflow(code);

		default : throw new NdIOException("Impossible to match IO type for flow: "+exportFormat);
		}
	}


	private class Outflow extends NdFieldComposer {

		public Outflow(int code) {
			super(code);
		}

		@Override
		public NdField getField() {
			return InterfaceNdField.this;
		}

		@Override
		public void publishFlowEncoding(ByteOutflow outflow) throws IOException {
			outflow.putUInt8(BOHR_Types.S8OBJECT);
		}

		@Override
		public void composeValue(RepoS8Object object, ByteOutflow outflow) throws IOException {
			RepoS8Object value = (RepoS8Object) handler.get(object);
			outflow.putStringUTF8(value != null ? value.S8_id : null);
		}
		
		@Override
		public void publishValue(NdFieldDelta delta, ByteOutflow outflow) throws IOException {
			outflow.putStringUTF8(((InterfaceNdFieldDelta) delta).index);
		}
	}
	/* </IO-outflow-section> */
}
