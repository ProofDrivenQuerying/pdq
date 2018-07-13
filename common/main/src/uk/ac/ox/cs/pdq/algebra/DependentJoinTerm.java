package uk.ac.ox.cs.pdq.algebra;

import java.util.Arrays;
import java.util.Map;

import org.apache.commons.collections4.CollectionUtils;
import org.junit.Assert;

import com.google.common.base.Preconditions;

import uk.ac.ox.cs.pdq.db.Attribute;


/**
 * 
 * @author Efthymia Tsamoura
 *
 */
public class DependentJoinTerm extends JoinTerm {
	protected static final long serialVersionUID = 3160309108592668317L;

	protected final RelationalTerm[] children = new RelationalTerm[2];

	/** The join conditions. */
	protected final Condition joinConditions;
	
	/** Input positions for the right hand child**/
	protected final Map<Integer,Integer> positionsInRightChildThatAreBoundFromLeftChild;

	/**  Cached string representation. */
	protected String toString = null;

	private DependentJoinTerm(RelationalTerm child1, RelationalTerm child2) {
		super(child1,child2,AlgebraUtilities.computeJoinConditions(new RelationalTerm[] {child1,child2}), true);
		Assert.assertNotNull(child1);
		Assert.assertNotNull(child2);
		// The first child most have at least one output that can be used as an input for the second.
		if (!CollectionUtils.containsAny(Arrays.asList(child1.getOutputAttributes()),Arrays.asList(child2.getInputAttributes()))) {
			System.out.println("Error " + Arrays.asList(child1.getOutputAttributes()) + " does not contain any of " + Arrays.asList(child2.getInputAttributes()));
			Assert.assertTrue(CollectionUtils.containsAny(Arrays.asList(child1.getOutputAttributes()),Arrays.asList(child2.getInputAttributes())));
		}
		this.children[0] = child1;
		this.children[1] = child2;
		this.positionsInRightChildThatAreBoundFromLeftChild = AlgebraUtilities.computePositionsInRightChildThatAreBoundFromLeftChild(child1, child2);
		this.joinConditions = AlgebraUtilities.computeJoinConditions(this.children);
	}
	
	public Map<Integer,Integer> getPositionsInLeftChildThatAreInputToRightChild() {
		return this.positionsInRightChildThatAreBoundFromLeftChild;
	}
	
	public Condition getJoinConditions() {
		return this.joinConditions;
	}

	/**
	 * Returns true iff the given input attribute in the right child is bound
	 * from the left child.
	 *  
	 * @return true iff the given attribute is bound from the left child. 
	 * @throws IllegalArgumentException if the given attribute is not an input
	 * attribute in the right child.
	 */
	public boolean isBound(Attribute attribute) {

		Preconditions.checkArgument(Arrays.asList(this.getChild(1).getInputAttributes()).contains(attribute));
		return this.joinMap().containsValue(attribute);
	}

	/**
	 * Returns an array containing those input attribute in the right child 
	 * that are bound from the left child.
	 *  
	 * @return An array of attributes. 
	 */
	public Attribute[] boundAttributes() {
		return Arrays.stream(this.getChild(1).getInputAttributes())
				.filter(attr -> this.isBound(attr)).toArray(Attribute[]::new);
	}
	
	@Override
	public String toString() {
		if(this.toString == null) {
			StringBuilder result = new StringBuilder();
			result.append("DependentJoin");
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
	
    public static DependentJoinTerm create(RelationalTerm child1, RelationalTerm child2) {
        return Cache.dependentJoinTerm.retrieve(new DependentJoinTerm(child1, child2));
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
