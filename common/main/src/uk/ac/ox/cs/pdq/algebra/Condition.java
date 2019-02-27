package uk.ac.ox.cs.pdq.algebra;

import java.io.Serializable;
import java.util.function.Predicate;

import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import uk.ac.ox.cs.pdq.db.tuple.Tuple;
import uk.ac.ox.cs.pdq.io.jaxb.adapters.ConditionAdapter;

/**
 * Represents a condition, that can be used to create -for example- a SelectionTerm, or a dependent join term. 
 *  
 * @author Unknown
 * @author Gabor
 */
@XmlJavaTypeAdapter(ConditionAdapter.class)
public abstract class Condition implements Serializable {
	private static final long serialVersionUID = -2227912493390798172L;

	/**
	 * Evaluates the condition on a Tuple.
	 * @param tuple A Tuple.
	 * @return true if the given tuple satisfies the condition; otherwise false.
	 */
	public abstract boolean isSatisfied(Tuple tuple);
	
	/**
	 * Returns a Predicate representation of the condition.
	 * @return The condition expressed as a java.util.function.Predicate object. 
	 */
	public Predicate<Tuple> asPredicate() {
		return (tuple) -> this.isSatisfied(tuple);
	}	
}
