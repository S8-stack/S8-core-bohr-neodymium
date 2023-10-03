package com.s8.io.bohr.neodymium.branch.operations;

import java.io.IOException;
import java.io.Writer;
import java.util.Map;

import com.s8.api.objects.repo.RepoS8Object;
import com.s8.io.bohr.atom.S8ShellStructureException;
import com.s8.io.bohr.neodymium.branch.NdGraph;
import com.s8.io.bohr.neodymium.exceptions.NdIOException;
import com.s8.io.bohr.neodymium.object.NdVertex;
import com.s8.io.bohr.neodymium.type.NdType;

public class CompareNdModule {
	
	

	/**
	 * 
	 * @throws S8ShellStructureException 
	 * @throws IOException 
	 */
	public static void deepCompare(NdGraph base, NdGraph target, Writer writer) throws IOException, S8ShellStructureException {
		
		writer.write("<delta-tracking:>\n");
		
		
		
		Map<String, NdVertex> baseVertices = base.vertices;
		
		target.vertices.forEach((id, vertex) -> {
			try {
				NdType type = vertex.type;
				
				NdVertex baseVertex = baseVertices.get(id);
				if(baseVertex == null) {
					writer.append("Object replace a null: "+id+"\n");
				}
				else if(!baseVertex.type.getSerialName().equals(type.getSerialName())) {
					writer.append("Object replacement: "+id+"\n");
				}
				else {
					RepoS8Object baseObject = baseVertex.getObject();
					type.deepCompare(baseObject, vertex.getObject(), writer);
				}
			} 
			catch (NdIOException e) {
				e.printStackTrace();
			} 
			catch (IOException e) {
				e.printStackTrace();
			} 
			catch (S8ShellStructureException e) {
				e.printStackTrace();
			}
		});
		
		
		/* <exposure> */
		RepoS8Object[] baseExposure = base.exposure;
		int baseRange = baseExposure.length;
		RepoS8Object[] exposure = target.exposure;
		int range = exposure.length;
		if(range != baseRange) {
			writer.append("Exposure length is not the same: "+range+"\n");
		}
		

		int r = baseRange < range ? baseRange : range;
		
		/* common part of range */
		for(int slot = 0; slot < r; slot++) {
			RepoS8Object exposed = exposure[slot];
			RepoS8Object b = baseExposure[slot];
			if(exposed != null && 
				(b == null || !b.S8_id.equals(exposed.S8_id))) {
				writer.append("Exposure is not the same: "+b.S8_id+"\n");
			}
		}
		
	
	/* <exposure> */
		
		
		writer.write("</delta-tracking>\n");
		writer.append("DEEP COMPARE TERMINATED\n\n");
	}

}
