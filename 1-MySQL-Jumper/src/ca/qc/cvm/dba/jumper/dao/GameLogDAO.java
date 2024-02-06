package ca.qc.cvm.dba.jumper.dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import ca.qc.cvm.dba.jumper.entity.GameLog;

public class GameLogDAO {

	/**
	 * M�thode permettant la sauvegarde d'une partie.
	 * 
	 * @param gameLog - possède les informations de la partie
	 * @return true si la sauvegarde a fonctionné, false autrement
	 */
	public static boolean save(GameLog gameLog) {
		boolean success = false;

		Connection connection = DBConnection.getConnection();
		try {
			PreparedStatement statement  = connection.prepareStatement("INSERT INTO game_logs(player_name, score) VALUES(?, ?)");
			statement.setString(1, gameLog.getPlayerName());
			statement.setInt(2, gameLog.getScore());
			statement.execute();
			statement.close();
			success = true;
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return success;
	}
	
	/**
	 * Permet de retourner les informations des meilleurs parties jouées
	 * 
	 * @param limit - Nombre de parties (GameLog) à retourner
	 * @return les meilleurs parties, selon leur score
	 */
	public static List<GameLog> getHighScores(int limit) {
		List<GameLog> scoreList = new ArrayList<GameLog>();
				
		Connection connection = DBConnection.getConnection();
		try {
			PreparedStatement statement  = connection.prepareStatement("SELECT * FROM game_logs ORDER BY score DESC");
			ResultSet result = statement.executeQuery();

			while(result.next()) {
				scoreList.add(new GameLog(result.getString("player_name"), result.getInt("score")));
			}

			statement.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return scoreList;
	}
}
