package uk.ac.ox.cs.pdq;

/**
 * Check the consistency of some parameters
 *
 * @author Julien Leblay
 * @author Efthymia Tsamoura
 */
public interface ConsistencyChecker<P1 extends Parameters, P2 extends Parameters, P3 extends Parameters> {
	/**
	 * @param p Parameters
	 * @throws InconsistentParametersException
	 */
	void check(P1 p1, P2 p2, P3 p3) throws InconsistentParametersException;
}
