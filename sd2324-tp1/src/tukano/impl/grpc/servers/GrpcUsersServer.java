package tukano.impl.grpc.servers;

import java.io.IOException;
import java.util.logging.Logger;

import tukano.api.java.Users;

public class GrpcUsersServer extends AbstractGrpcServer {
public static final int PORT = 13456;
	
	private static Logger Log = Logger.getLogger(GrpcUsersServer.class.getName());

	public GrpcUsersServer() {
		super( Log, Users.NAME, PORT, new GrpcUsersServerStub());
	}
	
	public static void main(String[] args) {
		try {
			new GrpcUsersServer().start();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}	
}
