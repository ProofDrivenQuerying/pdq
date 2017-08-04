package uk.ac.ox.cs.pdq.runtime.util;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.junit.Assert;

import com.google.common.base.Preconditions;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;

import uk.ac.ox.cs.pdq.algebra.AttributeEqualityCondition;
import uk.ac.ox.cs.pdq.algebra.Condition;
import uk.ac.ox.cs.pdq.algebra.ConjunctiveCondition;
import uk.ac.ox.cs.pdq.algebra.ConstantEqualityCondition;
import uk.ac.ox.cs.pdq.algebra.RelationalTerm;
import uk.ac.ox.cs.pdq.algebra.SimpleCondition;
import uk.ac.ox.cs.pdq.datasources.RelationAccessWrapper;
import uk.ac.ox.cs.pdq.datasources.utility.Tuple;
import uk.ac.ox.cs.pdq.datasources.utility.TupleType;
import uk.ac.ox.cs.pdq.db.AccessMethod;
import uk.ac.ox.cs.pdq.db.Attribute;
import uk.ac.ox.cs.pdq.db.Relation;
import uk.ac.ox.cs.pdq.db.TypedConstant;
import uk.ac.ox.cs.pdq.fol.Atom;
import uk.ac.ox.cs.pdq.fol.ConjunctiveQuery;
import uk.ac.ox.cs.pdq.fol.Predicate;
import uk.ac.ox.cs.pdq.fol.Term;
import uk.ac.ox.cs.pdq.fol.Variable;
import uk.ac.ox.cs.pdq.runtime.exec.iterator.TupleIterator;
import uk.ac.ox.cs.pdq.util.Typed;
import uk.ac.ox.cs.pdq.util.Utility;

public class RuntimeUtilities {

	/** The log. */
	private static Logger log = Logger.getLogger(RuntimeUtilities.class);
	
	
	public static Tuple createTuple(Object[] values, Attribute[] attributes) {
		return null;
	}
	
