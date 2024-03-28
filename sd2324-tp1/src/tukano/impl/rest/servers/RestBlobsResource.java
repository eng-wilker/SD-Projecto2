package tukano.impl.rest.servers;

import tukano.api.java.Blobs;
import tukano.api.rest.RestBlobs;
import tukano.impl.java.servers.JavaBlobs;

public class RestBlobsResource extends RestResource implements RestBlobs {

	final Blobs impl;
	public RestBlobsResource() {
		this.impl = new JavaBlobs();
	}
	
	@Override
	public void upload(String blobId, byte[] bytes) {
		super.resultOrThrow( impl.upload(blobId, bytes));
	}

	@Override
	public byte[] download(String blobId) {
		return super.resultOrThrow( impl.download( blobId ));
	}
}
