package tukano.impl.grpc.clients;

import static tukano.api.java.Result.error;
import static tukano.api.java.Result.ok;
import static tukano.api.java.Result.ErrorCode.INTERNAL_ERROR;

import java.net.URI;
import java.util.List;
import java.util.Map;
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
	protected static final int RETRY_SLEEP = 500;
	protected static final int GRPC_TIMEOUT = 3000;

	final protected URI serverURI;
	final protected Channel channel;

	protected GrpcClient(String serverUrl) {
		this.serverURI = URI.create(serverUrl);
		this.channel = ManagedChannelBuilder.forAddress(serverURI.getHost(), serverURI.getPort())
				.usePlaintext().enableRetry()
				.disableServiceConfigLookUp()
				.defaultServiceConfig( buildServiceConfig()).build();
		

	}
	
	protected <T> Result<T> reTry(Supplier<Result<T>> func) {
		try {
			var res = func.get();
			Log.info("OK: " + res + "\n");
			return res;
		} catch (StatusRuntimeException sre) {
			var code = sre.getStatus().getCode();
			if (code == Code.UNAVAILABLE || code == Code.DEADLINE_EXCEEDED) {
				Log.info("Timeout: " + sre.getMessage() + "\n");
				Sleep.ms(RETRY_SLEEP);
			}
			return error(statusToErrorCode(sre.getStatus()));
		} catch (Exception x) {
			x.printStackTrace();
			return Result.error(INTERNAL_ERROR);
		}
	}

	protected <T> Result<T> toJavaResult(Supplier<T> func) {
		return ok(func.get());
	}

	protected Result<Void> toJavaResult(Runnable proc) {
		proc.run();
		return ok();
	}

	protected static ErrorCode statusToErrorCode(Status status) {
		return switch (status.getCode()) {
		case OK -> ErrorCode.OK;
		case NOT_FOUND -> ErrorCode.NOT_FOUND;
		case ALREADY_EXISTS -> ErrorCode.CONFLICT;
		case PERMISSION_DENIED -> ErrorCode.FORBIDDEN;
		case INVALID_ARGUMENT -> ErrorCode.BAD_REQUEST;
		case UNIMPLEMENTED -> ErrorCode.NOT_IMPLEMENTED;
		default -> ErrorCode.INTERNAL_ERROR;
		};
	}

	
	public Map<String, Object> buildServiceConfig() {

	    return Map.of(
	        "loadBalancingConfig",
	            List.of(
	                Map.of("weighted_round_robin", Map.of()),
	                Map.of("round_robin", Map.of()),
	                Map.of("pick_first", Map.of("shuffleAddressList", true))),
	        "methodConfig",
	            List.of(
	                Map.of(
	                    "name", List.of(Map.of("service", "")),
	                    "waitForReady", true,
	                    "retryPolicy",
	                        Map.of(
	                            "maxAttempts", 4.0,
	                            "initialBackoff", "0.5s",
	                            "backoffMultiplier", 1.0,
	                            "maxBackoff", "1.0s",
	                            "retryableStatusCodes", List.of("UNAVAILABLE")))));
	  }

	
	@Override
	public String toString() {
		return serverURI.toString();
	}
}

