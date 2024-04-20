package tukano.impl.grpc.clients;

import java.io.ByteArrayOutputStream;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import com.google.protobuf.ByteString;

import io.grpc.Deadline;
import tukano.api.java.Result;
import tukano.impl.api.java.ExtendedBlobs;
import tukano.impl.grpc.generated_java.BlobsGrpc;
import tukano.impl.grpc.generated_java.BlobsProtoBuf.DeleteAllBlobsArgs;
import tukano.impl.grpc.generated_java.BlobsProtoBuf.DeleteArgs;
import tukano.impl.grpc.generated_java.BlobsProtoBuf.DownloadArgs;
import tukano.impl.grpc.generated_java.BlobsProtoBuf.UploadArgs;

public class GrpcBlobsClient extends GrpcClient implements ExtendedBlobs {

	final BlobsGrpc.BlobsBlockingStub _stub;

	public GrpcBlobsClient(String serverURI) {
		super(serverURI);
		_stub = BlobsGrpc.newBlockingStub( super.channel );
	}

	private BlobsGrpc.BlobsBlockingStub stub() {
		return _stub.withDeadline(Deadline.after(30L, TimeUnit.SECONDS));
	}
	
	private Result<Void> _upload(String blobId, byte[] bytes) {
		return super.toJavaResult(() -> {
			stub().upload( UploadArgs.newBuilder()
				.setBlobId( blobId )
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
			res.forEachRemaining( part -> {
				baos.writeBytes( part.getChunk().toByteArray() );
			});
			return baos.toByteArray();
		});
	}

	private Result<Void> _downloadToSink(String blobId, Consumer<byte[]> sink) {
		return super.toJavaResult(() -> {
			var res = stub().download( DownloadArgs.newBuilder()
				.setBlobId(blobId)
				.build());
			
			res.forEachRemaining( (part) -> sink.accept( part.getChunk().toByteArray()));	
		});
	}
	
	private Result<Void> _deleteAllBlobs(String userId, String token) {
		return super.toJavaResult(() -> {
			stub().deleteAllBlobs( DeleteAllBlobsArgs.newBuilder()
				.setUserId(userId)
				.setToken( token)
				.build());			
		});	
	}
	
	private Result<Void> _delete(String blobId, String token) {
		return super.toJavaResult(() -> {
			stub().delete( DeleteArgs.newBuilder()
				.setBlobId(blobId)
				.setToken(token)
				.build());			
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
	public Result<Void> deleteAllBlobs(String userId, String password) {
		return super.reTry( () -> _deleteAllBlobs( userId, password ) );
	}

	@Override
	public Result<Void> delete(String blobId, String token) {
		return super.reTry( () -> _delete( blobId, token ) );
	}
	
}
