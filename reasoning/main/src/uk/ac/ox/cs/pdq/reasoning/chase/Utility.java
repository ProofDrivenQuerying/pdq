package uk.ac.ox.cs.pdq.reasoning.chase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.tuple.Pair;

import com.google.common.base.Preconditions;

import uk.ac.ox.cs.pdq.db.Attribute;
import uk.ac.ox.cs.pdq.db.Relation;
import uk.ac.ox.cs.pdq.db.sql.SQLStatementBuilder;
import uk.ac.ox.cs.pdq.db.sql.WhereCondition;
import uk.ac.ox.cs.pdq.fol.Atom;
import uk.ac.ox.cs.pdq.fol.CanonicalNameGenerator;
import uk.ac.ox.cs.pdq.fol.Conjunction;
import uk.ac.ox.cs.pdq.fol.ConjunctiveQuery;
import uk.ac.ox.cs.pdq.fol.Constant;
import uk.ac.ox.cs.pdq.fol.Dependency;
import uk.ac.ox.cs.pdq.fol.Disjunction;
import uk.ac.ox.cs.pdq.fol.Formula;
import uk.ac.ox.cs.pdq.fol.Implication;
import uk.ac.ox.cs.pdq.fol.Term;
import uk.ac.ox.cs.pdq.fol.UntypedConstant;
import uk.ac.ox.cs.pdq.fol.Variable;

/**
 * 
 * @author Efthymia Tsamoura
 * @author Gabor
 */
public class Utility {

	/**
	 * Fire.
	 *
	 * @param mapping Map<Variable,Term>
	 * @param skolemize boolean
	 * @return TGD<L,R>
	 * @see uk.ac.ox.cs.pdq.ics.IC#fire(Map<Variable,Term>, boolean)
	 */
	public static Implication ground(Dependency dependency, Map<Variable, Constant> mapping, boolean skolemize) {
		Map<Variable, Constant> skolemizedMapping = mapping;
		if(skolemize) 
			skolemizedMapping = Utility.skolemizeMapping(dependency, mapping);
		Formula[] bodyAtoms = new Formula[dependency.getNumberOfBodyAtoms()];
		for(int bodyAtomIndex = 0; bodyAtomIndex < dependency.getNumberOfBodyAtoms(); ++bodyAtomIndex) 
			bodyAtoms[bodyAtomIndex] = Utility.applySubstitution(dependency.getBodyAtom(bodyAtomIndex), skolemizedMapping);

		Formula[] headAtoms = new Formula[dependency.getNumberOfHeadAtoms()];
		for(int headAtomIndex = 0; headAtomIndex < dependency.getNumberOfHeadAtoms(); ++headAtomIndex) 
			headAtoms[headAtomIndex] = Utility.applySubstitution(dependency.getHeadAtom(headAtomIndex), skolemizedMapping);
		Formula bodyConjunction = Conjunction.of(bodyAtoms);
		Formula headConjunction = Conjunction.of(headAtoms);
		return Implication.of(bodyConjunction, headConjunction);
	}

	public static Implication fire(Dependency dependency, Map<Variable, Constant> mapping) {
		Formula[] bodyAtoms = new Formula[dependency.getNumberOfBodyAtoms()];
		for(int bodyAtomIndex = 0; bodyAtomIndex < dependency.getNumberOfBodyAtoms(); ++bodyAtomIndex) 
			bodyAtoms[bodyAtomIndex] = Utility.applySubstitution(dependency.getBodyAtom(bodyAtomIndex), mapping);

		Formula[] headAtoms = new Formula[dependency.getNumberOfHeadAtoms()];
		for(int headAtomIndex = 0; headAtomIndex < dependency.getNumberOfHeadAtoms(); ++headAtomIndex) 
			headAtoms[headAtomIndex] = Utility.applySubstitution(dependency.getHeadAtom(headAtomIndex), mapping);
		Formula bodyConjunction = Conjunction.of(bodyAtoms);
		Formula headConjunction = Conjunction.of(headAtoms);
		return Implication.of(bodyConjunction, headConjunction);
	}

