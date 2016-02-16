package uk.ac.ox.cs.pdq;

// TODO: Auto-generated Javadoc
/**
 * Check the consistency of some parameters.
 *
 * @author Julien Leblay
 * @author Efthymia Tsamoura
 * @param <P1> the generic type
 * @param <P2> the generic type
 * @param <P3> the generic type
 */
public interface ConsistencyChecker<P1 extends Parameters, P2 extends Parameters, P3 extends Parameters> {
	
	/**
	 * Check.
	 *
	 * @param p1 the p1
	 * @param p2 the p2
	 * @param p3 the p3
	 * @throws InconsistentParametersException the inconsistent parameters exception
	 */
	void check(P1 p1, P2 p2, P3 p3) throws InconsistentParametersException;
}
