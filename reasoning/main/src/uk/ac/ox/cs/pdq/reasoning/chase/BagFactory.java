package uk.ac.ox.cs.pdq.reasoning.chase;

import java.util.Collection;

import org.apache.log4j.Logger;

import uk.ac.ox.cs.pdq.db.Schema;
import uk.ac.ox.cs.pdq.fol.Predicate;

import com.google.common.base.Preconditions;


/**
 * Creates bags of facts.
 * The returned bags  keep the schema dependencies satisfied by the facts in the current bag. 
 *
 * @author Efthymia Tsamoura
 */
public class BagFactory {

	protected static Logger log = Logger.getLogger(BagFactory.class);

	/** The input schema */
	protected final Schema schema;
	
	/**
	 * Constructor for BagFactory.
	 * @param schema
	 */
	public BagFactory(Schema schema) {
		Preconditions.checkNotNull(schema);
		this.schema = schema;
	}

	@Override
	public BagFactory clone() {
		return new BagFactory(this.schema);
	}

	/**
	 *
	 * @param facts
	 * @return
	 * 		a bag initialised with the input set of atoms
	 */
	public Bag createBag(Collection<Predicate> facts) {
		return new Bag(facts, this.schema.getDependencies());
	}
}
