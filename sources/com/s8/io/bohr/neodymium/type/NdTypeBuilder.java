package com.s8.io.bohr.neodymium.type;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicInteger;

import com.s8.api.objects.annotations.S8Field;
import com.s8.api.objects.annotations.S8Getter;
import com.s8.api.objects.annotations.S8ObjectType;
import com.s8.api.objects.annotations.S8Setter;
import com.s8.api.objects.repo.RepoS8Object;
import com.s8.io.bohr.neodymium.codebase.NdCodebaseBuilder;
import com.s8.io.bohr.neodymium.exceptions.NdBuildException;
import com.s8.io.bohr.neodymium.fields.NdField;
import com.s8.io.bohr.neodymium.fields.NdFieldBuilder;
import com.s8.io.bohr.neodymium.properties.NdFieldProperties;

/**
 * 
 *
 * @author Pierre Convert
 * Copyright (C) 2022, Pierre Convert. All rights reserved.
 * 
 */
public class NdTypeBuilder {



	private final Class<?> baseType;

	private final boolean isVerbose;




	private boolean isBuildable = false;


	private Map<String, NdFieldBuilder> fieldBuildersByName;



	private Constructor<?> constructor;



	private String name;



	private int nVertexReferences;




	private final S8ObjectType typeAnnotation;







	public NdTypeBuilder(Class<?> type, S8ObjectType typeAnnotation, boolean isVerbose) {
		super();

		this.baseType = type;
		this.typeAnnotation = typeAnnotation;
		this.isVerbose = isVerbose;
	}







	/**
	 * 
	 * @param name
	 * @return
	 */
	public NdFieldBuilder getFieldBuilder(String name) {
		return fieldBuildersByName.get(name);
	}

	/**
	 * 
	 * @param name
	 * @param fieldBuilder
	 */
	public void setFieldBuilder(String name, NdFieldBuilder fieldBuilder) {
		fieldBuildersByName.put(name, fieldBuilder);
	}




	/**
	 * 
	 * <h1>CALLED SECOND</h1>
	 * @param level
	 * @param factory
	 * @throws BohrTypeBuildingException 
	 * @throws BkException
	 * @throws Exception 
	 */
	public boolean process(NdCodebaseBuilder codebaseBuilder) throws NdBuildException {

		// check status
		check();

		resolveName();

		initializeAttributes();

		/* <constructor> */
		retrieveConstructor();
		/* </constructor> */

		// recursive extraction
		crawl(baseType, codebaseBuilder);
		/* </fields> */

		return isBuildable;
	}





	private void check() {

		int mods = baseType.getModifiers();

		/**
		 * Determine if abstract
		 */


		// is static ?
		if(!baseType.isMemberClass() || (baseType.isMemberClass() && Modifier.isStatic(mods))) {
			if(RepoS8Object.class.isAssignableFrom(baseType)) {
				isBuildable = true;
			}
			else if(isVerbose) {
				System.out.println("Type "+baseType+" is not a valid subType for S8Object");
			}
		}
		else if(isVerbose) {
			System.out.println("Type "+baseType+" rejected as not static");
		}
	}



	/**
	 * <h1>CALLED FIRST</h1>
	 * <p>Reslve name by checking direct case and nested class case.</p>
	 * @throws LithTypeBuildException
	 */
	private void resolveName() throws NdBuildException {
		if(isBuildable) {
			/* <code> */

			if(typeAnnotation==null) {
				throw new NdBuildException("Missing type annotation", baseType);
			}

			Class<?> enclosingType = baseType.getEnclosingClass();
			if(enclosingType!=null) {
				S8ObjectType enclosingTypeAnnotation = enclosingType.getAnnotation(S8ObjectType.class);
				if(enclosingTypeAnnotation!=null) {
					name = typeAnnotation.name()+'@'+enclosingTypeAnnotation.name();
				}
				else {
					name = typeAnnotation.name();
				}
			}
			else {
				name = typeAnnotation.name();
			}
			
		}

	}



