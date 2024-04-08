package tukano.impl.java.servers;


import static tukano.api.java.Result.error;
import static tukano.api.java.Result.ok;
import static tukano.api.java.Result.ErrorCode.BAD_REQUEST;
import static tukano.api.java.Result.ErrorCode.FORBIDDEN;
import static tukano.api.java.Result.ErrorCode.NOT_FOUND;
import static tukano.api.java.Result.ErrorCode.TIMEOUT;
import static tukano.impl.java.clients.Clients.BlobsClients;
import static tukano.impl.java.clients.Clients.UsersClients;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import tukano.api.Short;
import tukano.api.User;
import tukano.api.java.Result;
import tukano.api.java.Result.ErrorCode;
import tukano.impl.api.java.ExtendedShorts;
import tukano.impl.java.servers.data.Following;
import tukano.impl.java.servers.data.Likes;
import utils.Hibernate;

public class JavaShorts implements ExtendedShorts {
	AtomicLong counter = new AtomicLong(1000L);
	
	private static final long USER_CACHE_EXPIRATION = 3000;

	static record Credentials(String userId, String pwd) {
		static Credentials from(String userId, String pwd) {
			return new Credentials(userId, pwd);
		}
	}

	protected final LoadingCache<Credentials, Result<User>> usersCache = CacheBuilder.newBuilder()
			.expireAfterWrite(Duration.ofMillis(USER_CACHE_EXPIRATION)).removalListener((e) -> {
			}).build(new CacheLoader<>() {
				@Override
				public Result<User> load(Credentials u) throws Exception {
					var res = UsersClients.get().getUser(u.userId(), u.pwd());
					if (res.error() == TIMEOUT)
						return error(BAD_REQUEST);
					return res;
				}
			});
	
	protected final LoadingCache<String, Result<Short>> shortsCache = CacheBuilder.newBuilder()
			.expireAfterWrite(Duration.ofMillis(USER_CACHE_EXPIRATION)).removalListener((e) -> {
			}).build(new CacheLoader<>() {
				@Override
				public Result<Short> load(String shortId) throws Exception {
					
					final var QUERY_FMT = "SELECT count(*) FROM Likes l WHERE l.shortId = '%s'";		
					var query = String.format(QUERY_FMT, shortId);
					var likes = Hibernate.getInstance().sql(query, Long.class);
					
					var shrt = Hibernate.getInstance().getOne(shortId, Short.class);
					if( ! shrt.isOK() )
						return error( shrt.error() );					
					return ok( shrt.value().copyWith( likes.get(0) ) );
				}
			});
	
	
	@Override
	public Result<Short> createShort(String userId, String password) {
		var ures = getUser(userId, password);
		if( ! ures.isOK() )
			return error( ures.error() );
		
		var shortId = String.format("%s-%d", userId, counter.incrementAndGet());
		var blobUrl = String.format("%s/%s", getLeastLoadedBlobServerURI(), shortId); 
		var shrt = new Short(shortId, userId, blobUrl);
		var res = Hibernate.getInstance().persist( shrt );
		
		if( res.isOK() )
			return ok( shrt );
		else
			return error( res.error() );
	}

	

	@Override
	public Result<Short> getShort(String shortId) {
		if( shortId == null )
			return error(BAD_REQUEST);

		return shortFromCache(shortId);
	}

	
	@Override
	public Result<Void> deleteShort(String shortId, String password) {
		var sres = getShort( shortId );
		if( ! sres.isOK() )
			return error( sres.error() );
		
		var shrt = sres.value();
		var ures = getUser( shrt.getOwnerId(), password);
		if( ! ures.isOK() )
			return error( ures.error() );
		
		return Hibernate.getInstance().deleteOne( shrt );
	}

	
	@Override
	public Result<List<String>> getShorts(String userId) {
		final var QUERY_FMT = "SELECT s.shortId FROM Short s WHERE s.ownerId = '%s'";		
		var query = String.format(QUERY_FMT, userId);
		var hits = Hibernate.getInstance().sql(query, String.class);
		return ok(hits);
	}

	@Override
	public Result<Void> follow(String userId1, String userId2, boolean isFollowing, String password) {
		var ures1 = getUser( userId1, password );
		if( ! ures1.isOK() )
			return error( ures1.error() );
		
		var ures2 = getUser( userId1, "" );
		if( ures2.error() != FORBIDDEN )
			return error( NOT_FOUND );
		
		if( isFollowing )
			return Hibernate.getInstance().persist( new Following(userId1, userId2) );
		else
			return Hibernate.getInstance().deleteOne(new Following(userId1, userId2) );
			
	}

