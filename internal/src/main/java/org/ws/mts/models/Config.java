package org.ws.mts.models;

public class Config {
	private Database database;
	private Container container;
	private Logging logging;
	private Server server;
	
	public Database getDatabase() {
		return database;
	}

	public void setDatabase(Database database) {
		this.database = database;
	}

	public Container getContainer() {
		return container;
	}

	public void setContainer(Container container) {
		this.container = container;
	}
	
	public Logging getLogging() {
		return logging;
	}
	
	public void setLogging(Logging logging) {
		this.logging = logging;
	}

	public Server getServer() {
		return server;
	}
	
	public void setServer(Server server) {
		this.server = server;
	}
	
	public static class Database {
		private String host;
		private int port;
		private String database;
		private String user;
		private String password;
		
		public String getHost() {
			return host;
		}
		
		public void setHost(String host) {
			this.host = host;
		}
	
		public int getPort() {
			return port;
		}
	
		public void setPort(int port) {
			this.port = port;
		}
	
		public String getDatabase() {
			return database;
		}
	
		public void setDatabase(String database) {
			this.database = database;
		}
	
		public String getUser() {
			return user;
		}
	
		public void setUser(String user) {
			this.user = user;
		}
	
		public String getPassword() {
			return password;
		}
	
		public void setPassword(String password) {
			this.password = password;
		}
	}
	
	public static class Container {
		private String path;
		
		public String getPath() {
			return path;
		}
		
		public void setPath(String path) {
			this.path = path;
		}
		
	}
	
	public static class Server {
		private String host;
		private boolean ssl;
		
		public String getHost() {
			return host;
		}
		
		public void setHost(String host) {
			this.host = host;
		}
		
		public boolean useSsl() {
			return ssl;
		}
		
		public void setSsl(boolean ssl) {
			this.ssl = ssl;
		}
	}
	
	public static class Logging {
		private String info;
		private String debug;
		private String error;
		private String warn;
		
		public String getInfo() {
			return info;
		}
		
		public void setInfo(String info) {
			this.info = info;
		}
		
		public String getDebug() {
			return debug;
		}
		
		public void setDebug(String debug) {
			this.debug = debug;
		}
		
		public String getError() {
			return error;
		}
		
		public void setError(String error) {
			this.error = error;
		}
		
		public String getWarn() {
			return warn;
		}
		
		public void setWarn(String warn) {
			this.warn = warn;
		}
		
	}
	
}
