package tukano.impl.grpc.servers;

import java.util.logging.Logger;

import tukano.api.java.Shorts;

public class GrpcShortsServer extends AbstractGrpcServer {
public static final int PORT = 14567;
	
	private static Logger Log = Logger.getLogger(GrpcShortsServer.class.getName());

	public GrpcShortsServer() {
		super( Log, Shorts.NAME, PORT, new GrpcShortsServerStub());
	}
	
	public static void main(String[] args) throws Exception {
		new GrpcShortsServer().start();
	}	
}
