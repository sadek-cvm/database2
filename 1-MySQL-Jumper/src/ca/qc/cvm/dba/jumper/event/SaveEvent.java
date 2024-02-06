package ca.qc.cvm.dba.jumper.event;

import ca.qc.cvm.dba.jumper.entity.GameLog;

/**
 * �v�nement utilis� lorsque l'on veut sauvegarder une nouvelle
 * entr�e 
 */
public class SaveEvent extends CommonEvent {
	private GameLog gameLog;
	
	public SaveEvent(GameLog gameLog) {
		super(CommonEvent.Type.Save);
		
		this.gameLog = gameLog;
	}
	
	public GameLog getGameLog() {
		return gameLog;
	}
}
