package org.ws.mts.utils;

public class PostgreSQL {
	public static String stringify(Object o) {
		return String.format("'%s'", o);
	}
}
