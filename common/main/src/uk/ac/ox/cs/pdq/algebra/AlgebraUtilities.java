package uk.ac.ox.cs.pdq.algebra;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
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

import uk.ac.ox.cs.pdq.db.AccessMethod;
import uk.ac.ox.cs.pdq.db.Attribute;
import uk.ac.ox.cs.pdq.db.Relation;
import uk.ac.ox.cs.pdq.db.TypedConstant;

public class AlgebraUtilities {

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

	public static boolean assertSelectionCondition(SimpleCondition selectionCondition, Attribute[] outputAttributes) {
		if (selectionCondition instanceof ConstantEqualityCondition) {
			int position = ((ConstantEqualityCondition) selectionCondition).getPosition();
			if (position > outputAttributes.length || !((ConstantEqualityCondition) selectionCondition).getConstant().getType().equals(outputAttributes[position].getType()))
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

	protected static Map<Integer, Integer> computePositionsInRightChildThatAreBoundFromLeftChild(RelationalTerm left, RelationalTerm right) {
		Map<Integer, Integer> result = new LinkedHashMap<>();
		for (int index = 0; index < right.getNumberOfInputAttributes(); ++index) {
			Attribute attribute = right.getInputAttribute(index);
			int indexOf = Arrays.asList(left.getOutputAttributes()).indexOf(attribute);
			if (indexOf >= 0)
				result.put(index, indexOf);
		}
		return result;
	}

	protected static Attribute[] computeInputAttributesForDependentJoinTerm(RelationalTerm left, RelationalTerm right) {
		Attribute[] leftInputs = left.getInputAttributes();
		Attribute[] leftOutputs = left.getOutputAttributes();
		Attribute[] rightInputs = right.getInputAttributes();
		List<Attribute> result = Lists.newArrayList(leftInputs);
		for (int attributeIndex = 0; attributeIndex < right.getNumberOfInputAttributes(); attributeIndex++) {
			Attribute inputAttribute = right.getInputAttribute(attributeIndex);
			if (!Arrays.asList(leftOutputs).contains(inputAttribute))
				result.add(rightInputs[attributeIndex]);
		}
		return result.toArray(new Attribute[result.size()]);
	}

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

	public static Attribute[] computeInputAttributes(Relation relation, AccessMethod accessMethod) {
		Assert.assertNotNull(relation);
		Assert.assertNotNull(accessMethod);
		if (accessMethod.getInputs().length == 0) {
			return new Attribute[] {};
		}
		List<Attribute> inputs = new ArrayList<>();
		for (Integer i : accessMethod.getInputs()) {
			inputs.add(relation.getAttribute(i));
		}
		return inputs.toArray(new Attribute[inputs.size()]);
	}

	public static Attribute[] computeInputAttributes(Relation relation, AccessMethod accessMethod, Map<Integer, TypedConstant> inputConstants) {
		Assert.assertNotNull(relation);
		if (!(accessMethod != null && accessMethod.getInputs().length > 0) && (inputConstants == null || inputConstants.isEmpty())) {
			return new Attribute[0];
		}
		Assert.assertTrue(accessMethod != null && accessMethod.getInputs().length > 0);
		Assert.assertNotNull(inputConstants);
		for (Integer position : inputConstants.keySet()) {
			Assert.assertTrue(position < relation.getArity());
			Assert.assertTrue(Arrays.asList(accessMethod.getInputs()).contains(position));
		}
		List<Attribute> inputs = new ArrayList<>();
		for (Integer i : accessMethod.getInputs()) {
			if (!inputConstants.containsKey(i)) {
				inputs.add(relation.getAttribute(i));
			}
		}
		return inputs.toArray(new Attribute[inputs.size()]);
	}

	public static Attribute[] computeInputAttributes(RelationalTerm child1, RelationalTerm child2) {
		Assert.assertNotNull(child1);
		Assert.assertNotNull(child2);
		Attribute[] input = new Attribute[child1.getNumberOfInputAttributes() + child2.getNumberOfInputAttributes()];
		System.arraycopy(child1.getInputAttributes(), 0, input, 0, child1.getNumberOfInputAttributes());
		System.arraycopy(child2.getInputAttributes(), 0, input, child1.getNumberOfInputAttributes(), child2.getNumberOfInputAttributes());
		return input;
	}

	public static Attribute[] computeOutputAttributes(RelationalTerm child1, RelationalTerm child2) {
		Assert.assertNotNull(child1);
		Assert.assertNotNull(child2);
		Attribute[] input = new Attribute[child1.getNumberOfOutputAttributes() + child2.getNumberOfOutputAttributes()];
		System.arraycopy(child1.getOutputAttributes(), 0, input, 0, child1.getNumberOfOutputAttributes());
		System.arraycopy(child2.getOutputAttributes(), 0, input, child1.getNumberOfOutputAttributes(), child2.getNumberOfOutputAttributes());
		return input;
	}

	public static Attribute[] computeProperOutputAttributes(RelationalTerm child) {
		Assert.assertNotNull(child);
		List<Attribute> output = Lists.newArrayList();
		output.addAll(Arrays.asList(child.getOutputAttributes()));
		output.removeAll(Arrays.asList(child.getInputAttributes()));
		return output.toArray(new Attribute[output.size()]);
	}

	/**
	 * Gets the accesses.
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

}