	@Override
	public Result<List<String>> followers(String userId, String password) {
		var ures = getUser( userId, password );
		if( ! ures.isOK() )
			return error( ures.error() );

		final var QUERY_FMT = "SELECT f.follower FROM Following f WHERE f.followee = '%s'";		
		var query = String.format(QUERY_FMT, userId);
		var hits = Hibernate.getInstance().sql(query, String.class);
		return ok(hits);
	}

	@Override
	public Result<Void> like(String shortId, String userId, boolean isLiked, String password) {
		var sres = getShort(shortId);
		if( ! sres.isOK())
			return error( sres.error() );
		
		var ures = getUser(userId, password);
		if( ! ures.isOK())
			return error( ures.error() );
		
		shortsCache.invalidate( shortId );
		var ownerId = sres.value().getOwnerId();
		
		if( isLiked )
			return Hibernate.getInstance().persist( new Likes(userId, shortId, ownerId) );
		else 
			return Hibernate.getInstance().deleteOne(new Likes(userId, shortId, ownerId) );
		
	}

	@Override
	public Result<List<String>> likes(String shortId, String password) {
		var sres = getShort(shortId);
		if( ! sres.isOK())
			return error( sres.error() );

		var shrt = sres.value();
		var ures = getUser( shrt.getOwnerId(), password );
		if( ! ures.isOK() )
			return error( ures.error() );

		final var QUERY_FMT = "SELECT l.userId FROM Likes l WHERE l.shortId = '%s'";		
		var query = String.format(QUERY_FMT, shortId);
		var hits = Hibernate.getInstance().sql(query, String.class);
		return ok(hits);
	}

	@Override
	public Result<List<String>> getFeed(String userId, String password) {
		var ures = getUser(userId, password);
		if( ! ures.isOK())
			return error( ures.error() );
	
		final var QUERY_FMT = "SELECT s.shortId FROM Short s, Following f WHERE f.followee = s.ownerId AND f.follower = '%s'";		
		var query = String.format(QUERY_FMT, userId);
		var hits = Hibernate.getInstance().sql(query, String.class);		
		return ok(hits);
	}
		
	protected Result<User> getUser( String userId, String pwd) {
		try {
			return usersCache.get( new Credentials(userId, pwd));
		} catch (Exception x) {
			x.printStackTrace();
			return Result.error(ErrorCode.INTERNAL_ERROR);
		}
	}
	
	protected Result<Short> shortFromCache( String shortId ) {
		try {
			return shortsCache.get(shortId);
		} catch (ExecutionException e) {
			e.printStackTrace();
			return error(ErrorCode.INTERNAL_ERROR);
		}
	}

	// Extended API 
	
	@Override
	public Result<Void> deleteAllShorts(String userId, String password) {
		var ures = getUser(userId, password);
		if( ! ures.isOK() ) 
			return error( ures.error() );
		
		List<Object> toDelete = new ArrayList<>();

		final var QUERY_FMT1 = "SELECT * FROM Short s WHERE s.ownerId = '%s'";		
		Hibernate.getInstance().sql(String.format(QUERY_FMT1, userId), Short.class).forEach( s -> {
			shortsCache.invalidate( s.getShortId() );
			toDelete.add( s );
		});
		
		final var QUERY_FMT2 = "SELECT * FROM Following f WHERE f.follower = '%s' OR f.followee = '%s'";		
		Hibernate.getInstance().sql(String.format(QUERY_FMT2, userId, userId), Following.class).forEach( toDelete::add );
		
		final var QUERY_FMT3 = "SELECT * FROM Likes l WHERE l.ownerId = '%s' OR l.userId = '%s'";		
		Hibernate.getInstance().sql(String.format(QUERY_FMT3, userId, userId), Likes.class).forEach( l -> {
			shortsCache.invalidate( l.getShortId() );
			toDelete.add( l );
		});
		
		Hibernate.getInstance().delete( toDelete.toArray() );
		return ok();
	}

	// Extended API 

	@Override
	public Result<Void> verifyBlobURI(String blobURI) {
		return ok();
	}
	


	static record BlobServerCount(String baseURI, Long count) {};	
	private String getLeastLoadedBlobServerURI() {		
		final var QUERY = "SELECT REGEXP_SUBSTRING(s.blobUrl, '^(?:http:\\/\\/)?([^\\/]+)\\/([^\\/]+)') AS baseURI, count('*') AS usage From Short s GROUP BY baseURI";		
		var hits = Hibernate.getInstance().sql(QUERY, BlobServerCount.class);
		
		var candidates = hits.stream().collect( Collectors.toMap( BlobServerCount::baseURI, BlobServerCount::count));

		for( var uri : BlobsClients.instances() )
			 candidates.putIfAbsent( uri.toString(), 0L);

		var res = candidates.entrySet().stream().sorted( (e1, e2) -> Long.compare(e1.getValue(), e2.getValue())).findFirst();
		
		return res.isEmpty() ? "???" : res.get().getKey();
	}
}

