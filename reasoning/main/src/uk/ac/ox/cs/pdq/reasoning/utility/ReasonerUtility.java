package uk.ac.ox.cs.pdq.reasoning.utility;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.CollectionUtils;

import uk.ac.ox.cs.pdq.db.Attribute;
import uk.ac.ox.cs.pdq.db.Constraint;
import uk.ac.ox.cs.pdq.db.EGD;
import uk.ac.ox.cs.pdq.fol.Conjunction;
import uk.ac.ox.cs.pdq.fol.ConjunctiveQuery;
import uk.ac.ox.cs.pdq.fol.Constant;
import uk.ac.ox.cs.pdq.fol.Predicate;
import uk.ac.ox.cs.pdq.fol.Query;
import uk.ac.ox.cs.pdq.fol.Signature;
import uk.ac.ox.cs.pdq.fol.Variable;
import uk.ac.ox.cs.pdq.plan.CommandToTGDTranslator;
import uk.ac.ox.cs.pdq.reasoning.chase.EGDChaser;
import uk.ac.ox.cs.pdq.reasoning.chase.RestrictedChaser;
import uk.ac.ox.cs.pdq.reasoning.chase.state.DatabaseListState;
import uk.ac.ox.cs.pdq.reasoning.chase.state.ListState;
import uk.ac.ox.cs.pdq.reasoning.homomorphism.DBHomomorphismManager;
import uk.ac.ox.cs.pdq.util.Table;
import uk.ac.ox.cs.pdq.util.Utility;

import com.beust.jcommander.internal.Lists;

/**
 * 
 * @author Efthymia Tsamoura
 *
 */
public class ReasonerUtility {

	/**
	 * 
	 * @param left
	 * @param right
	 * @return
	 * 		returns true if there is an inclusion dependency from left to right on the common variables 
	 */
	public boolean existsInclustionDependency(Table left, Table right, Collection<? extends Constraint> constraints, RestrictedChaser restrictedChaser, DBHomomorphismManager detector) {
		Query<?> lquery = new CommandToTGDTranslator().toQuery(left);
		
		//Find the variables shared among the tables
		//These should be preserved when checking for entailment 
		List<Attribute> _toShare = Lists.newArrayList();
		_toShare.addAll((Collection<? extends Attribute>) CollectionUtils.intersection(left.getHeader(),right.getHeader()));
		Query<?> rquery = new CommandToTGDTranslator().toQuery(right, _toShare);
		Map<Variable, Constant> _toPreserve = Utility.retain(lquery.getFree2Canonical(), Utility.typedToVariable(_toShare));
	
		//Creates a chase state that consists of the canonical database of the input query.
		ListState state = new DatabaseListState(lquery, detector);
		return restrictedChaser.entails(state, _toPreserve, rquery, constraints);
	}

	/**
	 * 
	 * @param table
	 * @param candidateKeys
	 * @return
	 * 		true if the input set of attributes is a key of the input table
	 */
	public boolean isKey(Table table, List<Attribute> candidateKeys, Collection<? extends Constraint> constraints, EGDChaser egdChaser, DBHomomorphismManager detector) {
		//Create the set of EGDs that correspond to the given table and keys
		EGD egd = EGD.getEGDs(new Signature(table.getName(),table.getHeader().size()), (List<Attribute>) table.getHeader(), candidateKeys);
		
		Query<?> lquery = new ConjunctiveQuery(new Predicate(new Signature("Q", egd.getFree().size()), egd.getFree()), egd.getLeft());
		
		Query<?> rquery = new ConjunctiveQuery(new Predicate(new Signature("Q", egd.getRight().getTerms().size()), egd.getRight().getTerms()), 
				Conjunction.of(egd.getRight().getPredicates()));
		
		//Creates a chase state that consists of the canonical database of the input query.
		ListState state = new DatabaseListState(lquery, detector);
		return egdChaser.entails(state, lquery.getFree2Canonical(), rquery, constraints);
	}
}
