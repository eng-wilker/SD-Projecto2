package main;

import tukano.api.User;
import tukano.api.java.Result;
import tukano.impl.java.clients.Clients;
import tukano.impl.rest.servers.RestBlobsServer;
import tukano.impl.rest.servers.RestShortsServer;
import tukano.impl.rest.servers.RestUsersServer;

public class Main {
	
	static {
		System.setProperty("java.net.preferIPv4Stack", "true");
		System.setProperty("java.util.logging.SimpleFormatter.format", "%4$s: %5$s");
	}
	
	public static void main(String[] args ) throws Exception {
		new Thread( () -> {	
			RestUsersServer.main( new String[] {} );
		} ).start();		
		new Thread( () -> {	
			RestShortsServer.main( new String[] {} );
		} ).start();
		new Thread( () -> {	
			RestBlobsServer.main( new String[] {"-port", "9001"} );
		} ).start();
		new Thread( () -> {	
			RestBlobsServer.main( new String[] {"-port", "9002"} );
		} ).start();
		new Thread( () -> {	
			RestBlobsServer.main( new String[] {"-port", "9003"} );
		} ).start();

		Thread.sleep(3000);
		
		var users = Clients.UsersClients.get();
		var shorts = Clients.ShortsClients.get();
		
		 show(users.createUser( new User("wales", "12345", "jimmy@wikipedia.pt", "Jimmy Wales") ));
		 
		 show(users.createUser( new User("liskov", "12345", "liskov@mit.edu", "Barbara Liskov") ));
		 
		 show(users.updateUser("wales", "12345", new User("wales", "12345", "jimmy@wikipedia.com", "" ) ));
		 
		 show(users.deleteUser("wales", "12345"));
		 
		 show(users.searchUsers(""));
		
		show(shorts.createShort("liskov", "1234"));
		show(shorts.createShort("wales", "1234"));
		show(shorts.createShort("wales", "1234"));
		show(shorts.createShort("wales", "1234"));
		show(shorts.createShort("wales", "1234"));
		
		var s2 = "wales-2";
		
		show(shorts.follow("liskov", "wales", true, "1234"));
		show(shorts.followers("wales", "1234"));
		
		show(shorts.like(s2, "liskov", true, "1234"));
		show(shorts.like(s2, "liskov", true, "1234"));
		show(shorts.likes(s2 , "1234"));
		show(shorts.getFeed("liskov", "1234"));
		show(shorts.getShort( s2 ));
		
		show(shorts.getShorts( "wales" ));
		
		show(shorts.deleteAllShorts("wales", "12345"));

		show(shorts.followers("wales", "1234"));

		show(shorts.getFeed("liskov", "1234"));

		show(shorts.getShort( s2 ));

	}
	
	
	private static Result<?> show( Result<?> res ) {
		if( res.isOK() )
			System.err.println("OK: " + res.value() );
		else
			System.err.println("ERROR:" + res.error());
		return res;
		
	}
}
