package uk.ac.ox.cs.pdq.algebra2;

import java.util.List;

import uk.ac.ox.cs.pdq.db.Attribute;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

/**
 * 
 * @author Efthymia Tsamoura
 *
 */
public class NullaryRelationalTerm extends RelationalTerm{
		
	protected final List<Attribute> inputAttributes;
	
	public NullaryRelationalTerm(RelationalOperator operator) {
		super(operator, getOutputAttributes(operator));
		Preconditions.checkArgument(operator instanceof AccessOperator);
		this.inputAttributes = ((Access)operator).getInputAttributes();
	}
	
	private static List<Attribute> getOutputAttributes(RelationalOperator operator) {
		Preconditions.checkArgument(operator != null && (operator instanceof Access));
		if(operator instanceof Access) {
			return ((Access)operator).getOutputAttributes();
		}
		return null;
	}
	
	@Override
	public List<RelationalTerm> getChildren() {
		return ImmutableList.of();
	}
	
}
