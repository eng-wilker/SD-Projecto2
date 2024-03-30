package tukano.impl.java.servers;

import static tukano.api.java.Result.error;
import static tukano.api.java.Result.ok;
import static tukano.api.java.Result.ErrorCode.BAD_REQUEST;
import static tukano.api.java.Result.ErrorCode.FORBIDDEN;
import static tukano.api.java.Result.ErrorCode.NOT_FOUND;

import java.util.List;
import java.util.logging.Logger;

import tukano.api.User;
import tukano.api.java.Result;
import tukano.api.java.Users;
import utils.Hibernate;

public class JavaUsers implements Users {
	private static Logger Log = Logger.getLogger(JavaUsers.class.getName());

	@Override
	public Result<String> createUser(User user) {
		Log.info("createUser : " + user);

		if (user.userId() == null || user.pwd() == null || user.displayName() == null || user.email() == null)
			return error(BAD_REQUEST);

		var res = Hibernate.getInstance().persist(user);
		if (res.isOK())
			return ok(user.userId());
		else
			return error(res.error());
	}

	@Override
	public Result<User> getUser(String userId, String pwd) {
		Log.info("getUser : user = " + userId + "; pwd = " + pwd);

		if (userId == null)
			return error(BAD_REQUEST);


		var res = Hibernate.getInstance().getOne(userId, User.class);
		if (!res.isOK() && res.error() == NOT_FOUND)
			return res;

		var user = res.value();
		if ( ! user.getPwd().equals( pwd ))
			return error(FORBIDDEN);

		return res;
	}

	@Override
	public Result<User> updateUser(String userId, String pwd, User other) {
		Log.info("update : " + userId + ":" + pwd + ":" + other);

		if (userId == null || pwd == null )
			return error(BAD_REQUEST);

		var ures = Hibernate.getInstance().getOne(userId, User.class);
		if( ! ures.isOK() )
			return error( ures.error() );
		
		var user = ures.value();
		if( user.getPwd().equals( pwd ))
			return Hibernate.getInstance().updateOne( user.updateFrom( other ) );
		else
			return error( FORBIDDEN );
	}

	@Override
	public Result<User> deleteUser(String userId, String pwd) {
		Log.info("delete : " + userId + ":" + pwd);

		var res = getUser(userId, pwd);
		if (res.isOK())
			Hibernate.getInstance().deleteOne(res.value());

		return res;
	}

	@Override
	public Result<List<User>> searchUsers(String pattern) {
		Log.info("search : " + pattern);

		final var QUERY_FMT = "SELECT * FROM User u WHERE u.userId LIKE '%s%%'";

		var hits = Hibernate.getInstance()
				.sql(String.format(QUERY_FMT, pattern), User.class)
				.stream()
				.map(User::copyWithoutPassword)
				.toList();

		return ok(hits);
	}
}
