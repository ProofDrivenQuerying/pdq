package uk.ac.ox.cs.pdq.db;

import java.util.Collection;
import java.util.LinkedHashSet;

import uk.ac.ox.cs.pdq.fol.Atom;
/**
 * 
 * @author George K
 *
 */
public class DatabaseInstance implements Instance {
	
	/**  The instance's facts. */
	public LinkedHashSet<Atom> facts = new LinkedHashSet<Atom>();

	@Override
	public void addFacts(Collection<Atom> facts) {
		this.facts.addAll(facts);		
	}

	@Override
	public void removeFacts(Collection<Atom> facts) {
		this.facts.removeAll(facts);
	}

	@Override
	public Collection<Atom> getFacts() {
		return facts;
	}
	

}
