package uk.ac.ox.cs.pdq.fol;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

import org.junit.Assert;

/**
 * 
 * @author Efthymia Tsamoura
 *
 */
public final class Implication extends Formula {
	private static final long serialVersionUID = 8687642284214640115L;

	protected final Formula[] children;

	/**  TOCOMMENT: THIS MAKES NO SENSE The unary operator. */
	protected final LogicalSymbols operator = LogicalSymbols.IMPLIES;
	
	/**  Cached string representation of the atom. */
	private String toString = null;

	/**  Cached list of atoms. */
	private Atom[] atoms;

	/**  Cached list of terms. */
	private Term[] terms;

	/**  Cached list of free variables. */
	private Variable[] freeVariables;

	/**  Cached list of bound variables. */
	private Variable[] boundVariables;
	
	private Implication(Formula... children) {
		Assert.assertNotNull(children);
		Assert.assertTrue(children.length == 2);
		this.children = children.clone();
	}

	public static Implication of(Formula... children) {
		return Implication.create(children);
	}
	
	/**
	 *
	 * @return String
	 */
	@Override
	public String toString() {
		if(this.toString == null) {
			this.toString = "";
			this.toString += "(" + this.children[0].toString() + " --> " + this.children[1].toString() + ")";
		}
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
	
	public static Implication create(Formula... children) {
		return Cache.implication.retrieve(new Implication(children));
	}
	
	@Override
	public Formula getChild(int childIndex) {
		return this.children[childIndex];
	}

	@Override
	public int getNumberOfChildlen() {
		return this.children.length;
	}
}
