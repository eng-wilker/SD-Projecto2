package tukano.impl.java.clients;

import static tukano.api.java.Result.ErrorCode.INTERNAL_ERROR;
import static tukano.api.java.Result.ErrorCode.TIMEOUT;

import java.util.function.Supplier;
import java.util.logging.Logger;

import jakarta.ws.rs.ProcessingException;
import tukano.api.java.Result;
import tukano.impl.rest.clients.RestClient;
import utils.Sleep;

public class AbstractClient {
	private static Logger Log = Logger.getLogger(RestClient.class.getName());

	
	protected static final int MAX_RETRIES = 3;
	protected static final int RETRY_SLEEP = 1000;

	protected <T> Result<T> reTry(Supplier<Result<T>> func) {
		for (int i = 0; i < MAX_RETRIES; i++)
			try {
				return func.get();
			} catch (ProcessingException x) {
				Log.fine("Timeout: " + x.getMessage());
				Sleep.ms(RETRY_SLEEP);
			} catch (Exception x) {
				x.printStackTrace();
				return Result.error(INTERNAL_ERROR);
			}
		System.err.println("TIMEOUT...");
		return Result.error(TIMEOUT);
	}
}
