package ca.qc.cvm.dba.persinteret.event;

/**
 * �v�nement permettant de retourner � un panel parent 
 * (C'est pour le bouton retour)
 */
public class BackEvent extends CommonEvent {
	
	public BackEvent() {
		super(CommonEvent.Type.Back);
	}
}
