package ca.qc.cvm.dba.persinteret.dao;

import java.io.IOException;
import java.sql.SQLOutput;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.regex.Pattern;

import com.sleepycat.je.*;
import org.bson.Document;
import org.bson.types.Binary;

import com.mongodb.Block;
import com.mongodb.client.AggregateIterable;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

import ca.qc.cvm.dba.persinteret.entity.Person;
import org.neo4j.driver.Session;
import org.neo4j.driver.StatementResult;
import org.neo4j.driver.Record;
import org.neo4j.driver.types.Node;

import java.text.SimpleDateFormat;
import java.util.Date;
import com.sleepycat.je.*;

public class PersonDAO {

/**
	 * M�thode permettant de retourner la liste des personnes de la base de donn�es.
	 * 
	 * Notes importantes:
	 * - N'oubliez pas de limiter les r�sultats en fonction du param�tre limit
	 * - La liste doit �tre tri�es en ordre croissant, selon les noms des personnes
	 * - Le champ de filtre doit permettre de filtrer selon le pr�fixe du nom (insensible � la casse)
	 * - Si le champ withImage est � false, alors l'image (byte[]) n'a pas � faire partie des r�sultats
	 * - N'oubliez pas de mettre l'ID dans la personne, car c'est utile pour savePerson()
	 * - Il pourrait ne pas y avoir de filtre (champ filtre vide)
	 *  
	 * @param filterText champ filtre, peut �tre vide ou null
	 * @param withImage true si l'image est n�cessaire, false sinon.
	 * @param limit permet de restreindre les r�sultats
	 * @return la liste des personnes, selon le filtre si n�cessaire et filtre
	 */
	public static List<Person> getPeopleList(String filterText, boolean withImage, int limit) {
		final List<Person> peopleList = new ArrayList<Person>();

		try {
			// connect to databases
			Session dbNeo4j = Neo4jConnection.getConnection();
			Database dbBerkeley = BerkeleyConnection.getConnection();

			// search for all people
			Map<String, Object> params = new HashMap<String, Object>();
			params.put("prefix", filterText);
			params.put("limit", limit);
			StatementResult result = dbNeo4j.run("MATCH (p) WHERE toLower(p.name) STARTS WITH toLower($prefix) RETURN p ORDER BY p.name ASC LIMIT $limit", params);

			// for every person
			while (result.hasNext()){

				// fetch their data
				Record record = result.next();
				Node p = record.get("p").asNode();

				// format date of birth
				long dateOfBirthMillis = p.get("dateOfBirth").asLong();
				Instant instant = Instant.ofEpochMilli(dateOfBirthMillis);
				LocalDate localDate = instant.atZone(ZoneOffset.UTC).toLocalDate();
				DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
				String formattedDate = localDate.format(formatter);


				String id = p.get("id").toString();
				String name = p.get("name").toString();
				String codeName = p.get("codeName").toString();
				String status = p.get("status").toString();

				// initialize person
				Person person = new Person();
				person.setId(id);
				person.setName(name.substring(1, name.length() - 1)); // we use substring to remove quotations from the string
				person.setCodeName(codeName.substring(1, codeName.length() - 1));
				person.setStatus(status.substring(1, status.length() - 1));
				person.setDateOfBirth(formattedDate);

				// fetch every person they know
				List<String> listeConnexions = new ArrayList<>();
				Map<String, Object> paramsConnexions = new HashMap<String, Object>();
				paramsConnexions.put("id", Integer.parseInt(person.getId()));
				StatementResult resultConnexions = dbNeo4j.run("MATCH (p:Person)<-[:KNOWS]-(connection:Person) WHERE p.id = $id RETURN DISTINCT connection.name UNION MATCH (p:Person)-[:KNOWS]->(connection:Person) WHERE p.id = $id RETURN DISTINCT connection.name", paramsConnexions);

				while (resultConnexions.hasNext()) { // for every connection
					Record recordConnexions = resultConnexions.next();
					String c = recordConnexions.get("connection.name").asString();
					listeConnexions.add(c);
				}

				person.setConnexions(listeConnexions);

				// fetch image if asked
				if (withImage) {
					DatabaseEntry key = new DatabaseEntry(p.get("id").toString().getBytes("UTF-8"));
					DatabaseEntry value = new DatabaseEntry();
					if (dbBerkeley.get(null, key, value, LockMode.DEFAULT) == OperationStatus.SUCCESS) {
						byte[] image = value.getData();
						person.setImageData(image);
					}
				}

				// add person to list
				peopleList.add(person);

			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return peopleList;
	}

	/**
	 * M�thode permettant de sauvegarder une personne
	 * 
	 * Notes importantes:
	 * - Si le champ "id" n'est pas null, alors c'est une mise � jour, pas une insertion
	 * - Le nom de code est optionnel, le reste est obligatoire
	 * - Le nom de la personne doit �tre unique
	 * - Regarder comment est fait la classe Personne pour avoir une id�e des donn�es � sauvegarder
	 * - Pour cette m�thode, d�normaliser pourrait vous �tre utile. La performance des lectures est vitale.
	 * - Je vous conseille de sauvegarder la date de naissance en format long (en millisecondes)
	 * - Une connexion va dans les deux sens. Plus pr��is�ment, une personne X qui connait une autre personne Y
	 *   signifie que cette derni�re connait la personne X
	 * - Attention, les connexions d'une personne pourraient changer avec le temps (ajout/suppression)
	 * - N'oubliez pas de sauvegarder votre image dans BerkeleyDB
	 * 
	 * @param person
	 * @return true si succ�s, false sinon
	 */

	public static boolean save(Person person) {
		boolean success = false;

		String idStr = person.getId(); // optional
		String codeName = person.getCodeName(); // optional
		String name = person.getName();
		String status = person.getStatus();
		String dateOfBirthStr = person.getDateOfBirth();
		byte[] imageData = person.getImageData();
		List<String> connexions = person.getConnexions();

		// validation
		if(isNullOrEmpty(name) || isNullOrEmpty(status) || isNullOrEmpty(dateOfBirthStr) || imageData == null || imageData.length == 0)
			return success;

		try {

			// connect to databases
			Session dbNeo4j = Neo4jConnection.getConnection();
			Database dbBerkeley = BerkeleyConnection.getConnection();

			// check if name is unique
			Map<String, Object> params = new HashMap<String, Object>();
			params.put("name", name);
			StatementResult result = dbNeo4j.run("MATCH (p:Person) WHERE p.name = $name RETURN p", params);
			// if the name exists and an id is given
			if(result.hasNext() && isNullOrEmpty(idStr))
				return success;

			// if codeName is not given
			if(isNullOrEmpty(codeName))
				codeName = "";

			// format date into long (in milliseconds)
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
			LocalDate localDate = LocalDate.parse(dateOfBirthStr, formatter);
			long dateOfBirth = localDate.atStartOfDay().toInstant(java.time.ZoneOffset.UTC).toEpochMilli();

			// check if we insert or modify
			int id = 0;
			if(isNullOrEmpty(idStr)) {
				result = dbNeo4j.run("MATCH (p:Person) RETURN count(p) AS personCount");
				if (result.hasNext()){
					Record record = result.next();
					id = record.get("personCount").asInt();
				}
			}
			else
				id = Integer.parseInt(idStr);

			// add or update person
			params.put("id", id);
			params.put("codeName", codeName);
			params.put("status", status);
			params.put("dateOfBirth", dateOfBirth);
			dbNeo4j.run("MERGE (p:Person {id: $id}) SET p.name = $name, p.codeName = $codeName, p.dateOfBirth = $dateOfBirth, p.status = $status RETURN p", params);

			// add relations
			for (String connexion : connexions) {
				Map<String, Object> paramsConnexion = new HashMap<String, Object>();
				paramsConnexion.put("name", name);
				paramsConnexion.put("connection", connexion);
				dbNeo4j.run("MATCH (p:Person), (connection:Person) WHERE p.name = $name AND connection.name = $connection MERGE (p)-[r:KNOWS]->(connection)", paramsConnexion);
			}

			DatabaseEntry theKey = new DatabaseEntry(String.valueOf(id).getBytes("UTF-8"));
			DatabaseEntry theData = new DatabaseEntry(person.getImageData());
			dbBerkeley.put(null, theKey, theData);
			success = true;

		} catch (Exception e) {
			e.printStackTrace();
		}

		return success;
	}
	
	/**
	 * Suppression des donn�es/fiche d'une personne
	 * 
	 * @param person
	 * @return true si succ�s, false sinon
	 */
	public static boolean delete(Person person) {
		boolean success = false;
		try {
			Session dbNeo4j = Neo4jConnection.getConnection();
			Database dbBerkeley = BerkeleyConnection.getConnection();

			// delete from berkeley
			DatabaseEntry key = new DatabaseEntry(person.getId().getBytes("UTF-8"));
			dbBerkeley.delete(null, key);

			// delete from neo4j
			Map<String, Object> params = new HashMap<String, Object>();
			params.put("id", Integer.parseInt(person.getId()));
			dbNeo4j.run("MATCH (p:Person) WHERE p.id = $id DETACH DELETE p", params);

			success = true;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return success;
	}
	
	/**
	 * Suppression totale de toutes les donn�es du syst�me!
	 * 
	 * @return true si succ�s, false sinon
	 */
	public static boolean deleteAll() {
		boolean success = false;
		try {
			Session dbNeo4j = Neo4jConnection.getConnection();
			Database dbBerkeley = BerkeleyConnection.getConnection();

			//TODO delele all from berkeleys?
			//TODO delete relation methode?

			// delete all nodes in neo4j
			dbNeo4j.run("MATCH (p:Person) DETACH DELETE p");
			success = true;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return success;
	}
	
	/**
	 * M�thode qui retourne le ratio de personnes en libert� par rapport au nombre total de fiches.
	 * 
	 * @return ratio entre 0 et 100
	 */
	public static int getFreeRatio() {
		int num = 0;
		double peopleCount = (double) getPeopleCount();
		try {
			Session dbNeo4j = Neo4jConnection.getConnection();
			StatementResult result = dbNeo4j.run("MATCH (p:Person {status: 'Libre'}) RETURN COUNT(p) as freePersonCount");
			System.out.println(result.hasNext());
			if (result.hasNext()) { // for every connection
				Record recordConnexions = result.next();
				double freePersonCount = recordConnexions.get("freePersonCount").asDouble();
				System.out.println(freePersonCount);
				System.out.println(peopleCount);
				double ration = freePersonCount / peopleCount;
				num = (int) (ration * 100);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return num;
	}
	
	/**
	 * Nombre de photos actuellement sauvegard�es dans le syst�me
	 * @return nombre
	 */
	public static long getPhotoCount() {
		// iterate thru berkeley
		return 0;
	}
	
	/**
	 * Nombre de fiches pr�sente dans la base de donn�es
	 * @return nombre
	 */
	public static long getPeopleCount() {
		long personCount = 0;
		try {
			Session dbNeo4j = Neo4jConnection.getConnection();
			StatementResult resul = dbNeo4j.run("MATCH (p:Person) RETURN COUNT(p) as personCount");
			if (resul.hasNext()) { // for every connection
				Record recordConnexions = resul.next();
				personCount = recordConnexions.get("personCount").asLong();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return personCount;
	}
		
	/**
	 * Permet de savoir la personne la plus jeune du syst�me
	 * 
	 * @return nom de la personne
	 */
	public static String getYoungestPerson() {
		String youngestPerson = "--";
		try {
			Session dbNeo4j = Neo4jConnection.getConnection();
			StatementResult result = dbNeo4j.run("MATCH (p:Person) RETURN p ORDER BY p.dateOfBirth DESC LIMIT 1");

			if (result.hasNext()) {
				Record record = result.next();
				Node person = record.get("p").asNode();
				youngestPerson = person.get("name").toString();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return youngestPerson.substring(1, youngestPerson.length() - 1);
	}
	
	/**
	 * Afin de savoir la prochaine personne � investiguer,
	 * Il faut retourner la personne qui connait, ou est connu, du plus grand nombre de personnes 
	 * disparues ou d�c�d�es (morte). Cette personne doit �videmment avoir le statut "Libre"
	 * 
	 * @return nom de la personne
	 */
	public static String getNextTargetName() {

		return "--";
	}
	
	/**
	 * Permet de retourner, l'�ge moyen des personnes
	 * 
	 * @return par exemple, 20 (si l'�ge moyen est 20 ann�es arrondies)
	 */
	public static int getAverageAge() {
		int resultat = 0;
		try {
			Session dbNeo4j = Neo4jConnection.getConnection();
			StatementResult result = dbNeo4j.run("MATCH (p:Person) RETURN AVG(p.dateOfBirth) AS averageDateOfBirth");


			if (result.hasNext()) {
				Record record = result.next();
				long averageAgeMilis = record.get("averageDateOfBirth").asLong();
				Instant instant = Instant.ofEpochMilli(averageAgeMilis);
				LocalDate previousDate = instant.atZone(ZoneOffset.UTC).toLocalDate();
				LocalDate currentDate = LocalDate.now();
				resultat = (int) ChronoUnit.YEARS.between(previousDate, currentDate);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return resultat;
	}

	public static boolean isNullOrEmpty(String str) {
		return str == null || str.isEmpty();
	}
}
