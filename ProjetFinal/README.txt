readme dba projet final

Un fichier README.md doit être remis avec votre projet. Celui-ci doit contenir : 

1- Travail présenté par Ahmed Sadek et Erick R. Delgado Garcia
2- Quelles bases de données seront utilisées, et pour quel type d'information. La justification du choix 
des bases de données est très importante (entre 100 et 250 mots). 

Nous utilisons les bases de données Berkeley et Neo4J. 
Les avantages qu'offre la base de données Berkeley sont les suivants: 
- C'est un contenant qui est très permissif (accepte toute structure) et chaque enregistrement permet jusqu'à 4Go de données.
- Cette BD stocke les données en binaire, donc sera utile pour stocker l'image de l'usager.

Les avantages qu'offre la base de données Neo4j sont les suivants: 
- C'est une base de donnée orienté graphe, donc excellente pour sauvegarder des relations.
- L'utilisation de Cypher pour les requêtes rend son utilisation particulièrement intéressante. Ce langage étant spécifique aux bd orienté graphe, cela simplifie énormément les opérations.

Leur utilisation conjointe est utile pour le projet pour la raison suivante:
- Ceci permet un système d'enregistrement hybride. Neo4j peut gérer les relations complexes entre les nodes et Berkeley peut gérer les données non structurées plus volumineuses sous forme de clé-valeur par exemple.
 

3- Quelle seront les collections ou libellés utilisés?

- Nous aurons des libellés de type personne avec l'information relative à la personne pour la bd Neo4j.
- Nous aurons des collections de type image avec la photo de l'usager. Ce sera un enregistrement sur la bd Berkeley de type clé-valeur lié à une personne.

4- Où sont les index, ordre (asc/desc) + type (unique ou pas) ? 

Berkeley:

- Index sur la clé, asc et unique


Neo4J:

- Index sur date de naissance, ordre desc et non unique

- Index sur nom, asc, unique

- Status, asc et non unique