	/**
	 * <p>
	 * Initialize all attributes:
	 * </p>
	 * <ul>
	 * <li>fields</li>
	 * </ul>
	 */
	public void initializeAttributes() {
		/* <fields> */
		if(isBuildable) {
			fieldBuildersByName = new HashMap<String, NdFieldBuilder>();	
		}
	}


	/**
	 * lookUp this level 
	 * 
	 * @param type
	 * @param factory
	 * @throws LithTypeBuildException
	 * @throws BohrTypeBuildingException 
	 * @throws LthSerialException 
	 */
	public void crawl(Class<?> type, NdCodebaseBuilder codebaseBuilder) throws NdBuildException {

		/* <sub-types> */

		/* --> Always to be done, even if not buildable */
		Class<?>[] subTypes = typeAnnotation.sub();
		if(subTypes!=null) {
			for(Class<?> subType : subTypes) {
				codebaseBuilder.pushObjectType(subType);
			}
		}
		/* </sub-types> */


		/* <nested-types> */

		/* --> Always to be done, even if not buildable */
		Class<?>[] nestedTypes = type.getNestMembers();
		int nNestedTypes = nestedTypes.length;
		for(int i=1; i<nNestedTypes; i++) {
			codebaseBuilder.pushObjectType(nestedTypes[i]);
		}
		/* </nested-types> */



		// search superclass
		/* --> Always to be done, even if not buildable */
		Class<?> superType = type.getSuperclass();
		if(superType!=null && superType.isAnnotationPresent(S8ObjectType.class)) {

			// append superclass to type to be built and added to the Mapping
			codebaseBuilder.pushObjectType(superType);

			// perform extract on supertype
			crawl(superType, codebaseBuilder);
		}

		// search trough all declared field at this level
		if(isBuildable) {
			/* <fields> */
			Field[] declaredFields = type.getDeclaredFields();
			if(declaredFields!=null) {
				for(Field field : declaredFields) { onField(codebaseBuilder, field); }	
			}
			/* </fields> */



			/* <methods> */
			Method[] declaredMethods = type.getDeclaredMethods();
			if(declaredMethods!=null) {
				for(Method method : declaredMethods) { onMethod(codebaseBuilder, method); }
			}
			/* </methods> */	
		}

		/* </subTypes> */
	}


	/**
	 * 
	 * @return
	 */
	public Class<?> getRawType() {
		return baseType;
	}



	public void retrieveConstructor() throws NdBuildException {
		if(isBuildable) {
			try {
				/*
				 * retrieve constructor with no parameters 
				 */
				constructor = baseType.getConstructor(new Class<?>[]{});

			} 
			catch (NoSuchMethodException | SecurityException e) {
				throw new NdBuildException("missing public constructor with no parameters", baseType);
			}
			catch (ClassCastException e) {
				throw new NdBuildException("Must inherit DkObject", baseType);
			}	
			catch (NoClassDefFoundError e) {
				throw new NdBuildException("Failed to retrieve another class: "+e.getMessage(), baseType);
			}	
		}
	}



	public void filter(Field field) {
		/*
		// check ownership
		S8Owner ownerAnnotation = field.getAnnotation(S8Owner.class);
		if(ownerAnnotation!=null) {
			// TODO objectType.
		}
		 */
	}



