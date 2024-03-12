/**
 * 
 */
/**
 * @author pierreconvert
 *
 */
module com.s8.core.bohr.neodymium {
	
	

	/* <neodymium> */

	exports com.s8.core.bohr.neodymium.codebase;
	exports com.s8.core.bohr.neodymium.type;
	
	exports com.s8.core.bohr.neodymium.branch;
	exports com.s8.core.bohr.neodymium.branch.endpoint;
	exports com.s8.core.bohr.neodymium.branch.operations;
	
	exports com.s8.core.bohr.neodymium.object;
	exports com.s8.core.bohr.neodymium.handlers;
	exports com.s8.core.bohr.neodymium.properties;
	
	exports com.s8.core.bohr.neodymium.exceptions;

	exports com.s8.core.bohr.neodymium.fields;
	exports com.s8.core.bohr.neodymium.fields.primitives;
	exports com.s8.core.bohr.neodymium.fields.arrays;
	exports com.s8.core.bohr.neodymium.fields.objects;
	exports com.s8.core.bohr.neodymium.fields.collections;
	
	
	
	exports com.s8.core.bohr.neodymium.demos.repo0;
	exports com.s8.core.bohr.neodymium.demos.repo2;
	exports com.s8.core.bohr.neodymium.demos.repo3;
	
	
	
	
	/* </neodymium> */
	
	
	requires transitive com.s8.api;
	requires transitive com.s8.core.io.bytes;
	requires transitive com.s8.core.bohr.atom;
}