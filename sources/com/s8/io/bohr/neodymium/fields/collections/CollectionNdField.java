package com.s8.io.bohr.neodymium.fields.collections;

import java.io.IOException;
import java.io.Writer;

import com.s8.api.bohr.BOHR_Types;
import com.s8.api.objects.repo.RepoS8Object;
import com.s8.io.bohr.neodymium.fields.NdField;
import com.s8.io.bohr.neodymium.handlers.NdHandler;
import com.s8.io.bohr.neodymium.properties.NdFieldProperties;


/**
 * 
 * 
 *
 * @author Pierre Convert
 * Copyright (C) 2022, Pierre Convert. All rights reserved.
 * 
 */
public abstract class CollectionNdField extends NdField {

	

	public final static byte[] SEQUENCE = new byte[] {
			(byte) BOHR_Types.ARRAY,
			(byte) BOHR_Types.S8OBJECT
	};
	


	public CollectionNdField(int ordinal, NdFieldProperties properties, NdHandler handler) {
		super(ordinal, properties, handler);
	}






	public interface ItemConsumer {

		public void consume(RepoS8Object item) throws IOException;
	}


	public abstract void forEach(Object iterable, ItemConsumer consumer) throws IOException;




	private class Printer implements ItemConsumer {

		private boolean isInitialized = false;
		private Writer writer;

		public Printer(Writer writer) {
			super();
			this.writer = writer;
		}

		@Override
		public void consume(RepoS8Object item) throws IOException {
			if(isInitialized) {
				writer.write(" ,");	
			}
			else {
				isInitialized = true;
			}

			if(item!=null) {
				writer.write("(");
				writer.write(item.getClass().getCanonicalName());
				writer.write("): ");
				writer.write(item.S8_id.toString());	
			}
			else {
				writer.write("null");
			}
		}
	}


	@Override
	protected void printValue(RepoS8Object object, Writer writer) throws IOException {
		RepoS8Object[] array = (RepoS8Object[]) handler.get(object);
		if(array!=null) {
			writer.write('[');
			forEach(object, new Printer(writer));
			writer.write(']');
		}
		else {
			writer.write("null");
		}
	}
}
