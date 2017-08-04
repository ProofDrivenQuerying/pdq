package uk.ac.ox.cs.pdq.fol;

import uk.ac.ox.cs.pdq.InterningManager;

/**
 * Creates and maintains a cache of each object type in this package. The
 * purpose of this class is it make sure objects are never duplicated.
 * 
 * @author Gabor
 *
 */
public class Cache {

	protected static InterningManager<Atom> atom = null;
	protected static InterningManager<Clause> clause = null;
	protected static InterningManager<Conjunction> conjunction = null;
	protected static InterningManager<ConjunctiveQuery> conjunctiveQuery = null;
	protected static InterningManager<Dependency> dependency = null;
	protected static InterningManager<Disjunction> disjunction = null;
	protected static InterningManager<EGD> egd = null;
	protected static InterningManager<Implication> implication = null;
	protected static InterningManager<LinearGuarded> linearGuarded = null;
	protected static InterningManager<Literal> literal = null;
	protected static InterningManager<Negation> negation = null;
	protected static InterningManager<Predicate> predicate = null;
	protected static InterningManager<QuantifiedFormula> quantifiedFormula = null;
	protected static InterningManager<TGD> tgd = null;
	protected static InterningManager<UntypedConstant> untypedConstant = null;
	protected static InterningManager<Variable> variable = null;

	static {
		startCaches();
	}

	/**
	 * Needed in case we want to work with multiple schemas. Most commonly in case
	 * of unit testing.
	 */
	public static void reStartCaches() {
		atom = null;
		clause = null;
		conjunction = null;
		conjunctiveQuery = null;
		dependency = null;
		disjunction = null;
		egd = null;
		implication = null;
		linearGuarded = null;
		literal = null;
		negation = null;
		quantifiedFormula = null;
		tgd = null;
		untypedConstant = null;
		variable = null;

		startCaches();
	}

	private static void startCaches() {
		atom = new InterningManager<Atom>() {
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

		clause = new InterningManager<Clause>() {
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

		conjunction = new InterningManager<Conjunction>() {
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

		conjunctiveQuery = new InterningManager<ConjunctiveQuery>() {
			protected boolean equal(ConjunctiveQuery object1, ConjunctiveQuery object2) {
				if (!object1.child.equals(object2.child) || object1.freeVariables.length != object2.freeVariables.length)
					return false;
				for (int index = object1.freeVariables.length - 1; index >= 0; --index)
					if (!object1.freeVariables[index].equals(object2.freeVariables[index]))
						return false;

				for (java.util.Map.Entry<Variable, Constant> entry : object1.canonicalSubstitution.entrySet())
					if (!object2.canonicalSubstitution.containsKey(entry.getKey()) || !object2.canonicalSubstitution.get(entry.getKey()).equals(entry.getValue()))
						return false;
				return true;
			}

			protected int getHashCode(ConjunctiveQuery object) {
				int hashCode = object.child.hashCode();
				for (int index = object.freeVariables.length - 1; index >= 0; --index)
					hashCode = hashCode * 7 + object.freeVariables[index].hashCode();
				for (java.util.Map.Entry<Variable, Constant> entry : object.canonicalSubstitution.entrySet())
					hashCode = hashCode * 7 + entry.getKey().hashCode() + entry.getValue().hashCode();
				return hashCode;
			}
		};

		dependency = new InterningManager<Dependency>() {
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

		disjunction = new InterningManager<Disjunction>() {
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

		egd = new InterningManager<EGD>() {
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

		implication = new InterningManager<Implication>() {
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

		linearGuarded = new InterningManager<LinearGuarded>() {
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

		literal = new InterningManager<Literal>() {
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

		negation = new InterningManager<Negation>() {
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

		predicate = new InterningManager<Predicate>() {
			protected boolean equal(Predicate object1, Predicate object2) {
				return object1.name.equals(object2.name) && object1.arity == object2.arity && object1.isEquality == object2.isEquality;
			}

			protected int getHashCode(Predicate object) {
				return object.name.hashCode() + object.arity.hashCode() * 7;
			}
		};

		quantifiedFormula = new InterningManager<QuantifiedFormula>() {
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

		tgd = new InterningManager<TGD>() {
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

		untypedConstant = new InterningManager<UntypedConstant>() {
			protected boolean equal(UntypedConstant object1, UntypedConstant object2) {
				return object1.symbol.equals(object2.symbol);
			}

			protected int getHashCode(UntypedConstant object) {
				return object.symbol.hashCode() * 7;
			}
		};

		variable = new InterningManager<Variable>() {
			protected boolean equal(Variable object1, Variable object2) {
				return object1.symbol.equals(object2.symbol);
			}

			protected int getHashCode(Variable object) {
				return object.symbol.hashCode() * 7;
			}
		};

	}
}
