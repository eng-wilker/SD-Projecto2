package tukano.impl.grpc.clients;

import java.io.ByteArrayOutputStream;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import com.google.protobuf.ByteString;

import tukano.api.java.Blobs;
import tukano.api.java.Result;
import tukano.impl.api.java.ExtendedBlobs;
import tukano.impl.grpc.generated_java.BlobsGrpc;
import tukano.impl.grpc.generated_java.BlobsProtoBuf.DownloadArgs;
import tukano.impl.grpc.generated_java.BlobsProtoBuf.UploadArgs;

public class GrpcBlobsClient extends GrpcClient implements ExtendedBlobs {

	final BlobsGrpc.BlobsBlockingStub stub;

	public GrpcBlobsClient(String serverURI) {
		super(serverURI);
		stub = BlobsGrpc.newBlockingStub( super.channel )
				.withDeadlineAfter(GRPC_TIMEOUT, TimeUnit.MILLISECONDS);
	}

	public Result<Void> _upload(String blobId, byte[] bytes) {
		return super.toJavaResult(() -> {
			stub.upload( UploadArgs.newBuilder()
				.setData( ByteString.copyFrom(bytes))
				.build());

		});
	}

	public Result<byte[]> _download(String blobId) {
		return super.toJavaResult(() -> {
			var res = stub.download( DownloadArgs.newBuilder()
				.setBlobId(blobId)
				.build());			
			var baos = new ByteArrayOutputStream();
			res.forEachRemaining( chunk -> baos.writeBytes( chunk.toByteArray() ));
			return baos.toByteArray();
		});
	}

	public Result<Void> _downloadToSink(String blobId, Consumer<byte[]> sink) {
		return super.toJavaResult(() -> {
			var res = stub.download( DownloadArgs.newBuilder()
				.setBlobId(blobId)
				.build());
			
			res.forEachRemaining( (chunk) -> sink.accept( chunk.getChunk().toByteArray()));	
		});
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
		// TODO Auto-generated method stub
		
	}
	
}