	/**
	 * <p>
	 * append field
	 * </p>
	 * <p>
	 * <b>Note that testing if field must be collected with <code>isCollected</code>
	 * must be performed prior to using this method</b>
	 * </p>
	 * 
	 * @param field
	 * @param factory
	 * @throws LthSerialException 
	 * @throws BkException
	 */
	private void onField(NdCodebaseBuilder codebaseBuilder, Field field) throws NdBuildException {

		S8Field fieldAnnotation = field.getAnnotation(S8Field.class);
		if(fieldAnnotation!=null) {
			String name = fieldAnnotation.name();


			// non-mixing check

			NdFieldBuilder fieldBuilder = fieldBuildersByName.get(name);


			// aggregate with an already existing field
			if(fieldBuilder!=null) {

				fieldBuilder.attachField(field);
			}
			// create new field
			else {
				fieldBuilder = codebaseBuilder.getFieldFactory().captureField(field);
				NdFieldProperties props = fieldBuilder.properties;
				for(Class<?> embeddedType : props.embeddedTypes) { codebaseBuilder.pushObjectType(embeddedType);	}
				

				// apply filter before appending fields
				filter(field);

				// add fields
				fieldBuildersByName.put(name, fieldBuilder);	
			}
		}
		// else: do nothing (field is skipped)
	}



	public void onMethod(NdCodebaseBuilder codebaseBuilder, Method method) throws NdBuildException {

		boolean hasBeenCaptured = false;

		S8Getter getterAnnotation = method.getAnnotation(S8Getter.class);
		if(getterAnnotation !=null) {

			String name = getterAnnotation.name();
			NdFieldBuilder fieldBuilder = fieldBuildersByName.get(name);
			if(fieldBuilder!=null) { // update
				fieldBuilder.attachGetMethod(method);
			}
			else { // create
				fieldBuilder = codebaseBuilder.getFieldFactory().captureGetter(method);

				// add fields
				fieldBuildersByName.put(name, fieldBuilder);	

				// further crawl...
				NdFieldProperties props = fieldBuilder.properties;
				for(Class<?> embeddedType : props.embeddedTypes) { codebaseBuilder.pushObjectType(embeddedType);	}
				
			}
			hasBeenCaptured = true;
		}

		S8Setter setterAnnotation = method.getAnnotation(S8Setter.class);
		if(!hasBeenCaptured && setterAnnotation!=null) {

			String name = setterAnnotation.name();
			NdFieldBuilder fieldBuilder = fieldBuildersByName.get(name);
			if(fieldBuilder!=null) {
				fieldBuilder.attachSetMethod(method);
			}
			else {

				fieldBuilder = codebaseBuilder.getFieldFactory().captureSetter(method);
				NdFieldProperties props = fieldBuilder.properties;
				// add fields
				fieldBuildersByName.put(name, fieldBuilder);
				for(Class<?> embeddedType : props.embeddedTypes) { codebaseBuilder.pushObjectType(embeddedType);	}
				

			}
		}
		else if(hasBeenCaptured && setterAnnotation != null) {
			throw new NdBuildException("Cannot be getter and setter at the same time");
		}
	}



	/**
	 * Post build
	 * @throws NdBuildException 
	 */
	public NdType build() throws NdBuildException {

		if(!isBuildable) {
			throw new NdBuildException("This type is not buildable");
		}

		// generate type
		NdType type = new NdType(baseType);


		type.name = name;

		type.constructor = constructor;

		type.nVertexReferences = nVertexReferences;

		// finalize build
		fieldBuildersByName.forEach((name, builder) -> {
			//builder.build2(this);
		});


		// retrieve nb fields
		int nFields = fieldBuildersByName.size();


		// build map
		Map<String, NdField> fieldsByName = new HashMap<String, NdField>(nFields);
		AtomicInteger ordinator = new AtomicInteger(0);
		fieldBuildersByName.forEach((name, builder) -> {
			try {
				fieldsByName.put(name, builder.build(ordinator.getAndIncrement()));
			} catch (NdBuildException e) {
				if(isVerbose) {
					e.printStackTrace();	
				}
			}
		});
		type.fieldsByName = fieldsByName;


		// build array
		NdField[] fields = new NdField[nFields];
		NdField field;
		for(Entry<String, NdField> entry : fieldsByName.entrySet()) {
			field = entry.getValue();
			fields[field.ordinal] = field;
		}
		type.fields = fields;

		return type;
	}



	public int onVertexReferenced() {
		return this.nVertexReferences++;
	}



	public void subDiscover() {

	}
}
