package com.s8.io.bohr.neodymium.branch.endpoint;

import static com.s8.api.bohr.BOHR_Keywords.CLOSE_JUMP;
import static com.s8.api.bohr.BOHR_Keywords.CLOSE_SEQUENCE;
import static com.s8.api.bohr.BOHR_Keywords.CREATE_NODE;
import static com.s8.api.bohr.BOHR_Keywords.DECLARE_TYPE;
import static com.s8.api.bohr.BOHR_Keywords.DEFINE_JUMP_AUTHOR;
import static com.s8.api.bohr.BOHR_Keywords.DEFINE_JUMP_COMMENT;
import static com.s8.api.bohr.BOHR_Keywords.DEFINE_JUMP_TIMESTAMP;
import static com.s8.api.bohr.BOHR_Keywords.EXPOSE_NODE;
import static com.s8.api.bohr.BOHR_Keywords.OPEN_JUMP;
import static com.s8.api.bohr.BOHR_Keywords.OPEN_SEQUENCE;
import static com.s8.api.bohr.BOHR_Keywords.REMOVE_NODE;
import static com.s8.api.bohr.BOHR_Keywords.UPDATE_NODE;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.s8.api.bohr.BOHR_Keywords;
import com.s8.api.bytes.ByteInflow;
import com.s8.io.bohr.neodymium.branch.NdGraphDelta;
import com.s8.io.bohr.neodymium.codebase.NdCodebase;
import com.s8.io.bohr.neodymium.exceptions.NdIOException;
import com.s8.io.bohr.neodymium.fields.NdFieldDelta;
import com.s8.io.bohr.neodymium.fields.NdFieldParser;
import com.s8.io.bohr.neodymium.object.CreateNdObjectDelta;
import com.s8.io.bohr.neodymium.object.ExposeNdObjectDelta;
import com.s8.io.bohr.neodymium.object.RemoveNdObjectDelta;
import com.s8.io.bohr.neodymium.object.UpdateNdObjectDelta;
import com.s8.io.bohr.neodymium.type.NdType;
import com.s8.io.bohr.neodymium.type.NdTypeParser;


/**
 * 
 *
 * @author Pierre Convert
 * Copyright (C) 2022, Pierre Convert. All rights reserved.
 * 
 */
public class NdInbound {
	
	public interface GraphConsumer {
		
		public void onNewDelta(NdGraphDelta delta) throws IOException;
	}
	
	

	/**
	 * code base
	 */
	private final NdCodebase codebase;



	/**
	 * 
	 */
	private final Map<Long, NdTypeParser> mapByCode;



	private final Map<String, NdTypeParser> mapByVertex = new HashMap<>();



	private long version;

	/**
	 * 
	 * @param codebase
	 * @param graph
	 * @param isVerbose
	 */
	public NdInbound(NdCodebase codebase) {
		super();
		this.codebase = codebase;
		mapByCode = new HashMap<Long, NdTypeParser>();
	}



	/**
	 * 
	 * @param inflow
	 * @throws IOException
	 */
	public void pullFrame(ByteInflow inflow, GraphConsumer consumer) throws IOException {

		// check opening
		if(!inflow.matches(BOHR_Keywords.FRAME_HEADER)) { throw new IOException("DO NOT MATCH HEADER"); }
		parseSequence(inflow, consumer);
		if(!inflow.matches(BOHR_Keywords.FRAME_FOOTER)) { throw new IOException("DO NOT MATCH FOOTER"); }
	}
	
	/**
	 * 
	 * @param inflow
	 * @throws IOException
	 */
	private void parseSequence(ByteInflow inflow, GraphConsumer consumer) throws IOException {

		int code;

		if((code = inflow.getUInt8()) != OPEN_SEQUENCE) {
			throw new IOException("Sequence mist startwith an open sequecne tag");
		}

		while((code = inflow.getUInt8()) != CLOSE_SEQUENCE) {
			switch(code) {

			case OPEN_JUMP: parseBranchDelta(inflow, consumer); break;

			default : throw new IOException("Unsupported BOHR keyword code: "+Integer.toHexString(code));
			}
		}
	}

	/**
	 * 
	 * @param inflow
	 * @throws IOException
	 */
	public NdTypeParser onDeclareType(ByteInflow inflow) throws IOException {

		/* retrieve name of this newly declared type */
		String typeName = inflow.getStringUTF8();

		/* retrieve code assigned to this newly declared type */
		long typeCode = inflow.getUInt7x();

		/* check that this type code has not already been assigned */
		if(mapByCode.containsKey(typeCode)) {
			throw new NdIOException("A type has already defined for code: "+typeCode+"->"+typeName
					+". See "+mapByCode.get(typeCode).print(typeCode));
		}
		
		/* find corresponding type */
		NdType type = codebase.getTypeBySerialName(typeName);
		if(type == null) {
			throw new NdIOException("Failed to find type for name: "+typeName);
		}

		/* create typeInflow*/
		NdTypeParser typeParser = new NdTypeParser(type);

		/* store this typeInflow for later use */
		mapByCode.put(typeCode, typeParser);
		//typeInflowsByClass.put(type.getRuntimeName(), typeInflow);

		return typeParser;
	}


