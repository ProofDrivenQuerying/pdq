package uk.ac.ox.cs.pdq.algebra2;

import java.util.List;

import uk.ac.ox.cs.pdq.db.Attribute;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

/**
 * RelationalOperator defines a top-class for all logical relational operators.
 *
 * @author Julien Leblay
 */
public abstract class UnaryRelationalTerm extends RelationalTerm{
	
	private final List<RelationalTerm> children;
	
	protected final List<Attribute> inputAttributes;

	public UnaryRelationalTerm(RelationalOperator operator, RelationalTerm child) {
		super(operator, getOutputAttributes(operator, child));
		checkConstructor(operator, child);
		this.children = ImmutableList.of(child);
		this.inputAttributes = child.getOutputAttributes();
	}
	
	@Override
	public List<RelationalTerm> getChildren() {
		return this.children;
	}
	
	private static boolean checkConstructor(RelationalOperator operator, RelationalTerm child) {
		Preconditions.checkArgument(operator != null && child != null && (operator instanceof Selection || operator instanceof Projection || operator instanceof Rename));
		if(operator instanceof Projection) {
			return ((Projection)operator).getInputAttributes().equals(child.getOutputAttributes());
		}
		else if(operator instanceof Rename) {
			return ((Rename)operator).getInputAttributes().equals(child.getOutputAttributes());
		}
		else if(operator instanceof Selection) {
			return true;
		}
		return false;
	}
	
	private static List<Attribute> getOutputAttributes(RelationalOperator operator, RelationalTerm child) {
		Preconditions.checkArgument(operator != null && child != null && (operator instanceof Selection || operator instanceof Projection || operator instanceof Rename));
		if(operator instanceof Projection) {
			return ((Projection)operator).getOutputAttributes();
		}
		else if(operator instanceof Rename) {
			return ((Rename)operator).getOutputAttributes();
		}
		else if(operator instanceof Selection) {
			return child.getOutputAttributes();
		}
		return null;
	}
	
}
