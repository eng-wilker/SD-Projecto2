package utils;

import java.util.List;
import java.util.function.Consumer;

import org.hibernate.Session;

import tukano.api.java.Result;

public class DB {

	public static <T> List<T> sql(String query, Class<T> clazz) {
		return Hibernate.getInstance().sqlX(query, clazz);
	}
	
	
	public static <T> List<T> sqlX(Class<T> clazz, String fmt, Object ... args) {
		return Hibernate.getInstance().sqlX(String.format(fmt, args), clazz);
	}
	
	
	public static <T> Result<T> getOne(String id, Class<T> clazz) {
		return Hibernate.getInstance().getOne(id, clazz);
	}
	
	public static <T> Result<T> deleteOne(T obj) {
		return Hibernate.getInstance().deleteOne(obj);
	}
	
	public static <T> Result<T> updateOne(T obj) {
		return Hibernate.getInstance().updateOne(obj);
	}
	
	public static <T> Result<T> insertOne( T obj) {
		return Result.errorOrValue(Hibernate.getInstance().persist(obj), obj);
	}
	
	public static <T> Result<T> transaction( Consumer<Session> c) {
		return Hibernate.getInstance().execute( c::accept );
	}
}
