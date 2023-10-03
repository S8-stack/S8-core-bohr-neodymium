/**
 * 
 */
/**
 * @author pierreconvert
 *
 */
module com.s8.io.bohr.neodymium {
	
	

	/* <neodymium> */

	exports com.s8.io.bohr.neodymium.codebase;
	exports com.s8.io.bohr.neodymium.type;
	
	exports com.s8.io.bohr.neodymium.branch;
	exports com.s8.io.bohr.neodymium.branch.endpoint;
	exports com.s8.io.bohr.neodymium.branch.operations;
	
	exports com.s8.io.bohr.neodymium.object;
	exports com.s8.io.bohr.neodymium.handlers;
	exports com.s8.io.bohr.neodymium.properties;
	exports com.s8.io.bohr.neodymium.exceptions;

	exports com.s8.io.bohr.neodymium.fields;
	exports com.s8.io.bohr.neodymium.fields.primitives;
	exports com.s8.io.bohr.neodymium.fields.arrays;
	exports com.s8.io.bohr.neodymium.fields.objects;
	exports com.s8.io.bohr.neodymium.fields.collections;
	
	
	
	exports com.s8.io.bohr.neodymium.demos.repo0;
	exports com.s8.io.bohr.neodymium.demos.repo2;
	exports com.s8.io.bohr.neodymium.demos.repo3;
	
	
	
	
	/* </neodymium> */
	
	
	requires transitive com.s8.api;
	requires transitive com.s8.io.bytes;
	requires transitive com.s8.io.bohr.atom;
}