	public static Tuple createTuple(Tuple values1, Attribute[] attributes1, Tuple values2, Attribute[] attributes2) {
		return null;
	}
	
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
		Typed[] constants = new TypedConstant[values.length];
		for (int i = 0, l = values.length; i < l; i++) {
			constants[i] = Utility.cast(attributes[i].getType(), values[i].toString());
		}
		return TupleType.DefaultFactory.createFromTyped(constants).createTuple(constants);
	}
	
	public static boolean typeOfAttributesEqualsTupleType(TupleType tupleType, Attribute[] attributes) {
		return true;
	}
	
	/**
	 * Project.
	 *
	 * @param child TupleIterator
	 * @param inputTuple Tuple
	 * @return Tuple
	 */
	public static Tuple projectInputValuesForChild(TupleIterator child, Tuple inputTuple, Integer[] inputPositions) {
		Object[] result = new Object[child.getNumberOfInputAttributes()];
		for (int i = 0, l = inputPositions.length; i < l; i++) 
			result[i] = inputTuple.getValue(inputPositions[i]);
		return RuntimeUtilities.createTuple(result, child.getInputAttributes());
	}
	
	/**
	 * Project.
	 *
	 * @param parentInput the current input
	 * @param leftInput the left input
	 * @return an input tuple obtained by mixing inputs coming from the parent
	 * (currentInput) and the LHS (leftInput).
	 */
	public static Tuple projectInputValuesForChild(TupleIterator leftChild, TupleIterator rightChild, Tuple parentInput, Tuple leftInput, Integer sidewaysInputs[]) {
		Object[] result = new Object[rightChild.getNumberOfInputAttributes()];
		for (int i = 0, j = 0, l = sidewaysInputs.length; i < l; i++) {
			if (sidewaysInputs[i] >= 0) {
				result[i] = leftInput.getValue(sidewaysInputs[i]);
			} else {
				result[i] = parentInput.getValue(leftChild.getNumberOfInputAttributes() + j++);
			}
		}
		return createTuple(result, rightChild.getInputAttributes());
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
	
//	/**
//	 * Infer input mappings.
//	 *
//	 * @param inputColumns List<? extends Typed>
//	 * @param operators Collection<TupleIterator>
//	 * @return Map<TupleIterator,List<Integer>>
//	 */
//	protected static Map<TupleIterator, List<Integer>> inferInputMappings(
//			List<? extends Typed> inputColumns, Collection<TupleIterator> operators) {
//		Map<TupleIterator, List<Integer>> result = Maps.newLinkedHashMap();
//		List<Typed> unassigned = Lists.newArrayList(inputColumns);
//		for (TupleIterator op: operators) {
//			List<Typed> inputs = op.getInputColumns();
//			List<Integer> positions = new ArrayList<>(inputs.size());
//			for (Typed t: inputs) {
//				int position = inputColumns.indexOf(t);
//				if (position >= 0) {
//					positions.add(position);
//					unassigned.remove(t);
//				}
//			}
//			result.put(op, positions);
//		}
//		if (!unassigned.isEmpty()) {
//			throw new IllegalStateException("Inconsistent input mapping");
//		}
//		return result;
//	}

//	/**
//	 * Converts a list of Term to a list of Typed.
//	 *
//	 * @param terms List<? extends Term>
//	 * @param type TupleType
//	 * @return List<Typed>
//	 */
//	public static List<Typed> termsToTyped(Term[] terms, TupleType type) {
//		Preconditions.checkArgument(terms.length == type.size());
//		List<Typed> result = new ArrayList<>();
//		int i = 0;
//		for (Term t: terms) {
//			result.add(termToTyped(t, type.getType(i)));
//			i++;
//		}
//		return result;
//	}
//	
//	public static TupleType attributesToTyped(Attribute[] terms) {
//		Type[] result = new Type[terms.length];
//		int i = 0;
//		for (Attribute t: terms)
//			result[i++] = t.getType();
//		return TupleType.DefaultFactory.create(result);
//	}
//
//	/**
//	 * Converts a Term to a Typed.
//	 *
//	 * @param t Term
//	 * @param type Class<?>
//	 * @return Typed
//	 */
//	public static Typed termToTyped(Term t, Type type) {
//		if (t.isVariable() || t.isUntypedConstant()) 
//			return Attribute.create(type, String.valueOf(t));
//		else if (t instanceof TypedConstant) 
//			return (TypedConstant) t;
//		else 
//			throw new IllegalStateException("Unknown typed object: " + t);
//	}
//

//
//	/**
//	 * Converts a list of Term to a list of Typed.
//	 *
//	 * @param variables List<? extends Term>
//	 * @param type TupleType
//	 * @return List<Typed>
//	 */
//	public static List<Typed> variablesToTyped(Variable[] variables, TupleType type) {
//		Preconditions.checkArgument(variables.length == type.size());
//		List<Typed> result = new ArrayList<>();
//		int i = 0;
//		for (Term t: variables) {
//			result.add(termToTyped(t, type.getType(i)));
//			i++;
//		}
//		return result;
//	}
//	
//	/**
//	 * Generates a list of attribute whose name are the name as those of term in
//	 * the given predicate, and types match with the predicate attribute types.
//	 * @param variables List<? extends Term>
//	 * @param type TupleType
//	 * @return List<Attribute>
//	 */
//	public static List<Attribute> variablesToAttributes(Variable[] variables, TupleType type) {
//		Preconditions.checkArgument(variables.length == type.size());
//		List<Attribute> result = new ArrayList<>();
//		int i = 0;
//		for (Term t:variables) {
//			result.add(Attribute.create(type.getType(i++), t.toString()));
//		}
//		return result;
//	}
//
//	/**
//	 * Generates a list of terms matching the attributes of the input relation.
//	 *
//	 * @param query ConjunctiveQuery
//	 * @return List<Attribute>
//	 */
//	public static List<Attribute> termsToAttributes(ConjunctiveQuery query) {
//		List<Attribute> result = new ArrayList<>();
//		for (Variable t:query.getFreeVariables()) {
//			boolean found = false;
//			for (Atom p:query.getAtoms()) {
//				Predicate s = p.getPredicate();
//				if (s instanceof Relation) {
//					Relation r = (Relation) s;
//					int i = 0;
//					for (Term v : p.getTerms()) {
//						if (v.equals(t)) {
//							result.add(Attribute.create(r.getAttribute(i).getType(), t.toString()));
//							found = true;
//							break;
//						}
//						i++;
//					}
//				}
//				if (found) {
//					break;
//				}
//			}
//		}
//		assert result.size() == query.getFreeVariables().length : "Could not infer type of projected term in the query";
//		return result;
//	}
//
//	/**
//	 * Gets the tuple type.
//	 *
//	 * @param query the q
//	 * @return the tuple type of the input query
//	 */
//	public static TupleType getTupleType(ConjunctiveQuery query) {
//		Type[] result = new Class<?>[query.getFreeVariables().length];
//		boolean assigned = false;
//		for (int i = 0, l = result.length; i < l; i++) {
//			assigned = false;
//			Variable t = query.getFreeVariables()[i];
//			for (Atom f: query.getAtoms()) {
//				Predicate s = f.getPredicate();
//				if (s instanceof Relation) {
//					List<Integer> pos = Utility.getTermPositions(f, t);
//					if (!pos.isEmpty()) {
//						result[i] = ((Relation) s).getAttribute(pos.get(0)).getType();
//						assigned = true;
//						break;
//					}
//				}
//			}
//			if (!assigned) {
//				throw new IllegalStateException("Could not infer query type.");
//			}
//		}
//		return TupleType.DefaultFactory.create(result);
//	}
	
	/**
	 * Generates a list of terms matching the attributes of the input relation.
	 *
	 * @param query ConjunctiveQuery
	 * @return List<Attribute>
	 */
	public static Attribute[] getAttributesCorrespondingToFreeVariables(ConjunctiveQuery query) {
		Variable[] queryVariables = query.getFreeVariables();
		Attribute[] result = new Attribute[query.getFreeVariables().length];
		//for (Variable t:query.getFreeVariables()) {
		for(int index = 0; index < queryVariables.length; ++index) {
			Variable t = queryVariables[index];
			boolean found = false;
			for (Atom p:query.getAtoms()) {
				Predicate s = p.getPredicate();
				if (s instanceof Relation) {
					Relation r = (Relation) s;
					int i = 0;
					for (Term v : p.getTerms()) {
						if (v.equals(t)) {
							result[index] = Attribute.create(r.getAttribute(i).getType(), t.toString());
							found = true;
							break;
						}
						i++;
					}
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
			Object targetValue = ((ConstantEqualityCondition)condition).getConstant();
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
	public static Attribute[] getInputAttributes(RelationAccessWrapper relation, AccessMethod accessMethod) {
		Preconditions.checkArgument(relation.getAccessMethod(accessMethod.getName()) != null);
		Attribute[] attributes = relation.getAttributes();
		Attribute[] result = new Attribute[accessMethod.getNumberOfInputs()];
		for (int index = 0; index < accessMethod.getNumberOfInputs(); ++index) 
			result[index] = attributes[accessMethod.getInputPosition(index) - 1];
		return result;
	}
	
	public static Attribute[] computeInputAttributes(RelationAccessWrapper relation, AccessMethod accessMethod, Map<Integer, TypedConstant> inputConstants) {
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
		System.arraycopy(child1.getNumberOfOutputAttributes(), 0, input, 0, child1.getNumberOfOutputAttributes());
		System.arraycopy(child2.getNumberOfOutputAttributes(), 0, input, child1.getNumberOfOutputAttributes(), child2.getNumberOfOutputAttributes());
		return input;
	}
	
	public static Integer[] computePositionsOfInputAttributes(TupleIterator left, TupleIterator right) {
		List<Integer> result = new ArrayList<>();
		for (Attribute attribute: right.getInputAttributes()) {
			int indexOf = Arrays.asList(left.getOutputAttributes()).indexOf(attribute);
			if(indexOf >= 0)
				result.add(indexOf);
		}
		return result.toArray(new Integer[result.size()]);
	}
	
	public static Attribute[] computeInputAttributes(TupleIterator left, TupleIterator right, Integer[] sidewaysInput) {
		Attribute[] leftInputs = left.getInputAttributes();
		Attribute[] rightInputs = right.getInputAttributes();
		List<Attribute> result = Lists.newArrayList(leftInputs);
		for (int i = 0; i < sidewaysInput.length; i++) 
			result.add(rightInputs[i]);
		return result.toArray(new Attribute[result.size()]);
	}
	
//	/**
//	 * To tuple.
//	 *
//	 * @param type TupleType
//	 * @param attributes List<Attribute>
//	 * @param values Constant[]
//	 * @return a tuple view of the given collection of terms.
//	 */
//	public static Tuple toTuple(TupleType type, Attribute[] attributes, Term[] values) {
//		Preconditions.checkArgument(attributes.length == values.length);
//		Object[] constants = new Object[values.length];
//		for (int i = 0, l = values.length; i < l; i++) {
//			constants[i] = Utility.cast(attributes[i].getType(), values[i].toString());
//		}
//		return type.createTuple(constants);
//	}
}
