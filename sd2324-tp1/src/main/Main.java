package main;

import java.io.File;
import java.net.URI;

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

		Thread.sleep(5000);
		
		var users = Clients.UsersClients.get();
		var shorts = Clients.ShortsClients.get();
		var blobs = Clients.BlobsClients.all();
		
		 show(users.createUser( new User("wales", "12345", "jimmy@wikipedia.pt", "Jimmy Wales") ));
		 
		 show(users.createUser( new User("liskov", "12345", "liskov@mit.edu", "Barbara Liskov") ));
		 
		 show(users.updateUser("wales", "12345", new User("wales", "12345", "jimmy@wikipedia.com", "" ) ));
		 
		 show(users.deleteUser("wales", "12345"));
		 
		 show(users.searchUsers(""));
		
		
		Result<tukano.api.Short> s1, s2;
		
		
		show(s2 = shorts.createShort("liskov", "12345"));
		
		show(s1 = shorts.createShort("wales", "12345"));
		show(shorts.createShort("wales", "12345"));
		show(shorts.createShort("wales", "12345"));
		show(shorts.createShort("wales", "12345"));
		
		
		var blobUrl = URI.create(s2.value().getBlobUrl());
		System.out.println( blobUrl );
		
		var blobId = new File( blobUrl.getPath() ).getName();
		System.out.println( blobId );
		
		blobs.forEach( b -> b.upload(blobId, new byte[1024]));

		
		var s2id = s2.value().getShortId();
		
		show(shorts.follow("liskov", "wales", true, "12345"));
		show(shorts.followers("wales", "12345"));
		
		show(shorts.like(s2id, "liskov", true, "12345"));
		show(shorts.like(s2id, "liskov", true, "12345"));
		show(shorts.likes(s2id , "12345"));
		show(shorts.getFeed("liskov", "12345"));
		show(shorts.getShort( s2id ));
		
		show(shorts.getShorts( "wales" ));
		
		show(shorts.deleteAllShorts("wales", "12345"));

		show(shorts.followers("wales", "12345"));

		show(shorts.getFeed("liskov", "12345"));

		show(shorts.getShort( s2id ));

		
		blobs.forEach( b -> {
			var r = b.download(blobId);
			System.out.println( r.value().length );
			
		});
		System.exit(0);
	}
	
	
	private static Result<?> show( Result<?> res ) {
		if( res.isOK() )
			System.err.println("OK: " + res.value() );
		else
			System.err.println("ERROR:" + res.error());
		return res;
		
	}
}
