package com.s8.core.bohr.neodymium.io;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import com.s8.api.bytes.ByteInflow;
import com.s8.core.bohr.neodymium.branch.NdBranch;
import com.s8.core.bohr.neodymium.branch.endpoint.NdInbound;
import com.s8.core.bohr.neodymium.branch.endpoint.NdOutbound;
import com.s8.core.bohr.neodymium.codebase.NdCodebase;
import com.s8.core.bohr.neodymium.repository.NdRepositoryMetadata;
import com.s8.core.io.bytes.linked.LinkedByteInflow;
import com.s8.core.io.bytes.linked.LinkedByteOutflow;
import com.s8.core.io.bytes.linked.LinkedBytes;
import com.s8.core.io.bytes.linked.LinkedBytesIO;
import com.s8.core.io.json.JSON_Lexicon;
import com.s8.core.io.json.types.JSON_CompilingException;
import com.s8.core.io.json.utilities.JOOS_BufferedFileReader;
import com.s8.core.io.json.utilities.JOOS_BufferedFileWriter;

/**
 * 
 */
public class NdIOModule {


	public final NdCodebase codebase;

	public final JSON_Lexicon lexicon;




	/**
	 * 
	 * @param codebase
	 * @throws JSON_CompilingException 
	 */
	public NdIOModule(NdCodebase codebase) throws JSON_CompilingException {
		super();
		this.codebase = codebase;
		this.lexicon = JSON_Lexicon.from(NdRepositoryMetadata.class); 
	}


	/**
	 * 
	 * @param metadata
	 * @param resourceFolderPath
	 * @throws IOException
	 */
	public void writeMetadata(NdRepositoryMetadata metadata, Path path) throws IOException {
		if(metadata.nIO_hasUnsavedChanges) {
			FileChannel channel = FileChannel.open(path, new OpenOption[]{ 
					StandardOpenOption.WRITE
			});

			JOOS_BufferedFileWriter writer = new JOOS_BufferedFileWriter(channel, StandardCharsets.UTF_8, 256);

			lexicon.compose(writer, metadata, "   ", false);

			writer.close();
			channel.close();

			metadata.nIO_hasUnsavedChanges = false;
		}
	}



	public NdRepositoryMetadata readMetadata(Path path) throws IOException {

		FileChannel channel = FileChannel.open(path, new OpenOption[]{ 
				StandardOpenOption.READ
		});

		/**
		 * lexicon
		 */

		JOOS_BufferedFileReader reader = new JOOS_BufferedFileReader(channel, StandardCharsets.UTF_8, 64);

		NdRepositoryMetadata metadata = (NdRepositoryMetadata) lexicon.parse(reader, true);

		reader.close();
		channel.close();

		return metadata;
	}
	
	

	/**
	 * 
	 * @param id
	 * @return
	 */
	public NdBranch createBranch(String id) {
		return new NdBranch(codebase, id);
	}


	

	/**
	 * 
	 * @param codebase
	 * @param repoPath
	 * @param id
	 * @param isVerbose
	 * @return
	 * @throws IOException
	 */
	public NdBranch readBranch(Path path, String id, boolean isVerbose) throws IOException {

		LinkedBytes head = LinkedBytesIO.read(path, isVerbose);

		/* build inflow */
		ByteInflow inflow = new LinkedByteInflow(head);

		/* build inbound session */
		NdInbound inbound = new NdInbound(codebase);

		/* build branch */
		NdBranch branch = new NdBranch(codebase, id);

		/* load branch */
		inbound.pullFrame(inflow, delta -> branch.appendDelta(delta));

		return branch;
	}

	
	
	
	public void writeBranch(NdBranch branch, Path path) throws IOException {		
		if(branch.nIO_hasUnsavedChanges) {
			
			Files.createDirectories(path);
	
			/* build inflow */
			LinkedByteOutflow outflow = new LinkedByteOutflow();

			/* build outbound session */
			NdOutbound outbound = new NdOutbound(codebase);

			/* push branch */
			outbound.pushFrame(outflow, branch.getSequence());

			/* write to disk */

			LinkedBytesIO.write(outflow.getHead(), path, true);
			
			branch.nIO_hasUnsavedChanges = false;
		}
	}




}
