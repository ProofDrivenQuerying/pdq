package uk.ac.ox.cs.pdq.algebra2;

import java.util.List;

import org.apache.commons.collections4.CollectionUtils;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

/**
 * RelationalOperator defines a top-class for all logical relational operators.
 *
 * @author Julien Leblay
 */
public abstract class BinaryRelationalTerm extends RelationalTerm{
	
	private final List<RelationalTerm> children;
	/**
	 * Instantiates a new operator.
	 * @param input TupleType
	 * @param output TupleType
	 */
	protected BinaryRelationalTerm(RelationalOperator operator, RelationalTerm child1, RelationalTerm child2) {
		super(operator, Lists.newArrayList(CollectionUtils.union(child1.getOutputAttributes(), child2.getOutputAttributes())));
		Preconditions.checkArgument(child1 != null && child2 != null);
		this.children = ImmutableList.of(child1, child2);
	}
	
	@Override
	public List<RelationalTerm> getChildren() {
		return this.children;
	}
	
}
