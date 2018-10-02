package uk.ac.ox.cs.pdq.fol;

import java.util.Arrays;
import java.util.List;

import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.junit.Assert;

import uk.ac.ox.cs.pdq.io.jaxb.adapters.DependencyAdapter;
import uk.ac.ox.cs.pdq.util.Utility;

/**
 * A universally quantified implication where the body is a quantifier-free formula and 
 * the head is an existentially-quantified or quantifier-free formula.
 *
 * @author Efthymia Tsamoura
 */
@XmlJavaTypeAdapter(DependencyAdapter.class)
public class Dependency extends QuantifiedFormula {

	private static final long serialVersionUID = 6522148218362709983L;
	protected String name;
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
		this.name = "default";
		this.body = body;
		this.head = head;
		this.bodyAtoms = this.body.getAtoms();
		this.headAtoms = this.head.getAtoms();
	}
	
	protected Dependency(Formula body, Formula head, String name) {
		super(LogicalSymbols.UNIVERSAL, body.getFreeVariables(), Implication.create(body,head));
		Assert.assertTrue(isUnquantified(body));
		Assert.assertTrue(isExistentiallyQuantified(head) || isUnquantified(head));
		Assert.assertTrue(Arrays.asList(body.getFreeVariables()).containsAll(Arrays.asList(head.getFreeVariables())));
		this.name = name;
		this.body = body;
		this.head = head;
		this.bodyAtoms = this.body.getAtoms();
		this.headAtoms = this.head.getAtoms();
	}
	
	protected Dependency(Atom[] body, Atom[] head) {
		this(Conjunction.create(body), createHead(body, head));
	}
	
	
	protected Dependency(Atom[] body, Atom[] head, String name) {
		this(Conjunction.create(body), createHead(body, head), name);
	}
	
	private static Formula createHead(Atom[] body, Atom[] head) {
		List<Variable> bodyVariables = Utility.getVariables(body);
		List<Variable> headVariables = Utility.getVariables(head);
		if(bodyVariables.containsAll(headVariables)) 
			return Conjunction.create(head);
		else {
			headVariables.removeAll(bodyVariables);
			return QuantifiedFormula.create(LogicalSymbols.EXISTENTIAL, headVariables.toArray(new Variable[headVariables.size()]), Conjunction.create(head));
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
	
	public String getName() {
		return name;
	}
	
	public int getNumberOfBodyAtoms() {
		return this.bodyAtoms.length;
	}
	
	public int getNumberOfHeadAtoms() {
		return this.headAtoms.length;
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
	
    public static Dependency create(Atom[] body, Atom[] head) {
        return Cache.dependency.retrieve(new Dependency(body, head));
    }

    public static Dependency create(Atom[] body, Atom[] head, String name) {
        return Cache.dependency.retrieve(new Dependency(body, head, name));
    }
}
