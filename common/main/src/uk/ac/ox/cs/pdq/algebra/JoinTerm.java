// This file is part of PDQ (https://github.com/michaelbenedikt/pdq) which is released under the MIT license.
// See accompanying LICENSE for copyright notice and full details.

package uk.ac.ox.cs.pdq.algebra;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.Assert;

import com.google.common.base.Preconditions;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;

import uk.ac.ox.cs.pdq.db.Attribute;
import uk.ac.ox.cs.pdq.fol.Constant;
import uk.ac.ox.cs.pdq.fol.Formula;
import uk.ac.ox.cs.pdq.fol.Term;
import uk.ac.ox.cs.pdq.fol.TypedConstant;

/**
 * CartesianProduct with simple conditions.
 * @author Efthymia Tsamoura
 *
 */
public class JoinTerm extends CartesianProductTerm {
	protected static final long serialVersionUID = -2424275295263353630L;

	/** The join conditions. */
	protected final Condition joinConditions;

	/**  Cached string representation. */
	protected String toString = null;
	
	protected JoinTerm(RelationalTerm child1, RelationalTerm child2) {
		this(child1, child2,JoinTerm.computeJoinConditions(new RelationalTerm[] {child1,child2}));
	}
	
	protected JoinTerm(RelationalTerm child1, RelationalTerm child2, Condition joinConditions) {
		this(child1, child2, joinConditions, false);
	}
	
	protected JoinTerm(RelationalTerm child1, RelationalTerm child2, Condition joinConditions, boolean isDependentJoin) {
		super(child1, child2, isDependentJoin);
		JoinTerm.assertJoinCondition(joinConditions, child1, child2);
		this.joinConditions = joinConditions;
	}

	public Condition getJoinConditions() {
		return this.joinConditions;
	}

	/** Filters the attribute equality conditions from all other conditions.
	 * @return
	 */
	public AttributeEqualityCondition[] simpleJoinConditions() {
		if (joinConditions instanceof AttributeEqualityCondition)
			return new AttributeEqualityCondition[] {(AttributeEqualityCondition) joinConditions};
		
		SimpleCondition[] conditions = ((ConjunctiveCondition) joinConditions).getSimpleConditions();
		List<AttributeEqualityCondition> attEC = new ArrayList<>();
		for (SimpleCondition sc:conditions) {
			if (sc instanceof AttributeEqualityCondition)
				attEC.add((AttributeEqualityCondition) sc);
		}
		return attEC.toArray(new AttributeEqualityCondition[attEC.size()]);
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
    
    public static JoinTerm create(RelationalTerm child1, RelationalTerm child2, Condition joinConditions) {
        return Cache.joinTerm.retrieve(new JoinTerm(child1, child2, joinConditions));
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
			RelationalTermAsLogic TNewlogic = CartesianProductTerm.merge(T1logic,T2logic);
			// no conditions, simple join.
			return TNewlogic;
		} else {
			// this case deals with different joins conditions.
			return JoinTerm.applyConditions(T1logic,T2logic,this);
		}
	}

	/**
	 * Asserts that the given condition can be applied to the given attribute types.
	 * Returns false when there are mismatching types.
	 * 
	 * @param joinConditions
	 * @param left
	 * @param right
	 * @return
	 */
	public static boolean assertJoinCondition(Condition joinConditions, RelationalTerm left, RelationalTerm right) {
		if (joinConditions instanceof ConjunctiveCondition) {
			for (SimpleCondition conjunct : ((ConjunctiveCondition) joinConditions).getSimpleConditions()) {
				if (conjunct instanceof AttributeEqualityCondition
						&& !assertJoinCondition((AttributeEqualityCondition) conjunct, left, right))
					return false;
			}
			return true;
		} else if (joinConditions instanceof AttributeEqualityCondition)
			return assertJoinCondition((AttributeEqualityCondition) joinConditions, left, right);
		else
			return false;
	}

	/**
	 * Asserts that the given condition can be applied to the given attribute types.
	 * Returns false when there are mismatching types.
	 * 
	 * @param joinCondition
	 * @param left
	 * @param right
	 * @return
	 */
	public static boolean assertJoinCondition(AttributeEqualityCondition joinCondition, RelationalTerm left,
			RelationalTerm right) {
		int numberOfAttributesLeftChild = left.getNumberOfOutputAttributes();
		if (joinCondition.getPosition() >= left.getNumberOfOutputAttributes()
				|| joinCondition.getOther() - numberOfAttributesLeftChild >= right.getNumberOfOutputAttributes())
			return false;
		Type typeOfLeftAttribute = left.getOutputAttribute(joinCondition.getPosition()).getType();
		Type typeOfRightAttribute = right.getOutputAttribute(joinCondition.getOther() - numberOfAttributesLeftChild)
				.getType();
		if (!typeOfLeftAttribute.equals(typeOfRightAttribute))
			return false;
		return true;
	}

