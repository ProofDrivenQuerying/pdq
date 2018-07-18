package uk.ac.ox.cs.pdq.algebra;

import org.junit.Assert;


/**
 * 
 * @author Efthymia Tsamoura
 *
 */
public class CartesianProductTerm extends RelationalTerm {
	protected static final long serialVersionUID = -8806125496554968085L;
	protected final RelationalTerm[] children = new RelationalTerm[2];

	/**  Cached string representation. */
	protected String toString = null;

	
	protected CartesianProductTerm(RelationalTerm child1, RelationalTerm child2, boolean isDependentJoin) {
		super(AlgebraUtilities.computeInputAttributes(child1, child2,isDependentJoin), AlgebraUtilities.computeOutputAttributes(child1, child2));
		Assert.assertNotNull(child1);
		Assert.assertNotNull(child2);
		this.children[0] = child1;
		this.children[1] = child2;
	}

	protected CartesianProductTerm(RelationalTerm child1, RelationalTerm child2) {
		this(child1, child2,false);
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		if(this.toString == null) {
			StringBuilder result = new StringBuilder();
			result.append("CartesianProduct");
			result.append('{');
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
	
    public static CartesianProductTerm create(RelationalTerm child1, RelationalTerm child2) {
        return Cache.cartesianProductTerm.retrieve(new CartesianProductTerm(child1, child2));
    }

	@Override
	public RelationalTerm getChild(int childIndex) {
		Assert.assertTrue(childIndex == 0 || childIndex == 1);
		return this.children[0];
	}

	@Override
	public Integer getNumberOfChildren() {
		return this.children.length;
	}
	
	/**
	 * 5) Inductive case for a cartesian product term T_1 times T_2 where the
	 * attributes of T_1 and T_2 are disjoint.
	 * 
	 * let (phi_1, M_1)=T_1.toLogic let (phi_2, M_2)=T_2.toLogic
	 * 
	 * revise phi_1 and M_1 to avoid any variable overlap with phi_2.
	 * 
	 * return phi_3, M_3 where
	 * 
	 * phi_3= phi_1 \wedge phi_2
	 * 
	 * M_3 has domain that is the union of the domains of M_1 and M_2, and M_3(a)=
	 * M_1(a) on the domain of M_1 while M_3(a)= M_2(a) on the domain of M_2
	 */
	@Override
	public RelationalTermAsLogic toLogic() {
		RelationalTerm T1 = getChildren()[0];
		RelationalTerm T2 = getChildren()[1];
		RelationalTermAsLogic t1Logic = T1.toLogic();
		RelationalTermAsLogic t2Logic = T2.toLogic();
		return AlgebraUtilities.merge(t1Logic,t2Logic);
	}
}
