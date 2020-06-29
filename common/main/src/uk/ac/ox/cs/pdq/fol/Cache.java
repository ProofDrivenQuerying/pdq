// This file is part of PDQ (https://github.com/ProofDrivenQuerying/pdq) which is released under the MIT license.
// See accompanying LICENSE for copyright notice and full details.

package uk.ac.ox.cs.pdq.fol;

import uk.ac.ox.cs.pdq.ClassManager;

/**
 * Creates and maintains a cache of each object type in this package. The
 * purpose of this class is it make sure objects are never duplicated.
 * 
 * @author Gabor
 *
 */
public class Cache {

	protected static ClassManager<Atom> atom = null;
	protected static ClassManager<Clause> clause = null;
	protected static ClassManager<Conjunction> conjunction = null;
	protected static ClassManager<ConjunctiveQuery> conjunctiveQuery = null;
	protected static ClassManager<ConjunctiveQueryWithInequality> conjunctiveQueryWithInequality = null;
	protected static ClassManager<Dependency> dependency = null;
	protected static ClassManager<Disjunction> disjunction = null;
	protected static ClassManager<EGD> egd = null;
	protected static ClassManager<Implication> implication = null;
	protected static ClassManager<LinearGuarded> linearGuarded = null;
	protected static ClassManager<Literal> literal = null;
	protected static ClassManager<Negation> negation = null;
	protected static ClassManager<Predicate> predicate = null;
	protected static ClassManager<QuantifiedFormula> quantifiedFormula = null;
	protected static ClassManager<TGD> tgd = null;
	protected static ClassManager<UntypedConstant> untypedConstant = null;
	protected static ClassManager<TypedConstant> typedConstant = null;
	protected static ClassManager<Variable> variable = null;

	static {
		startCaches();
	}

	/**
	 * Needed in case we want to work with multiple schemas. Most commonly in case
	 * of unit testing.
	 */
	public static void reStartCaches() {
		atom.reset();
		clause.reset();
		conjunction.reset();
		conjunctiveQuery.reset();
		conjunctiveQueryWithInequality.reset();
		dependency.reset();
		disjunction.reset();
		egd.reset();
		implication.reset();
		linearGuarded.reset();
		literal.reset();
		negation.reset();
		quantifiedFormula.reset();
		tgd.reset();
		untypedConstant.reset();
		variable.reset();
		typedConstant.reset();
	}

