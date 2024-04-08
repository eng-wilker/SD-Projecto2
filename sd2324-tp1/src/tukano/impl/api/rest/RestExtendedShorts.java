package tukano.impl.api.rest;

import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.QueryParam;
import tukano.api.rest.RestShorts;

@Path(RestShorts.PATH)
public interface RestExtendedShorts extends RestShorts {

	String BLOBS = "/blobs";
	String BLOB_ID = "blobId";

	
	@DELETE
	@Path("/{" + USER_ID + "}" + SHORTS)
	void deleteAllShorts(@PathParam(USER_ID) String userId, @QueryParam(PWD) String password);
	
	
	
	@GET
	@Path("/{" + BLOB_ID + "}" + BLOBS )
	void verifyBlobURI(@PathParam(BLOB_ID) String blobId);	
}
