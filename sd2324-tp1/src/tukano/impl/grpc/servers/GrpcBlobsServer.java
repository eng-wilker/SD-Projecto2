package tukano.impl.grpc.servers;

import java.util.logging.Logger;

import tukano.api.java.Users;

public class GrpcBlobsServer extends AbstractGrpcServer {
public static final int PORT = 13456;
	
	private static Logger Log = Logger.getLogger(GrpcBlobsServer.class.getName());

	public GrpcBlobsServer() {
		super( Log, Users.NAME, PORT, new GrpcUsersServerStub());
	}
	
	public static void main(String[] args) throws Exception {
		new GrpcBlobsServer().start();
	}	
}
