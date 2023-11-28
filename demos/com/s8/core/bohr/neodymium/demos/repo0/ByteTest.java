package com.s8.core.bohr.neodymium.demos.repo0;

public class ByteTest {

	public static void main(String[] args) {

		byte b = (byte) 0xf5;
		System.out.println(b);
		int val = 0xff & b;
		System.out.println(val);
	}

}