	/**
	 * TOCOMMENT there is no "canonicalNames" mentioned in the comment says here.
	 * Skolemize mapping.
	 *
	 * @param mapping the mapping
	 * @return 		If canonicalNames is TRUE returns a copy of the input mapping
	 * 		augmented such that Skolem constants are produced for
	 *      the existentially quantified variables
	 */
	public static Map<Variable, Constant> skolemizeMapping(Dependency dependency, Map<Variable, Constant> mapping) {
		Map<Variable, Constant> result = new LinkedHashMap<>(mapping);
		for(Variable variable:dependency.getExistential()) {
			if (!result.containsKey(variable)) {
				result.put(variable, 
						UntypedConstant.create(CanonicalNameGenerator.getTriggerWitness(dependency, mapping, variable)));
			}
		}
		
		return result;
	}

	public static Formula applySubstitution(Formula formula, Map<Variable, Constant> mapping) {
		if(formula instanceof Conjunction) {
			Formula child1 = applySubstitution(((Conjunction)formula).getChildren()[0], mapping);
			Formula child2 = applySubstitution(((Conjunction)formula).getChildren()[1], mapping);
			return Conjunction.of(child1, child2);
		}
		else if(formula instanceof Disjunction) {
			Formula child1 = applySubstitution(((Disjunction)formula).getChildren()[0], mapping);
			Formula child2 = applySubstitution(((Disjunction)formula).getChildren()[1], mapping);
			return Disjunction.of(child1, child2);
		}
		else if(formula instanceof Implication) {
			Formula child1 = applySubstitution(((Implication)formula).getChildren()[0], mapping);
			Formula child2 = applySubstitution(((Implication)formula).getChildren()[1], mapping);
			return Implication.of(child1, child2);
		}
		else if(formula instanceof ConjunctiveQuery) {
			Atom[] atoms = ((ConjunctiveQuery)formula).getAtoms();
			Formula[] bodyAtoms = new Formula[atoms.length];
			for (int atomIndex = 0; atomIndex < atoms.length; ++atomIndex) 
				bodyAtoms[atomIndex] = applySubstitution(atoms[atomIndex],mapping);
			return Conjunction.of(bodyAtoms);
		}
		else if(formula instanceof Atom) {
			Term[] nterms = new Term[((Atom)formula).getNumberOfTerms()];
			for (int termIndex = 0; termIndex < ((Atom)formula).getNumberOfTerms(); ++termIndex) {
				Term term = ((Atom)formula).getTerm(termIndex);
				if (term.isVariable() && mapping.containsKey(term)) 
					nterms[termIndex] = mapping.get(term);
				else 
					nterms[termIndex] = term;
			}
			return Atom.create(((Atom)formula).getPredicate(), nterms);
		}
		throw new java.lang.RuntimeException("Unsupported formula type");
	}

