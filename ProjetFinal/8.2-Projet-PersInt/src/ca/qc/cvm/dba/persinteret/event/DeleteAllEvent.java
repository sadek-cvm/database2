package ca.qc.cvm.dba.persinteret.event;

import ca.qc.cvm.dba.persinteret.entity.Person;

/**
 * �v�nement utilis� lorsque l'on veut supprimer toute les donn�es
 */
public class DeleteAllEvent extends CommonEvent {
	
	public DeleteAllEvent() {
		super(CommonEvent.Type.DeleteAll);
	}
}
