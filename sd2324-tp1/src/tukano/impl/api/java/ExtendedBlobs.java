package tukano.impl.api.java;

import tukano.api.java.Blobs;
import tukano.api.java.Result;

public interface ExtendedBlobs extends Blobs {

	Result<Void> deleteAllBlobs( String userId );
}
