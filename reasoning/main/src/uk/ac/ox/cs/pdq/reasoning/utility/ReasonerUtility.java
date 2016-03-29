package uk.ac.ox.cs.pdq.reasoning.utility;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.log4j.Logger;

import uk.ac.ox.cs.pdq.db.Attribute;
import uk.ac.ox.cs.pdq.db.Constraint;
import uk.ac.ox.cs.pdq.db.EGD;
import uk.ac.ox.cs.pdq.db.TGD;
import uk.ac.ox.cs.pdq.fol.Conjunction;
import uk.ac.ox.cs.pdq.fol.ConjunctiveQuery;
import uk.ac.ox.cs.pdq.fol.Constant;
import uk.ac.ox.cs.pdq.fol.Equality;
import uk.ac.ox.cs.pdq.fol.Atom;
import uk.ac.ox.cs.pdq.fol.Query;
import uk.ac.ox.cs.pdq.fol.Predicate;
import uk.ac.ox.cs.pdq.fol.Term;
import uk.ac.ox.cs.pdq.fol.Variable;
import uk.ac.ox.cs.pdq.reasoning.chase.ParallelEGDChaser;
import uk.ac.ox.cs.pdq.reasoning.chase.RestrictedChaser;
import uk.ac.ox.cs.pdq.reasoning.chase.state.ChaseState;
import uk.ac.ox.cs.pdq.reasoning.chase.state.DatabaseListState;
import uk.ac.ox.cs.pdq.reasoning.chase.state.ListState;
import uk.ac.ox.cs.pdq.reasoning.homomorphism.DBHomomorphismManager;
import uk.ac.ox.cs.pdq.util.Table;
import uk.ac.ox.cs.pdq.util.Utility;

import com.beust.jcommander.internal.Lists;
import com.google.common.base.Preconditions;

// TODO: Auto-generated Javadoc
/**
 * The Class ReasonerUtility.
 *
 * @author Efthymia Tsamoura
 */
public class ReasonerUtility {

	/** The log. */
	protected static Logger log = Logger.getLogger(ReasonerUtility.class);

	/**
	 * Checks if is key.
	 *
	 * @param table the table
	 * @param candidateKeys the candidate keys
	 * @param constraints the constraints
	 * @param egdChaser the egd chaser
	 * @param detector the detector
	 * @return 		true if the input set of attributes is a key of the input table
	 */
	public boolean isKey(Table table, List<Attribute> candidateKeys, Collection<? extends Constraint> constraints, ParallelEGDChaser egdChaser, DBHomomorphismManager detector) {
		//Create the set of EGDs that correspond to the given table and keys
		EGD egd = EGD.getEGDs(new Predicate(table.getName(),table.getHeader().size()), (List<Attribute>) table.getHeader(), candidateKeys);
		
		Query<?> lquery = new ConjunctiveQuery(new Atom(new Predicate("Q", egd.getFree().size()), egd.getFree()), egd.getLeft());
		
		Query<?> rquery = new ConjunctiveQuery(new Atom(new Predicate("Q", egd.getRight().getTerms().size()), egd.getRight().getTerms()), 
				Conjunction.of(egd.getRight().getAtoms()));
		
		//Creates a chase state that consists of the canonical database of the input query.
		ListState state = new DatabaseListState(lquery, detector);
		return egdChaser.entails(state, lquery.getFreeToCanonical(), rquery, constraints);
	}
		
	/**
	 * Checks if is active trigger.
	 *
	 * @param match the match
	 * @param s the s
	 * @return 		true if the input trigger is active.
	 * 
	 * (From modern dependency theory notes)
	 * Consider an instance I, a set Base of values, and a TGD
	 * 		\delta = \forall x  \sigma(\vec{x}) --> \exists y  \tau(\vec{x}, \vec{y})
	 * 		A trigger for \delta in I is a homomorphism h of \sigma into I. A trigger is active if it
	 * 		does not extend to a homomorphism h0 into I. Informally, a trigger is a tuple \vec{c}
	 * 		satisfying \sigma, and it is active if there is no witness \vec{y} that makes \tau holds.
	 */
	public boolean isActiveTrigger(Match match, ChaseState s) {
		Preconditions.checkNotNull(match);
		if(match.getQuery() instanceof EGD) {
			
			Preconditions.checkArgument(s instanceof DatabaseListState);
			for(Equality equality:((EGD)match.getQuery()).getRight()) {
				Term leftTerm = equality.getTerms().get(0);
				Term rightTerm = equality.getTerms().get(1);
				Constant leftConstant = match.getMapping().get(leftTerm);
				Constant rightConstant = match.getMapping().get(rightTerm);
				Preconditions.checkArgument(rightConstant != null && rightConstant != null);

				if(((DatabaseListState)s).getConstantClasses().getClass(leftConstant) == null ||
				((DatabaseListState)s).getConstantClasses().getClass(rightConstant) == null	|| 
				!((DatabaseListState)s).getConstantClasses().getClass(leftConstant).equals(((DatabaseListState)s).getConstantClasses().getClass(rightConstant))) {
					log.trace("Match " + match + " is active ");
					return true;
				}
			}
			log.trace("Match " + match + " is not active ");
			return false;
		}
		
		Map<Variable, Constant> mapping = match.getMapping();
		Constraint constraint = ((Constraint)match.getQuery());
		Map<Variable, ? extends Term> input = Utility.retain(mapping, constraint.getBothSideVariables());
		Conjunction.Builder cb = Conjunction.builder();
		for (Atom p: constraint.getLeft().getAtoms()) {
			cb.and(p);
		}
		for (Atom p: constraint.getRight().getAtoms()) {
			cb.and(p);
		}
		TGD tgd = new TGD((Conjunction<Atom>) cb.build(), Conjunction.<Atom>of());
		List<Match> matches = s.getMaches(tgd);
		Set<Variable> variables = constraint.getBothSideVariables();
		for(Match m:matches) {
			Map<Variable, Constant> map = Utility.retain(m.getMapping(), variables);
			if (map.equals(input)) {
				return false;
			}
		}
		return true;
	}
	
	/**
	 * Checks if is open trigger.
	 *
	 * @param match the match
	 * @param s the s
	 * @return 		true if the constraint kept in the input match has been already fired with the input homomorphism
	 */
	public boolean isOpenTrigger(Match match, ChaseState s) {
		Map<Variable, Constant> mapping = match.getMapping();
		Constraint constraint = (Constraint) match.getQuery();
		Constraint grounded = constraint.fire(mapping, true);
		return !s.getFiringGraph().isFired(constraint, grounded.getLeft().getAtoms());
	}
}
