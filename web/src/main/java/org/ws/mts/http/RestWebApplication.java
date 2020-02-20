package org.ws.mts.http;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Map;
import java.util.Scanner;

import javax.servlet.MultipartConfigElement;

import org.cuba.log.Configurator;
import org.cuba.log.Log;
import org.cuba.log.stream.LogPrintStream;
import org.cuba.log.stream.LogStream;
import org.cuba.log.stream.PooledLogStream;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.MultipartConfigFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.util.unit.DataSize;
import org.ws.mts.database.DatabaseContext;
import org.ws.mts.models.Config;
import org.ws.mts.models.Config.Container;
import org.ws.mts.models.Config.Database;
import org.ws.mts.models.Config.Logging;
import org.ws.mts.models.Config.Server;
import org.ws.mts.service.AuthService;
import org.ws.mts.service.AuthServiceImpl;
import org.ws.mts.service.PhotoService;
import org.ws.mts.service.PhotoServiceImpl;
import org.ws.mts.service.UserService;
import org.ws.mts.service.UserServiceImpl;

import com.google.gson.Gson;

@ComponentScan
@SpringBootApplication
public class RestWebApplication {	
	private static Config config;
	private Log log;
	private DatabaseContext dbctx;
	
	private void configureLog() throws IOException {
		Logging logging = config.getLogging();
		Configurator logConfigurator = new Configurator();
		
		LogStream systemOutStream = new LogPrintStream(System.out);
		LogStream systemErrStream = new LogPrintStream(System.err);
		
		PooledLogStream info = new PooledLogStream();
		info.register(systemOutStream);
		info.register(logToFile(logging.getInfo()));
		logConfigurator.info(info);
		
		PooledLogStream debug = new PooledLogStream();
		debug.register(systemOutStream);
		debug.register(logToFile(logging.getDebug()));
		logConfigurator.debug(debug);
		
		PooledLogStream error = new PooledLogStream();
		error.register(systemErrStream);
		error.register(logToFile(logging.getError()));
		logConfigurator.error(error);
		
		PooledLogStream warn = new PooledLogStream();
		warn.register(systemErrStream);
		warn.register(logToFile(logging.getWarn()));
		logConfigurator.warn(warn);
		
		log = new Log(logConfigurator.build());
	}
	
	private void configureDatabase() {
		Database dbcfg = config.getDatabase();
		dbctx = new DatabaseContext(log, dbcfg.getHost(), dbcfg.getPort(), dbcfg.getDatabase(), dbcfg.getUser(), dbcfg.getPassword());
	}
	
	private static void fileConfig() throws Exception {
		File file = new File("ws_mts_cfg.json");
		if(!file.exists()) {
			throw new RuntimeException("Config file ws_mts_cfg.json is missed");
		}
		
		Gson gson = new Gson();
		try(FileReader reader = new FileReader(file)) {
			config = gson.fromJson(reader, Config.class);
		}
	}
	
	private static void envConfig() throws Exception {
		config = new Config();
		Database db = new Database();
		
		Map<String, String> env = System.getenv();
		String uri = env.get("DATABASE_URL");
        
        String[] colonParts = uri.split(":");
        String[] atSignParts = colonParts[2].split("@");
        String[] slashParts = colonParts[3].split("/");
        
        String user = colonParts[1].substring(2);
        String password  = atSignParts[0];
        String host = atSignParts[1];
        String port = slashParts[0];
        String database = slashParts[1];
        
        db.setHost(host);
        db.setPort(Integer.valueOf(port));
        db.setUser(user);
        db.setPassword(password);
		db.setDatabase(database);
		config.setDatabase(db);
		
		Container container = new Container();
		container.setPath(env.get("CONTAINER"));
		config.setContainer(container);
		
		Logging logging = new Logging();
		logging.setDebug(env.get("LOG_FILE"));
		logging.setError(env.get("LOG_FILE"));
		logging.setWarn(env.get("LOG_FILE"));
		logging.setInfo(env.get("LOG_FILE"));
		config.setLogging(logging);
		
		Server server = new Server();
		server.setHost(env.get("HOST"));
		server.setSsl(true);
		config.setServer(server);
	}
	
	private static void autoConfig(String key, String host) {
		config = new Config();
		
		System.out.println(config);
		
		Database db = new Database();
		db.setHost("localhost");
        db.setPort(5432);
        db.setUser(key);
        db.setPassword(key);
		db.setDatabase(key);
		config.setDatabase(db);

		File keyDir = new File(key);
		keyDir.mkdir();
		
		Container container = new Container();
		File files = new File(keyDir, "uploads");
		files.mkdir();
		container.setPath(files.getAbsolutePath());
		config.setContainer(container);
		
		Logging logging = new Logging();
		File logs = new File(keyDir, "logs");
		logs.mkdir();
		logging.setDebug(new File(logs, "debug.log").getAbsolutePath());
		logging.setError(new File(logs, "error.log").getAbsolutePath());
		logging.setWarn(new File(logs, "warn.log").getAbsolutePath());
		logging.setInfo(new File(logs, "info.log").getAbsolutePath());
		config.setLogging(logging);
		
		Server server = new Server();
		server.setHost(host);
		server.setSsl(true);
		config.setServer(server);
	}
	
	public RestWebApplication() throws Exception {
		configureLog();
		configureDatabase();
	}

	@Bean
	public Log getLog() {
		return log;
	}
	
	@Bean
    public MultipartConfigElement multipartConfigElement() {
        MultipartConfigFactory factory = new MultipartConfigFactory();
        factory.setMaxFileSize(DataSize.parse("32MB"));
        factory.setMaxRequestSize(DataSize.parse("32MB"));
        return factory.createMultipartConfig();
    }
	
	@Bean
	public Config getConfiguration() {
		return config;
	}

	@Bean
	public DatabaseContext getDbContext() {
		return dbctx;
	}
	
	@Bean
	public AuthService getAuthService() throws SQLException {
		return new AuthServiceImpl(dbctx);
	}
	
	@Bean
	public UserService getUserService() throws SQLException {
		return new UserServiceImpl(dbctx);
	}
	
	@Bean 
	PhotoService getPhotoService() throws SQLException {
		Container container = config.getContainer();
		return new PhotoServiceImpl(dbctx, container.getPath());
	}
		
	private LogStream logToFile(String path) throws IOException {
		return new LogPrintStream(new PrintStream(path));
	}
	
	public static void main(String[] args) throws Exception {
		try {
			if(args.length > 0) {
				switch(args[0]) {
					case "auto": autoConfig(args[1], args[2]); break;
					case "file": fileConfig(); break;
					case "env": envConfig(); break;
				}
			} else {
				envConfig();
			}
			
			SpringApplication.run(RestWebApplication.class, args);
		} catch(Exception e) {
			System.err.println("FATAL EXCEPTION OCCURRED");
			System.out.println(Arrays.toString(args));
			System.err.println("========================================");
			e.printStackTrace();
			Scanner scanner = new Scanner(System.in);
			scanner.useDelimiter("");
			System.err.println("========================================");
			System.err.println("PRESS ENTER TO EXIT");
			scanner.next();
			scanner.close();
		}
	}
}