	/**
	 * This will create a where condition that makes sure the left and right side of an equality is different (the EGD is active), and we only return it once. 
	 * When c!=c' then it is also true that c'!=c, but the query result will only contain one of them.
	 * This function should be used only in case the EGD is coming from functional dependencies (such that the left and right side's predicates are the same)
	 * 
	 * @param source the source
	 * @param constraints the constraints
	 * @param relationNamesToDatabaseTables 
	 * @return 		predicates that correspond to fact constraints
	 */
	public static WhereCondition createConditionForEGDsCreatedFromFunctionalDependencies(Atom[] conjuncts, Map<String, Relation> relationNamesToDatabaseTables, SQLStatementBuilder builder) {
		// get left and right side of the equality
		String lalias = builder.aliases.get(conjuncts[0]);
		String ralias = builder.aliases.get(conjuncts[1]);
		lalias = lalias==null ? conjuncts[0].getPredicate().getName():lalias;
		ralias = ralias==null ? conjuncts[1].getPredicate().getName():ralias;
		StringBuilder eq = new StringBuilder();
		// get InstanceID attribute name that we will use to make sure we only have c!=c' results since c' was created later, hence it has larger InstanceID.
		String leftAttributeName = relationNamesToDatabaseTables.get(conjuncts[0].getPredicate().getName()).getAttribute(conjuncts[0].getPredicate().getArity()-1).getName();
		String rightAttributeName = relationNamesToDatabaseTables.get(conjuncts[1].getPredicate().getName()).getAttribute(conjuncts[1].getPredicate().getArity()-1).getName();
		eq.append(lalias).append(".").
		append(leftAttributeName).append(">");
		eq.append(ralias).append(".").
		append(rightAttributeName);
		List<String> res = new ArrayList<String>();
		res.add(eq.toString());
		return new WhereCondition(res);
	}
	/**
	 * Creates the attribute equalities.
	 *
	 * @param source the source
	 * @return 		explicit equalities (String objects of the form A.x1 = B.x2) of the implicit equalities in the input conjunction (the latter is denoted by repetition of the same term)
	 */
	public static WhereCondition createNestedAttributeEqualitiesForActiveTriggers(Atom[] extendedBodyAtoms, Atom[] extendedHeadAtoms, SQLStatementBuilder builder) {
			List<String> attributePredicates = new ArrayList<String>();
			//The right atom should be an equality
			//We add additional checks to be sure that we have to do with EGDs
			for(Atom rightAtom:extendedHeadAtoms) {
				Relation rightRelation = (Relation) rightAtom.getPredicate();
				String rightAlias = builder.aliases.get(rightAtom);
				Map<Integer,Pair<String,Attribute>> rightToLeft = new HashMap<Integer,Pair<String,Attribute>>();
				for(Term term:rightAtom.getTerms()) {
					List<Integer> rightPositions = uk.ac.ox.cs.pdq.util.Utility.search(rightAtom.getTerms(), term); //all the positions for the same term should be equated
					Preconditions.checkArgument(rightPositions.size() == 1);
					for(Atom leftAtom:extendedBodyAtoms) {
						Relation leftRelation = (Relation) leftAtom.getPredicate();
						String leftAlias = builder.aliases.get(leftAtom);
						List<Integer> leftPositions = uk.ac.ox.cs.pdq.util.Utility.search(leftAtom.getTerms(), term); 
						Preconditions.checkArgument(leftPositions.size() <= 1);
						if(leftPositions.size() == 1) {
							rightToLeft.put(rightPositions.get(0), Pair.of(leftAlias==null ? leftRelation.getName():leftAlias, leftRelation.getAttribute(leftPositions.get(0))));
						}
					}
				}
				Preconditions.checkArgument(rightToLeft.size()==2);
				Iterator<Entry<Integer, Pair<String, Attribute>>> entries;
				Entry<Integer, Pair<String, Attribute>> entry;

				entries = rightToLeft.entrySet().iterator();
				entry = entries.next();

				StringBuilder result = new StringBuilder();
				result.append("(");
				result.append(entry.getValue().getLeft()).append(".").append(entry.getValue().getRight().getName()).append('=');
				result.append(rightAlias==null ? rightRelation.getName():rightAlias).append(".").append(rightRelation.getAttribute(0).getName());

				entry = entries.next();

				result.append(" AND ");
				result.append(entry.getValue().getLeft()).append(".").append(entry.getValue().getRight().getName()).append('=');
				result.append(rightAlias==null ? rightRelation.getName():rightAlias).append(".").append(rightRelation.getAttribute(1).getName());

				entries = rightToLeft.entrySet().iterator();
				entry = entries.next();

				result.append(" OR ");
				result.append(entry.getValue().getLeft()).append(".").append(entry.getValue().getRight().getName()).append('=');
				result.append(rightAlias==null ? rightRelation.getName():rightAlias).append(".").append(rightRelation.getAttribute(1).getName());

				entry = entries.next();

				result.append(" AND ");
				result.append(entry.getValue().getLeft()).append(".").append(entry.getValue().getRight().getName()).append('=');
				result.append(rightAlias==null ? rightRelation.getName():rightAlias).append(".").append(rightRelation.getAttribute(0).getName());

				result.append(")");

				attributePredicates.add(result.toString());

			}
			return new WhereCondition(attributePredicates);
	}


}
