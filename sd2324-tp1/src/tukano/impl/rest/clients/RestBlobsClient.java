package tukano.impl.rest.clients;

import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.MediaType;
import tukano.api.java.Result;
import tukano.api.rest.RestBlobs;
import tukano.impl.api.java.ExtendedBlobs;

public class RestBlobsClient extends RestClient implements ExtendedBlobs {

	public RestBlobsClient(String serverURI) {
		super(serverURI, RestBlobs.PATH);
	}

	private Result<Void> _upload(String blobId, byte[] bytes) {
		return super.toJavaResult(
				target.path(blobId)
				.request()
				.post( Entity.entity(bytes, MediaType.APPLICATION_OCTET_STREAM_TYPE)));
	}

	private Result<byte[]> _download(String blobId) {
		return super.toJavaResult(
				target.path(blobId)
				.request()
				.accept(MediaType.APPLICATION_OCTET_STREAM_TYPE)
				.get(), byte[].class);
	}

	private Result<Void> _deleteAllBlobs(String userId) {
		return super.toJavaResult(
				target.path(userId)
				.request()
				.delete());
	}

	
	@Override
	public Result<Void> upload(String blobId, byte[] bytes) {
		return super.reTry( () -> _upload(blobId, bytes));
	}

	@Override
	public Result<byte[]> download(String blobId) {
		return super.reTry( () -> _download(blobId));
	}


	@Override
	public void deleteAllBlobs(String userId) {
		super.reTry( () -> _deleteAllBlobs(userId));
	}
}
