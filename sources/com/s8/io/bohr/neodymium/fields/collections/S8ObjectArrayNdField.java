package com.s8.io.bohr.neodymium.fields.collections;

import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Queue;

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
public class S8ObjectArrayNdField extends CollectionNdField {



	public final static NdFieldPrototype PROTOTYPE = new NdFieldPrototype(){


		@Override
		public NdFieldProperties captureField(Field field) throws NdBuildException {
			Class<?> baseType = field.getType();
			if(baseType.isArray()) {
				S8Field annotation = field.getAnnotation(S8Field.class);
				if(annotation != null) {
					Class<?> componentType = baseType.getComponentType();
					if(RepoS8Object.class.isAssignableFrom(componentType)) {
						NdFieldProperties properties = new NdFieldProperties(this, NdHandlerType.FIELD, 
								componentType);
						properties.setFieldAnnotation(annotation);
						return properties;
					}
					else {
						throw new NdBuildException("S8Annotated field of type List must have its "
								+"parameterized type inheriting from S8Object", field);
					}
				}
				else { return null; }
			}
			else { return null; }
		}


		@Override
		public NdFieldProperties captureSetter(Method method) throws NdBuildException {
			Class<?> baseType = method.getParameterTypes()[0];
			if(baseType.isArray()) {
				S8Setter annotation = method.getAnnotation(S8Setter.class);
				if(annotation != null) {
					Class<?> componentType = baseType.getComponentType();
					if(RepoS8Object.class.isAssignableFrom(componentType)) {
						NdFieldProperties properties = new NdFieldProperties(this, NdHandlerType.GETTER_SETTER_PAIR, 
								Array.class, componentType);
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
			else { return null; }
		}

		@Override
		public NdFieldProperties captureGetter(Method method) throws NdBuildException {

			Class<?> baseType = method.getReturnType();
			if(baseType.isArray()) {
				S8Getter annotation = method.getAnnotation(S8Getter.class);
				if(annotation != null) {
					Class<?> componentType = baseType.getComponentType();
					if(RepoS8Object.class.isAssignableFrom(componentType)) {
						NdFieldProperties properties = new NdFieldProperties(this, NdHandlerType.GETTER_SETTER_PAIR, 
								Array.class, componentType);
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
			else { return null; }
		}


		@Override
		public NdFieldBuilder createFieldBuilder(NdFieldProperties properties, NdHandler handler) {
			return new Builder<>(properties, handler);
		}
	};



	private static class Builder<T> extends NdFieldBuilder {

		public Builder(NdFieldProperties properties, NdHandler handler) {
			super(properties, handler);
		}

		@Override
		public NdFieldPrototype getPrototype() {
			return PROTOTYPE;
		}

		@Override
		public NdField build(int ordinal) throws NdBuildException {
			return new S8ObjectArrayNdField(ordinal, properties, handler);
		}
	}


	
	public final Class<?> componentType;

	
	/**
	 * 
	 * @param ordinal
	 * @param properties
	 * @param handler
	 * @throws NdBuildException
	 */
	public S8ObjectArrayNdField(int ordinal, NdFieldProperties properties, NdHandler handler) throws NdBuildException {
		super(ordinal, properties, handler);
		this.componentType = properties.embeddedTypes[0];
		if(componentType == null) {
			throw new NdBuildException("Undefined component type");
		}
	}


	@Override
	public void sweep(RepoS8Object object, GraphCrawler crawler) throws NdIOException {
		RepoS8Object[] array = (RepoS8Object[]) handler.get(object);
		if(array!=null) {
			for(RepoS8Object item : array) {
				if(item!=null) {
					crawler.accept(item);
				}
			}
		}
	}


	@Override
	public void collectReferencedBlocks(RepoS8Object object, Queue<String> references) {
		// No ext references
	}


	@Override
	public void DEBUG_print(String indent) {
		System.out.println(indent+name+": (S8Object[])");
	}

	@Override
	public void computeFootprint(RepoS8Object object, MemoryFootprint weight) throws NdIOException {
		RepoS8Object[] array = (RepoS8Object[]) handler.get(object);
		if(array!=null) {
			weight.reportInstance();
			weight.reportReferences(array.length);	
		}
	}


	@Override
	public void deepClone(RepoS8Object origin, RepoS8Object clone, BuildScope scope) throws NdIOException {
		RepoS8Object[] objects = (RepoS8Object[]) handler.get(origin);
		if(objects!=null) {
			
			int n = objects.length;
			String[] itemIdentifers = new String[n];
			for(int i=0; i<n; i++) {
				RepoS8Object object = objects[i];
				itemIdentifers[i] = object != null ? object.S8_id : null;
			}
			
			
			scope.appendBinding(new BuildScope.Binding() {

				@Override
				public void resolve(BuildScope scope) throws NdIOException {
 					Object clonedArray = Array.newInstance(componentType, n);
					for(int index = 0; index < n; index++) {
						String id = itemIdentifers[index];
						RepoS8Object indexedObject = (id != null) ? scope.retrieveObject(id) : null;
						Array.set(clonedArray, index, indexedObject);
					}
					
					handler.set(clone, clonedArray);
				}
			});	
		}
		else {
			handler.set(clone, null);
		}

	}


	@Override
	public boolean hasDiff(RepoS8Object base, RepoS8Object update) throws NdIOException {
		RepoS8Object[] baseValue = (RepoS8Object[]) handler.get(base);
		RepoS8Object[] updateValue = (RepoS8Object[]) handler.get(update);
		return !areEqual(baseValue, updateValue);
	}

	@Override
	public NdFieldDelta produceDiff(RepoS8Object object) throws NdIOException {
		RepoS8Object[] array = (RepoS8Object[]) handler.get(object);
		String[] indices = null;
		if(array!=null) {
			int n = array.length;
			indices = new String[n];
			RepoS8Object item;
			for(int i=0; i<n; i++) {
				item = array[i];
				indices[i] = item != null ? item.S8_id : null;
			}
		}
		return new S8ObjectArrayNdFieldDelta(this, indices);
	}




	private static boolean areEqual(RepoS8Object[] array0, RepoS8Object[] array1) {

		// check nulls
		if(array0 == null) { return array1==null; }
		if(array1 == null) { return array0==null; }

		// check lengths
		int n0 = array0.length;
		int n1 = array1.length;
		if(n0!=n1) { return false; }

		// check values
		RepoS8Object obj0, obj1;
		for(int i=0; i<n0; i++) {
			obj0 = array0[i];
			obj1 = array1[i];
			if((obj0==null && obj1!=null) 
					
					// one is null while the other is non-null
					|| (obj1==null && obj0!=null) 
					
					// both non null with different indices
					|| (obj0!=null && obj1!=null && !obj0.S8_id.equals(obj1.S8_id))) { 
				return false; 
			}
		}
		return true;
	}



	@Override
	public String printType() {
		return "Object[]";
	}


	@Override
	public void forEach(Object iterable, ItemConsumer consumer) throws IOException {
		RepoS8Object[] array = (RepoS8Object[]) iterable;
		if(array!=null) {
			int n = array.length;
			for(int i=0; i<n; i++) {
				consumer.consume(array[i]);
			}
		}
	}

	@Override
	public boolean isValueResolved(RepoS8Object object) {
		return false; // never resolved
	}




	

	/* <IO-inflow-section> */
	@Override
	public NdFieldParser createParser(ByteInflow inflow) throws IOException {
		if(inflow.matches(SEQUENCE)) {
			return new Inflow();
		}
		else {
			throw new IOException("Only one possible encoding! ");
		}
	}


	private class Inflow extends NdFieldParser {


		@Override
		public S8ObjectArrayNdField getField() {
			return S8ObjectArrayNdField.this;
		}

		@Override
		public NdFieldDelta deserializeDelta(ByteInflow inflow) throws IOException {
			return new S8ObjectArrayNdFieldDelta(S8ObjectArrayNdField.this, deserializeIndices(inflow));
		}

		/**
		 * 
		 * @param inflow
		 * @return
		 * @throws IOException
		 */
		public String[] deserializeIndices(ByteInflow inflow) throws IOException {
			int length = (int) inflow.getUInt7x();
			if(length >= 0) {

				/* <data> */
				String[] indices = new String[length];
				for(int index=0; index<length; index++) { indices[index] = inflow.getStringUTF8(); }
				/* </data> */

				/* append bindings */
				return indices;
			}
			else if(length == -1) {

				// set list
				return null;
			}
			else {
				throw new NdIOException("Illegal code for List object length");
			}
		}
	}
	/* </IO-inflow-section> */



	/* <IO-outflow-section> */
	@Override
	public NdFieldComposer createComposer(int code) throws NdIOException {
		switch(exportFormat) {
		case DEFAULT_FLOW_TAG: case "obj[]" : return new Composer(code);
		default : throw new NdIOException("Impossible to match IO type for flow: "+exportFormat);
		}
	}


	private class Composer extends NdFieldComposer {

		public Composer(int code) {
			super(code);
		}

		@Override
		public NdField getField() {
			return S8ObjectArrayNdField.this;
		}

		@Override
		public void publishFlowEncoding(ByteOutflow outflow) throws IOException {
			outflow.putByteArray(SEQUENCE);
		}

		@Override
		public void composeValue(RepoS8Object object, ByteOutflow outflow) throws IOException {

			// array
			RepoS8Object[] array = (RepoS8Object[]) handler.get(object);
			if(array!=null) {
				int length = array.length;
				outflow.putUInt7x(length);
				for(int i=0; i<length; i++) {
					RepoS8Object itemObject = array[i];
					outflow.putStringUTF8(itemObject != null ? itemObject.S8_id : null);
				}
			}
			else {
				// advertise NULL
				outflow.putUInt7x(-1);
			}
		}


		@Override
		public void publishValue(NdFieldDelta delta, ByteOutflow outflow) throws IOException {
			serialize(outflow, ((S8ObjectArrayNdFieldDelta) delta).itemIdentifiers);
		}


		public void serialize(ByteOutflow outflow, String[] indices) throws IOException {
			if(indices!=null) {
				int length = indices.length;
				outflow.putUInt7x(length);
				for(int i=0; i<length; i++) { outflow.putStringUTF8(indices[i]); }
			}
			else {
				// advertise NULL
				outflow.putUInt7x(-1);
			}
		}
	}
	/* </IO-outflow-section> */
}
