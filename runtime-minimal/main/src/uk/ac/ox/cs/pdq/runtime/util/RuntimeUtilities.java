package uk.ac.ox.cs.pdq.runtime.util;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.junit.Assert;

import com.google.common.base.Preconditions;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;

import uk.ac.ox.cs.pdq.algebra.AttributeEqualityCondition;
import uk.ac.ox.cs.pdq.algebra.Condition;
import uk.ac.ox.cs.pdq.algebra.ConjunctiveCondition;
import uk.ac.ox.cs.pdq.algebra.ConstantEqualityCondition;
import uk.ac.ox.cs.pdq.algebra.SimpleCondition;
import uk.ac.ox.cs.pdq.datasources.RelationAccessWrapper;
import uk.ac.ox.cs.pdq.datasources.utility.Tuple;
import uk.ac.ox.cs.pdq.datasources.utility.TupleType;
import uk.ac.ox.cs.pdq.db.AccessMethod;
import uk.ac.ox.cs.pdq.db.Attribute;
import uk.ac.ox.cs.pdq.db.Relation;
import uk.ac.ox.cs.pdq.db.Schema;
import uk.ac.ox.cs.pdq.db.TypedConstant;
import uk.ac.ox.cs.pdq.fol.Atom;
import uk.ac.ox.cs.pdq.fol.ConjunctiveQuery;
import uk.ac.ox.cs.pdq.fol.Term;
import uk.ac.ox.cs.pdq.fol.Variable;
import uk.ac.ox.cs.pdq.runtime.exec.iterator.TupleIterator;
import uk.ac.ox.cs.pdq.util.Utility;

public class RuntimeUtilities {
	
	/** The log. */
	private static Logger log = Logger.getLogger(RuntimeUtilities.class);
	
	/**
	 * To tuple.
	 *
	 * @param type TupleType
	 * @param attributes List<Attribute>
	 * @param values Constant[]
	 * @return a tuple view of the given collection of terms.
	 */
	public static Tuple createTuple(Attribute[] attributes, Term[] values) {
		Preconditions.checkArgument(attributes.length == values.length);
		Object[] output = new Object[values.length];
		for (int i = 0, l = values.length; i < l; i++) 
			output[i] = Utility.cast(attributes[i].getType(), values[i].toString());
		TupleType tupleType = TupleType.DefaultFactory.createFromTyped(attributes);
		return tupleType.createTuple(output);
	}
	
	public static boolean typeOfAttributesEqualsTupleType(TupleType tupleType, Attribute[] attributes) {
		if(attributes.length != tupleType.size())
			return false;
		for(int attributeIndex = 0; attributeIndex < attributes.length; ++attributeIndex) {
			if(!attributes[attributeIndex].getType().equals(tupleType.getType(attributeIndex)))
				return false;
		}
		return true;
	}
	
	/**
	 * Project.
	 *
	 * @param child TupleIterator
	 * @param inputTuple Tuple
	 * @return Tuple
	 */
	public static Object[] projectValuesInInputPositions(Tuple inputTuple, Integer[] inputPositions) {
		Object[] result = new Object[inputPositions.length];
		for (int i = 0, l = inputPositions.length; i < l; i++) 
			result[i] = inputTuple.getValue(inputPositions[i]);
		return result;
	}
		
