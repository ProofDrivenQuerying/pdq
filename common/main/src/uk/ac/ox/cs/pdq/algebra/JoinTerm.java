package uk.ac.ox.cs.pdq.algebra;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

import org.junit.Assert;

import com.google.common.base.Preconditions;

import uk.ac.ox.cs.pdq.db.Attribute;
import uk.ac.ox.cs.pdq.db.TypedConstant;
import uk.ac.ox.cs.pdq.util.Tuple;

/**
 *
 * TOCOMMENT: IS THIS A NATURAL JOIN? IF SO EXPLAIN 
 * @author Efthymia Tsamoura
 *
 */
public class JoinTerm extends RelationalTerm {
	protected static final long serialVersionUID = -2424275295263353630L;

	protected final RelationalTerm[] children = new RelationalTerm[2];

	/** The join conditions. */
	protected final Condition joinConditions;

	/**  Cached string representation. */
	protected String toString = null;
	
	protected JoinTerm(RelationalTerm child1, RelationalTerm child2) {
		super(AlgebraUtilities.computeInputAttributes(child1, child2), 
				AlgebraUtilities.computeOutputAttributes(child1, child2));
		Assert.assertNotNull(child1);
		Assert.assertNotNull(child2);
		this.children[0] = child1;
		this.children[1] = child2;
		this.joinConditions = AlgebraUtilities.computeJoinConditions(this.children);
	}

	public Condition getJoinConditions() {
		return this.joinConditions;
	}

	public TypeEqualityCondition[] simpleJoinConditions() {
		if (joinConditions instanceof TypeEqualityCondition)
			return new TypeEqualityCondition[] {(TypeEqualityCondition) joinConditions};
		
		SimpleCondition[] conditions = ((ConjunctiveCondition) joinConditions).getSimpleConditions();
		return Arrays.copyOf(conditions, conditions.length, TypeEqualityCondition[].class);
	}
	/**
	 * Returns an attribute map representing the join condition.
	 * 
	 * @return A {@code Map} from left child attributes to right child attributes.
	 */
	public Map<Attribute, Attribute> joinMap() {

		Attribute[] leftAttrs = this.getChild(0).getOutputAttributes();
		Attribute[] rightAttrs = this.getChild(1).getOutputAttributes();
		return Arrays.asList(this.simpleJoinConditions()).stream()
				.collect(Collectors.toMap(
						c -> leftAttrs[c.getPosition()], 
						c -> rightAttrs[c.getOther() - leftAttrs.length]
						));
	}

	/**
	 * Returns the tuple-dependent join condition for this plan. This is the generic 
	 * join condition, which tests only type equality (and is returned by the no-argument 
	 * version of this method), specialised to value(s) in the given tuple.
	 * 
	 * For each TypeEqualityCondition c in the generic join condition, the return value
	 * contains a corresponding ConstantEqualityCondition, which is satisfied on any
	 * tuple whose value at index c.other is equal to the value of the given tuple at 
	 * index c.position.
	 * 
	 * In a JoinTerm, these indices correspond to the concatenated attributes from the 
	 * left+right children. In a DependentJoinTerm the indices correspond to attributes 
	 * in the right child only 
	 * 
	 * @param tuple A Tuple 
	 * @return A ConstantEqualityCondition object or conjunction of such.
	 */
	public Condition getJoinCondition(Tuple tuple) {

		if (this.joinConditions instanceof TypeEqualityCondition)
			return this.getJoinCondition(tuple, (TypeEqualityCondition) this.joinConditions);

		// If the join condition is not a TypeEqualityCondition then it must
		// be a conjunction of multiple TypeEqualityConditions. In that case, handle them 
		// one-by-one and return a conjunction of ConstantEqualityConditions.
		Condition[] simpleConditions = ((ConjunctiveCondition) joinConditions).getSimpleConditions();
		SimpleCondition[] predicates = new SimpleCondition[simpleConditions.length];
		for (int i = 0; i != simpleConditions.length; i++)
			predicates[i] = this.getJoinCondition(tuple, (TypeEqualityCondition) simpleConditions[i]);

		return ConjunctiveCondition.create(predicates);
	}
	/*
	 * Every simple condition in the joinCondition is a TypeEqualityCondition. This method
	 * converts such a condition into a ConstantEqualityCondition, based on the value at a
	 * particular index in a given tuple.
	 */
	protected ConstantEqualityCondition getJoinCondition(Tuple tuple,
			TypeEqualityCondition condition) {

		int position = condition.getPosition();
		Preconditions.checkArgument(tuple.size() > position);

		/* Return a ConstantEqualityCondition derived from the 
		 * TypeEqualityCondition join condition & the tuple. The returned 
		 * condition will be satisfied on any tuple whose value at index 
		 * 'other' is equal to the value of the given tuple at index 'position'.
		 */
		TypedConstant constant = TypedConstant.create(tuple.getValues()[position]);
		int other = condition.getOther();

		return ConstantEqualityCondition.create(other, constant); 
	}
	@Override
	public String toString() {
		if(this.toString == null) {
			StringBuilder result = new StringBuilder();
			result.append("Join");
			result.append('{');
			result.append('[').append(this.joinConditions).append(']');
			result.append(this.children[0].toString());
			result.append(',');
			result.append(this.children[1].toString());
			result.append('}');
			this.toString = result.toString();
		}
		return this.toString;
	}

	@Override
	public RelationalTerm[] getChildren() {
		return this.children.clone();
	}
    
    public static JoinTerm create(RelationalTerm child1, RelationalTerm child2) {
        return Cache.joinTerm.retrieve(new JoinTerm(child1, child2));
    }
    
	@Override
	public RelationalTerm getChild(int childIndex) {
		Assert.assertTrue(childIndex == 0 || childIndex == 1);
		return this.children[childIndex];
	}
	
	@Override
	public Integer getNumberOfChildren() {
		return this.children.length;
	}
	
	/**
	 * 6) Inductive case for natural join of T1 and T2 with common attributes
	 * d1...dk.
	 * 
	 * let (phi_1, M_1)=T_1.toLogic let (phi_2, M_2)=T_2.toLogic
	 * 
	 * revise phi_1 so that variables are disjoint form phi_2 variables, and revise
	 * M_1 accordingly.
	 * 
	 * let x1... xk be M_1(d1)... M_1(dk) let y1....yk be M_2(d1) ... M_2(dk)
	 * 
	 * let sigma be the substitution taking xi to yi
	 * 
	 * let phi'_1 be applying sigma to phi_1
	 * 
	 * We return phi'_1 \wedge phi_2 as the formula
	 * 
	 * The mapping M_3 has domain that is the union of the domains of M_1 and M_2,
	 * and M_3(a)= sigma(M_1(a)) on the domain of M_1 while M_3(a)= rho( M_2(a)) on
	 * the domain of M_2
	 */
	@Override
	public RelationalTermAsLogic toLogic() {
		RelationalTermAsLogic T1logic = getChildren()[0].toLogic();
		RelationalTermAsLogic T2logic = getChildren()[1].toLogic();
		if (getConditions().isEmpty()) { 
			RelationalTermAsLogic TNewlogic = AlgebraUtilities.merge(T1logic,T2logic);
			// no conditions, simple join.
			return TNewlogic;
		} else {
			// this case deals with different joins conditions.
			return AlgebraUtilities.applyConditions(T1logic,T2logic,this);
		}
	}
}
