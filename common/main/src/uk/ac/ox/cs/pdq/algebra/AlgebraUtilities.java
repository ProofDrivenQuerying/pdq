package uk.ac.ox.cs.pdq.algebra;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Assert;

import com.google.common.base.Preconditions;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;

import uk.ac.ox.cs.pdq.db.AccessMethodDescriptor;
import uk.ac.ox.cs.pdq.db.Attribute;
import uk.ac.ox.cs.pdq.db.Relation;
import uk.ac.ox.cs.pdq.fol.Atom;
import uk.ac.ox.cs.pdq.fol.Conjunction;
import uk.ac.ox.cs.pdq.fol.Constant;
import uk.ac.ox.cs.pdq.fol.Formula;
import uk.ac.ox.cs.pdq.fol.Term;
import uk.ac.ox.cs.pdq.fol.TypedConstant;

public class AlgebraUtilities {

	/**
	 * Asserts that the given condition can be applied to the given attribute types.
	 * Returns false when there are mismatching types.
	 * 
	 * @param selectionCondition
	 * @param outputAttributes
	 * @return
	 */
	public static boolean assertSelectionCondition(Condition selectionCondition, Attribute[] outputAttributes) {
		if (selectionCondition instanceof ConjunctiveCondition) {
			for (SimpleCondition conjunct : ((ConjunctiveCondition) selectionCondition).getSimpleConditions()) {
				if (assertSelectionCondition(conjunct, outputAttributes) == false)
					return false;
			}
			return true;
		} else
			return assertSelectionCondition((SimpleCondition) selectionCondition, outputAttributes);
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
	 * Asserts that the given condition can be applied to the given attribute types.
	 * Returns false when there are mismatching types.
	 * 
	 * @param selectionCondition
	 * @param outputAttributes
	 * @return
	 */
	public static boolean assertSelectionCondition(SimpleCondition selectionCondition, Attribute[] outputAttributes) {
		if (selectionCondition instanceof ConstantInequalityCondition) {
			int position = ((ConstantInequalityCondition) selectionCondition).getPosition();
			if (position > outputAttributes.length || !((ConstantInequalityCondition) selectionCondition).getConstant()
					.getType().equals(outputAttributes[position].getType()))
				return false;
			else
				return true;
		} else if (selectionCondition instanceof ConstantEqualityCondition) {
			int position = ((ConstantEqualityCondition) selectionCondition).getPosition();
			if (position > outputAttributes.length || !((ConstantEqualityCondition) selectionCondition).getConstant()
					.getType().equals(outputAttributes[position].getType()))
				return false;
			else
				return true;
		} else if (selectionCondition instanceof AttributeEqualityCondition) {
			int position = ((AttributeEqualityCondition) selectionCondition).getPosition();
			int other = ((AttributeEqualityCondition) selectionCondition).getOther();
			if (position > outputAttributes.length || other > outputAttributes.length)
				return false;
			else
				return true;
		} else
			throw new RuntimeException("Unknown operator type");
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
	 * The access method descriptor contains the index of the attribute only. This
	 * method maps those indexes to the actual Attribute objects.
	 * 
	 * @param relation
	 * @param accessMethod
	 * @return
	 */
	public static Attribute[] computeInputAttributes(Relation relation, AccessMethodDescriptor accessMethod) {
		Assert.assertNotNull(relation);
		Assert.assertNotNull(accessMethod);
		if (accessMethod.getInputs().length == 0) {
			return new Attribute[] {};
		}
		List<Attribute> inputs = new ArrayList<>();
		for (Integer i : accessMethod.getInputs()) {
			inputs.add(relation.getAttributes()[i]);
		}
		return inputs.toArray(new Attribute[inputs.size()]);
	}

	/**
	 * Tests if the given access method and relation is compatible or not. Generates
	 * a list of attributes by filtering out the provided input constants from the
	 * accessMethod's inputs.
	 * 
	 * @param relation
	 * @param accessMethod
	 * @param inputConstants
	 * @return
	 */
	public static Attribute[] computeInputAttributes(Relation relation, AccessMethodDescriptor accessMethod,
			Map<Integer, TypedConstant> inputConstants) {
		Assert.assertNotNull(relation);
		if (!(accessMethod != null && accessMethod.getInputs().length > 0)
				&& (inputConstants == null || inputConstants.isEmpty())) {
			return new Attribute[0];
		}
		Assert.assertTrue(accessMethod != null && accessMethod.getInputs().length > 0);
		Assert.assertNotNull(inputConstants);
		for (Integer position : inputConstants.keySet()) {
			Assert.assertTrue(position < relation.getAttributes().length);
			Assert.assertTrue(Arrays.asList(accessMethod.getInputs()).contains(position));
		}
		List<Attribute> inputs = new ArrayList<>();
		for (Integer i : accessMethod.getInputs()) {
			if (!inputConstants.containsKey(i)) {
				inputs.add(relation.getAttributes()[i]);
			}
		}
		return inputs.toArray(new Attribute[inputs.size()]);
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
	 * Generates a list of attributes where the input attributes are filtered out
	 * from the output attributes.
	 * 
	 * @param child
	 * @return
	 */
	public static Attribute[] computeProperOutputAttributes(RelationalTerm child) {
		Assert.assertNotNull(child);
		List<Attribute> output = Lists.newArrayList();
		output.addAll(Arrays.asList(child.getOutputAttributes()));
		output.removeAll(Arrays.asList(child.getInputAttributes()));
		return output.toArray(new Attribute[output.size()]);
	}

	/**
	 * Returns all accesses in the given RelationalTerm.
	 * 
	 * @param operator
	 *            the operator
	 * @return the access operators that are children of the input operator
	 */
	public static Set<AccessTerm> getAccesses(RelationalTerm operator) {
		// functionality moved to the relationalTerm class in order to be able to cache
		// the accesses, since it is a slow operation to generate the list, but needed
		// frequently.
		return operator.getAccesses();
	}

	/**
	 * Since every input attribute is also an output of any relational term, it is
	 * possible to map the renamings from the output to the inputs. This function
	 * for each input attribute will find out the index of this attribute in the
	 * output list, and using this index will map it to an Attribute in the given
	 * renamings parameter.
	 * 
	 * @param renamings
	 * @param child
	 * @return
	 */
	public static Attribute[] computeRenamedInputAttributes(Attribute[] renamings, RelationalTerm child) {
		Attribute[] newInputAttributes = new Attribute[child.getNumberOfInputAttributes()];
		Attribute[] oldOutputAttributes = child.getOutputAttributes();
		for (int index = 0; index < child.getNumberOfInputAttributes(); ++index) {
			int indexInputAttribute = Arrays.asList(oldOutputAttributes).indexOf(child.getInputAttribute(index));
			Preconditions.checkArgument(indexInputAttribute >= 0, "Input attribute not found");
			newInputAttributes[index] = renamings[indexInputAttribute];
		}
		return newInputAttributes;
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

	/**
	 * Replaces a term (Variable or constant) with a new term in a formula.
	 * 
	 * @param phiNew
	 * @param term
	 * @param constant
	 * @return
	 */
	public static Formula replaceTerm(Formula phi, Term old, Term newTerm) {
		if (phi instanceof Atom) {
			Term[] terms = phi.getTerms();
			for (int i = 0; i < terms.length; i++) {
				if (terms[i].equals(old)) {
					terms[i] = newTerm;
				}
			}
			return Atom.create(((Atom) phi).getPredicate(), terms);
		} else {
			Atom[] atoms = ((Conjunction) phi).getAtoms();
			Atom[] newAtoms = new Atom[atoms.length];
			for (int i = 0; i < atoms.length; i++) {
				newAtoms[i] = (Atom) replaceTerm(atoms[i], old, newTerm);
			}
			return Conjunction.create(newAtoms);
		}
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
		RelationalTermAsLogic TNewlogic = AlgebraUtilities.merge(t1logic, t2logic);
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
					phiNew = AlgebraUtilities.replaceTerm(phiNew, t2logic.getMapping().get(a),
							t1logic.getMapping().get(b));
					mapNew.put(b, t1logic.getMapping().get(b));
				} else {
					phiNew = AlgebraUtilities.replaceTerm(phiNew, t1logic.getMapping().get(b),
							t2logic.getMapping().get(a));
					mapNew.put(b, t2logic.getMapping().get(a));
				}
			} else if (s instanceof ConstantEqualityCondition) {
				TypedConstant constant = ((ConstantEqualityCondition) s).getConstant();
				int position = ((ConstantEqualityCondition) s).getPosition();
				Attribute a = joinTerm.getOutputAttribute(position);
				if (t1logic.getMapping().get(a) != null)
					phiNew = AlgebraUtilities.replaceTerm(phiNew, t1logic.getMapping().get(a), constant);
				if (t2logic.getMapping().get(a) != null)
					phiNew = AlgebraUtilities.replaceTerm(phiNew, t2logic.getMapping().get(a), constant);
				mapNew.put(a, constant);
			}
		}
		return new RelationalTermAsLogic(phiNew, mapNew);
	}

}
