package uk.ac.ox.cs.pdq.fol;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

import org.junit.Assert;

import uk.ac.ox.cs.pdq.InterningManager;

/**
 * 
 * @author Efthymia Tsamoura
 *
 */
public final class Disjunction extends Formula {

	private static final long serialVersionUID = 3184751820033978895L;

	protected final Formula[] children;

	/**  The unary operator. */
	protected final LogicalSymbols operator = LogicalSymbols.OR;

	/**  Cashed string representation of the atom. */
	private String toString = null;

	/**  Cashed list of atoms. */
	private Atom[] atoms;

	/**  Cashed list of terms. */
	private Term[] terms;

	/**  Cashed list of free variables. */
	private Variable[] freeVariables;

	/**  Cashed list of bound variables. */
	private Variable[] boundVariables;

	private Disjunction(Formula... children) {
		Assert.assertNotNull(children);
		Assert.assertTrue(children.length == 2);
		this.children = children.clone();
	}

	public static Formula of(Formula... children) {
		if(children.length == 2) 
			return Disjunction.create(children[0], children[1]);
		else if(children.length > 2) {
			Formula[] destination = new Formula[children.length - 1];
			System.arraycopy(children, 1, destination, 0, children.length - 1);
			Formula right = Conjunction.of(destination);
			return Disjunction.create(children[0], right);
		}
		else if(children.length == 1) 
			return children[0];
		else 
			throw new java.lang.RuntimeException("Illegal number of arguments");
	}

	/**
	 * To string.
	 *
	 * @return String
	 */
	@Override
	public String toString() {
		if(this.toString == null)
			this.toString = "(" + this.children[0].toString() + " | " + this.children[1].toString() + ")";
		return this.toString;
	}

	@Override
	public int getId() {
		return this.hashCode();
	}

	@SuppressWarnings("unchecked")
	@Override
	public Formula[] getChildren() {
		return this.children.clone();
	}

	@Override
	public Atom[] getAtoms() {
		if(this.atoms == null) {
			Set<Atom> atoms = new LinkedHashSet<>();
			atoms.addAll(Arrays.asList(this.children[0].getAtoms()));
			atoms.addAll(Arrays.asList(this.children[1].getAtoms()));
			this.atoms = atoms.toArray(new Atom[atoms.size()]);
		}
		return this.atoms.clone();
	}

	@Override
	public Term[] getTerms() {
		if(this.terms == null) {
			Set<Term> terms = new LinkedHashSet<>();
			terms.addAll(Arrays.asList(this.children[0].getTerms()));
			terms.addAll(Arrays.asList(this.children[1].getTerms()));
			this.terms = terms.toArray(new Term[terms.size()]);
		}
		return this.terms.clone();
	}

	@Override
	public Variable[] getFreeVariables() {
		if(this.freeVariables == null) {
			Set<Variable> variables = new LinkedHashSet<>();
			variables.addAll(Arrays.asList(this.children[0].getFreeVariables()));
			variables.addAll(Arrays.asList(this.children[1].getFreeVariables()));
			this.freeVariables = variables.toArray(new Variable[variables.size()]);
		}
		return this.freeVariables.clone();
	}

	@Override
	public Variable[] getBoundVariables() {
		if(this.boundVariables == null) {
			Set<Variable> variables = new LinkedHashSet<>();
			variables.addAll(Arrays.asList(this.children[0].getBoundVariables()));
			variables.addAll(Arrays.asList(this.children[1].getBoundVariables()));
			this.boundVariables = variables.toArray(new Variable[variables.size()]);
		}
		return this.boundVariables.clone();
	}

	protected Object readResolve() {
		return s_interningManager.intern(this);
	}

	protected static final InterningManager<Disjunction> s_interningManager = new InterningManager<Disjunction>() {
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

	public static Disjunction create(Formula... children) {
		return s_interningManager.intern(new Disjunction(children));
	}
}
