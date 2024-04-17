package tukano.impl.api.java;

import java.io.File;
import java.net.URI;

import tukano.api.java.Blobs;
import tukano.api.java.Result;

public interface ExtendedBlobs extends Blobs {

	Result<Void> delete( String blobId, String token );
	
	Result<Void> deleteAllBlobs( String userId, String token );
	
	default Result<Void> deleteUrl( String url, String token ) {
		var uri = URI.create( url );
		var blobId = new File( uri.getPath() ).getName();
		return delete( blobId, token);
	} 
}
