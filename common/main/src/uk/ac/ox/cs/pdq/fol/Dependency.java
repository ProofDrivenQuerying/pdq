package uk.ac.ox.cs.pdq.fol;

import java.util.Arrays;
import java.util.List;

import org.junit.Assert;

import uk.ac.ox.cs.pdq.InterningManager;
import uk.ac.ox.cs.pdq.util.Utility;

/**
 * A universally quantified implication where the body is a quantifier-free formula and 
 * the head is an existentially-quantified or quantifier-free formula.
 *
 * @author Efthymia Tsamoura
 */
public class Dependency extends QuantifiedFormula {

	private static final long serialVersionUID = 6522148218362709983L;
	protected final Formula body;
	protected final Formula head;
	
	/**  The dependency's universally quantified variables. */
	protected Variable[] universal;

	/**  The dependency's existentially quantified variables. */
	protected Variable[] existential;
	
	protected final Atom[] bodyAtoms;
	
	protected final Atom[] headAtoms;
	
	protected Dependency(Formula body, Formula head) {
		super(LogicalSymbols.UNIVERSAL, body.getFreeVariables(), Implication.create(body,head));
		Assert.assertTrue(isUnquantified(body));
		Assert.assertTrue(isExistentiallyQuantified(head) || isUnquantified(head));
		Assert.assertTrue(Arrays.asList(body.getFreeVariables()).containsAll(Arrays.asList(head.getFreeVariables())));
		this.body = body;
		this.head = head;
		this.bodyAtoms = this.body.getAtoms();
		this.headAtoms = this.head.getAtoms();
	}
	
	
	protected Dependency(Atom[] body, Atom[] head) {
		this(Conjunction.of(body), createHead(body, head));
	}
	
	private static Formula createHead(Atom[] body, Atom[] head) {
		List<Variable> bodyVariables = Utility.getVariables(body);
		List<Variable> headVariables = Utility.getVariables(head);
		if(bodyVariables.containsAll(headVariables)) 
			return Conjunction.of(head);
		else {
			headVariables.removeAll(bodyVariables);
			return QuantifiedFormula.create(LogicalSymbols.EXISTENTIAL, headVariables.toArray(new Variable[headVariables.size()]), Conjunction.of(head));
		}
	}
	
	private static boolean isUnquantified(Formula formula) {
		if(formula instanceof Conjunction || formula instanceof Implication || formula instanceof Disjunction) 
			return isUnquantified(formula.getChildren()[0]) && isUnquantified(formula.getChildren()[1]);
		else if(formula instanceof Negation) 
			return isUnquantified(formula.getChildren()[0]);
		else if(formula instanceof Literal) 
			return true;
		else if(formula instanceof Atom) 
			return true;
		else if(formula instanceof QuantifiedFormula) 
			return false;
		return false;
	}
	
	private static boolean isExistentiallyQuantified(Formula formula) {
		if(formula instanceof Conjunction || formula instanceof Implication || formula instanceof Disjunction) 
			return isUnquantified(formula.getChildren()[0]) && isUnquantified(formula.getChildren()[0]);
		else if(formula instanceof Negation) 
			return isUnquantified(formula.getChildren()[0]);
		else if(formula instanceof Literal) 
			return true;
		else if(formula instanceof Atom) 
			return true;
		else if(formula instanceof QuantifiedFormula) {
			if(((QuantifiedFormula) formula).isExistential()) {
				return true;
			}
			else {
				return false;
			}
		}
		return false;
	}

	/**
	 * TOCOMMENT it would be better to say getBody or getHead; it's not really clear what left and right is.
	 * Gets the left-hand side of this constraint.
	 *
	 * @return the left-hand side of this constraint
	 */
	public Formula getBody() {
		return this.body;
	}

	/**
	 * Gets the right-hand side of this constraint.
	 *
	 * @return the right-hand side of this constraint
	 */
	public Formula getHead() {
		return this.head; 
	}
	
	/**
	 * Gets the universally quantified variables.
	 *
	 * @return List<Variable>
	 */
	public Variable[] getUniversal() {
		if(this.universal == null) 
			this.universal = this.variables;
		return this.universal.clone();
	}

	/**
	 * Gets the existentially quantified variables.
	 *
	 * @return List<Variable>
	 */
	public Variable[] getExistential() {
		if(this.existential == null) 
			this.existential = this.head.getBoundVariables();
		return this.existential.clone();
	}
	
	
	public int getNumberOfBodyAtoms() {
		return this.bodyAtoms.length;
	}
	
	public int getNumberOfHeadAtoms() {
		return this.bodyAtoms.length;
	}
	
	public Atom getBodyAtom(int bodyAtomIndex) {
		return this.bodyAtoms[bodyAtomIndex];
	}
	
	public Atom getHeadAtom(int headAtomIndex) {
		return this.headAtoms[headAtomIndex];
	}
	
	public Atom[] getBodyAtoms() {
		return this.bodyAtoms.clone();
	}
	
	public Atom[] getHeadAtoms() {
		return this.headAtoms.clone();
	}
	
    protected Object readResolve() {
        return s_interningManager.intern(this);
    }

    protected static final InterningManager<Dependency> s_interningManager = new InterningManager<Dependency>() {
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
    
//    public static Dependency create(Formula body, Formula head) {
//        return s_interningManager.intern(new Dependency(body, head));
//    }
//    
    public static Dependency create(Atom[] body, Atom[] head) {
        return s_interningManager.intern(new Dependency(body, head));
    }
}