	/**
	 * Finds the position pairs for the dependent join's tunnelled variables.
	 * 
	 * @param left
	 * @param right
	 * @return
	 */
	protected static Map<Integer, Integer> computePositionsInRightChildThatAreBoundFromLeftChild(RelationalTerm left,
			RelationalTerm right) {
		Map<Integer, Integer> result = new LinkedHashMap<>();
		for (int index = 0; index < right.getNumberOfInputAttributes(); ++index) {
			Attribute attribute = right.getInputAttribute(index);
			int indexOf = Arrays.asList(left.getOutputAttributes()).indexOf(attribute);
			if (indexOf >= 0)
				result.put(index, indexOf);
		}
		return result;
	}

	/**
	 * Finds all variables in the given relational terms, and computes attribute
	 * equality conditions. Using these conditions creates and returns the
	 * ConjunctiveCondition
	 * 
	 * @param children
	 * @return
	 */
	protected static ConjunctiveCondition computeJoinConditions(RelationalTerm[] children) {
		Multimap<Attribute, Integer> joinVariables = LinkedHashMultimap.create();
		int totalCol = 0;
		// Cluster patterns by variables
		Set<Attribute> inChild = new LinkedHashSet<>();
		for (RelationalTerm child : children) {
			inChild.clear();
			for (int i = 0, l = child.getNumberOfOutputAttributes(); i < l; i++) {
				Attribute col = child.getOutputAttributes()[i];
				if (!inChild.contains(col)) {
					joinVariables.put(col, totalCol);
					inChild.add(col);
				}
				totalCol++;
			}
		}

		List<SimpleCondition> equalities = new ArrayList<>();
		// Remove clusters containing only one pattern
		for (Iterator<Attribute> keys = joinVariables.keySet().iterator(); keys.hasNext();) {
			Collection<Integer> cluster = joinVariables.get(keys.next());
			if (cluster.size() < 2) {
				keys.remove();
			} else {
				Iterator<Integer> i = cluster.iterator();
				Integer left = i.next();
				while (i.hasNext()) {
					Integer right = i.next();
					equalities.add(AttributeEqualityCondition.create(left, right));
				}
			}
		}
		return ConjunctiveCondition.create(equalities.toArray(new SimpleCondition[equalities.size()]));
	}


	/**
	 * Converts the input joinTerm (or DependentJoinTerm) to logic by applying
	 * conditions.
	 * 
	 * @param t1logic
	 *            toLogic result from the left side
	 * @param t2logic
	 *            toLogic result from the right side of the join
	 * @param joinTerm
	 *            join or dependent join term
	 * @return
	 */
	public static RelationalTermAsLogic applyConditions(RelationalTermAsLogic t1logic, RelationalTermAsLogic t2logic,
			RelationalTerm joinTerm) {
		List<SimpleCondition> conditions = joinTerm.getConditions();
		RelationalTermAsLogic TNewlogic = CartesianProductTerm.merge(t1logic, t2logic);
		Formula phiNew = TNewlogic.getFormula();
		Map<Attribute, Term> mapNew = TNewlogic.getMapping();

		// Apply conditions
		for (SimpleCondition s : conditions) {
			if (s instanceof AttributeEqualityCondition) {
				int position = ((AttributeEqualityCondition) s).getPosition();
				int other = ((AttributeEqualityCondition) s).getOther();
				Attribute a = joinTerm.getOutputAttribute(position);
				Attribute b = joinTerm.getOutputAttribute(other);
				Preconditions.checkState(a.equals(b));
				if (t1logic.getMapping().get(b) instanceof Constant) {
					phiNew = SelectionTerm.replaceTerm(phiNew, t2logic.getMapping().get(a),
							t1logic.getMapping().get(b));
					mapNew.put(b, t1logic.getMapping().get(b));
				} else {
					phiNew = SelectionTerm.replaceTerm(phiNew, t1logic.getMapping().get(b),
							t2logic.getMapping().get(a));
					mapNew.put(b, t2logic.getMapping().get(a));
				}
			} else if (s instanceof ConstantEqualityCondition) {
				TypedConstant constant = ((ConstantEqualityCondition) s).getConstant();
				int position = ((ConstantEqualityCondition) s).getPosition();
				Attribute a = joinTerm.getOutputAttribute(position);
				if (t1logic.getMapping().get(a) != null)
					phiNew = SelectionTerm.replaceTerm(phiNew, t1logic.getMapping().get(a), constant);
				if (t2logic.getMapping().get(a) != null)
					phiNew = SelectionTerm.replaceTerm(phiNew, t2logic.getMapping().get(a), constant);
				mapNew.put(a, constant);
			}
		}
		return new RelationalTermAsLogic(phiNew, mapNew);
	}

}
