package uk.ac.ox.cs.pdq.algebra;

import java.util.List;
import java.util.Objects;

import uk.ac.ox.cs.pdq.db.AccessMethod;
import uk.ac.ox.cs.pdq.db.AccessMethod.Types;
import uk.ac.ox.cs.pdq.db.Relation;
import uk.ac.ox.cs.pdq.fol.Term;
import uk.ac.ox.cs.pdq.plan.AccessOperator;
import uk.ac.ox.cs.pdq.util.TupleType;
import uk.ac.ox.cs.pdq.util.Utility;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

/**
 * Logical operator representation of an scan (input free access).
 *
 * @author Julien Leblay
 * @author Ethymia Tsamoura
 */
public class Scan extends RelationalOperator implements AccessOperator {

	/** The input table of the access. */
	private final Relation relation;

	/** The free access method associated with this scan. */
	private final AccessMethod accessMethod;

	/** TOCOMMENT The columns. */
	protected List<Term> columns;
	
	/** The output constants this access. */
	protected final List<Term> outputTerms;

	/**
	 * Instantiates a new projection.
	 *
	 * @param relation Relation
	 */
	public Scan(Relation relation) {
		this(relation, findFreeAccessMethod(relation), Utility.typedToTerms(relation.getAttributes()));
	}

	/**
	 * Find free access method.
	 *
	 * @param relation Relation
	 * @return AccessMethod
	 */
	private static AccessMethod findFreeAccessMethod(Relation relation) {
		Preconditions.checkArgument(relation != null);
		Preconditions.checkArgument(relation.hasFreeAccess(), 
				"Attempting to the instantiate a Scan operator from a relation without free access.");
		for (AccessMethod b: relation.getAccessMethods()) {
			if (b.getType() == Types.FREE) {
				return b;
			}
		}
		return null;
	}

	/**
	 * Instantiates a new scan operator.
	 *
	 * @param relation Relation
	 * @param accessMethod AccessMethod
	 * @param outputTerms the output terms
	 */
	protected Scan(Relation relation, AccessMethod accessMethod, List<Term> outputTerms) {
		super(TupleType.DefaultFactory.createFromTyped(relation.getAttributes()));
		Preconditions.checkArgument(accessMethod != null);
		Preconditions.checkArgument(accessMethod.getType() == Types.FREE);
		Preconditions.checkArgument(relation != null);
		this.relation = relation;
		this.accessMethod = accessMethod;
		this.columns = Utility.typedToTerms(relation.getAttributes());
		this.outputTerms = outputTerms;
	}

	/**
	 * Gets the relation to be scanned.
	 *
	 * @return the relation scanned by the operator
	 * @see uk.ac.ox.cs.pdq.plan.AccessOperator#getRelation()
	 */
	@Override
	public Relation getRelation() {
		return this.relation;
	}

	/*
	 * (non-Javadoc)
	 * @see uk.ac.ox.cs.pdq.algebra.RelationalOperator#deepCopy()
	 */
	@Override
	public Scan deepCopy() throws RelationalOperatorException {
		return new Scan(this.relation);
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder result = new StringBuilder();
		result.append(this.getClass().getSimpleName()).append('(');
		result.append(this.relation.getName()).append(')');
		return result.toString();
	}

	/**
	 * TOCOMMENT What is this?
	 * Gets the depth.
	 *
	 * @return Integer
	 */
	@Override
	public Integer getDepth() {
		return 1;
	}

	/*
	 * (non-Javadoc)
	 * @see uk.ac.ox.cs.pdq.algebra.RelationalOperator#getColumn(int)
	 */
	@Override
	public Term getColumn(int i) {
		Preconditions.checkArgument(0 <= i && i < this.columns.size()) ;
		return this.columns.get(i);
	}

	/*
	 * (non-Javadoc)
	 * @see uk.ac.ox.cs.pdq.algebra.RelationalOperator#getColumns()
	 */
	@Override
	public List<Term> getColumns() {
		return this.columns;
	}

	/**
	 * Gets the input terms.
	 *
	 * @return List<Term>
	 */
	@Override
	public List<Term> getInputTerms() {
		return Lists.newArrayList();
	}

	/**
	 * Two scan operators are equal if they scan the same terms of the same relation 
	 * ("same" in both cases tested with the corresponding equals() method).
	 *
	 * @param o the other scan operator 
	 * @return boolean
	 */
	@Override
	public boolean equals(Object o) {
		return super.equals(o)
				&& this.getClass().isInstance(o)
				&& this.relation.equals(((Scan) o).relation)
				&& this.columns.equals(((Scan) o).columns);

	}


	@Override
	public int hashCode() {
		return Objects.hash(this.outputType, this.inputType, this.columns,
				this.relation, this.metadata);
	}

	/**
	 * Gets the access method.
	 *
	 * @return AccessMethod
	 * @see uk.ac.ox.cs.pdq.plan.AccessOperator#getAccessMethod()
	 */
	@Override
	public AccessMethod getAccessMethod() {
		return this.accessMethod;
	}

	/**
	 * Checks if is closed.
	 *
	 * @return boolean
	 */
	@Override
	public boolean isClosed() {
		return true;
	}

	/**
	 * TOCOMMENT
	 * Checks if is quasi leaf.
	 *
	 * @return boolean
	 */
	@Override
	public boolean isQuasiLeaf() {
		return true;
	}

	/*
	 * (non-Javadoc)
	 * @see uk.ac.ox.cs.pdq.algebra.RelationalOperator#isLeftDeep()
	 */
	@Override
	public boolean isLeftDeep() {
		return true;
	}

	/*
	 * (non-Javadoc)
	 * @see uk.ac.ox.cs.pdq.algebra.RelationalOperator#isRightDeep()
	 */
	@Override
	public boolean isRightDeep() {
		return true;
	}
}
