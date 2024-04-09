package ca.qc.cvm.dba.magix.dao;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.mongodb.*;
import com.mongodb.client.MongoDatabase;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;

public class DBConnection {
	private static MongoClient mongoClient; 
	private static MongoDatabase connection;
	private static final String DB_NAME = "magix_db";
	private static final String DB_HOST = "localhost";
	
	/**
	 * M�thode qui permet de retourner une connexion � la base de donn�es
	 * 
	 * @return
	 */
	public static MongoDatabase getConnection() {
		if (connection == null) {
			
			try {
				Logger mongoLogger = Logger.getLogger( "org.mongodb" );
				mongoLogger.setLevel(Level.WARNING);
				
				mongoClient = new MongoClient(DB_HOST);
				CodecRegistry pojoCodecRegistry = org.bson.codecs.configuration.CodecRegistries.fromRegistries(MongoClientSettings.getDefaultCodecRegistry(), org.bson.codecs.configuration.CodecRegistries.fromProviders(PojoCodecProvider.builder().automatic(true).build()));
				connection = mongoClient.getDatabase(DB_NAME).withCodecRegistry(pojoCodecRegistry);
				
			} 
			catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		return connection;
	}
	
	/**
	 * M�thode permettant de tester la connexion
	 * 
	 * @return si la connexion est ouverte ou pas
	 */
	public static boolean connectionCredentialsValid() {
		MongoDatabase c = getConnection();
		boolean valid = c != null;
		releaseConnection();
		
		return valid;
	}
	
	public static void releaseConnection() {
		if (connection != null) {
			try {
				mongoClient.close();
				connection = null;
			} 
			catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
