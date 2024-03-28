package tukano.impl.java.servers;

import static tukano.api.java.Result.error;
import static tukano.api.java.Result.ok;
import static tukano.api.java.Result.ErrorCode.CONFLICT;
import static tukano.api.java.Result.ErrorCode.FORBIDDEN;
import static tukano.api.java.Result.ErrorCode.INTERNAL_ERROR;
import static tukano.api.java.Result.ErrorCode.NOT_FOUND;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.function.Consumer;

import tukano.api.java.Blobs;
import tukano.api.java.Result;
import utils.Hash;
import utils.IO;

public class JavaBlobs implements Blobs {

	private static final int CHUNK_SIZE = 4096;

	@Override
	public Result<Void> upload(String blobId, byte[] bytes) {
		if( ! validBlobId() )
			return error(FORBIDDEN );
		
		var file = new File(blobId);
		if (file.exists()) {
			if (Arrays.equals(Hash.sha256(bytes), Hash.sha256(IO.read(file))))
				return ok();
			else
				return error(CONFLICT);

		}
		IO.write(file, bytes);
		return ok();
	}


	@Override
	public Result<byte[]> download(String blobId) {
		var file = new File(blobId);
		if (file.exists())
			return ok(IO.read(file));
		else
			return error(NOT_FOUND);
	}
	
	

	@Override
	public Result<Void> downloadToSink(String blobId, Consumer<byte[]> sink) {
		if( ! validBlobId() )
			return error(FORBIDDEN );
		
		var file = new File( blobId );
		if( ! file.exists() )
			return error(NOT_FOUND);
		
		try(var fis = new FileInputStream(file)) {
			int n;
			var chunk = new byte[CHUNK_SIZE];
			while( (n = fis.read(chunk)) > 0)
				sink.accept( Arrays.copyOf(chunk, n));

			return ok();
		} catch( IOException x ) {
			return error(INTERNAL_ERROR);
		}
	}
	
	
	private boolean validBlobId() {
		return true;
	}

}
