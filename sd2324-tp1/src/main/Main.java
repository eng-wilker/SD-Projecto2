package main;

import java.io.File;
import java.net.URI;
import java.nio.ByteBuffer;
import java.util.Random;

import tukano.api.User;
import tukano.api.java.Result;
import tukano.impl.grpc.servers.GrpcBlobsServer;
import tukano.impl.grpc.servers.GrpcShortsServer;
import tukano.impl.grpc.servers.GrpcUsersServer;
import tukano.impl.java.clients.Clients;
import utils.Hash;
import utils.Hex;

public class Main {
	
	static {
		System.setProperty("java.net.preferIPv4Stack", "true");
		System.setProperty("java.util.logging.SimpleFormatter.format", "%4$s: %5$s");
	}
	
	public static void main(String[] args ) throws Exception {
		new Thread( () -> {	
			GrpcUsersServer.main( new String[] {} );
		} ).start();		
		new Thread( () -> {	
			GrpcShortsServer.main( new String[] {} );
		} ).start();
		new Thread( () -> {	
			GrpcBlobsServer.main( new String[] {"-port", "9001"} );
		} ).start();
		new Thread( () -> {	
//			GrpcBlobsServer.main( new String[] {"-port", "9002"} );
		} ).start();
		new Thread( () -> {	
//			GrpcBlobsServer.main( new String[] {"-port", "9003"} );
		} ).start();

		Thread.sleep(1000);
		
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
		System.out.println( "------->" + blobUrl );
		
		var blobId = new File( blobUrl.getPath() ).getName();
		System.out.println( "BlobID:" + blobId );
		
		var bytes = randomBytes( 1 );
		
		blobs.forEach( b -> b.upload(blobId, bytes));

		
		var s2id = s2.value().getShortId();
		
		show(shorts.follow("liskov", "wales", true, "12345"));
		show(shorts.followers("wales", "12345"));
		
		show(shorts.like(s2id, "liskov", true, "12345"));
		show(shorts.like(s2id, "liskov", true, "12345"));
		show(shorts.likes(s2id , "12345"));
		show(shorts.getFeed("liskov", "12345"));
		show(shorts.getShort( s2id ));
		
		show(shorts.getShorts( "wales" ));
		
		show(shorts.deleteAllShorts("wales", "12345", ""));

		show(shorts.followers("wales", "12345"));

		show(shorts.getFeed("liskov", "12345"));

		show(shorts.getShort( s2id ));

		
		blobs.forEach( b -> {
			var r = b.download(blobId);
			System.out.println( Hex.of(Hash.sha256( bytes )) + "-->" + Hex.of(Hash.sha256( r.value() )));
			
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
	
	private static byte[] randomBytes(int size) {
		var r = new Random(1L);

		var bb = ByteBuffer.allocate(size);
		
		r.ints(size).forEach( i -> bb.put( (byte)(i & 0xFF)));		

		return bb.array();
		
	}
}
