package uk.ac.ox.cs.pdq.data.memory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import uk.ac.ox.cs.pdq.algebra.AttributeEqualityCondition;
import uk.ac.ox.cs.pdq.algebra.ConjunctiveCondition;
import uk.ac.ox.cs.pdq.algebra.ConstantEqualityCondition;
import uk.ac.ox.cs.pdq.algebra.SimpleCondition;
import uk.ac.ox.cs.pdq.data.PhysicalQuery;
import uk.ac.ox.cs.pdq.db.TypedConstant;
import uk.ac.ox.cs.pdq.fol.Atom;
import uk.ac.ox.cs.pdq.fol.Conjunction;
import uk.ac.ox.cs.pdq.fol.ConjunctiveQuery;
import uk.ac.ox.cs.pdq.fol.Variable;

/**
 * This is a query that was created from formulas such as ConjunctiveQuery or a
 * dependency activeness check, but converted to SQL language.
 * 
 * @author Gabor
 *
 */
public class MemoryQuery extends PhysicalQuery {
	/**
	 * Attribute equality condition means that the left and right side of a
	 * Conjunction has one or more variable that needs to have the same value. These
	 * conditions are stored under the corresponding Conjunction object in this map.
	 */
	private Map<Conjunction, ConjunctiveCondition> attributeEqualityConditions;

	/**
	 * Constant equality condition means that a relation has to have a constant term
	 * at a certain index. This map stores the name of the relation and the
	 * conditions that needs to be met.
	 */
	private Map<String, List<ConstantEqualityCondition>> constantEqualityConditions;

	public MemoryQuery(ConjunctiveQuery source) {
		super(source);
		attributeEqualityConditions = new HashMap<>();
		constantEqualityConditions = new HashMap<>();
		if (source.getBody() instanceof Atom) {
			initConstantEqualityConditions((Atom) source.getBody());
		}
		if (source.getBody() instanceof Conjunction) {
			initAttributeEqualityConditions((Conjunction) source.getBody(), source.getBoundVariables());
			for (Atom a : ((Conjunction) source.getBody()).getAtoms()) {
				initConstantEqualityConditions(a);
			}
		}
	}

	/**
	 * Populates the constantEqualityConditions HashMap. For an Atom(Predicate P,
	 * Variable(x), TypedConstant(13), TypedConstant("Apple")) it should create a
	 * key P, with a list value that contains two conditions for the two typed
	 * constants.
	 * 
	 * @param body
	 */
	private void initConstantEqualityConditions(Atom body) {
		for (int i = 0; i < body.getTerms().length; i++) {
			if (body.getTerms()[i] instanceof TypedConstant) {
				if (!constantEqualityConditions.containsKey(body.getPredicate().getName())) {
					constantEqualityConditions.put(body.getPredicate().getName(), new ArrayList<ConstantEqualityCondition>());
				}
				constantEqualityConditions.get(body.getPredicate().getName()).add(ConstantEqualityCondition.create(i, (TypedConstant) body.getTerms()[i]));
			}
		}
	}

	/**
	 * populates the attributeEqualityConditions hashMap. For each Conjunction it
	 * will check if we have a normal or dependent join, and for dependent joins it
	 * will describe what attributes have to be the same.
	 * 
	 * @param con
	 * @param variables
	 */
	private void initAttributeEqualityConditions(Conjunction con, Variable[] variables) {
		Collection<SimpleCondition> predicates = new ArrayList<>();
		if (con.getChild(0) instanceof Atom && con.getChild(1) instanceof Atom) {
			for (Variable v : variables) {
				int leftIndex = Arrays.asList(con.getChild(0).getTerms()).indexOf(v);
				int rightIndex = Arrays.asList(con.getChild(1).getTerms()).indexOf(v);
				if (leftIndex >= 0 && rightIndex >= 0) {
					predicates.add(AttributeEqualityCondition.create(leftIndex, rightIndex));
				}
			}
			if (predicates.size() > 0) {
				ConjunctiveCondition cc = ConjunctiveCondition.create(predicates.toArray(new SimpleCondition[predicates.size()]));
				attributeEqualityConditions.put(con, cc);
			}
		} else {
			for (Variable v : variables) {
				int leftIndex = Arrays.asList(con.getChild(0).getTerms()).indexOf(v);
				int rightIndex = Arrays.asList(con.getChild(1).getTerms()).indexOf(v);
				if (leftIndex >= 0 && rightIndex >= 0) {
					predicates.add(AttributeEqualityCondition.create(leftIndex, rightIndex));
				}
			}
			if (predicates.size() > 0) {
				ConjunctiveCondition cc = ConjunctiveCondition.create(predicates.toArray(new SimpleCondition[predicates.size()]));
				if (con.getChild(0) instanceof Conjunction) {
					initAttributeEqualityConditions((Conjunction) con.getChild(0), variables);
				}
				if (con.getChild(1) instanceof Conjunction) {
					initAttributeEqualityConditions((Conjunction) con.getChild(1), variables);
				}
				attributeEqualityConditions.put(con, cc);
			}
		}
	}

	/**
	 * For each conjunctions that represents a dependent join it describes what
	 * attributes have to be equal.
	 * 
	 * @return
	 */
	public Map<Conjunction, ConjunctiveCondition> getAttributeEqualityConditions() {
		return attributeEqualityConditions;
	}

	/**
	 * For each predicate name it lists all constants that needs to match this
	 * query.
	 * 
	 * @return
	 */
	public Map<String, List<ConstantEqualityCondition>> getConstantEqualityConditions() {
		return constantEqualityConditions;
	}
}
