package org.ws.mts.http;

import java.io.IOException;
import java.io.PrintStream;
import java.sql.SQLException;

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

@ComponentScan
@SpringBootApplication
public class RestWebApplication {	
	private Config config;
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
	
	private void loadConfig() throws Exception {
		/*File file = new File("ws_mts_cfg.json");
		if(!file.exists()) {
			throw new RuntimeException("Config file ws_mts_cfg.json is missed");
		}
		
		Gson gson = new Gson();
		try(FileReader reader = new FileReader(file)) {
			config = gson.fromJson(reader, Config.class);
		}*/
		
		config = new Config();
		Database db = new Database();
		
		String uri = System.getenv().get("DATABASE_URL");
        
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
		container.setPath("uploads/");
		config.setContainer(container);
		
		Logging logging = new Logging();
		logging.setDebug("server.log");
		logging.setError("server.log");
		logging.setWarn("server.log");
		logging.setInfo("server.log");
		config.setLogging(logging);
		
		Server server = new Server();
		server.setHost("ws-mts-api.herokuapp.com");
		server.setSsl(true);
		config.setServer(server);
	}
	
	public RestWebApplication() throws Exception {
		loadConfig();
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
	
	@Bean PhotoService getPhotoService() throws SQLException {
		Container container = config.getContainer();
		return new PhotoServiceImpl(dbctx, container.getPath());
	}
	
	private LogStream logToFile(String path) throws IOException {
		return new LogPrintStream(new PrintStream(path));
	}
	
	public static void main(String[] args) {
		SpringApplication.run(RestWebApplication.class, args);
	}
}
