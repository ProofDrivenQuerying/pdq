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
	 * Builds a query given a set of free variables and two or more Atoms.
	 * The query is grounded using the input mapping of variables to constants.
	 * 
	 * @param freeVariables
	 * @param children
	 */
	protected ConjunctiveQuery(Variable[] freeVariables, Atom[] children) {
		Assert.assertNotNull(children);
		Assert.assertTrue(children.length > 0);
		if (children.length == 1) {
			//Check that the body is a conjunction of positive atoms
			Assert.assertTrue(Arrays.asList(children[0].getFreeVariables()).containsAll(Arrays.asList(freeVariables)));
			this.child = children[0];
			this.freeVariables = freeVariables.clone();
			this.boundVariables = ArrayUtils.removeElements(child.getFreeVariables(), freeVariables);
			this.atoms = child.getAtoms();
		} else {
			
			Conjunction conjunction = (Conjunction)Conjunction.of(children);
			Assert.assertTrue(Arrays.asList(conjunction.getFreeVariables()).containsAll(Arrays.asList(freeVariables)));
			this.child = conjunction;
			this.freeVariables = freeVariables.clone();
			this.boundVariables = ArrayUtils.removeElements(child.getFreeVariables(), freeVariables);
			this.atoms = child.getAtoms();
		}
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

    public static ConjunctiveQuery create(Variable[] freeVariables, Atom[] children) {
        return Cache.conjunctiveQuery.retrieve(new ConjunctiveQuery(freeVariables, children));
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
	public static ConjunctiveQuery createFromLogicFormula(RelationalTermAsLogic logicFormula) {
		Formula formula = logicFormula.getFormula();
		List<Variable> freeVariables = null;
		
		Formula flatFormula = null;
		if (formula instanceof QuantifiedFormula) {
			flatFormula = Conjunction.of(formula.getChild(0));
			freeVariables = Arrays.asList(formula.getFreeVariables()); 
		} else  {
			freeVariables = new ArrayList<>();
			for (Term t: logicFormula.getMapping().values()) {
				if (t.isVariable())
					freeVariables.add((Variable)t);
			}
			flatFormula = formula;
		}
		return create(freeVariables.toArray(new Variable[freeVariables.size()]),flatFormula.getAtoms());
	}

}
