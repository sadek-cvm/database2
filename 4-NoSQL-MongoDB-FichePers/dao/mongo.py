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
		return 0

	# Suppression d'une personne
	# Aucune valeur à retourner
	def deletePerson(id):
		pass

	# Sauvegarder d'une personne
	# Si "id is None", alors insertion, sinon c'est une mise à jour
	# Aucune valeur à retourner
	def savePerson(firstName, lastName, age, previousJobs, id = None):
		pass

	# Recherche dans la BD. Le paramètre Age est optionnel.
	# Doit retourner un tableau de personnes(documents MongoDB).
	def search(age, limit):
		# age est un string, vous devez donc le transformer en int comme suit : int(age)
		results = []
		query = {}

		return results
