package ca.qc.cvm.dba.grumpy.dao;

import java.io.File;
import java.util.*;
import java.util.Map.Entry;

import com.sun.j3d.utils.behaviors.sensor.Mouse6DPointerBehavior;
import org.neo4j.driver.Driver;
import org.neo4j.driver.Record;
import org.neo4j.driver.Session;
import org.neo4j.driver.StatementResult;
import org.neo4j.driver.Transaction;
import org.neo4j.driver.internal.value.NodeValue;
import org.neo4j.driver.internal.value.RelationshipValue;
import org.neo4j.driver.types.Node;
import org.neo4j.driver.types.Relationship;

public class ConceptDAO {
		
	/**
	 * Permet de vérifier si l'index sur les noeuds existe
	 * 
	 * @return true si l'index existe, sinon false
	 */
	public static boolean indexExists() {
		boolean exists = false;

		try {
			Session session = DBConnection.getConnection();
			if(session.run("SHOW indexes where name = \"idx_concept\"").hasNext()){
				exists = true;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return exists;
	}
	
	/**
	 * Permet la création d'un index, mais seulement si nécessaire.
	 * 
	 * Il vous faudra donc, dans cette méthode, appeler la méthode "indexExists",
	 * qui permet de vérifier si oui ou non l'index existe déjà
	 * 
	 * @return true si l'index vient d'être crée, sinon false (il existait déjà)
	 */
	public static boolean addIndex() {
		boolean added = false;

		try {
			Session session = DBConnection.getConnection();
			if (indexExists()) {
				session.run("CREATE INDEX idx_concept FOR (c:Concept) ON (c.nom)");
				added = true;
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		return added;
	}
	
	/**
	 * Ajout de noeuds et d'une relation
	 * À l'étape 1, il faut ajouter à tous les coups et retourner false si .a fonctionne
	 * À l'étape 4, il y aura une modification pour vérifier si les noeuds existent déjà
	 * 	 
	 * @param concept1 exemple : chat
	 * @param link exemple : aime
	 * @param concept2 exemple : dormir
	 * @return false s'il ajoute la relation, true s'il la connaissait déjà
	 */
	public static boolean addKnowledge(String concept1, String link, String concept2) {
		boolean alreadyKnow = true;

		try {
			Session session= DBConnection.getConnection();

			Map<String, Object> params = new HashMap<String, Object>();
			params.put("concept1", concept1);
			session.run("MERGE (a:Concept {nom: $concept1}) RETURN a", params);

			params = new HashMap<String, Object>();
			params.put("concept2", concept2);
			session.run("MERGE (a:Concept {nom: $concept2}) RETURN a", params);

			params = new HashMap<String, Object>();
			params.put("concept1", concept1);
			params.put("concept2", concept2);
			params.put("link", link);
			session.run("MATCH (a:Concept),(b:Concept) WHERE a.nom = $concept1 AND b.nom = $concept2 MERGE (a)-[r:LIEN { desc : $link } ]->(b)", params);
			alreadyKnow = false;
		}
		catch (Exception e) {
			e.printStackTrace();
		}



		return alreadyKnow;
	}
	
	/**
	 * Permet de rechercher et de retourner un noeud d'un concept, s'il existe
	 * 
	 * @param concept nom du concept
	 * @return le noeud du concept, ou null s'il n'existe pas
	 */
	private static Node getConceptNode(String concept) {
		Node node = null;

		try {
			Session session= DBConnection.getConnection();

			Map<String, Object> params = new HashMap<String, Object>();
			params.put("concept1", concept);
			StatementResult res = session.run("MERGE (a:Concept {nom: $concept1}) RETURN a", params);
			node = res.next().get("a").asNode();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return node;
	}
	
	/**
	 * Permet de retourner une relation entre deux noeuds
	 * 
	 * @param concept1 Le premier concept
	 * @param concept2 Le deuxième concept
	 * @param relation La relation recherchée
	 * @return la relation Neo4j si elle existe, ou null sinon
	 */
	public static Relationship getRelationship(Node concept1, Node concept2, String relation) {
		Relationship r = null;

		try {
			Session session= DBConnection.getConnection();

			Map<String, Object> params = new HashMap<String, Object>();
			params = new HashMap<String, Object>();
			params.put("concept1", concept1);
			params.put("concept2", concept2);
			params.put("link", relation);
			session.run("MATCH (a:Concept),(b:Concept) WHERE a.nom = $concept1 AND b.nom = $concept2 MERGE (a)-[r:LIEN { desc : $link } ]->(b)", params);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return r;
	}
	
	/**
	 * Permet de supprimer tous les noeuds et relation de la BD
	 */
	public static void flash() {		
		try {
			Session session= DBConnection.getConnection();
			session.run("MATCH (n) DETACH DELETE n");
			session.run("DROP INDEX \"idx_concept\"");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Permet de retourner l'état de la mémoire de Grumpy
	 * 
	 * @return [nbNeuds, nbRelations]
	 */
	public static Integer[] getStats() {
		int nbRelations = 0;
		int nbNodes = 0;

		try {
			Session session= DBConnection.getConnection();

			StatementResult nbNo = session.run("MATCH (Concept) RETURN COUNT(Concept) as count");
			StatementResult nbRelat = session.run("MATCH p=()-->() RETURN COUNT (p) as count");

			Record recNbNode = nbNo.next();
			Record recNbRelat = nbRelat.next();

			nbNodes = recNbNode.get("count").asInt();
			nbRelations = recNbRelat.get("count").asInt();
		}
		catch (Exception e) {
			e.printStackTrace();
		}


		return new Integer[]{nbNodes, nbRelations};
	}
	
	/**
	 * Permet de vérifier s'il y a une relation distance entre deux noeuds
	 * 
	 * @param concept1 premier concept
	 * @param link (lien qui doit être présent quelque part dans le chemin entre les deux noeuds
	 * @param concept2 deuxième concept (qui peut être plusieurs noeuds/relation plus loin)
	 * @return Le message de Grumpy (J'sais pas s'il ne trouve pas le chemin, sinon le chemin détaillé)
	 */
	public static String interrogate(String concept1, String link, String concept2) {
		String message = "J'sais pas...";
		
		try {
			Node node1 = getConceptNode(concept1);
			Node node2 = getConceptNode(concept2);
			
			if (node1 != null && node2 != null) {
				Map<String, Object> params = new HashMap<String, Object>();
				params.put("name1", node1.get("nom"));
				params.put("name2", node2.get("nom"));
				params.put("info", link.toLowerCase());
				
				List<Object> obj = DBConnection.getPath("MATCH p=(n1)-[r2*0..10]->(n2) WHERE n1.nom = $name1 AND n2.nom = $name2 AND ANY (x IN relationships(p) WHERE x.desc = $info) RETURN p as path LIMIT 1", params);
				
				if (obj.size() > 0) {
					message = "En effet, car ";
					
					for (Object o : obj) {
						if (o instanceof Node) {
							Node node = (Node)o;
							message += "[" + node.get("nom").asString() + "] ";
						}
						else {
							Relationship r = (Relationship)o;
							message += "" + r.get("desc").asString() + " ";
						}					
					}				
				}
			}
        }
		catch (Exception e) {
			e.printStackTrace();
		}
		
		return message;
	}
}
