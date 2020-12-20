// This file is part of PDQ (https://github.com/ProofDrivenQuerying/pdq) which is released under the MIT license.
// See accompanying LICENSE for copyright notice and full details.

package uk.ac.ox.cs.pdq.fol;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.apache.commons.lang3.ArrayUtils;

import uk.ac.ox.cs.pdq.algebra.RelationalTermAsLogic;
import uk.ac.ox.cs.pdq.db.Relation;
import uk.ac.ox.cs.pdq.db.Schema;
import uk.ac.ox.cs.pdq.io.jaxb.adapters.QueryAdapter;
import uk.ac.ox.cs.pdq.util.Utility;

/**
 * A conjunctive query (CQ) is a first order formula of the form \exists x_1, \ldots, x_n \Wedge A_i,
 * where A_i are atoms with arguments that are either variables or constants.

 * @author Efthymia Tsamoura
 * @author Stefano
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
		assert (children != null);
		assert (children.length > 0);
		if (children.length == 1) {
			//Check that the body is a conjunction of positive atoms
			assert (Arrays.asList(children[0].getFreeVariables()).containsAll(Arrays.asList(freeVariables)));
			this.child = children[0];
			this.freeVariables = freeVariables.clone();
			this.boundVariables = ArrayUtils.removeElements(child.getFreeVariables(), freeVariables);
			this.atoms = child.getAtoms();
		} else {
			
			Conjunction conjunction = (Conjunction)Conjunction.create(children);
			assert (Arrays.asList(conjunction.getFreeVariables()).containsAll(Arrays.asList(freeVariables)));
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
		assert (childIndex == 0);
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
			flatFormula = Conjunction.create(formula.getChild(0));
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

	/**
	 * Gets the types of a query's free variables
	 *
	 * @param query
	 *            the q
	 * @return a list of types for each free variable of the query
	 */
	public Type[] computeVariableTypes(Schema schema) {
		Variable[] freeVariables = this.getFreeVariables();
		Type[] types = new Type[this.getFreeVariables().length];
		boolean assigned = false;
		for (int i = 0, l = types.length; i < l; i++) {
			assigned = false;
			Variable t = freeVariables[i];
			Atom[] atoms = this.getAtoms();
			for (Atom atom : atoms) {
				Relation s = schema.getRelation(atom.getPredicate().getName());
				List<Integer> pos = Utility.search(atom.getTerms(), t);
				if (!pos.isEmpty()) {
					types[i] = s.getAttribute(pos.get(0)).getType();
					assigned = true;
					break;
				}
			}
			if (!assigned)
				throw new IllegalStateException("Could not infer query type.");
		}
		return types;
	}

}
