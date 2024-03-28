package tukano.impl.java.servers;

import java.io.IOException;
import java.util.logging.Logger;


public abstract class AbstractServer {
	protected static final String INETADDR_ANY = "0.0.0.0";

	final protected Logger Log;
	final protected String serverURI;
	final protected String service;
	
	protected AbstractServer(Logger log, String service, String serverURI) {
		this.Log = log;
		this.service = service;
		this.serverURI = serverURI;
	}
		
	abstract protected void start() throws IOException;
	
	static {
		System.setProperty("java.net.preferIPv4Stack", "true");
		System.setProperty("java.util.logging.SimpleFormatter.format", "%4$s: %5$s");
	}
}
