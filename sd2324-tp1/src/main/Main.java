package main;

import tukano.api.java.Result;
import tukano.impl.java.clients.Clients;

public class Main {
	
	static {
		System.setProperty("java.net.preferIPv4Stack", "true");
		System.setProperty("java.util.logging.SimpleFormatter.format", "%4$s: %5$s");
	}
	
	public static void main(String[] args ) throws Exception {
		
//		var users = Clients.UsersClients.get();
		var shorts = Clients.ShortsClients.get();
		
//		 show(users.createUser( new User("wales", "12345", "jimmy@wikipedia.pt", "Jimmy Wales") ));
//		 
//		 show(users.createUser( new User("liskov", "12345", "liskov@mit.edu", "Barbara Liskov") ));
//		 
//		 show(users.updateUser("wales", "12345", new User("wales", "12345", "jimmy@wikipedia.com", "" ) ));
//		 
//		 show(users.deleteUser("wales", "12345"));
//		 
//		 show(users.searchUsers(""));
		
		show(shorts.createShort("smd", "1234"));
		show(shorts.createShort("nmp", "1234"));
		show(shorts.createShort("nmp", "1234"));
		show(shorts.createShort("nmp", "1234"));
		show(shorts.createShort("nmp", "1234"));
		
		var s2 = "nmp-2";
		
		show(shorts.follow("smd", "nmp", true, "1234"));
		show(shorts.followers("nmp", "1234"));
		
		show(shorts.like(s2, "smd", true, "1234"));
		show(shorts.like(s2, "nmp", true, "1234"));
		show(shorts.likes(s2 , "1234"));
		show(shorts.getFeed("smd", "1234"));
		show(shorts.getShort( s2 ));
		
		show(shorts.getShorts( "nmp" ));
		
		show(shorts.deleteAllShorts("smd", "12345"));

		show(shorts.followers("nmp", "1234"));

		show(shorts.getFeed("smd", "1234"));

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
