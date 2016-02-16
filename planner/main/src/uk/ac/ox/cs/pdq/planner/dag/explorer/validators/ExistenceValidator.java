package uk.ac.ox.cs.pdq.planner.dag.explorer.validators;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;

import uk.ac.ox.cs.pdq.db.AccessMethod;
import uk.ac.ox.cs.pdq.db.Relation;
import uk.ac.ox.cs.pdq.planner.dag.ApplyRule;
import uk.ac.ox.cs.pdq.planner.dag.ConfigurationUtility;
import uk.ac.ox.cs.pdq.planner.dag.DAGChaseConfiguration;
import uk.ac.ox.cs.pdq.planner.dag.DAGConfiguration;

import com.google.common.base.Preconditions;


// TODO: Auto-generated Javadoc
/**
 * Requires the input pair of configurations to be non trivial and ...
 * @author Efthymia Tsamoura

 */
public class ExistenceValidator implements Validator{

	/** List of accesses of the output plan. */
	private final List<Pair<Relation,AccessMethod>> accesses;


	/**
	 * Constructor for ExistanceValidator.
	 *
	 * @param accesses the accesses
	 */
	public ExistenceValidator(List<Pair<Relation,AccessMethod>> accesses) {
		Preconditions.checkArgument(accesses != null && !accesses.isEmpty());
		this.accesses = accesses;
	}

	/**
	 * Validate.
	 *
	 * @param left DAGConfiguration
	 * @param right DAGConfiguration
	 * @return boolean
	 * @see uk.ac.ox.cs.pdq.dag.explorer.validators.Validator#validate(DAGConfiguration, DAGConfiguration)
	 */
	@Override
	public boolean validate(DAGChaseConfiguration left, DAGChaseConfiguration right) {
		return  this.doExist(left, right) && ConfigurationUtility.isNonTrivial(left, right);
	}

	/**
	 * Validate.
	 *
	 * @param left DAGConfiguration
	 * @param right DAGConfiguration
	 * @param depth int
	 * @return boolean
	 * @see uk.ac.ox.cs.pdq.dag.explorer.validators.Validator#validate(DAGConfiguration, DAGConfiguration, int)
	 */
	@Override
	public boolean validate(DAGChaseConfiguration left, DAGChaseConfiguration right, int depth) {
		return left.getHeight() + right.getHeight() == depth && this.validate(left, right);
	}

	/**
	 * Clone.
	 *
	 * @return Validator
	 * @see uk.ac.ox.cs.pdq.planner.dag.explorer.validators.Validator#clone()
	 */
	@Override
	public Validator clone() {
		return new ExistenceValidator(this.accesses);
	}
	
	/**
	 * Do exist.
	 *
	 * @param left the left
	 * @param right the right
	 * @return true, if successful
	 */
	private boolean doExist(DAGChaseConfiguration left, DAGChaseConfiguration right) {
		List<Pair<Relation,AccessMethod>> accesses = new ArrayList<>();
		for(ApplyRule applyRule:left.getApplyRulesList()) {
			accesses.add(Pair.of(applyRule.getRelation(), applyRule.getRule().getAccessMethod()));
		}
		for(ApplyRule applyRule:right.getApplyRulesList()) {
			accesses.add(Pair.of(applyRule.getRelation(), applyRule.getRule().getAccessMethod()));
		}
		if(Collections.indexOfSubList(this.accesses, accesses) != -1) {
			return true;
		}
		return false;
	}

}
