package tukano.impl.rest.clients;

import static tukano.api.java.Result.error;
import static tukano.api.java.Result.ok;

import java.util.List;

import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.GenericType;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import tukano.api.User;
import tukano.api.java.Result;
import tukano.api.java.Users;
import tukano.api.rest.RestUsers;


public class RestUsersClient extends RestClient implements Users {

	public RestUsersClient( String serverURI ) {
		super( serverURI, RestUsers.PATH );
	}
		
	private Result<String> _createUser(User user) {
		Response r = target.request()
				.accept(MediaType.APPLICATION_JSON)
				.post(Entity.entity(user, MediaType.APPLICATION_JSON));

		var status = r.getStatus();
		if( status != Status.OK.getStatusCode() )
			return error( getErrorCodeFrom(status));
		else
			return ok( r.readEntity( String.class ));
	}

	private Result<User> _getUser(String userId, String pwd) {
		Response r = target.path( userId )
				.queryParam(RestUsers.PWD, pwd).request()
				.accept(MediaType.APPLICATION_JSON)
				.get();

		var status = r.getStatus();
		if( status != Status.OK.getStatusCode() )
			return error( getErrorCodeFrom(status));
		else
			return ok( r.readEntity( User.class ));
	}
	
	public Result<User> _updateUser(String userId, String password, User user) {
		Response r = target
				.path( userId )
				.queryParam(RestUsers.PWD, password)
				.request()
				.accept(MediaType.APPLICATION_JSON)
				.put(Entity.entity(user, MediaType.APPLICATION_JSON));

		var status = r.getStatus();
		if( status != Status.OK.getStatusCode() )
			return error( getErrorCodeFrom(status));
		else
			return ok( r.readEntity( User.class ));
	}

	public Result<User> _deleteUser(String userId, String password) {
		Response r = target
				.path( userId )
				.queryParam(RestUsers.PWD, password)
				.request()
				.accept(MediaType.APPLICATION_JSON)
				.delete();

		var status = r.getStatus();
		if( status != Status.OK.getStatusCode() )
			return error( getErrorCodeFrom(status));
		else
			return ok( r.readEntity( User.class ));
	}

	public Result<List<User>> _searchUsers(String pattern) {
		Response r = target
				.queryParam(RestUsers.QUERY, pattern)
				.request()
				.accept(MediaType.APPLICATION_JSON)
				.get();

		var status = r.getStatus();
		if( status != Status.OK.getStatusCode() )
			return error( getErrorCodeFrom(status));
		else
			return ok( r.readEntity( new GenericType<List<User>>() {}));
	}

	@Override
	public Result<String> createUser(User user) {
		return super.reTry( () -> _createUser(user));
	}

	@Override
	public Result<User> getUser(String userId, String pwd) {
		return super.reTry( () -> _getUser(userId, pwd));
	}

	@Override
	public Result<User> updateUser(String userId, String pwd, User user) {
		return super.reTry( () -> _updateUser(userId, pwd, user));
	}

	@Override
	public Result<User> deleteUser(String userId, String pwd) {
		return super.reTry( () -> _deleteUser(userId, pwd));
	}

	@Override
	public Result<List<User>> searchUsers(String pattern) {
		return super.reTry( () -> _searchUsers(pattern));
	}
}
