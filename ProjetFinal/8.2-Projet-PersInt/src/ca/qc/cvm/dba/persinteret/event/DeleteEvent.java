package ca.qc.cvm.dba.persinteret.event;

import ca.qc.cvm.dba.persinteret.entity.Person;

/**
 * �v�nement utilis� lorsque l'on veut supprimer une entr�e
 */
public class DeleteEvent extends CommonEvent {
	private Person person;
	
	public DeleteEvent(Person person) {
		super(CommonEvent.Type.Delete);
		
		this.person = person;
	}
	
	public Person getPerson() {
		return person;
	}
}
