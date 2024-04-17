package tukano.impl.java.clients;

import java.net.URI;
import java.util.Arrays;
import java.util.Collection;
import java.util.function.Function;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.re2j.Pattern;

import tukano.api.java.Blobs;
import tukano.api.java.Result;
import tukano.impl.discovery.Discovery;


public class ClientFactory<T> {

	private static final String REST = "/rest";
	private static final String GRPC = "/grpc";

	private final String serviceName;
	private final Function<String, T> restClientFunc;
	private final Function<String, T> grpcClientFunc;
	
	ClientFactory( String serviceName, Function<String, T> restClientFunc, Function<String, T> grpcClientFunc) {
		this.restClientFunc = restClientFunc;
		this.grpcClientFunc = grpcClientFunc;
		this.serviceName = serviceName;
	}
	
	private T newClient( String serverURI ) {
		if (serverURI.endsWith(REST))
			return restClientFunc.apply( serverURI );
		else if (serverURI.endsWith(GRPC))
			return grpcClientFunc.apply( serverURI );
		else
			throw new RuntimeException("Unknown service type..." + serverURI);	
	}
	
	LoadingCache<URI, T> clients = CacheBuilder.newBuilder()
			.build(new CacheLoader<>() {
				@Override
				public T load(URI uri) throws Exception {
					return newClient( uri.toString() );
				}
			});
	
	
	public T get() {
		var uris = Discovery.getInstance().knownUrisOf(serviceName, 1);
		return get(uris[0]);
	}
	
	public T get(URI uri) {
		try {
			return clients.get(uri);
		} catch (Exception x) {
			x.printStackTrace();
			throw new RuntimeException( Result.ErrorCode.INTERNAL_ERROR.toString());
		}
	}	
	
	public T get(String url) {
		try {
			var regex = Pattern.compile("^(\\w+:\\/\\/)?([^\\/]+)\\/([^\\/]+)");
			var matcher = regex.matcher( url );
			var uriStr = matcher.find() ? matcher.group() : "???";
			return clients.get( URI.create( uriStr ) );
		} catch (Exception x) {
			x.printStackTrace();
			throw new RuntimeException( Result.ErrorCode.INTERNAL_ERROR.toString());
		}
	}
	
	public Collection<String> urls() {
		return Arrays.asList(Discovery.getInstance().knownUrisOf(serviceName, 1))
				.stream()
				.map( uri -> String.format("%s/%s", uri, Blobs.NAME))
				.toList();
	}
	
	public Collection<T> all() {
		return Arrays.asList( Discovery.getInstance().knownUrisOf(serviceName, 1) ).stream().map( this::get ).toList();
	}
}