	/**
	 * 
	 * @param code
	 * @return
	 * @throws NdIOException
	 */
	public NdTypeParser getTypeParserByCode(long code) throws NdIOException {
		NdTypeParser typeParser = mapByCode.get(code);
		if(typeParser == null) {
			throw new NdIOException("Failed to find typeInflow for code: "+Long.toHexString(code));
		}
		return typeParser;
	}	



	
	/**
	 * 
	 * @param graph
	 * @param inflow
	 * @throws IOException
	 */
	private void parseBranchDelta(ByteInflow inflow, GraphConsumer consumer) throws IOException {


		int code;

		long version = inflow.getUInt64();
		
		/* last aisggnede leta : int6', since -1 can be used */
		long lastAssignedDelta = inflow.getInt64();
		
		NdGraphDelta delta = new NdGraphDelta(version);
		delta.lastAssignedIndex = lastAssignedDelta;

		while((code = inflow.getUInt8()) != CLOSE_JUMP) {
			switch(code) {
		
			/* <metadatas> */
			case DEFINE_JUMP_TIMESTAMP: onDefineTimestamp(inflow.getUInt64(), delta); break;
			case DEFINE_JUMP_AUTHOR: onDefineAuthor(inflow.getStringUTF8(), delta); break;
			case DEFINE_JUMP_COMMENT: onDefineComment(inflow.getStringUTF8(), delta); break;
			/* </metadatas> */
			
			case DECLARE_TYPE: onDeclareType(inflow); break;
			
			case CREATE_NODE: onCreateNode(inflow, delta); break;
			case UPDATE_NODE: onUpdateNode(inflow, delta); break;
			case EXPOSE_NODE: onExposeNode(inflow, delta); break;
			case REMOVE_NODE: onRemoveNode(inflow, delta); break;

			default : throw new IOException("Unsupported BOHR keyword code: "+Integer.toHexString(code));
			}
		}

		consumer.onNewDelta(delta);
	}

	

	public class N6ObjectListener implements NdTypeParser.DeltaListener {

		private List<NdFieldDelta> deltas;
		
		public N6ObjectListener(List<NdFieldDelta> deltas) {
			super();
			this.deltas = deltas;
		}

		@Override
		public void onFieldValueChange(NdFieldParser parser, ByteInflow inflow) throws IOException {

			/* read value */
			deltas.add(parser.deserializeDelta(inflow));
		}

	}



	
	

	
	private void onDefineTimestamp(long timestamp, NdGraphDelta delta) throws NdIOException {
		delta.setTimestamp(timestamp);
	}
	

	private void onDefineAuthor(String author, NdGraphDelta delta) throws NdIOException {
		delta.setAuthor(author);
	}
	

	private void onDefineComment(String comment, NdGraphDelta delta) throws NdIOException {
		delta.setComment(comment);
	}
	
	private void onCreateNode(ByteInflow inflow, NdGraphDelta delta) throws IOException {

		if(version < 0) { throw new NdIOException("Version is not defined"); }
		
		long typeCode = inflow.getUInt7x();
		
		NdTypeParser typeParser = getTypeParserByCode(typeCode);
		
		String index = inflow.getStringUTF8();
		
		/* create vertex */
		mapByVertex.put(index, typeParser);

		// object delta
		CreateNdObjectDelta objectDelta = new CreateNdObjectDelta(index, typeParser.getType(), new ArrayList<NdFieldDelta>());
		
		N6ObjectListener objectListener = new N6ObjectListener(objectDelta.deltas);

		// type parser
		typeParser.parse(inflow, objectListener);

		delta.objectDeltas.add(objectDelta);
	}

	


	private void onUpdateNode(ByteInflow inflow, NdGraphDelta delta) throws IOException {

		/* index */
		String index = inflow.getStringUTF8();
		
		/* retrieve vertex */
		NdTypeParser typeParser = mapByVertex.get(index);
		
		if(typeParser == null) { throw new IOException("Out of context exception"); }


		/* create update object delta */
		UpdateNdObjectDelta objectDelta = new UpdateNdObjectDelta(index, typeParser.type, new ArrayList<NdFieldDelta>());

		/* listener */
		N6ObjectListener objectListener = new N6ObjectListener(objectDelta.deltas);
		
		// type parser
		typeParser.parse(inflow, objectListener);
		
		/* append */
		delta.appendObjectDelta(objectDelta);
	}


	

	/**
	 * 
	 * @param inflow
	 * @throws IOException
	 */
	private void onExposeNode(ByteInflow inflow, NdGraphDelta delta) throws IOException {
		
		/* index */
		String index = inflow.getStringUTF8();

		/* retrieve slot */
		int slot = inflow.getUInt8();

		delta.appendObjectDelta(new ExposeNdObjectDelta(index, slot));
	}
	
	
	/**
	 * 
	 * @param inflow
	 * @throws IOException
	 */
	private void onRemoveNode(ByteInflow inflow, NdGraphDelta delta) throws IOException {
		/* index */
		String index = inflow.getStringUTF8();

		delta.appendObjectDelta(new RemoveNdObjectDelta(index));
	}

}