	private static synchronized void startCaches() {
		atom = new ClassManager<Atom>() {
			protected boolean equal(Atom object1, Atom object2) {
				if (!object1.predicate.equals(object2.predicate) || object1.terms.length != object2.terms.length)
					return false;
				for (int index = object1.terms.length - 1; index >= 0; --index)
					if (!object1.terms[index].equals(object2.terms[index]))
						return false;
				return true;
			}

			protected int getHashCode(Atom object) {
				int hashCode = object.predicate.hashCode();
				for (int index = object.terms.length - 1; index >= 0; --index)
					hashCode = hashCode * 7 + object.terms[index].hashCode();
				return hashCode;
			}
		};

		clause = new ClassManager<Clause>() {
			protected boolean equal(Clause object1, Clause object2) {
				if (object1.literals.length != object2.literals.length)
					return false;
				for (int index = object1.literals.length - 1; index >= 0; --index)
					if (!object1.literals[index].equals(object2.literals[index]))
						return false;
				return true;
			}

			protected int getHashCode(Clause object) {
				int hashCode = 0;
				for (int index = object.literals.length - 1; index >= 0; --index)
					hashCode = hashCode * 7 + object.literals[index].hashCode();
				return hashCode;
			}
		};

		conjunction = new ClassManager<Conjunction>() {
			protected boolean equal(Conjunction object1, Conjunction object2) {
				if (object1.children.length != object2.children.length)
					return false;
				for (int index = object1.children.length - 1; index >= 0; --index)
					if (!object1.children[index].equals(object2.children[index]))
						return false;
				return true;
			}

			protected int getHashCode(Conjunction object) {
				int hashCode = 0;
				for (int index = object.children.length - 1; index >= 0; --index)
					hashCode = hashCode * 7 + object.children[index].hashCode();
				return hashCode;
			}
		};
		conjunctiveQueryWithInequality =new ClassManager<ConjunctiveQueryWithInequality>() {
			protected boolean equal(ConjunctiveQueryWithInequality object1, ConjunctiveQueryWithInequality object2) {
				if (!object1.child.equals(object2.child) || object1.freeVariables.length != object2.freeVariables.length)
					return false;
				
				for (int index = object1.freeVariables.length - 1; index >= 0; --index)
					if (!object1.freeVariables[index].equals(object2.freeVariables[index]))
						return false;
				
				return true;
			}

			protected int getHashCode(ConjunctiveQueryWithInequality object) {
				int hashCode = object.child.hashCode();
				for (int index = object.freeVariables.length - 1; index >= 0; --index) {
					hashCode = hashCode * 7 + object.freeVariables[index].hashCode();
				}
				return hashCode;
			}
		};
		conjunctiveQuery = new ClassManager<ConjunctiveQuery>() {
			protected boolean equal(ConjunctiveQuery object1, ConjunctiveQuery object2) {
				if (!object1.child.equals(object2.child) || object1.freeVariables.length != object2.freeVariables.length)
					return false;
				
				for (int index = object1.freeVariables.length - 1; index >= 0; --index)
					if (!object1.freeVariables[index].equals(object2.freeVariables[index]))
						return false;
				
				return true;
			}

			protected int getHashCode(ConjunctiveQuery object) {
				int hashCode = object.child.hashCode();
				for (int index = object.freeVariables.length - 1; index >= 0; --index) {
					hashCode = hashCode * 7 + object.freeVariables[index].hashCode();
				}
				return hashCode;
			}
		};

		dependency = new ClassManager<Dependency>() {
			protected boolean equal(Dependency object1, Dependency object2) {
				if (!object1.head.equals(object2.head) || !object1.body.equals(object2.body) || object1.variables.length != object2.variables.length)
					return false;
				for (int index = object1.variables.length - 1; index >= 0; --index)
					if (!object1.variables[index].equals(object2.variables[index]))
						return false;
				return true;
			}

			protected int getHashCode(Dependency object) {
				int hashCode = object.head.hashCode() + object.body.hashCode() * 7;
				for (int index = object.variables.length - 1; index >= 0; --index)
					hashCode = hashCode * 8 + object.variables[index].hashCode();
				return hashCode;
			}
		};

		disjunction = new ClassManager<Disjunction>() {
			protected boolean equal(Disjunction object1, Disjunction object2) {
				if (object1.children.length != object2.children.length)
					return false;
				for (int index = object1.children.length - 1; index >= 0; --index)
					if (!object1.children[index].equals(object2.children[index]))
						return false;
				return true;
			}

			protected int getHashCode(Disjunction object) {
				int hashCode = 0;
				for (int index = object.children.length - 1; index >= 0; --index)
					hashCode = hashCode * 7 + object.children[index].hashCode();
				return hashCode;
			}
		};

		egd = new ClassManager<EGD>() {
			protected boolean equal(EGD object1, EGD object2) {
				if (!object1.head.equals(object2.head) || !object1.body.equals(object2.body) || object1.variables.length != object2.variables.length)
					return false;
				for (int index = object1.variables.length - 1; index >= 0; --index)
					if (!object1.variables[index].equals(object2.variables[index]))
						return false;
				return true;
			}

			protected int getHashCode(EGD object) {
				int hashCode = object.head.hashCode() + object.body.hashCode() * 7;
				for (int index = object.variables.length - 1; index >= 0; --index)
					hashCode = hashCode * 8 + object.variables[index].hashCode();
				return hashCode;
			}
		};

		implication = new ClassManager<Implication>() {
			protected boolean equal(Implication object1, Implication object2) {
				if (object1.children.length != object2.children.length)
					return false;
				for (int index = object1.children.length - 1; index >= 0; --index)
					if (!object1.children[index].equals(object2.children[index]))
						return false;
				return true;
			}

			protected int getHashCode(Implication object) {
				int hashCode = 0;
				for (int index = object.children.length - 1; index >= 0; --index)
					hashCode = hashCode * 7 + object.children[index].hashCode();
				return hashCode;
			}
		};

		linearGuarded = new ClassManager<LinearGuarded>() {
			protected boolean equal(LinearGuarded object1, LinearGuarded object2) {
				if (!object1.head.equals(object2.head) || !object1.body.equals(object2.body) || object1.variables.length != object2.variables.length)
					return false;
				for (int index = object1.variables.length - 1; index >= 0; --index)
					if (!object1.variables[index].equals(object2.variables[index]))
						return false;
				return true;
			}

			protected int getHashCode(LinearGuarded object) {
				int hashCode = object.head.hashCode() + object.body.hashCode() * 7;
				for (int index = object.variables.length - 1; index >= 0; --index)
					hashCode = hashCode * 8 + object.variables[index].hashCode();
				return hashCode;
			}
		};

		literal = new ClassManager<Literal>() {
			protected boolean equal(Literal object1, Literal object2) {
				if (!object1.operator.equals(object2.operator) || !object1.predicate.equals(object2.predicate) || object1.terms.length != object2.terms.length)
					return false;
				for (int index = object1.terms.length - 1; index >= 0; --index)
					if (!object1.terms[index].equals(object2.terms[index]))
						return false;
				return true;
			}

			protected int getHashCode(Literal object) {
				int hashCode = object.predicate.hashCode();
				for (int index = object.terms.length - 1; index >= 0; --index)
					hashCode = hashCode * 7 + object.terms[index].hashCode();
				return hashCode;
			}
		};

		negation = new ClassManager<Negation>() {
			protected boolean equal(Negation object1, Negation object2) {
				if (!object1.child.equals(object2.child) || !object1.operator.equals(object2.operator))
					return false;
				return true;
			}

			protected int getHashCode(Negation object) {
				int hashCode = object.child.hashCode() + object.operator.hashCode() * 7;
				return hashCode;
			}
		};

		predicate = new ClassManager<Predicate>() {
			protected boolean equal(Predicate object1, Predicate object2) {
				return object1.name.equals(object2.name) && object1.arity == object2.arity && object1.isEquality == object2.isEquality;
			}

			protected int getHashCode(Predicate object) {
				return object.name.hashCode() + object.arity.hashCode() * 7;
			}
		};

		quantifiedFormula = new ClassManager<QuantifiedFormula>() {
			protected boolean equal(QuantifiedFormula object1, QuantifiedFormula object2) {
				if (!object1.operator.equals(object2.operator) || !object1.child.equals(object2.child) || object1.variables.length != object2.variables.length)
					return false;
				for (int index = object1.variables.length - 1; index >= 0; --index)
					if (!object1.variables[index].equals(object2.variables[index]))
						return false;
				return true;
			}

			protected int getHashCode(QuantifiedFormula object) {
				int hashCode = object.child.hashCode() + object.operator.hashCode() * 7;
				for (int index = object.variables.length - 1; index >= 0; --index)
					hashCode = hashCode * 8 + object.variables[index].hashCode();
				return hashCode;
			}
		};

		tgd = new ClassManager<TGD>() {
			protected boolean equal(TGD object1, TGD object2) {
				if (!object1.head.equals(object2.head) || !object1.body.equals(object2.body) || object1.variables.length != object2.variables.length)
					return false;
				for (int index = object1.variables.length - 1; index >= 0; --index)
					if (!object1.variables[index].equals(object2.variables[index]))
						return false;
				return true;
			}

			protected int getHashCode(TGD object) {
				int hashCode = object.head.hashCode() + object.body.hashCode() * 7;
				for (int index = object.variables.length - 1; index >= 0; --index)
					hashCode = hashCode * 8 + object.variables[index].hashCode();
				return hashCode;
			}
		};

		untypedConstant = new ClassManager<UntypedConstant>() {
			protected boolean equal(UntypedConstant object1, UntypedConstant object2) {
				return object1.symbol.equals(object2.symbol);
			}

			protected int getHashCode(UntypedConstant object) {
				return object.symbol.hashCode() * 7;
			}
		};
		
	    typedConstant  = new ClassManager<TypedConstant>() {
	        protected boolean equal(TypedConstant object1, TypedConstant object2) {
	            return object1.value.equals(object2.value);
	        }

	        protected int getHashCode(TypedConstant object) {
	            return object.value.hashCode() * 7;
	        }
	    };

		variable = new ClassManager<Variable>() {
			protected boolean equal(Variable object1, Variable object2) {
				return object1.symbol.equals(object2.symbol);
			}

			protected int getHashCode(Variable object) {
				return object.symbol.hashCode() * 7;
			}
		};

	}
}
