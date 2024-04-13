package tukano.impl.grpc.clients;

import static tukano.api.java.Result.error;
import static tukano.api.java.Result.ok;
import static tukano.api.java.Result.ErrorCode.INTERNAL_ERROR;
import static tukano.api.java.Result.ErrorCode.TIMEOUT;

import java.net.URI;
import java.util.function.Supplier;
import java.util.logging.Logger;

import io.grpc.Channel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Status;
import io.grpc.Status.Code;
import io.grpc.StatusRuntimeException;
import tukano.api.java.Result;
import tukano.api.java.Result.ErrorCode;
import utils.Sleep;

public class GrpcClient {
	private static Logger Log = Logger.getLogger(GrpcClient.class.getName());

	protected static final int MAX_RETRIES = 10;
	protected static final int RETRY_SLEEP = 1000;
	protected static final int GRPC_TIMEOUT = 3000;
	
	final protected URI server;
	final protected Channel channel;
	
	protected GrpcClient(String serverURI ) {
		this.server = URI.create(serverURI);
		this.channel = ManagedChannelBuilder.forAddress(server.getHost(), server.getPort()).usePlaintext().build();
	}
		
	protected <T> Result<T> reTry(Supplier<Result<T>> func) {
		for (int i = 0; i < MAX_RETRIES; i++)
			try {
				return func.get();
			} catch (StatusRuntimeException sre) {
				var code = sre.getStatus().getCode();
				if( code == Code.UNAVAILABLE || code == Code.DEADLINE_EXCEEDED ) {
					Log.fine("Timeout: " + sre.getMessage());
					Sleep.ms( RETRY_SLEEP );
					continue;
				}
				return error( statusToErrorCode( sre.getStatus() ) );
			} catch (Exception x) {
				x.printStackTrace();
				return Result.error(INTERNAL_ERROR);
			}
		System.err.println("TIMEOUT...");
		return Result.error(TIMEOUT);
	}
	
	protected <T> Result<T> toJavaResult(Supplier<T> func) {
		try {
			return ok(func.get());
		} catch(StatusRuntimeException sre) {
			return error( statusToErrorCode( sre.getStatus() ) );
		}
	}
	
	protected <T> Result<T> toJavaResult(Runnable proc) {
		try {
			proc.run();
			return ok();
		} catch(StatusRuntimeException sre) {
			return error( statusToErrorCode( sre.getStatus() ) );
		}
	}
	
	protected static ErrorCode statusToErrorCode( Status status ) {
    	return switch( status.getCode() ) {
    		case OK -> ErrorCode.OK;
    		case NOT_FOUND -> ErrorCode.NOT_FOUND;
    		case ALREADY_EXISTS -> ErrorCode.CONFLICT;
    		case PERMISSION_DENIED -> ErrorCode.FORBIDDEN;
    		case INVALID_ARGUMENT -> ErrorCode.BAD_REQUEST;
    		case UNIMPLEMENTED -> ErrorCode.NOT_IMPLEMENTED;
    		default -> ErrorCode.INTERNAL_ERROR;
    	};
    }	
}
