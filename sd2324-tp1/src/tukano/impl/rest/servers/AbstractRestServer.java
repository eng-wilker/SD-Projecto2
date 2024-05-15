package tukano.impl.rest.servers;

import java.net.InetAddress;
import java.net.URI;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Logger;
import javax.net.ssl.SSLContext;

import org.glassfish.jersey.jdkhttp.JdkHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;

import tukano.impl.discovery.Discovery;
import tukano.impl.java.servers.AbstractServer;
import utils.IP;

public abstract class AbstractRestServer extends AbstractServer {
	private static final String SERVER_BASE_URI = "https://%s:%s%s";
	private static final String REST_CTX = "/rest";

	protected AbstractRestServer(Logger log, String service, int port) {
		super(log, service, String.format(SERVER_BASE_URI, IP.hostAddress(), port, REST_CTX));
	}

	protected void start() {

		ResourceConfig config = new ResourceConfig();

		registerResources(config);

		var nserverURI = URI.create(serverURI.replace(IP.hostAddress(), INETADDR_ANY));

		try {
			JdkHttpServerFactory.createHttpServer(nserverURI, config, SSLContext.getDefault());
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}

		Discovery.getInstance().announce(service, super.serverURI);

		Log.info(String.format("%s Server ready @ %s\n", service, serverURI));
	}

	abstract void registerResources(ResourceConfig config);
}