	public static ConjunctiveCondition computeJoinConditions(TupleIterator[] children) {
		Multimap<Attribute, Integer> joinVariables = LinkedHashMultimap.create();
		int totalCol = 0;
		// Cluster patterns by variables
		Set<Attribute> inChild = new LinkedHashSet<>();
		for (TupleIterator child:children) {
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
	 * Generates a list of terms matching the attributes of the input relation.
	 *
	 * @param query ConjunctiveQuery
	 * @return List<Attribute>
	 */
	public static Attribute[] getAttributesCorrespondingToFreeVariables(ConjunctiveQuery query, Schema schema) {
		Variable[] queryVariables = query.getFreeVariables();
		Attribute[] result = new Attribute[query.getFreeVariables().length];
		//for (Variable t:query.getFreeVariables()) {
		for(int index = 0; index < queryVariables.length; ++index) {
			Variable t = queryVariables[index];
			boolean found = false;
			for (Atom p:query.getAtoms()) {
				Relation r = schema.getRelation(p.getPredicate().getName());
				int i = 0;
				for (Term v : p.getTerms()) {
					if (v.equals(t)) {
						result[index] = Attribute.create(r.getAttribute(i).getType(), t.toString());
						found = true;
						break;
					}
					i++;
				}
				if (found) {
					break;
				}
			}
		}
		assert result.length == query.getFreeVariables().length : "Could not infer type of projected term in the query";
		return result;
	}


	public static boolean isSatisfied(Condition condition, Tuple tuple) {
		if(condition instanceof AttributeEqualityCondition) {
			assert tuple.size() > ((AttributeEqualityCondition)condition).getPosition() && 
			tuple.size() > ((AttributeEqualityCondition)condition).getOther():"Tuple must comply for bound given by the predicate positions";
			try {
				Object sourceValue = tuple.getValue(((AttributeEqualityCondition)condition).getPosition());
				Object targetValue = tuple.getValue(((AttributeEqualityCondition)condition).getOther());
				if (sourceValue == null) {
					return tuple.getType().getType(((AttributeEqualityCondition)condition).getPosition())
							.equals(tuple.getType().getType(((AttributeEqualityCondition)condition).getOther()))
							&& targetValue == null;
				}
				if (sourceValue instanceof Comparable<?> && targetValue instanceof Comparable<?>) {
					try {
						Method m = Comparable.class.getMethod("compareTo", Object.class);
						return ((int) m.invoke(sourceValue, targetValue)) == 0;
					} catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
						log.warn(e.getMessage());
					}
				}
				return sourceValue.equals(targetValue);
			} catch (Exception e) {
				log.error(e.getMessage(),e);
			}
			return false;
		}
		else if(condition instanceof ConstantEqualityCondition) {
			assert tuple.size() > ((ConstantEqualityCondition)condition).getPosition() : "Tuple must comply for bound given by the predicate positions";
			Object sourceValue = tuple.getValue(((ConstantEqualityCondition)condition).getPosition());
			Object targetValue = ((ConstantEqualityCondition)condition).getConstant().getValue();
			if (sourceValue == null) 
				return targetValue == null;
			if (sourceValue instanceof Comparable<?> && targetValue instanceof Comparable<?>) {
				try {
					Method m = Comparable.class.getMethod("compareTo", Object.class);
					return ((int) m.invoke(sourceValue, targetValue)) == 0;
				} catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
					log.warn("Problem comparing " + sourceValue + " to " + targetValue + ": " + e);
				}
			}
			return sourceValue.equals(targetValue);
		}
		else if(condition instanceof ConjunctiveCondition) {
			for (SimpleCondition simpleCondition: ((ConjunctiveCondition) condition).getSimpleConditions()) {
				if (!isSatisfied(simpleCondition,tuple)) 
					return false;
			}
			return true;
		}
		throw new RuntimeException("Unknown condition type");
	}
	
	/**
	 * @param accessMethod an access method of this relation
	 * @return 		the relation's input attributes for input binding
	 */
	public static Attribute[] computeInputAttributes(RelationAccessWrapper relation, AccessMethod accessMethod) {
		Assert.assertNotNull(relation);
		Assert.assertNotNull(accessMethod);
		if(accessMethod.getInputs().length == 0) {
			return new Attribute[]{};
		}
		List<Attribute> inputs = new ArrayList<>();
		for(Integer i:accessMethod.getInputs()) {
			inputs.add(relation.getAttribute(i));
		}
		return inputs.toArray(new Attribute[inputs.size()]);
	}
	
