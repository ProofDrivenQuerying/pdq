package uk.ac.ox.cs.pdq.db;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;

import uk.ac.ox.cs.pdq.db.homomorphism.HomomorphismManager;
import uk.ac.ox.cs.pdq.fol.Atom;

public interface Instance {
	
	/**  The instance's facts. */
	public LinkedHashSet<Atom> facts = new LinkedHashSet<Atom>();
	
	/**  Queries and updates the database of facts *. */
	public HomomorphismManager manager = null;

}
