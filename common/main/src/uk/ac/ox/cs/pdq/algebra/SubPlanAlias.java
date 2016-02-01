package uk.ac.ox.cs.pdq.algebra;

import java.util.List;

import uk.ac.ox.cs.pdq.fol.Term;
import uk.ac.ox.cs.pdq.plan.Plan;
import uk.ac.ox.cs.pdq.util.TupleType;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

/**
 * Alias operator which acts as a pointed to other sub-plans.
 *
 * @author Julien Leblay
 */
@Deprecated
public class SubPlanAlias extends RelationalOperator {

	private static int globalId = 0;
	private static final String ALIAS_PREFIX = "ALIAS";

	/** The sub-plan this operator aliases to . */
	public Plan subPlan;

	public final int id;

	private final List<? extends Term> output;
	private final List<? extends Term> input;

	/**
	 * Constructor for SubPlanAlias.
	 * @param output List<Term>
	 * @param outputType TupleType
	 */
	public SubPlanAlias(List<Term> output, TupleType outputType) {
		this(Lists.<Term> newLinkedList(), TupleType.EmptyTupleType, output, outputType, null);
	}

	/**
	 * Constructor for SubPlanAlias.
	 * @param p Plan
	 */
	public SubPlanAlias(Plan p) {
		this(p.getInputs(), p.getOperator().getInputType(), p.getOutput(), p.getOperator().getType(), p);
	}

	/**
	 * Constructor for SubPlanAlias.
	 * @param input List<? extends Term>
	 * @param inputType TupleType
	 * @param output List<? extends Term>
	 * @param outputType TupleType
	 * @param subPlan Plan
	 */
	private SubPlanAlias(
			List<? extends Term> input, TupleType inputType, List<? extends Term> output, TupleType outputType,
			Plan subPlan) {
		super(inputType, outputType);
		Preconditions.checkArgument(subPlan != null ? subPlan.getInputs().equals(input) : true);
		Preconditions.checkArgument(subPlan != null ? subPlan.getOutput().equals(output) : true);
		this.output = output;
		this.input = input;
		this.subPlan = subPlan;
		this.id = globalId++;
	}

	/**
	 * @param p Plan
	 */
	public void setPlan(Plan p) {
		this.subPlan = p;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see uk.ac.ox.cs.pdq.plan.relational.logical.LogicalOperator#deepCopy()
	 */
	@Override
	public SubPlanAlias deepCopy() throws RelationalOperatorException {
		return new SubPlanAlias(this.subPlan);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder result = new StringBuilder();
		result.append(this.getClass().getSimpleName()).append('(');
		result.append(ALIAS_PREFIX).append(this.id).append(')');
		return result.toString();
	}

	/**
	 * @return Plan
	 */
	public Plan getPlan() {
		return this.subPlan;
	}

	/**
	 * @return Integer
	 */
	@Override
	public Integer getDepth() {
		if (this.subPlan != null) {
			return ((RelationalOperator) this.subPlan.getOperator()).getDepth();
		}
		return null;
	}

	/**
	 * @return boolean
	 */
	@Override
	public boolean isClosed() {
		if (this.subPlan != null) {
			return ((RelationalOperator) this.subPlan.getOperator()).isClosed();
		}
		return false;
	}

	/**
	 * @param i int
	 * @return Term
	 */
	@Override
	public Term getColumn(int i) {
		return this.getColumns().get(i);
	}

	/**
	 * @return List<Term>
	 */
	@Override
	public List<Term> getColumns() {
		return (List<Term>) this.output;
	}

	/**
	 * @return List<Term>
	 */
	@Override
	public List<Term> getInputTerms() {
		return (List<Term>) this.input;
	}

	/**
	 * @return boolean
	 */
	@Override
	public boolean isQuasiLeaf() {
		if (this.subPlan != null) {
			return ((RelationalOperator) this.subPlan.getOperator()).isQuasiLeaf();
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see uk.ac.ox.cs.pdq.plan.relational.logical.LogicalOperator#isLeftDeep()
	 */
	@Override
	public boolean isLeftDeep() {
		if (this.subPlan != null) {
			return ((RelationalOperator) this.subPlan.getOperator()).isLeftDeep();
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * uk.ac.ox.cs.pdq.plan.relational.logical.LogicalOperator#isRightDeep()
	 */
	@Override
	public boolean isRightDeep() {
		if (this.subPlan != null) {
			return ((RelationalOperator) this.subPlan.getOperator()).isRightDeep();
		}
		return false;
	}
}
