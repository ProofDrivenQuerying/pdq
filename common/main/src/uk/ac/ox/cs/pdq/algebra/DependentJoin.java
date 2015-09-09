package uk.ac.ox.cs.pdq.algebra;

import java.util.ArrayList;
import java.util.List;

import uk.ac.ox.cs.pdq.fol.Term;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;


/**
 * A dependent join operator
 *
 * @author Efthymia Tsamoura
 * @author Julien Leblay
 */
public class DependentJoin extends Join {

	/** A map each RHS input to the position of its corresponding output in the LHS, -1 otherwise. */
	protected final List<Integer> sidewaysInput;

	/**
	 * Instantiates a new join given the input left and right children
	 * @param left
	 * @param right
	 */
	public DependentJoin(RelationalOperator left, RelationalOperator right) {
		this(Lists.newArrayList(left, right), inferSidewaysInputs(left, right));
	}

	/**
	 * Instantiates a new join.
	 *
	 * @param left LogicalOperator
	 * @param right LogicalOperator
	 * @param sidewaysInput List<Integer>
	 */
	public DependentJoin(RelationalOperator left, RelationalOperator right, List<Integer> sidewaysInput) {
		this(Lists.newArrayList(left, right), sidewaysInput);
	}

	/**
	 * Instantiates a new join given the input children
	 * @param children
	 * @param sidewaysInput
	 */
	private DependentJoin(List<RelationalOperator> children, List<Integer> sidewaysInput) {
		super(inferInputTerms(children, sidewaysInput), children);
		Preconditions.checkArgument(children.size() == 2);
		Preconditions.checkArgument(children.get(1).getInputTerms().size() >= sidewaysInput.size());
		this.sidewaysInput = sidewaysInput;
	}

	/**
	 * Infer the tuple type of the given collection of children.
	 * @param children
	 * @param sidewaysInput List<Integer>
	 * @return List<Term>
	 */
	protected static List<Term> inferInputTerms(List<RelationalOperator> children, List<Integer> sidewaysInput) {
		Preconditions.checkArgument(children.size() == 2);
		List<Term> leftInputs = children.get(0).getInputTerms();
		List<Term> rightInputs = children.get(1).getInputTerms();
		List<Term> result = Lists.newArrayList(leftInputs);
		for (int i = 0, l = sidewaysInput.size(); i < l; i++) {
			if (sidewaysInput.get(i) < 0) {
				result.add(rightInputs.get(i));
			}
		}
		return result;
	}

	/**
	 * @return LogicalOperator
	 */
	public RelationalOperator getLeft() {
		return this.children.get(0);
	}

	/**
	 * @return LogicalOperator
	 */
	public RelationalOperator getRight() {
		return this.children.get(1);
	}


	/**
	 * @param children
	 * @return List<Integer>
	 */
	private static List<Integer> inferSidewaysInputs(RelationalOperator left, RelationalOperator right) {
		Preconditions.checkNotNull(left);
		Preconditions.checkNotNull(right);
		List<Integer> result = new ArrayList<>(right.getInputTerms().size());
		for (Term term: right.getInputTerms()) {
			result.add(left.getColumns().indexOf(term));
		}
		return result;
	}

	/**
	 * Deep copy.
	 * @return a deep copy of the operator.
	 * @throws RelationalOperatorException
	 */
	@Override
	public DependentJoin deepCopy() throws RelationalOperatorException {
		return new DependentJoin(this.getLeft(), this.getRight(), Lists.newArrayList(this.sidewaysInput));
	}

	/**
	 * @return List<Integer>
	 */
	public List<Integer> getSidewaysInput() {
		return this.sidewaysInput;
	}

	/**
	 * @return true if the right child has at least one input coming from the left child.
	 */
	public boolean hasSidewaysInputs() {
		for (int i: this.sidewaysInput) {
			if (i >= 0) {
				return true;
			}
		}
		return false;
	}
}
