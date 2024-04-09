from pymongo import MongoClient
import datetime

class Connection:
	client = None

	def verifyConnection():
		if Connection.client is None:
			try:
				Connection.client = MongoClient('localhost', 27017)
				db = Connection.client.list_database_names()
			except Exception as e:
				Connection.client = None
			finally:
				pass

		return Connection.client is not None

	def closeConnection():
		Connection.client.close()


class PersonDAO:

	# Fonction permettant de trouver le nombre de personnes dans la BD
	# Doit retourner un int/nombre
	def getSize():
		return Connection.client.personDB.persons.count_documents({})

	# Suppression d'une personne
	# Aucune valeur à retourner
	def deletePerson(id):
		Connection.client.personDB.persons.delete_many({"_id" : id})

	# Sauvegarder d'une personne
	# Si "id is None", alors insertion, sinon c'est une mise à jour
	# Aucune valeur à retourner
	def savePerson(firstName, lastName, age, previousJobs, id = None):
		person = {"firstName": firstName,
				  "lastName": lastName,
				  "age": age,
				  "jobs": previousJobs}

		if id is not None:
			person["_id"] = id
			Connection.client.personDB.persons.replace_one({"_id" : id}, person) # ou upsert et pas de insert_one!
		else:
			Connection.client.personDB.persons.insert_one(person)

	# Recherche dans la BD. Age et requiredJob est optionnel, donc pourrait être de longueur 0
	# Doit retourner un tableau de personnes(documents MongoDB).
	def search(age, requiredJob, limit):
		results = []
		query = {}

		if len(age) > 0:
			query["age"] = { "$gte" : int(age) }

		if len(requiredJob) > 0:
			query["jobs"] = requiredJob
		
		cursor = Connection.client.personDB.persons.find(query).sort("firstName", 1).limit(limit)

		for document in cursor:
			results.append(document)

		return results
