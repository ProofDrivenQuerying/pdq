package uk.ac.ox.cs.pdq.fol;

import java.util.List;

import com.google.common.base.Preconditions;

/**
 * A universally quantified implication where the body is a quantifier-free formula and 
 * the head is an existentially-quantified or quantifier-free formula.
 *
 * @author Efthymia Tsamoura
 */
public class Dependency extends QuantifiedFormula {

	protected final Formula body;
	protected final Formula head;
	
	/**  The dependency's universally quantified variables. */
	protected List<Variable> universal;

	/**  The dependency's existentially quantified variables. */
	protected List<Variable> existential;
	
	public Dependency(LogicalSymbols operator, List<Variable> variables, Implication implication) {
		super(operator, variables, implication);
		Preconditions.checkArgument(isUnquantified(implication.getChildren().get(0)));
		Preconditions.checkArgument(isExistentiallyQuantified(implication.getChildren().get(1)) ||
				isUnquantified(implication.getChildren().get(1)));
		Preconditions.checkArgument(implication.getChildren().get(0).getFreeVariables().
				containsAll(implication.getChildren().get(1).getFreeVariables()));
		this.body = implication.getChildren().get(0);
		this.head = implication.getChildren().get(1);
	}
	
	public Dependency(Formula body, Formula head) {
		super(LogicalSymbols.UNIVERSAL, body.getFreeVariables(), new Implication(body,head));
		Preconditions.checkArgument(isUnquantified(body));
		Preconditions.checkArgument(isExistentiallyQuantified(head) || isUnquantified(head));
		Preconditions.checkArgument(body.getFreeVariables().containsAll(head.getFreeVariables()));
		this.body = body;
		this.head = head;
	}
	
	private static boolean isUnquantified(Formula formula) {
		if(formula instanceof Conjunction || formula instanceof Implication || formula instanceof Disjunction) {
			return isUnquantified(formula.getChildren().get(0)) && isUnquantified(formula.getChildren().get(1));
		}
		else if(formula instanceof Negation) {
			return isUnquantified(formula.getChildren().get(0));
		}
		else if(formula instanceof Literal) {
			return true;
		}
		else if(formula instanceof Atom) {
			return true;
		}
		else if(formula instanceof QuantifiedFormula) {
			return false;
		}
		return false;
	}
	
	private static boolean isExistentiallyQuantified(Formula formula) {
		if(formula instanceof Conjunction || formula instanceof Implication || formula instanceof Disjunction) {
			return isUnquantified(formula.getChildren().get(0)) && isUnquantified(formula.getChildren().get(1));
		}
		else if(formula instanceof Negation) {
			return isUnquantified(formula.getChildren().get(0));
		}
		else if(formula instanceof Literal) {
			return true;
		}
		else if(formula instanceof Atom) {
			return true;
		}
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
	public List<Variable> getUniversal() {
		if(this.universal == null) {
			this.universal = this.variables;
		}
		return this.universal;
	}

	/**
	 * Gets the existentially quantified variables.
	 *
	 * @return List<Variable>
	 */
	public List<Variable> getExistential() {
		if(this.existential == null) {
			this.existential = this.head.getBoundVariables();
		}
		return this.existential;
	}
}
