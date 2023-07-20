package com.s8.io.bohr.neodymium.branch;

import java.util.HashMap;
import java.util.Map;

import com.s8.io.bohr.neodymium.object.NdObject;
import com.s8.io.bohr.neodymium.object.NdVertex;
import com.s8.io.bohr.neodymium.type.BuildScope;


/**
 * 
 * @author pierreconvert
 *
 */
public class NdGraph {

	
	public static NdGraph createZeroStart() {
		return new NdGraph(0, new HashMap<>(), new NdObject[4], -1);
	}
	

	/**
	 * 
	 */
	public long version;

	
	/**
	 * public final NdBranch branch;
	 */
	public NdObject[] exposure;
	
	

	/**
	 * The interior mapping
	 */
	public final Map<String, NdVertex> vertices;


	/**
	 * 
	 */
	public long lastAssignedIndex;
	
	
	
	
	
	/**
	 * 
	 * @param vertices
	 * @param exposure
	 */
	public NdGraph(long version, Map<String, NdVertex> vertices, NdObject[] exposure, long lastAssignedIndex) {
		super();
		this.version = version;
		this.vertices = vertices;
		this.exposure = exposure;
		this.lastAssignedIndex = lastAssignedIndex;
	}
	
	
	
	
	


	public NdObject retrieveObject(String index) {
		return vertices.get(index).object;
	}
	

	/**
	 * 
	 * @param index
	 * @return
	 */
	public NdVertex getVertex(String index) {
		return vertices.get(index);
	}





	

	
	
	/**
	 * <b>(Internal use only)</b>
	 * @param port
	 * @param object
	 */
	
	public void expose(int port, NdObject vertex) {
		int range = exposure.length;
		if(port >= range) {
			NdObject[] expansion = new NdObject[range > 0 ? 2 * range : 2];
			for(int i=0; i<range; i++) {
				expansion[i] = exposure[i];
			}
			exposure = expansion;
		}
		exposure[port] = vertex;
	}
	
	/**
	 * <b>(Internal use only)</b>
	 * @param vertices
	 */
	/*
	public void expose(NdVertex[] exposure) {
		this.exposure = exposure;
	}
	*/
	
	

	public int getExposureRange() {
		return exposure.length;
	}



	public BuildScope createBuildContext() {
		return new BuildScope() {
			@Override
			public NdObject retrieveObject(String index) {
				return vertices.get(index).object;
			}
		};
	}

	
	
}
