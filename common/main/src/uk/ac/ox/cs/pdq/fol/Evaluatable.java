package uk.ac.ox.cs.pdq.fol;

import java.util.List;

// TODO: Auto-generated Javadoc
/**
 * Common interface to any formula that can be evaluated. For instance, both
 * queries and integrity constraints fall under this category.
 *
 * @author Julien Leblay
 */
public interface Evaluatable {

	/**
	 * Gets the free.
	 *
	 * @return List<Term>
	 */
	List<Term> getFree();

	/**
	 * Gets the body.
	 *
	 * @return Formula
	 */
	Formula getBody();
}
