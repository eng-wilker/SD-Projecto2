package tukano.impl.rest.clients;

import tukano.api.java.Blobs;
import tukano.api.java.Result;
import tukano.api.rest.RestBlobs;

public class RestBlobsClient extends RestClient implements Blobs {

	public RestBlobsClient(String serverURI) {
		super(serverURI, RestBlobs.PATH);
	}

	public Result<Void> _upload(String blobId, byte[] bytes) {
		// TODO Auto-generated method stub
		return null;
	}

	public Result<byte[]> _download(String blobId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Result<Void> upload(String blobId, byte[] bytes) {
		return super.reTry( () -> upload(blobId, bytes));
	}

	@Override
	public Result<byte[]> download(String blobId) {
		return super.reTry( () -> download(blobId));
	}

}
