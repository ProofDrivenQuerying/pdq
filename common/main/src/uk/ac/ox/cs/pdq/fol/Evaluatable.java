package uk.ac.ox.cs.pdq.fol;

import java.util.List;

/**
 * Common interface to any formula that can be evaluated. For instance, both
 * queries and integrity constraints fall under this category.
 *
 * @author Julien Leblay
 */
public interface Evaluatable {

	/**
	 * @return List<Term>
	 */
	List<Term> getFree();

	/**
	 * @return Formula
	 */
	Formula getBody();
}
