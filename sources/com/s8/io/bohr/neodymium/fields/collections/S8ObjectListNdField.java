package com.s8.io.bohr.neodymium.fields.collections;

import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
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
 * <p> Internal object ONLY</p>
 *
 * @author Pierre Convert
 * Copyright (C) 2022, Pierre Convert. All rights reserved.
 * 
 */
public class S8ObjectListNdField<T extends RepoS8Object> extends CollectionNdField {




	public final static NdFieldPrototype PROTOTYPE = new NdFieldPrototype(){


		@Override
		public NdFieldProperties captureField(Field field) throws NdBuildException {
			Class<?> baseType = field.getType();
			if(List.class.isAssignableFrom(baseType)) {
				S8Field annotation = field.getAnnotation(S8Field.class);
				if(annotation != null) {
					Type parameterType = field.getGenericType();
					Class<?> typeArgument =  getParameterTypeClass(parameterType);

					if(RepoS8Object.class.isAssignableFrom(typeArgument)) {
						NdFieldProperties properties = new NdFieldProperties(this, NdHandlerType.FIELD, 
								typeArgument);
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
			if(List.class.isAssignableFrom(baseType)) {
				S8Setter annotation = method.getAnnotation(S8Setter.class);
				if(annotation != null) {
					Type parameterType = method.getGenericParameterTypes()[0];
					Class<?> typeArgument = getParameterTypeClass(parameterType);

					if(RepoS8Object.class.isAssignableFrom(typeArgument)) {
						NdFieldProperties properties = new NdFieldProperties(this, NdHandlerType.GETTER_SETTER_PAIR, 
								typeArgument);
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
			if(List.class.isAssignableFrom(baseType)) {
				S8Getter annotation = method.getAnnotation(S8Getter.class);
				if(annotation != null) {
					Type parameterType = method.getGenericReturnType();
					Class<?> typeArgument = getParameterTypeClass(parameterType);

					if(RepoS8Object.class.isAssignableFrom(typeArgument)) {
						NdFieldProperties properties = new NdFieldProperties(this, NdHandlerType.GETTER_SETTER_PAIR,
								typeArgument);
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



		private Class<?> getParameterTypeClass(Type parameterType) throws NdBuildException {
			ParameterizedType parameterizedType = (ParameterizedType) parameterType; 
			Type typeArgument = parameterizedType.getActualTypeArguments()[0];
			if(typeArgument instanceof Class<?>) {
				return (Class<?>) typeArgument;	
			}
			else if(typeArgument instanceof ParameterizedType){
				return (Class<?>) ((ParameterizedType) typeArgument).getRawType();
			}
			else {
				throw new NdBuildException("Cannot handle type: "+parameterizedType);
			}
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
			return new S8ObjectListNdField<>(ordinal, properties, handler);
		}

	}



	public final static int NULL_OBJECT_INDEX = -8;


	private Class<?> baseType;


	public S8ObjectListNdField(int ordinal, NdFieldProperties properties, NdHandler handler) throws NdBuildException {
		super(ordinal, properties, handler);
		baseType = properties.embeddedTypes[0];
	}


	/**
	 * 
	 * @author pierreconvert
	 *
	 * @param <T>
	 */
	private static class ListBinding<T> implements BuildScope.Binding {

		private List<T> list;

		private String[] itemIdentifiers;


		public ListBinding(List<T> list, String[] ids) {
			super();
			this.list = list;
			this.itemIdentifiers = ids;
		}

		@SuppressWarnings("unchecked")
		@Override
		public void resolve(BuildScope scope) throws NdIOException {
			int length = itemIdentifiers.length;
			for(int index = 0; index < length; index++) {
				String id = itemIdentifiers[index];
				if(id != null) {
					// might be null
					RepoS8Object struct = scope.retrieveObject(id);
					if(struct!=null) {
						list.add((T) struct);		
					}
					else {
						throw new NdIOException("Failed to retrive object for index="+id);
					}	
				}
				else {
					list.add(null);		
				}
			}
		}
	}




	@Override
	public void sweep(RepoS8Object object, GraphCrawler crawler) {
		try {
			@SuppressWarnings("unchecked")
			List<T> list = (List<T>) handler.get(object);


			if(list!=null) {
				for(RepoS8Object item : list) {
					if(item!=null) { crawler.accept(item); }
				}
			}
		} 
		catch (IllegalArgumentException e) {
			e.printStackTrace();
		}
		catch (ClassCastException e) {
			e.printStackTrace();
		} 
		catch (NdIOException e) {
			e.printStackTrace();
		}
	}


	@Override
	public void computeFootprint(RepoS8Object object, MemoryFootprint weight) throws NdIOException {

		@SuppressWarnings("unchecked")
		List<RepoS8Object> list = (List<RepoS8Object>) handler.get(object);
		if(list!=null) {
			weight.reportInstances(1+list.size()); // the array object itself	
			weight.reportReferences(list.size());
		}
	}


	@Override
	public void deepClone(RepoS8Object origin, RepoS8Object clone, BuildScope scope) throws NdIOException {

		@SuppressWarnings("unchecked")
		List<T> value = (List<T>) handler.get(origin);
		if(value!=null) {
			int n = value.size();

			List<T> clonedList = new ArrayList<T>(n);
			String[] indices = new String[n];
			for(int i=0; i<n; i++) {
				indices[i] = value.get(i).S8_id;
			}

			handler.set(clone, clonedList);
			scope.appendBinding(new ListBinding<>(clonedList, indices));
		}
		else {
			handler.set(clone, null);
		}
	}


	@Override
	public void collectReferencedBlocks(RepoS8Object object, Queue<String> references) {
		/* not referencing external values */
	}


	@Override
	public void DEBUG_print(String indent) {
		System.out.println(indent+name+": (List<? extends S8Object>)");
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean hasDiff(RepoS8Object base, RepoS8Object update) throws NdIOException {
		List<T> baseValue = (List<T>) handler.get(base);
		List<T> updateValue = (List<T>) handler.get(update);
		return !areEqual(baseValue, updateValue);
	}



	@Override
	public NdFieldDelta produceDiff(RepoS8Object object) throws NdIOException {
		@SuppressWarnings("unchecked")
		List<T> array = (List<T>) handler.get(object);
		String[] indices = null;
		if(array!=null) {
			int n = array.size();
			indices = new String[n];
			RepoS8Object item;
			for(int i=0; i<n; i++) {
				item = array.get(i);
				indices[i] = (item != null ? item.S8_id : null);	
			}
		}
		return new S8ObjectListNdFieldDelta<>(this, indices);
	}




	private boolean areEqual(List<T> array0, List<T> array1) {

		// check nulls
		if(array0 == null) { return array1==null; }
		if(array1 == null) { return array0==null; }

		// check lengths
		int n0 = array0.size();
		int n1 = array1.size();
		if(n0!=n1) { return false; }

		// check values
		RepoS8Object obj0, obj1;
		for(int i=0; i<n0; i++) {
			obj0 = array0.get(i);
			obj1 = array1.get(i);
			if((obj0==null && obj1!=null) || (obj1==null && obj0!=null) // one is null while the other is non-null
					|| (obj0!=null  && obj1!=null // both non null with different indices
					&& !obj0.S8_id.equals(obj1.S8_id))) { 
				return false; 
			}
		}
		return true;
	}


	@Override
	protected void printValue(RepoS8Object object, Writer writer) throws IOException {
		@SuppressWarnings("unchecked")
		List<T> list = (List<T>) handler.get(object);
		if(list!=null) {
			boolean isInitialized = false;
			writer.write('[');
			int n = list.size();
			for(int i=0; i<n; i++) {
				if(isInitialized) {
					writer.write(" ,");	
				}
				else {
					isInitialized = true;
				}

				RepoS8Object value = list.get(i);
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
			writer.write(']');
		}
		else {
			writer.write("null");
		}
	}


	@Override
	public String printType() {
		return "List<"+baseType.getCanonicalName()+">";
	}




	@Override
	public void forEach(Object iterable, ItemConsumer consumer) throws IOException {
		if(iterable!=null) {
			@SuppressWarnings("unchecked")
			List<T> list = (List<T>) iterable;
			for(T item : list) {
				consumer.consume(item);
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
			return new DefaultParser();
		}
		else {
			throw new IOException("Only one possible encoding! ");
		}
	}


	private class DefaultParser extends NdFieldParser {

		@Override
		public S8ObjectListNdField<T> getField() {
			return S8ObjectListNdField.this;
		}

		@Override
		public NdFieldDelta deserializeDelta(ByteInflow inflow) throws IOException {
			return new S8ObjectListNdFieldDelta<>(S8ObjectListNdField.this,  deserializeIndices(inflow));
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
		case DEFAULT_FLOW_TAG: case "obj[]" : return new DefaultComposer(code);
		default : throw new NdIOException("Impossible to match IO type for flow: "+exportFormat);
		}
	}


	private class DefaultComposer extends NdFieldComposer {

		public DefaultComposer(int code) {
			super(code);
		}

		@Override
		public NdField getField() {
			return S8ObjectListNdField.this;
		}

		@Override
		public void publishFlowEncoding(ByteOutflow outflow) throws IOException {
			outflow.putByteArray(SEQUENCE);
		}

		@Override
		public void composeValue(RepoS8Object object, ByteOutflow outflow) throws IOException {
			@SuppressWarnings("unchecked")
			List<T> list = (List<T>) handler.get(object);

			if(list!=null) {

				int length = list.size();

				outflow.putUInt7x(length);
				for(int i=0; i<length; i++) {
					T itemObject = list.get(i);
					outflow.putStringUTF8(itemObject!=null ? itemObject.S8_id : null);
				}
			}
			else {
				// advertise NULL
				outflow.putUInt7x(-1);
			}
		}


		@Override
		public void publishValue(NdFieldDelta delta, ByteOutflow outflow) throws IOException {
			serialize(outflow, ((S8ObjectListNdFieldDelta<?>) delta).itemIdentifiers);
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
