package com.s8.io.bohr.neodymium.properties;

import com.s8.api.exceptions.S8BuildException;
import com.s8.api.objects.annotations.S8Field;
import com.s8.api.objects.annotations.S8Getter;
import com.s8.api.objects.annotations.S8Setter;
import com.s8.io.bohr.neodymium.exceptions.NdBuildException;
import com.s8.io.bohr.neodymium.fields.NdFieldPrototype;
import com.s8.io.bohr.neodymium.handlers.NdHandlerType;


/**
 * 
 * 
 * 
 * @author Pierre Convert
 * Copyright (C) 2022, Pierre Convert. All rights reserved.
 * 
 */
public class NdFieldProperties {

	
	
	private NdFieldPrototype prototype;

	public final NdHandlerType handlerType;

	/**
	 * 
	 */
	private boolean isNameDefined = false;

	/**
	 * 
	 */
	private String name;



	private boolean isFlowDefined = false;

	/**
	 * 
	 */
	private String exportFormat;



	
	/**
	 * Type of object in array, or in list, or key of map
	 * @return
	 */
	public final Class<?>[] embeddedTypes;

	

	

	/**
	 * 
	 * @param mode
	 */
	public NdFieldProperties(NdFieldPrototype prototype,
			NdHandlerType handlerType,
			Class<?> baseType) {
		super();
		this.prototype = prototype;
		this.handlerType = handlerType;
		
		this.embeddedTypes = new Class<?>[] { baseType };
	}
	
	
	
	/**
	 * 
	 * @param mode
	 */
	public NdFieldProperties(NdFieldPrototype prototype,
			NdHandlerType handlerType,
			Class<?> baseType,
			Class<?> componentType) {
		super();
		this.prototype = prototype;
		this.handlerType = handlerType;
		
		this.embeddedTypes = new Class<?>[] { baseType, componentType };
	}
	
	/**
	 * 
	 * @param mode
	 */
	public NdFieldProperties(NdFieldPrototype prototype,
			NdHandlerType handlerType,
			Class<?> baseType,
			Class<?>[] embeddedTypes) {
		super();
		this.prototype = prototype;
		this.handlerType = handlerType;
		this.embeddedTypes = embeddedTypes;
	}




	/**
	 * 
	 * @return
	 */
	public NdFieldPrototype getPrototype() {
		return prototype;
	}



	public String getName() {
		return name;
	}


	public void setName(String name) {
		this.name = name;
		this.isNameDefined = true;
	}



	public String getExportFormat() {
		return exportFormat;
	}


	public void setExportFormat(String format) {
		this.exportFormat = format;
		this.isFlowDefined = true;
	}


	/**
	 * 
	 * @param right
	 * @throws S8BuildException 
	 */
	public void merge(NdFieldProperties right) throws NdBuildException {
		
		if(handlerType != right.handlerType) {
			throw new NdBuildException("Cannot mix FIELD and GETTER_SETTER_PAIR approach");
		}
		
		
		/* compare parameter 1, if any */
		int nTypes = embeddedTypes.length;
		if(right.embeddedTypes.length != nTypes) {
			throw new NdBuildException("<T1> type discrepancy: nb of param Types");
		}
		for(int i = 0; i < nTypes; i++) {
			Class<?> leftParam = embeddedTypes[i];
			Class<?> rightParam = right.embeddedTypes[i];
			if((leftParam !=null && rightParam != null && !leftParam.equals(rightParam)) ||
					(leftParam != null && rightParam == null) || 
					(leftParam == null && rightParam != null)) {
				throw new NdBuildException("<T1> type discrepancy: "+leftParam+" <-> "+rightParam);
			}	
		}
		

		// name
		if(!isNameDefined && right.isNameDefined) {
			setName(right.name);
		}
		else if(isNameDefined && right.isNameDefined && !name.equals(right.name)) {
			throw new NdBuildException("<name> discrepancy: "+name+" <-> "+right.name);
		}

		// flow
		if(!isFlowDefined && right.isFlowDefined) {
			setExportFormat(right.name);
		}
		else if(isFlowDefined && right.isFlowDefined && !exportFormat.equals(right.exportFormat)) {
			throw new NdBuildException("<export-format> discrepancy: "+exportFormat+" <-> "+right.exportFormat);
		}
	}





	/**
	 * 
	 * @param annotation
	 * @return
	 * @throws NdBuildException 
	 */
	public void setFieldAnnotation(S8Field annotation) {
		setName(annotation.name());
		setExportFormat(annotation.export());
	}


	/**
	 * 
	 * @param annotation
	 * @return
	 * @throws NdBuildException 
	 */
	public void setGetterAnnotation(S8Getter annotation) {
		setName(annotation.name());
		setExportFormat(annotation.export());
	}

	/**
	 * 
	 * @param annotation
	 * @return
	 * @throws NdBuildException 
	 */
	public void setSetterAnnotation(S8Setter annotation) {
		setName(annotation.name());
	}



	public Class<?> getBaseType() {
		return embeddedTypes[0];
	}

}
