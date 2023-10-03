package com.s8.io.bohr.neodymium.branch.operations;

import java.io.IOException;
import java.io.Writer;

import com.s8.api.objects.repo.RepoS8Object;
import com.s8.io.bohr.atom.S8ShellStructureException;
import com.s8.io.bohr.neodymium.branch.NdGraph;
import com.s8.io.bohr.neodymium.exceptions.NdIOException;
import com.s8.io.bohr.neodymium.type.NdType;

public class PrintNdModule {
	
	/**
	 * 
	 * @throws IOException 
	 */
	public static void print(NdGraph graph, Writer writer) throws IOException {
		
		writer.write("<shell:>\n");
		
		graph.vertices.forEach((index, vertex) -> {
			try {
				NdType type = vertex.type;
				RepoS8Object object = vertex.getObject();
				type.print(object, writer);
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
		writer.write("\n</shell:>");
	}

}
