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
import org.junit.Assert;

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
import uk.ac.ox.cs.pdq.db.AccessMethod;
import uk.ac.ox.cs.pdq.db.Attribute;
import uk.ac.ox.cs.pdq.db.Relation;
import uk.ac.ox.cs.pdq.db.TypedConstant;
import uk.ac.ox.cs.pdq.fol.Atom;
import uk.ac.ox.cs.pdq.fol.ConjunctiveQuery;
import uk.ac.ox.cs.pdq.fol.Constant;
import uk.ac.ox.cs.pdq.fol.Predicate;
import uk.ac.ox.cs.pdq.fol.Term;
import uk.ac.ox.cs.pdq.fol.UntypedConstant;
import uk.ac.ox.cs.pdq.fol.Variable;
import uk.ac.ox.cs.pdq.util.Utility;

// TODO: Auto-generated Javadoc
/**
 * 
 * @author Efthymia Tsamoura
 *
 */
public class PlanCreationUtility {
	
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

	/**
	 * Creates a linear plan by appending the access and middlewares commands of the input configuration to the input parent plan.
	 * The top level operator is a projection that projects the terms toProject.
	 * 
	 * The newly created access and middleware command are created as follows:
	 * For an exposed fact f, If f has been exposed by an input-free accessibility axiom (access method), 
	 * then create an input-free access else create a dependent access operator.
	 * If f has schema constants in output positions or repeated constants, then these schema constants map to filtering predicates.
	 * Finally, project the variables that correspond to output chase constants. 
	 *
	 * @param configuration the c
	 * @param parent 		The input parent plan. This is the plan of the parent configuration of c, i.e., the configuration that is augmented with the exposed facts of c.
	 * @param toProject 		Terms to project in the resulting plan
	 * @return the left deep plan
	 */
	@SuppressWarnings("serial")
	public static RelationalTerm createSingleAccessPlan(Relation relation, AccessMethod accessMethod, Collection<Atom> exposedFacts) {
		Assert.assertNotNull(relation);
		Assert.assertNotNull(accessMethod);
		Assert.assertTrue(exposedFacts != null && exposedFacts.size() > 0);
		Assert.assertTrue(Arrays.asList(relation.getAccessMethods()).contains(accessMethod));
		RelationalTerm op1 = null;
		AccessTerm access = null;
		//planRelation is a copy of the relation without the extra attribute in the schema, needed for chasing
		Relation planRelation = null;
		//Iterate over each exposed fact
		for (Atom exposedFact: exposedFacts) {
			Assert.assertTrue(exposedFact.getPredicate().equals(relation));
			if (access == null) {
				Attribute[] attributes = new Attribute[relation.getArity()-1];
				System.arraycopy(relation.getAttributes(), 0, attributes, 0, attributes.length); 
				planRelation = new Relation(relation.getName(), attributes, relation.getAccessMethods()){};
				//Compute the input constants
				Map<Integer, TypedConstant> inputConstants = computeInputConstants(accessMethod, exposedFact.getTerms());
				//Create an access operator
				access = AccessTerm.create(planRelation, accessMethod, inputConstants);
			}
			//Rename the output attributes
			Attribute[] renamings = computeRenamedAttributes(planRelation.getAttributes(), exposedFact.getTerms());
			//Add a rename operator 
			RelationalTerm op2 = RenameTerm.create(renamings, access); 		
			//Find if this fact has schema constants in output positions or repeated constants
			//If yes, then compute the filtering conditions
			Condition filteringConditions = PlanCreationUtility.createFilteringConditions(exposedFact.getTerms());
			if (filteringConditions != null) 
				op2 = SelectionTerm.create(filteringConditions, access);
			if (op1 == null) 
				op1 = op2;
			else 
				op1 = JoinTerm.create(op1, op2);
		}
		return op1;
	}
	
	//TODO implement this method
	private static Map<Integer, TypedConstant> computeInputConstants(AccessMethod method, Term[] terms) {
		Map<Integer, TypedConstant> ret = new HashMap<>();
		for(Integer i: method.getInputs()) {
			if (terms[i] instanceof TypedConstant)
				ret.put(i, (TypedConstant)terms[i]);
		}
		return ret;
	}
	
	private static Attribute[] computeRenamedAttributes(Attribute[] attributes, Term[] terms) {
		Assert.assertTrue(attributes.length == terms.length);
		Attribute[] renamings = new Attribute[terms.length];
		for(int index = 0; index < terms.length; ++index)
			renamings[index] = Attribute.create(attributes[index].getType(), terms[index].toString());
		return renamings;
	}
	/**
	 * Creates the select predicates.
	 *
	 * @param terms List<Term>
	 * @return 	 	a conjunction of select conditions that the output values of a source must satisfy
	 * 		based on the exposed fact's terms.
	 * 		The select conditions enforce value equality when two terms are equal
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
	 * Gets the tuple type.
	 *
	 * @param query the q
	 * @return the tuple type of the input query
	 */
	private static Type[] computeVariableTypes(ConjunctiveQuery query) {
		Variable[] freeVariables = query.getFreeVariables();
		Type[] types = new Class<?>[query.getFreeVariables().length];
		boolean assigned = false;
		for (int i = 0, l = types.length; i < l; i++) {
			assigned = false;
			Variable t = freeVariables[i];
			Atom[] atoms = query.getAtoms();
			for (Atom atom:atoms) {
				Predicate s = atom.getPredicate();
				if (s instanceof Relation) {
					List<Integer> pos = Utility.search(atom.getTerms(), t);
					if (!pos.isEmpty()) {
						types[i] = ((Relation) s).getAttribute(pos.get(0)).getType();
						assigned = true;
						break;
					}
				}
			}
			if (!assigned) 
				throw new IllegalStateException("Could not infer query type.");
		}
		return types;
	}

	/**
	 * Creates the final projection.
	 *
	 * @param query Query
	 * @param plan LogicalOperator
	 * @return Projection
	 */
	public static ProjectionTerm createFinalProjection(ConjunctiveQuery query, RelationalTerm plan) {
		List<Attribute> projections = new ArrayList<>();
		Type[] variableTypes = computeVariableTypes(query);
		Variable[] freeVariables = query.getFreeVariables();
		for (int index = 0; index < freeVariables.length; ++index)  {
			Constant constant = query.getSubstitutionOfFreeVariablesToCanonicalConstants().get(freeVariables[index]);
			Attribute attribute = Attribute.create(variableTypes[index], ((UntypedConstant)constant).getSymbol());
			Assert.assertTrue(Arrays.asList(plan.getOutputAttributes()).contains(attribute));
			projections.add(attribute);
		}
		return ProjectionTerm.create(projections.toArray(new Attribute[projections.size()]), plan);
	}

}
