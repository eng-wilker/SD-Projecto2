package tukano.impl.api.rest;

import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import tukano.api.rest.RestShorts;

@Path(RestShorts.PATH)
public interface RestExtendedBlobs extends RestShorts {

	@DELETE
	@Path("/{" + USER_ID + "}")
	void deleteAllBlobs(@PathParam(USER_ID) String userId);
		
}
