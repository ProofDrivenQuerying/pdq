package uk.ac.ox.cs.pdq.fol;

import static uk.ac.ox.cs.pdq.fol.LogicalSymbols.EXISTENTIAL;
import static uk.ac.ox.cs.pdq.fol.LogicalSymbols.UNIVERSAL;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

import org.junit.Assert;

import uk.ac.ox.cs.pdq.InterningManager;
import uk.ac.ox.cs.pdq.util.Utility;

/**
 * 
 * @author Efthymia Tsamoura
 *
 */
public class QuantifiedFormula extends Formula {

	private static final long serialVersionUID = 4140335835004772885L;

	protected final Formula child;

	/**  The unary operator. */
	protected final LogicalSymbols operator;

	/**  The quantified variables. */
	protected final Variable[] variables;

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

	/**
	 * Instantiates a new quantified formula.
	 *
	 * @param operator 		Input quantifier operator
	 * @param variables 		Input quantified variables
	 * @param child 		Input child
	 */
	public QuantifiedFormula(LogicalSymbols operator, Variable[] variables, Formula child) {
		Assert.assertTrue(operator == UNIVERSAL || operator == EXISTENTIAL);
		Assert.assertNotNull(child);
		Assert.assertNotNull(variables);
		Assert.assertTrue(Utility.getVariables(child).containsAll(Arrays.asList(variables)));
		this.child = child;
		this.operator = operator;
		this.variables = variables.clone();
	}

	/**
	 * Checks if is universal.
	 *
	 * @return boolean
	 */
	public boolean isUniversal() {
		return this.operator == LogicalSymbols.UNIVERSAL;
	}


	/**
	 * Checks if is existential.
	 *
	 * @return boolean
	 */
	public boolean isExistential() {
		return this.operator == LogicalSymbols.EXISTENTIAL;
	}


	@Override
	public java.lang.String toString() {
		if(this.toString == null) {
			this.toString = "";
			String op = this.operator.equals(LogicalSymbols.UNIVERSAL) ? "forall" : "exists";
			this.toString += "(" + op;
			this.toString += "[";
            for (int index = 0; index < variables.length; index++) {
                if (index > 0)
                	this.toString += ",";
                this.toString += variables[index].toString();
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
		if(this.atoms == null) {
			this.atoms = this.child.getAtoms();
		}
		return this.atoms.clone();
	}

	@Override
	public Term[] getTerms() {
		if(this.terms == null) {
			this.terms = this.child.getTerms();
		}
		return this.terms.clone();
	}

	@Override
	public Variable[] getFreeVariables() {
		if(this.freeVariables == null) {
			Set<Variable> variables = new LinkedHashSet<>();
			variables.addAll(Arrays.asList(this.child.getFreeVariables()));
			variables.removeAll(Arrays.asList(this.variables));
			this.freeVariables = variables.toArray(new Variable[variables.size()]);
		}
		return this.freeVariables;
	}

	@Override
	public Variable[] getBoundVariables() {
		if(this.boundVariables == null) {
			Set<Variable> variables = new LinkedHashSet<>();
			variables.addAll(Arrays.asList(this.child.getBoundVariables()));
			variables.addAll(Arrays.asList(this.variables));
			this.boundVariables = variables.toArray(new Variable[variables.size()]);
		}
		return this.boundVariables;
	}

	public static QuantifiedFormula of(LogicalSymbols operator, Variable[] variables, Formula child) {
		return new QuantifiedFormula(operator, variables, child);
	}

	public LogicalSymbols getOperator() {
		return this.operator;
	}
	
	public Variable[] getTopLevelQuantifiedVariables() {
		return this.variables.clone();
	}
	
    protected Object readResolve() {
        return s_interningManager.intern(this);
    }

    protected static final InterningManager<QuantifiedFormula> s_interningManager = new InterningManager<QuantifiedFormula>() {
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

    public static QuantifiedFormula create(LogicalSymbols operator, Variable[] variables, Formula child) {
        return s_interningManager.intern(new QuantifiedFormula(operator, variables, child));
    }
}
