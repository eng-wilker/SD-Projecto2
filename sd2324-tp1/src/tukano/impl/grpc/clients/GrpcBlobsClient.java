package tukano.impl.grpc.clients;

import java.io.ByteArrayOutputStream;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import com.google.protobuf.ByteString;

import tukano.api.java.Result;
import tukano.impl.api.java.ExtendedBlobs;
import tukano.impl.grpc.generated_java.BlobsProtoBuf.DownloadArgs;
import tukano.impl.grpc.generated_java.BlobsProtoBuf.UploadArgs;
import tukano.impl.grpc.generated_java.BlobsGrpc;
import tukano.impl.grpc.generated_java.ShortsGrpc;

public class GrpcBlobsClient extends GrpcClient implements ExtendedBlobs {

	final BlobsGrpc.BlobsBlockingStub _stub;

	public GrpcBlobsClient(String serverURI) {
		super(serverURI);
		_stub = BlobsGrpc.newBlockingStub( super.channel );
	}

	private BlobsGrpc.BlobsBlockingStub stub() {
		return _stub.withDeadlineAfter( GRPC_TIMEOUT, TimeUnit.MILLISECONDS );
	}
	
	private Result<Void> _upload(String blobId, byte[] bytes) {
		return super.toJavaResult(() -> {
			stub().upload( UploadArgs.newBuilder()
				.setData( ByteString.copyFrom(bytes))
				.build());

		});
	}

	private Result<byte[]> _download(String blobId) {
		return super.toJavaResult(() -> {
			var res = stub().download( DownloadArgs.newBuilder()
				.setBlobId(blobId)
				.build());			
			var baos = new ByteArrayOutputStream();
			res.forEachRemaining( chunk -> baos.writeBytes( chunk.toByteArray() ));
			return baos.toByteArray();
		});
	}

	private Result<Void> _downloadToSink(String blobId, Consumer<byte[]> sink) {
		return super.toJavaResult(() -> {
			var res = stub().download( DownloadArgs.newBuilder()
				.setBlobId(blobId)
				.build());
			
			res.forEachRemaining( (chunk) -> sink.accept( chunk.getChunk().toByteArray()));	
		});
	}
	
	private Result<Void> _deleteAllBlobs(String userId) {
//		return super.toJavaResult(() -> {
//			stub.deleteAllBlobs( DeleteAllBlobsArgs.newBuilder()
//				.setUserId(userId)
//				.build());			
//		});	
		return null;
	}
	
	@Override
	public Result<Void> upload(String blobId, byte[] bytes) {
		return super.reTry(() -> _upload(blobId, bytes));
	}

	@Override
	public Result<byte[]> download(String blobId) {
		return super.reTry( () -> _download(blobId));
	}

	@Override
	public Result<Void> downloadToSink(String blobId, Consumer<byte[]> sink) {
		return super.reTry( () -> _downloadToSink(blobId, sink));
	}

	@Override
	public void deleteAllBlobs(String userId) {
		super.reTry( () -> _deleteAllBlobs( userId ) );
	}
	
}
