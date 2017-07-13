package uk.ac.ox.cs.pdq.algebra;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Assert;

import com.google.common.collect.Lists;

import uk.ac.ox.cs.pdq.db.AccessMethod;
import uk.ac.ox.cs.pdq.db.Attribute;
import uk.ac.ox.cs.pdq.db.Relation;
import uk.ac.ox.cs.pdq.db.TypedConstant;

public class AlgebraUtilities {

	public static Attribute[] getInputAttributes(Relation relation, AccessMethod accessMethod) {
		Assert.assertNotNull(relation);
		Assert.assertNotNull(accessMethod);
		if(accessMethod.getInputs().length == 0) {
			return null;
		}
		List<Attribute> inputs = new ArrayList<>();
		for(Integer i:accessMethod.getInputs()) {
			inputs.add(relation.getAttribute(i));
		}
		return inputs.toArray(new Attribute[inputs.size()]);
	}

	public static Attribute[] getInputAttributes(Relation relation, AccessMethod accessMethod, Map<Integer, TypedConstant> inputConstants) {
		Assert.assertNotNull(relation);
		Assert.assertTrue(accessMethod != null && accessMethod.getInputs().length > 0);
		Assert.assertNotNull(inputConstants);
		for(Integer position:inputConstants.keySet()) {
			Assert.assertTrue(position < relation.getArity());
			Assert.assertTrue(Arrays.asList(accessMethod.getInputs()).contains(position));
		}
		List<Attribute> inputs = new ArrayList<>();
		for(Integer i:accessMethod.getInputs()) {
			if(!inputConstants.containsKey(i)) {
				inputs.add(relation.getAttribute(i));
			}
		}
		return inputs.toArray(new Attribute[inputs.size()]);
	}
	
	public static Attribute[] getInputAttributes(RelationalTerm child1, RelationalTerm child2) {
		Assert.assertNotNull(child1);
		Assert.assertNotNull(child2);
		Attribute[] input = new Attribute[child1.getNumberOfInputAttributes() + child2.getNumberOfInputAttributes()];
		System.arraycopy(child1.getInputAttributes(), 0, input, 0, child1.getNumberOfInputAttributes());
		System.arraycopy(child2.getInputAttributes(), 0, input, child1.getNumberOfInputAttributes(), child2.getNumberOfInputAttributes());
		return input;
	}
	
	public static Attribute[] getOutputAttributes(RelationalTerm child1, RelationalTerm child2) {
		Assert.assertNotNull(child1);
		Assert.assertNotNull(child2);
		Attribute[] input = new Attribute[child1.getNumberOfOutputAttributes() + child2.getNumberOfOutputAttributes()];
		System.arraycopy(child1.getNumberOfOutputAttributes(), 0, input, 0, child1.getNumberOfOutputAttributes());
		System.arraycopy(child2.getNumberOfOutputAttributes(), 0, input, child1.getNumberOfOutputAttributes(), child2.getNumberOfOutputAttributes());
		return input;
	}
	
	public static Attribute[] getProperOutputAttributes(RelationalTerm child) {
		Assert.assertNotNull(child);
		List<Attribute> output = Lists.newArrayList();
		output.addAll(Arrays.asList(child.getOutputAttributes()));
		output.removeAll(Arrays.asList(child.getInputAttributes()));
		return output.toArray(new Attribute[output.size()]);
	}
	
	/**
	 * Gets the accesses.
	 *
	 * @param operator the operator
	 * @return the access operators that are children of the input operator
	 */
	public static Set<AccessTerm> getAccesses(RelationalTerm operator) {
		Set<AccessTerm> result = new LinkedHashSet<>();
		if (operator instanceof AccessTerm) {
			result.add(((AccessTerm) operator));
			return result;
		}
		else if (operator instanceof JoinTerm) {
			for (RelationalTerm child: ((JoinTerm)operator).getChildren()) 
				result.addAll(getAccesses(child));
			return result;
		}
		else if (operator instanceof DependentJoinTerm) {
			for (RelationalTerm child: ((DependentJoinTerm)operator).getChildren()) 
				result.addAll(getAccesses(child));
			return result;
		}
		else if (operator instanceof SelectionTerm) {
			result.addAll(getAccesses(((SelectionTerm)operator).getChildren()[0]));
		}
		else if (operator instanceof ProjectionTerm) {
			result.addAll(getAccesses(((ProjectionTerm)operator).getChildren()[0]));
		}
		return result;
	}
}
