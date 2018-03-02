package uk.ac.ox.cs.pdq.fol;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.apache.commons.lang3.ArrayUtils;
import org.junit.Assert;

import uk.ac.ox.cs.pdq.algebra.RelationalTermAsLogic;
import uk.ac.ox.cs.pdq.io.jaxb.adapters.QueryAdapter;

/**
 * A conjunctive query (CQ) is a first order formula of the form \exists x_1, \ldots, x_n \Wedge A_i,
 * where A_i are atoms with arguments that are either variables or constants.

 * @author Efthymia Tsamoura
 *
 */
@XmlJavaTypeAdapter(QueryAdapter.class)
public class ConjunctiveQuery extends Formula {
	
	private static final long serialVersionUID = 2619152028298729812L;

	protected final Formula child;
	
	/**  Cached string representation of the atom. */
	private String toString = null;

	/**  Cached list of free variables. */
	protected final Variable[] freeVariables;

	/**  Cached list of bound variables. */
	private final Variable[] boundVariables;
	
	private final Atom[] atoms;

	/**
	 * Builds a query given a set of free variables and its conjunction.
	 * The query is grounded using the input mapping of variables to constants.
	 */
	protected ConjunctiveQuery(Variable[] freeVariables, Conjunction child) {
		//Check that the body is a conjunction of positive atoms
		Assert.assertTrue(isConjunctionOfAtoms(child));
		if (!Arrays.asList(child.getFreeVariables()).containsAll(Arrays.asList(freeVariables))) {
			System.out.println();
		}
		Assert.assertTrue(Arrays.asList(child.getFreeVariables()).containsAll(Arrays.asList(freeVariables)));
		
		this.child = child;
		this.freeVariables = freeVariables.clone();
		this.boundVariables = ArrayUtils.removeElements(child.getFreeVariables(), freeVariables);
		this.atoms = child.getAtoms();
	}
	
	/**
	 * Builds a query given a set of free variables and an atom.
	 * The query is grounded using the input mapping of variables to constants.
	 */
	protected ConjunctiveQuery(Variable[] freeVariables, Atom child) {
		//Check that the body is a conjunction of positive atoms
		Assert.assertTrue(isConjunctionOfAtoms(child));
		Assert.assertTrue(Arrays.asList(child.getFreeVariables()).containsAll(Arrays.asList(freeVariables)));
		this.child = child;
		this.freeVariables = freeVariables.clone();
		this.boundVariables = ArrayUtils.removeElements(child.getFreeVariables(), freeVariables);
		this.atoms = child.getAtoms();
	}
	
	private static boolean isConjunctionOfAtoms(Formula formula) {
		if(formula instanceof Conjunction) {
			return isConjunctionOfAtoms(formula.getChildren()[0]) && isConjunctionOfAtoms(formula.getChildren()[1]);
		}
		if(formula instanceof Atom) {
			return true;
		}
		return false;
	}
	
	/**
	 * Checks if the query is a boolean query or not. 
	 *
	 */
	public boolean isBoolean() {
		return this.getFreeVariables().length == 0;
	}
	
	@Override
	public java.lang.String toString() {
		if(this.toString == null) {
			this.toString = "";
			String op = this.boundVariables.length == 0 ? "" : "exists";
			this.toString += "(" + op;
			this.toString += "[";
            for (int index = 0; index < boundVariables.length; index++) {
                if (index > 0)
                	this.toString += ",";
                this.toString += boundVariables[index].toString();
            }
            this.toString += "]";
            this.toString += this.child.toString() + ")";
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
		return new Formula[]{this.child};
	}

	@Override
	public Atom[] getAtoms() {
		return this.child.getAtoms();
	}

	@Override
	public Term[] getTerms() {
		return this.child.getTerms();
	}

	@Override
	public Variable[] getFreeVariables() {
		return this.freeVariables.clone();
	}

	@Override
	public Variable[] getBoundVariables() {
		return this.boundVariables.clone();
	}

    public static ConjunctiveQuery create(Variable[] freeVariables, Conjunction child) {
        return Cache.conjunctiveQuery.retrieve(new ConjunctiveQuery(freeVariables, child));
    }
    
    public static ConjunctiveQuery create(Variable[] freeVariables, Atom child) {
        return Cache.conjunctiveQuery.retrieve(new ConjunctiveQuery(freeVariables, child));
    }
    
	@Override
	public Formula getChild(int childIndex) {
		Assert.assertTrue(childIndex == 0);
		return this.child;
	}

	/** Same as getChild(0), sice it can have only one child.
	 * @return
	 */
	public Formula getBody() {
		return this.child;
	}

	@Override
	public int getNumberOfChildren() {
		return 1;
	}

	public Atom getAtom(int atomIndex) {
		return this.atoms[atomIndex];
	}
	
	public int getNumberOfAtoms() {
		return this.atoms.length;
	}
	/** Creates a ConjunctiveQuery from a logicFormula
	 * @param logicFormula
	 * @return
	 */
	public static ConjunctiveQuery create(RelationalTermAsLogic logicFormula) {
		Formula phi = logicFormula.getFormula();
		List<Variable> freeVariables = null;
		
		Formula flatFormula = null;
		if (phi instanceof QuantifiedFormula) {
			flatFormula = Conjunction.of(phi.getChild(0));
			freeVariables = Arrays.asList(phi.getFreeVariables()); 
		} else  {
			freeVariables = new ArrayList<>();
			for (Term t: logicFormula.getMapping().values()) {
				if (t.isVariable())
					freeVariables.add((Variable)t);
			}
			flatFormula = phi;
		}
		if (flatFormula instanceof Atom) {
			return create(freeVariables.toArray(new Variable[freeVariables.size()]),(Atom)flatFormula);
		} else if (flatFormula instanceof Conjunction) {
			return create(freeVariables.toArray(new Variable[freeVariables.size()]),(Conjunction)flatFormula);
		}
		return null;
	}

}
