package com.s8.io.bohr.neodymium.fields;

import java.io.IOException;
import java.io.Writer;
import java.util.Queue;

import com.s8.api.bytes.ByteInflow;
import com.s8.api.bytes.MemoryFootprint;
import com.s8.api.objects.repo.RepoS8Object;
import com.s8.io.bohr.atom.S8ShellStructureException;
import com.s8.io.bohr.neodymium.exceptions.NdIOException;
import com.s8.io.bohr.neodymium.handlers.NdHandler;
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
public abstract class NdField {

	public final static String DEFAULT_FLOW_TAG = "(default)";

	
	public final int ordinal;
	
	/**
	 * 
	 */
	public final NdHandler handler;


	/**
	 * 
	 */
	public final String name;

	/**
	 * 
	 */
	public final String exportFormat;

	
	/**
	 * 
	 * @param properties
	 * @param handler
	 */
	public NdField(int ordinal, NdFieldProperties properties, NdHandler handler) {
		super();
		
		this.ordinal = ordinal;
		
		/* <field-properties> */
		this.name = properties.getName();
		this.exportFormat = properties.getExportFormat();
		/* </field-properties> */
		
		/* <handler> */
		this.handler = handler;
		/* </handler> */
	}


	
	
	protected NdHandler getHandler() {
		return handler;
	}

	
	
	
	/**
	 * 
	 * @param exportFormat (null = use flow define in field)
	 * @return
	 * @throws  
	 */
	public abstract NdFieldComposer createComposer(int code) throws NdIOException;
	
	
	public abstract NdFieldParser createParser(ByteInflow inflow) throws IOException;
	
	


	public abstract void computeFootprint(RepoS8Object object, MemoryFootprint weight) throws NdIOException;

	/**
	 * Collect all INTERNAL (belonging to this block) object not already in the
	 * state passed as argument, and switch their state to the state passed as
	 * argument
	 * 
	 * @param object
	 * @param state
	 * @param referenced
	 * @throws IOException
	 * @throws S8ShellStructureException 
	 */
	public abstract void sweep(RepoS8Object object, GraphCrawler crawler) throws NdIOException;

	/**
	 * Collect all external blocks with flag not already set to true
	 * 
	 * @param object
	 */
	public abstract void collectReferencedBlocks(RepoS8Object object, Queue<String> references);

	

	/**
	 * Print field for debugging purposes
	 * 
	 * @param indent
	 */
	public abstract void DEBUG_print(String indent);

	/**
	 * 
	 * @param clone
	 * @param bindings
	 * @throws LthSerialException
	 */
	public abstract void deepClone(RepoS8Object origin, RepoS8Object clone, BuildScope scope) throws NdIOException;

	/**
	 * 
	 * @param base
	 * @param update
	 * @param outflow
	 * @throws LthSerialException
	 * @throws IOException
	 */
	public abstract boolean hasDiff(RepoS8Object base, RepoS8Object update) throws NdIOException;


	/**
	 * <p>
	 * <b>IMPORTANT NOTICE<b>: It is often a good idea to track change AND remap at
	 * the same time, that's why we take advantage of the scope
	 * </p>
	 * 
	 * @param object
	 * @param scope
	 * @return
	 * @throws IOException
	 */
	public abstract NdFieldDelta produceDiff(RepoS8Object object) throws NdIOException;

	

	public void print(RepoS8Object object, Writer writer) throws IOException, S8ShellStructureException {
		writer.append("(");
		writer.append(printType());
		writer.append(") ");
		writer.append(name);
		writer.append(": ");
		printValue(object, writer);
	}
	

	/**
	 * print standard name of field type
	 */
	public abstract String printType();


	protected abstract void printValue(RepoS8Object object, Writer writer) throws NdIOException, 
	IOException, S8ShellStructureException;

	

	/**
	 * 
	 * @param object
	 * @return true is the object has been resolved, false otherwise
	 * @throws LthSerialException 
	 */
	public abstract boolean isValueResolved(RepoS8Object object) throws NdIOException;

	
}
