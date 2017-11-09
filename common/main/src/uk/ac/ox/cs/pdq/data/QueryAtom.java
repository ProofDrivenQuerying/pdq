package uk.ac.ox.cs.pdq.data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import uk.ac.ox.cs.pdq.algebra.ConstantEqualityCondition;
import uk.ac.ox.cs.pdq.db.Attribute;
import uk.ac.ox.cs.pdq.db.Relation;
import uk.ac.ox.cs.pdq.fol.Atom;
import uk.ac.ox.cs.pdq.fol.Term;
import uk.ac.ox.cs.pdq.fol.Variable;

/**
 * This class intends to map all the relevant information about an atom of a
 * conjunctiveQuery.
 * 
 * For example if you have a relation R with attributes a and b, you can have a
 * query Q such as Q= R(Variable(x),13) where Variable(x) is a free variable
 * representing the R.a column and the 13 is a constant that has to match column
 * R.b. In this query we have only one atom. The QueryAtom object will have the
 * body of the query as queryAtom, the relation R as parentRelation, the
 * variable and the constant as terms, and the constantEquality condition.
 * 
 * @author Gabor
 *
 */
public class QueryAtom {
	private Atom queryAtom;
	private Relation relation;
	private Collection<ConstantEqualityCondition> constantEqualityConditions;
	/**
	 * If this queryAtom contains a free variable we need to know which attribute it corresponds to.
	 */
	private Map<Variable,Integer> freeVariablePositions;
	protected QueryAtom(Atom queryAtom, Relation parentRelation) {
		this.queryAtom = queryAtom;
		this.relation = parentRelation;
		constantEqualityConditions = new ArrayList<>();
		freeVariablePositions = new HashMap<>();
	}

	public Attribute getAttributeAtIndex(int index) {
		return relation.getAttribute(index);
	}

	public int getAttributeForQueryTerm(Term term) {
		return Arrays.asList(queryAtom.getTerms()).indexOf(term);
	}

	public Atom getConjunctiveQueryAtom() {
		return queryAtom;
	}

	public Relation getRelation() {
		return relation;
	}

	public Collection<Term> getQueryTerms() {
		return Arrays.asList(queryAtom.getTerms());
	}

	public boolean hasConstantEqualityCondition() {
		return !constantEqualityConditions.isEmpty();
	}

	public Collection<ConstantEqualityCondition> getConstantEqualityConditions() {
		return constantEqualityConditions;
	}

	public void addConstantEqualityCondition(ConstantEqualityCondition constantEqualityCondition) {
		this.constantEqualityConditions.add(constantEqualityCondition);
	}
	public void addFreeVariable(Variable v, Integer index) {
		this.freeVariablePositions.put(v, index);
	}
	public void addFreeVariable(Variable v, Attribute attribute) {
		this.freeVariablePositions.put(v, relation.getAttributePosition(attribute.getName()));
	}
	public Map<Variable,Integer> getFreeVariableToPosition() {
		return this.freeVariablePositions;
	}

	public static QueryAtom findAtomFor(Collection<QueryAtom> queryAtoms, String relationName) {
		if (relationName == null)
			return null;
		for (QueryAtom a: queryAtoms) {
			if (relationName.equals(a.getRelation().getName())) 
				return a;
		}
		return null;
	}
}
