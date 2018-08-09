package uk.ac.ox.cs.pdq.planner.util;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections4.CollectionUtils;

import com.google.common.base.Preconditions;

import uk.ac.ox.cs.pdq.algebra.AccessTerm;
import uk.ac.ox.cs.pdq.algebra.AttributeEqualityCondition;
import uk.ac.ox.cs.pdq.algebra.Condition;
import uk.ac.ox.cs.pdq.algebra.ConjunctiveCondition;
import uk.ac.ox.cs.pdq.algebra.ConstantEqualityCondition;
import uk.ac.ox.cs.pdq.algebra.DependentJoinTerm;
import uk.ac.ox.cs.pdq.algebra.JoinTerm;
import uk.ac.ox.cs.pdq.algebra.ProjectionTerm;
import uk.ac.ox.cs.pdq.algebra.RelationalTerm;
import uk.ac.ox.cs.pdq.algebra.RenameTerm;
import uk.ac.ox.cs.pdq.algebra.SelectionTerm;
import uk.ac.ox.cs.pdq.algebra.SimpleCondition;
import uk.ac.ox.cs.pdq.db.AccessMethodDescriptor;
import uk.ac.ox.cs.pdq.db.Attribute;
import uk.ac.ox.cs.pdq.db.Relation;
import uk.ac.ox.cs.pdq.db.Schema;
import uk.ac.ox.cs.pdq.fol.Atom;
import uk.ac.ox.cs.pdq.fol.ConjunctiveQuery;
import uk.ac.ox.cs.pdq.fol.Constant;
import uk.ac.ox.cs.pdq.fol.Term;
import uk.ac.ox.cs.pdq.fol.TypedConstant;
import uk.ac.ox.cs.pdq.fol.UntypedConstant;
import uk.ac.ox.cs.pdq.fol.Variable;
import uk.ac.ox.cs.pdq.planner.ExplorationSetUp;
import uk.ac.ox.cs.pdq.util.Utility;

/**
 * 
 * @author Efthymia Tsamoura
 *
 */
public class PlanCreationUtility {
	
	/**
	 * Methods to create a linear plan by appending the access and middleware commands of the input configuration to the input parent plan.
	 * 
	 * The newly created access and middleware command are created as follows:
	 * For an exposed fact f, If f has been exposed by an input-free accessibility axiom (access method), 
	 * then create an input-free access else create a dependent access operator.
	 * If f has schema constants in output positions or repeated constants, then these schema constants map to filtering predicates.
	 * Finally, project the variables that correspond to output chase constants. 
	 * */


/**
 * TOCOMMENT: IS THIS FIRST METHOD EVER USED? IF SO, NEED SOME EXPLANATION HOW IT FITS
 * THE DISCUSSION ABOVE
 */
	public static RelationalTerm createPlan(RelationalTerm left, RelationalTerm right) {
		Preconditions.checkNotNull(left);
		Preconditions.checkNotNull(right);
		Set<Attribute> outputs = new HashSet<Attribute>(Arrays.asList(left.getOutputAttributes()));
		Set<Attribute> inputs = new HashSet<Attribute>(Arrays.asList(right.getInputAttributes()));
		if (CollectionUtils.containsAny(outputs, inputs)) 
			return DependentJoinTerm.create(left, right);
		else 
			return JoinTerm.create(left, right);
	}

	 /*Creates a single access plans
	 * @param relation: the relation being accessed the c
	 * @param accessMethod the access being used
	 * @exposedFacts: the facts being exposed by the access: used in order to name the attributes in the output
	 * @return the term representing the output  plan
	 */
	public static RelationalTerm createSingleAccessPlan(Relation relation, AccessMethodDescriptor accessMethod, Collection<Atom> exposedFacts) {
		Preconditions.checkNotNull(relation);
		Preconditions.checkNotNull(accessMethod);
		Preconditions.checkArgument(exposedFacts != null && exposedFacts.size() > 0);
		Preconditions.checkArgument(Arrays.asList(relation.getAccessMethods()).contains(accessMethod));
                //op1 will accumulate terms as more and more renamings are added
		RelationalTerm op1 = null;
                //access will store the term prior to any renamings
		AccessTerm access = null;
		//planRelation is a copy of the relation 
		Relation planRelation = null;
		//Iterate over each exposed fact
		for (Atom exposedFact: exposedFacts) {
			Preconditions.checkArgument(exposedFact.getPredicate().getName().equals(relation.getName()));
              //TOCOMENT: WHY NOT PULL THIS OUT BEFORE THE LOOP?
			if (access == null) {
				Attribute[] attributes = new Attribute[relation.getArity()];
				System.arraycopy(relation.getAttributes(), 0, attributes, 0, attributes.length); 
				planRelation = Relation.create(relation.getName(), attributes, relation.getAccessMethods());
				//Compute the input constants
				Map<Integer, TypedConstant> inputConstants = computeInputConstants(accessMethod, exposedFact.getTerms());
				//Create an access operator
				access = AccessTerm.create(planRelation, accessMethod, inputConstants);
			}

			//Rename the output attributes in the output according to the exposed facts
			Attribute[] renamings = computeRenamedAttributes(planRelation.getAttributes(), exposedFact.getTerms());
			//Add a rename operator; and put the result in op1
			if (op1 == null) {
				op1 = RenameTerm.create(renamings, access); 		
				//Find if this fact has schema constants in output positions or repeated constants
				//If yes, then compute the filtering conditions
				Condition filteringConditions = PlanCreationUtility.createFilteringConditions(exposedFact.getTerms());
				if (filteringConditions != null && ! checkEquality(filteringConditions, access.getInputConstants())) {
					op1 = SelectionTerm.create(filteringConditions, op1);
				}
			} else {
				RelationalTerm op2 = RenameTerm.create(renamings, access); 		
				Condition filteringConditions = PlanCreationUtility.createFilteringConditions(exposedFact.getTerms());
				if (filteringConditions != null && ! checkEquality(filteringConditions, access.getInputConstants())) {
					op2 = SelectionTerm.create(filteringConditions, op2);
				}
				op1 = JoinTerm.create(op1, op2);
			}
		}
		return op1;
	}
	
