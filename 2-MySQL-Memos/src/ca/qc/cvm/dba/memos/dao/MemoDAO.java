package ca.qc.cvm.dba.memos.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import ca.qc.cvm.dba.memos.entity.Category;
import ca.qc.cvm.dba.memos.entity.Memo;

public class MemoDAO {
	/**
	 * M�thode qui permet d'ajouter un m�mo
	 * 
	 * @param categoryId l'ID de la cat�gorie
	 * @param memo Le texte du m�mo
	 * @return vrai/faux, selon si l'op�ration a fonctionn�e ou pas
	 */
	public static boolean addMemo(int categoryId, String memo) {
		boolean success = false;
		Connection connection = DBConnection.getConnection();
		try {
			PreparedStatement statement  = connection.prepareStatement("INSERT INTO memo(id_category, memo, created) VALUES(?, ?, NOW())");
			statement.setInt(1, categoryId);
			statement.setString(2, memo);
			statement.execute();
			statement.close();
			success = true;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return success;
	}
	
	/**
	 * M�thode qui permet de supprimer un m�mo
	 * 
	 * @param id du m�mo
	 * @return vrai/faux, selon si l'op�ration a fonctionn�e ou pas
	 */
	public static boolean deleteMemo(int id) {
		boolean success = false;
		Connection connection = DBConnection.getConnection();

		try {
			PreparedStatement statement = connection.prepareStatement("DELETE FROM memo WHERE id = ?");
			statement.setInt(1, id);
			statement.execute();
			statement.close();
			success = true;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return success;
	}
	
	/**
	 * M�thode qui retourne l'ensemble des m�mos
	 * 
	 * @param id L'id de l'usager
	 * @return une liste de m�mos (si aucun m�mo, retourner une liste vide)
	 */
	public static List<Memo> getMemoList(int userId) {
		List<Memo> memos = new ArrayList<Memo>();
		Connection connection = DBConnection.getConnection();
		try {
			PreparedStatement statement  = connection.prepareStatement("SELECT * FROM memo JOIN category ON memo.id_category = category.id WHERE id_user = ?");
			statement.setInt(1, userId);
			ResultSet result = statement.executeQuery();

			while(result.next()) {
				memos.add(new Memo(result.getInt("id"), result.getNString("name"), result.getNString("memo"), result.getDate("created")));
			}

			statement.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return memos;
	}

	/**
	 * M�thode pour faire une recherche FULL TEXT
	 * 
	 * ELLE NE FAIT PAS PARTIE DE CE QUI DOIT �TRE FAIT DANS LE PROJET!
	 * 
	 * @param id L'id de l'usager
	 * @param categoryId l'ID de la cat�gorie (peut �tre nulle!!!)
	 * @param text texte � consid�rer pour la recherche
	 * @return une liste de m�mos (si aucun r�sultat, retourner une liste vide)
	 */
	public static List<Memo> searchMemos(int userId, Integer categoryId, String text) {
		List<Memo> memos = new ArrayList<Memo>();
		Connection connection = DBConnection.getConnection();
				
		return memos;
	}
}