	public static Attribute[] computeInputAttributes(RelationAccessWrapper relation, AccessMethod accessMethod, Map<Integer, TypedConstant> inputConstants) {
		// Assert.assertNotNull(relation);
		Preconditions.checkNotNull(relation);
		if (!(accessMethod != null && accessMethod.getInputs().length > 0) && (inputConstants ==null || inputConstants.isEmpty())) {
			return new Attribute[0];
		}
		//Assert.assertTrue(accessMethod != null && accessMethod.getInputs().length > 0);
		Preconditions.checkNotNull(accessMethod);
		Preconditions.checkArgument(accessMethod.getInputs().length > 0);
		//Assert.assertNotNull(inputConstants);
		Preconditions.checkNotNull(inputConstants);
		for(Integer position:inputConstants.keySet()) {
			// Assert.assertTrue(position < relation.getArity());
			Preconditions.checkArgument(position < relation.getArity());
			// Assert.assertTrue(Arrays.asList(accessMethod.getInputs()).contains(position));
			Preconditions.checkArgument(Arrays.asList(accessMethod.getInputs()).contains(position));
		}
		List<Attribute> inputs = new ArrayList<>();
		for(Integer i:accessMethod.getInputs()) {
			if(!inputConstants.containsKey(i)) {
				inputs.add(relation.getAttribute(i));
			}
		}
		return inputs.toArray(new Attribute[inputs.size()]);
	}
	
	public static Attribute[] computeInputAttributes(TupleIterator child1, TupleIterator child2) {
		Assert.assertNotNull(child1);
		Assert.assertNotNull(child2);
		Attribute[] input = new Attribute[child1.getNumberOfInputAttributes() + child2.getNumberOfInputAttributes()];
		System.arraycopy(child1.getInputAttributes(), 0, input, 0, child1.getNumberOfInputAttributes());
		System.arraycopy(child2.getInputAttributes(), 0, input, child1.getNumberOfInputAttributes(), child2.getNumberOfInputAttributes());
		return input;
	}

	public static Attribute[] computeOutputAttributes(TupleIterator child1, TupleIterator child2) {
		Assert.assertNotNull(child1);
		Assert.assertNotNull(child2);
		Attribute[] input = new Attribute[child1.getNumberOfOutputAttributes() + child2.getNumberOfOutputAttributes()];
		System.arraycopy(child1.getOutputAttributes(), 0, input, 0, child1.getNumberOfOutputAttributes());
		System.arraycopy(child2.getOutputAttributes(), 0, input, child1.getNumberOfOutputAttributes(), child2.getNumberOfOutputAttributes());
		return input;
	}
	
	public static Map<Integer,Integer> computePositionsInRightChildThatAreBoundFromLeftChild(TupleIterator left, TupleIterator right) {
		Map<Integer,Integer> result = new LinkedHashMap<>();
		for (int index = 0; index < right.getNumberOfInputAttributes(); ++index) {
			Attribute attribute = right.getInputAttribute(index);
			int indexOf = Arrays.asList(left.getOutputAttributes()).indexOf(attribute);
			if(indexOf >= 0)
				result.put(index, indexOf);
		}
		return result;
	}

	public static Attribute[] computeInputAttributesForDependentJoin(TupleIterator left, TupleIterator right) {
		Attribute[] leftInputs = left.getInputAttributes();
		Attribute[] leftOutputs = left.getOutputAttributes();
		Attribute[] rightInputs = right.getInputAttributes();
		List<Attribute> result = Lists.newArrayList(leftInputs);
		for (int attributeIndex = 0; attributeIndex < right.getNumberOfInputAttributes(); attributeIndex++) {
			Attribute inputAttribute = right.getInputAttribute(attributeIndex);
			if(!Arrays.asList(leftOutputs).contains(inputAttribute))
				result.add(rightInputs[attributeIndex]);
		}
		return result.toArray(new Attribute[result.size()]);
	}
}
