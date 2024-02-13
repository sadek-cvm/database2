package ca.qc.cvm.dba.memos.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import ca.qc.cvm.dba.memos.entity.User;
import ca.qc.cvm.dba.memos.util.BCrypt;

public class UserDAO {

	/**
	 * En fonction d'un nom d'usager et mot de passe, vérifier si l'usager existe bel et bien dans la BD
	 * 
	 * @param username nom d'usager
	 * @param password mot de passe
	 * @return l'usager ou null si les informations sont erronées
	 */
	public static User login(String username, char[] password) {
		User user = null;
		Connection connection = DBConnection.getConnection();
		try {
			PreparedStatement statement  = connection.prepareStatement("SELECT * FROM user WHERE username = ?");
			statement.setString(1, username);
			ResultSet result = statement.executeQuery();

			if(result.next()) {
				if (BCrypt.checkpw(String.valueOf(password), result.getNString("password"))) {
					user = new User(result.getInt("id"), username);
				}
				else {
					user = null;
				}
			}

			statement.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return user;
	}
	
	/**
	 * M�thode qui permet d'enregistrer un nouveau membre
	 * 
	 * @param username nom d'usager
	 * @param password son mot de passe
	 * @return
	 */
	public static boolean register(String username, char[] password) {
		boolean success = false;
		Connection connection = DBConnection.getConnection();
		
		// Doit retourner success à "true" lorsque l'enregistrement a fonctionné
		try {
			PreparedStatement statement  = connection.prepareStatement("INSERT INTO user(username, password) VALUES(?, ?)");
			statement.setString(1, username);
			String motDePasse = BCrypt.hashpw(String.valueOf(password), BCrypt.gensalt());
			statement.setString(2, motDePasse);
			statement.execute();
			statement.close();
			success = true;
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return success;
	}
}
