// This file is part of PDQ (https://github.com/ProofDrivenQuerying/pdq) which is released under the MIT license.
// See accompanying LICENSE for copyright notice and full details.

package uk.ac.ox.cs.pdq.algebra;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;

import com.google.common.collect.Lists;

import uk.ac.ox.cs.pdq.db.Attribute;
import uk.ac.ox.cs.pdq.fol.Conjunction;
import uk.ac.ox.cs.pdq.fol.Formula;
import uk.ac.ox.cs.pdq.fol.Term;


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
		super(CartesianProductTerm.computeInputAttributes(child1, child2,isDependentJoin), CartesianProductTerm.computeOutputAttributes(child1, child2));
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
		return CartesianProductTerm.merge(t1Logic,t2Logic);
	}

	/**
	 * Creates a list that contains both left and right side output attributes. No
	 * filtering, duplication is allowed.
	 * 
	 * @param child1
	 * @param child2
	 * @return
	 */
	public static Attribute[] computeOutputAttributes(RelationalTerm child1, RelationalTerm child2) {
		Assert.assertNotNull(child1);
		Assert.assertNotNull(child2);
		Attribute[] input = new Attribute[child1.getNumberOfOutputAttributes() + child2.getNumberOfOutputAttributes()];
		System.arraycopy(child1.getOutputAttributes(), 0, input, 0, child1.getNumberOfOutputAttributes());
		System.arraycopy(child2.getOutputAttributes(), 0, input, child1.getNumberOfOutputAttributes(),
				child2.getNumberOfOutputAttributes());
		return input;
	}

	/**
	 * Generates a list of attributes by adding the left and right inputs. In case
	 * of dependent join it will not return the right-inputs that are provided as an
	 * output from the left side. accessMethod's inputs.
	 * 
	 * @param left
	 * @param right
	 * @param isDependentJoinTerm
	 * @return
	 */
	public static Attribute[] computeInputAttributes(RelationalTerm left, RelationalTerm right,
			boolean isDependentJoinTerm) {
		Assert.assertNotNull(left);
		Assert.assertNotNull(right);
		Attribute[] leftInputs = left.getInputAttributes();
		Attribute[] leftOutputs = left.getOutputAttributes();
		Attribute[] rightInputs = right.getInputAttributes();
		List<Attribute> result = Lists.newArrayList(leftInputs);
		for (int attributeIndex = 0; attributeIndex < right.getNumberOfInputAttributes(); attributeIndex++) {
			Attribute inputAttribute = right.getInputAttribute(attributeIndex);

			if (!isDependentJoinTerm || !Arrays.asList(leftOutputs).contains(inputAttribute)) {
				// only dependent join maps attribute from left to right.
				result.add(rightInputs[attributeIndex]);
			}
		}
		return result.toArray(new Attribute[result.size()]);

	}

	/**
	 * Merges two RelationalTermAsLogic object into one that contains the
	 * conjunction formula of the two source formulas.
	 * 
	 * @param left
	 * @param right
	 * @return
	 */
	public static RelationalTermAsLogic merge(RelationalTermAsLogic left, RelationalTermAsLogic right) {
		if (left == null && right == null)
			return null;
		if (left == null)
			return right;
		Formula phi_1 = left.getFormula();
		Formula phi_2 = right.getFormula();
		Formula phi = Conjunction.create(phi_1, phi_2);

		Map<Attribute, Term> map_1 = left.getMapping();
		Map<Attribute, Term> map_2 = right.getMapping();
		Map<Attribute, Term> map = new HashMap<>();
		map.putAll(map_1);
		map.putAll(map_2);

		return new RelationalTermAsLogic(phi, map);
	}	
}