	private static boolean checkEquality(Condition filteringConditions, Map<Integer, TypedConstant> inputConstants) {
		if (filteringConditions instanceof ConjunctiveCondition) {
			SimpleCondition[] conditions = ((ConjunctiveCondition) filteringConditions).getSimpleConditions();
			for (SimpleCondition s: conditions) {
				if (!checkEquality(s,inputConstants))
					return false;
			}
			return true;
		}
		if (filteringConditions instanceof ConstantEqualityCondition) {
			TypedConstant constant = ((ConstantEqualityCondition) filteringConditions).getConstant();
			int position = ((ConstantEqualityCondition) filteringConditions).getPosition();
			if (!inputConstants.containsKey(position))
				return false;
			return inputConstants.get(position).equals(constant);
		}
		
		return false;
	}

/**
 *Look for constants in the input positions
 */
	private static Map<Integer, TypedConstant> computeInputConstants(AccessMethodDescriptor method, Term[] terms) {
		Map<Integer, TypedConstant> ret = new HashMap<>();
		for(Integer i: method.getInputs()) {
			if (terms[i] instanceof TypedConstant)
				ret.put(i, (TypedConstant)terms[i]);
		}
		return ret;
	}
	
/**
 * renames tha attributes in the first list based on the names of terms in the second lists
 */
	private static Attribute[] computeRenamedAttributes(Attribute[] attributes, Term[] terms) {
		Preconditions.checkArgument(attributes.length == terms.length);
		Attribute[] renamings = new Attribute[terms.length];
		for(int index = 0; index < terms.length; ++index)
			renamings[index] = Attribute.create(attributes[index].getType(), terms[index].toString());
		return renamings;
	}
	/**
	 * Creates the select predicates.
	 *
	 * @param terms List<Term>
	 * @return 	 	a conjunction of selectioin conditions that the output values of a source must satisfy
	 * 		based on the exposed fact's terms.
	 * 		The selection conditions enforce value equality when two terms are equal
	 * 		and equality to a constant when an exposed fact's term is mapped to a schema constant.
	 * 		The returned list is null if there does not exist any select condition
	 */
	public static Condition createFilteringConditions(Term[] terms) {
		Set<SimpleCondition> result = new LinkedHashSet<>();
		Integer termIndex = 0;
		for (Term term : terms) {
			if (term instanceof TypedConstant) 
				result.add(ConstantEqualityCondition.create(termIndex, (TypedConstant) term));
			else {
				List<Integer> appearances = Utility.search(terms, term);
				if (appearances.size() > 1) {
					for (int i = 0; i < appearances.size() - 1; ++i) {
						Integer indexI = appearances.get(i);
						for (int j = i + 1; j < appearances.size(); ++j) {
							Integer indexJ = appearances.get(j);
							result.add(AttributeEqualityCondition.create(indexI, indexJ));
						}
					}
				}
			}
			++termIndex;
		}
		return result.isEmpty() ? null : ConjunctiveCondition.create(result.toArray(new SimpleCondition[result.size()]));
	}
	
	/**
	 * Gets the types of a query's free variables
	 *
	 * @param query the q
	 * @return a list of types for each free variable of the query
	 */
	private static Type[] computeVariableTypes(ConjunctiveQuery query,Schema schema) {
		Variable[] freeVariables = query.getFreeVariables();
 //TOCOMMENT: Class</>?
		Type[] types = new Class<?>[query.getFreeVariables().length];
		boolean assigned = false;
		for (int i = 0, l = types.length; i < l; i++) {
			assigned = false;
			Variable t = freeVariables[i];
			Atom[] atoms = query.getAtoms();
			for (Atom atom:atoms) {
				Relation s = schema.getRelation(atom.getPredicate().getName());
				List<Integer> pos = Utility.search(atom.getTerms(), t);
				if (!pos.isEmpty()) {
					types[i] = s.getAttribute(pos.get(0)).getType();
					assigned = true;
					break;
				}
			}
			if (!assigned) 
				throw new IllegalStateException("Could not infer query type.");
		}
		return types;
	}

	/**
	 * Creates the final projection, based on the free variables of the query
	 *
	 * @param query Query
	 * @param plan partial plan wihtout the final projection
	 * @return Projection
	 */
	public static ProjectionTerm createFinalProjection(ConjunctiveQuery query, RelationalTerm plan, Schema schema) {
		List<Attribute> projections = new ArrayList<>();
		Type[] variableTypes = computeVariableTypes(query,schema);
		Variable[] freeVariables = query.getFreeVariables();
		for (int index = 0; index < freeVariables.length; ++index)  {
			Constant constant = ExplorationSetUp.getCanonicalSubstitutionOfFreeVariables().get(query).get(freeVariables[index]);
			Attribute attribute = Attribute.create(variableTypes[index], ((UntypedConstant)constant).getSymbol());
			if (! Arrays.asList(plan.getOutputAttributes()).contains(attribute)) {
//TOCOMENT: SYSTEM.OUT.PRINTLIN???
				System.out.println("Invalid plan!" + plan.getOutputAttributes() + " should contain " + attribute +" but it doesn't.");
				Preconditions.checkArgument(Arrays.asList(plan.getOutputAttributes()).contains(attribute));
			}
			projections.add(attribute);
		}
		return ProjectionTerm.create(projections.toArray(new Attribute[projections.size()]), plan);
	}

}
