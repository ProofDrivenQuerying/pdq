package uk.ac.ox.cs.pdq.fol;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.apache.commons.lang3.ArrayUtils;
import org.junit.Assert;

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
	
	/**  Cashed string representation of the atom. */
	private String toString = null;

	/** 
	 * see #42
	 * 
	 * The grounding. */
	protected final Map<Variable, Constant> canonicalSubstitution;
	
	/**  
	 * See #42, together with the grounding field a few lines below, they are very confusing.
	 * 
	 * Map of query's free variables to chase constants. */
	protected final Map<Variable, Constant> canonicalSubstitutionOfFreeVariables;
	
	/**  Cashed list of free variables. */
	protected final Variable[] freeVariables;

	/**  Cashed list of bound variables. */
	private final Variable[] boundVariables;
	
	private final Atom[] atoms;

	/**
	 * Builds a query given a set of free variables and its conjunction.
	 * The query is grounded using the input mapping of variables to constants.
	 */
	private ConjunctiveQuery(Variable[] freeVariables, Conjunction child, Map<Variable, Constant> canonicalSubstitution) {
		//Check that the body is a conjunction of positive atoms
		Assert.assertTrue(isConjunctionOfAtoms(child));
		Assert.assertTrue(Arrays.asList(child.getFreeVariables()).containsAll(Arrays.asList(freeVariables)));
		this.child = child;
		this.freeVariables = freeVariables.clone();
		this.boundVariables = ArrayUtils.removeElements(child.getFreeVariables(), freeVariables);
		this.canonicalSubstitution = canonicalSubstitution;
		this.canonicalSubstitutionOfFreeVariables = new LinkedHashMap<>();
		this.canonicalSubstitutionOfFreeVariables.putAll(canonicalSubstitution);
		for(Variable variable:this.getBoundVariables()) 
			this.canonicalSubstitutionOfFreeVariables.remove(variable);
		this.atoms = child.getAtoms();
	}
	
	/**
	 * Builds a query given a set of free variables and an atom.
	 * The query is grounded using the input mapping of variables to constants.
	 */
	private ConjunctiveQuery(Variable[] freeVariables, Atom child, Map<Variable, Constant> canonicalSubstitution) {
		//Check that the body is a conjunction of positive atoms
		Assert.assertTrue(isConjunctionOfAtoms(child));
		Assert.assertTrue(Arrays.asList(child.getFreeVariables()).containsAll(Arrays.asList(freeVariables)));
		this.child = child;
		this.freeVariables = freeVariables.clone();
		this.boundVariables = ArrayUtils.removeElements(child.getFreeVariables(), freeVariables);
		this.canonicalSubstitution = canonicalSubstitution;
		this.canonicalSubstitutionOfFreeVariables = new LinkedHashMap<>();
		this.canonicalSubstitutionOfFreeVariables.putAll(canonicalSubstitution);
		for(Variable variable:this.getBoundVariables()) 
			this.canonicalSubstitutionOfFreeVariables.remove(variable);
		this.atoms = child.getAtoms();
	}
	
	/**
	 * Builds a query given a set of free variables and its conjunction.
	 */
	private ConjunctiveQuery(Variable[] freeVariables, Conjunction child) {
		this(freeVariables, child, generateSubstitutionToCanonicalVariables(child));
	}
	
	/**
	 * Builds a query given a set of free variables and an atom.
	 */
	private ConjunctiveQuery(Variable[] freeVariables, Atom child) {
		this(freeVariables, child, generateSubstitutionToCanonicalVariables(child));
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
	 * see #42
	 * Generate canonical mapping.
	 *
	 * @param formula the body
	 * @return 		a mapping of variables of the input conjunction to constants. 
	 * 		A fresh constant is created for each variable of the conjunction. 
	 * 		This method is invoked by the conjunctive query constructor when the constructor is called with empty input canonical mapping.
	 */
	public static Map<Variable, Constant> generateSubstitutionToCanonicalVariables(Formula formula) {
		Map<Variable, Constant> canonicalMapping = new LinkedHashMap<>();
			for (Atom atom: formula.getAtoms()) {
				for (Term t: atom.getTerms()) {
					if (t.isVariable()) {
						Constant c = canonicalMapping.get(t);
						if (c == null) {
							c = UntypedConstant.create(CanonicalNameGenerator.getName());
							canonicalMapping.put((Variable) t, c);
						}
					}
				}
			}
		return canonicalMapping;
	}
	
	/**
	 * Checks if the query is boolean boolean.
	 *
	 */
	public boolean isBoolean() {
		return this.getFreeVariables().length == 0;
	}
	
	/**
	 * Gets the mapping of the free query variables to canonical constants.
	 *
	 * @return a map of query's free variables to its canonical constants.
	 * Given a CQ Q, the canonical database of Q is the instance which has for each atom R(\vec{v}) 
	 * in Q a corresponding fact for relation R with \vec{v} as a tuple. The canonical constants are the constants of the canonical database of Q
	 */
	public Map<Variable, Constant> getSubstitutionOfFreeVariablesToCanonicalConstants() {
		return this.canonicalSubstitutionOfFreeVariables;
	}
	
	public Map<Variable, Constant> getSubstitutionToCanonicalConstants() {
		return this.canonicalSubstitution;
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
	
	
    public static ConjunctiveQuery create(Variable[] freeVariables, Conjunction child, Map<Variable, Constant> canonicalSubstitution) {
        return Cache.conjunctiveQuery.retrieve(new ConjunctiveQuery(freeVariables, child, canonicalSubstitution));
    }
    
    public static ConjunctiveQuery create(Variable[] freeVariables, Atom child, Map<Variable, Constant> canonicalSubstitution) {
        return Cache.conjunctiveQuery.retrieve(new ConjunctiveQuery(freeVariables, child, canonicalSubstitution));
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

	@Override
	public int getNumberOfChildlen() {
		return 1;
	}

	public Atom getAtom(int atomIndex) {
		return this.atoms[atomIndex];
	}
	
	public int getNumberOfAtoms() {
		return this.atoms.length;
	}
	
}
