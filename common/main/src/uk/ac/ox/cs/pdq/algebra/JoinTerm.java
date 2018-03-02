package uk.ac.ox.cs.pdq.algebra;

import org.junit.Assert;

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
	
	private JoinTerm(RelationalTerm child1, RelationalTerm child2) {
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
