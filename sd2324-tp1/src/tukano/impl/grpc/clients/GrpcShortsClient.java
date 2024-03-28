package tukano.impl.grpc.clients;

import static tukano.impl.grpc.common.DataModelAdaptor.GrpcShort_to_Short;

import java.util.List;
import java.util.concurrent.TimeUnit;

import tukano.api.Short;
import tukano.api.java.Result;
import tukano.impl.api.java.ExtendedShorts;
import tukano.impl.grpc.generated_java.ExtendedShortsGrpc;
import tukano.impl.grpc.generated_java.ShortsProtoBuf.CreateShortArgs;
import tukano.impl.grpc.generated_java.ShortsProtoBuf.DeleteShortArgs;
import tukano.impl.grpc.generated_java.ShortsProtoBuf.FollowArgs;
import tukano.impl.grpc.generated_java.ShortsProtoBuf.FollowersArgs;
import tukano.impl.grpc.generated_java.ShortsProtoBuf.GetFeedArgs;
import tukano.impl.grpc.generated_java.ShortsProtoBuf.GetShortArgs;
import tukano.impl.grpc.generated_java.ShortsProtoBuf.GetShortsArgs;
import tukano.impl.grpc.generated_java.ShortsProtoBuf.LikeArgs;
import tukano.impl.grpc.generated_java.ShortsProtoBuf.LikesArgs;
import tukano.impl.grpc.generated_java.ExtendedShortsProtoBuf.*;

public class GrpcShortsClient extends GrpcClient implements ExtendedShorts {

	final ExtendedShortsGrpc.ExtendedShortsBlockingStub stub;

	public GrpcShortsClient(String serverURI) {
		super(serverURI);
		stub = ExtendedShortsGrpc.newBlockingStub( super.channel )
				.withDeadlineAfter(GRPC_TIMEOUT, TimeUnit.MILLISECONDS);
	}

	public Result<Short> _createShort(String userId, String password) {
		return super.toJavaResult(() -> {
			var res = stub.createShort(CreateShortArgs.newBuilder()
					.setUserId( userId )
					.setPassword( password )
					.build());
			return GrpcShort_to_Short(res.getValue());
		});
	}

	public Result<Void> _deleteShort(String shortId, String password) {
		return super.toJavaResult(() -> {
			stub.deleteShort(DeleteShortArgs.newBuilder()
					.setShortId(shortId)
					.setPassword( password )
					.build());
		});
	}

	public Result<Short> _getShort(String shortId) {
		return super.toJavaResult(() -> {
			var res = stub.getShort(GetShortArgs.newBuilder()
					.setShortId( shortId )
					.build());
			return GrpcShort_to_Short(res.getValue() );
		});
	}

	public Result<List<String>> _getShorts(String userId) {
		return super.toJavaResult(() -> {
			var res = stub.getShorts(GetShortsArgs.newBuilder()
					.setUserId( userId )
					.build());
			
			return res.getShortIdList();
		});
	}

	public Result<Void> _follow(String userId1, String userId2, boolean isFollowing, String password) {
		return super.toJavaResult(() -> {
			stub.follow(FollowArgs.newBuilder()
					.setUserId1( userId1 )
					.setUserId2( userId2)
					.setIsFollowing( isFollowing )
					.setPassword( password )
					.build());
		});
	}

	public Result<List<String>> _followers(String userId, String password) {
		return super.toJavaResult(() -> {
			var res = stub.followers(FollowersArgs.newBuilder()
					.setUserId( userId )
					.setPassword( password )
					.build());
			
			return res.getUserIdList();
		});
	}

	public Result<Void> _like(String shortId, String userId, boolean isLiked, String password) {
		return super.toJavaResult(() -> {
			stub.like(LikeArgs.newBuilder()
					.setShortId( shortId )
					.setUserId( userId )
					.setIsLiked( isLiked )
					.setPassword( password )
					.build());
		});
	}

	public Result<List<String>> _likes(String shortId, String password) {
		return super.toJavaResult(() -> {
			var res = stub.likes(LikesArgs.newBuilder()
					.setShortId( shortId )
					.setPassword( password )
					.build());
			
			return res.getUserIdList();
		});
	}

	public Result<List<String>> _getFeed(String userId, String password) {
		return super.toJavaResult(() -> {
			var res = stub.getFeed(GetFeedArgs.newBuilder()
					.setUserId( userId )
					.setPassword( password )
					.build());
			
			return res.getShortIdList();
		});
	}

	public Result<Void> _deleteAllShorts(String userId, String password) {
		return super.toJavaResult(() -> {
			stub.deleteAllShorts( 
					DeleteAllShortsArgs.newBuilder()
					.setUserId(password)
					.setPassword(password)
					.build());
		});
	}
	
	@Override
	public Result<Short> createShort(String userId, String password) {
		return super.reTry( () -> _createShort(userId, password) );
	}

	@Override
	public Result<Void> deleteShort(String shortId, String password) {
		return super.reTry( () -> _deleteShort(shortId, password) );
	}

	@Override
	public Result<Short> getShort(String shortId) {
		return super.reTry( () -> _getShort(shortId) );
	}

	@Override
	public Result<List<String>> getShorts(String userId) {
		return super.reTry( () -> _getShorts(userId) );
	}

	@Override
	public Result<Void> follow(String userId1, String userId2, boolean isFollowing, String password) {
		return super.reTry( () -> _follow( userId1, userId2, isFollowing, password ));
	}

	@Override
	public Result<List<String>> followers(String userId, String password) {
		return super.reTry( () -> _followers(userId, password));
	}

	@Override
	public Result<Void> like(String shortId, String userId, boolean isLiked, String password) {
		return super.reTry( () -> _like( shortId, userId, isLiked, password ));
	}

	@Override
	public Result<List<String>> likes(String shortId, String password) {
		return super.reTry( () -> _likes(shortId, password));
	}

	@Override
	public Result<List<String>> getFeed(String userId, String password) {
		return super.reTry( () -> _getFeed(userId, password));
	}

	@Override
	public Result<Void> deleteAllShorts(String userId, String password) {
		return super.reTry( () -> _deleteAllShorts(userId, password));
	}
}
