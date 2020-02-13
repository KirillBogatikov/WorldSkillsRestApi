package org.ws.mts.utils;

import java.util.Random;

public class Generator {
	private static final String ALPHABET = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
	private static Random random = new Random();
	
	public static String generate(int size) {
		StringBuilder builder = new StringBuilder();
		
		for(int i = 0; i < size; i++) {
			char s = ALPHABET.charAt(random.nextInt(ALPHABET.length()));
			builder.append(s);
		}
		
		return builder.toString();
	}
}
