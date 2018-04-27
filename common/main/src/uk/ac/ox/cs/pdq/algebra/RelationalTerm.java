package uk.ac.ox.cs.pdq.algebra;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.junit.Assert;

import com.google.common.base.Preconditions;

import uk.ac.ox.cs.pdq.db.Attribute;
import uk.ac.ox.cs.pdq.io.jaxb.adapters.RelationalTermAdapter;

/**
 * 
 * @author Efthymia Tsamoura
 * @author Tim Hobson
 */
@XmlJavaTypeAdapter(RelationalTermAdapter.class)
public abstract class RelationalTerm implements Serializable, Plan {

	private static final long serialVersionUID = 1734503933593174613L;

	protected final Attribute[] inputAttributes;

	protected final Attribute[] outputAttributes;

	/**
	 * List of access children. used often enough to deserve a local cache holding
	 * this list.
	 */
	private Set<AccessTerm> accessesCached = null;

	protected RelationalTerm(Attribute[] inputAttributes, Attribute[] outputAttributes) {
		Assert.assertTrue(outputAttributes != null);
		this.inputAttributes = inputAttributes;
		this.outputAttributes = outputAttributes;
	}

	public Attribute[] getOutputAttributes() {
		return this.outputAttributes.clone();
	}

	public Attribute[] getInputAttributes() {
		return this.inputAttributes.clone();
	}

	public Attribute getOutputAttribute(int index) {
		return this.outputAttributes[index];
	}

	public Attribute getInputAttribute(int index) {
		return this.inputAttributes[index];
	}

	public int getNumberOfOutputAttributes() {
		return this.outputAttributes.length;
	}

	public int getNumberOfInputAttributes() {
		return this.inputAttributes.length;
	}

	public abstract RelationalTerm[] getChildren();

	public abstract RelationalTerm getChild(int childIndex);

	public abstract Integer getNumberOfChildren();

	/**
	 * Gets the accesses, and caches them, since it is a slow operation to generate
	 * the list, but needed frequently.
	 * 
	 * @param operator
	 *            the operator
	 * @return the access operators that are children of the input operator
	 */
	public Set<AccessTerm> getAccesses() {
		if (accessesCached != null)
			return accessesCached;

		Set<AccessTerm> result = new LinkedHashSet<>();
		if (this instanceof AccessTerm) {
			result.add(((AccessTerm) this));
			accessesCached = result;
			return result;
		} else if (this instanceof JoinTerm) {
			for (RelationalTerm child : ((JoinTerm) this).getChildren())
				result.addAll(child.getAccesses());
			accessesCached = result;
			return result;
		} else if (this instanceof DependentJoinTerm) {
			for (RelationalTerm child : ((DependentJoinTerm) this).getChildren())
				result.addAll(child.getAccesses());
			accessesCached = result;
			return result;
		} else if (this instanceof SelectionTerm) {
			result.addAll(((SelectionTerm) this).getChildren()[0].getAccesses());
		} else if (this instanceof ProjectionTerm) {
			result.addAll(((ProjectionTerm) this).getChildren()[0].getAccesses());
		} else if (this instanceof RenameTerm) {
			result.addAll(((RenameTerm) this).getChildren()[0].getAccesses());
		}
		accessesCached = result;
		return result;
	}

	/**
	 * Sets one of the child query plans. Once set, a child cannot be reset. 
	 * @param index the array index
	 * @param child the child query plan
	 */
	protected void setChild(int index, Plan child) {
		Preconditions.checkElementIndex(index, this.getChildren().length);
		// Enforce the rule that child plans cannot be reset.
		Preconditions.checkState(this.getChildren()[index] == null, 
				"Child at index " + index + " is already non-null and cannot be reset.");
		Preconditions.checkNotNull(child);
		this.setChild(index, child);
	}

	/**
	 * Gets the position of an output attribute.
	 * 
	 * @param attribute the attribute
	 * @return the index of the corresponding attribute, or -1 iff the plan has no such attribute. 
	 */
	@Override
	public int getAttributePosition(Attribute attribute) {

		List<Attribute> attrs = Arrays.asList(this.outputAttributes);
		if (!attrs.stream().anyMatch(attr -> attr.equals(attribute)))
			return -1;
		return attrs.indexOf(attribute);
	}
	
	// TODO: Untested!
	@Override
	public Set<AccessTerm> accessPlans() {

		Set<AccessTerm> ret = new LinkedHashSet<AccessTerm>();

		if (this instanceof AccessTerm)
			ret.add((AccessTerm) this);
		else {
			for (Plan child: this.getChildren()) 
				ret.addAll(child.accessPlans());
		}
		return ret;
	}

	public boolean isClosed() {
		return this.getInputAttributes().length == 0;
	}

	@Override
	public Integer[] getInputIndices() {

		return Arrays.stream(this.getInputAttributes())
				.map(a -> Arrays.asList(this.getOutputAttributes()).indexOf(a))
				.sorted()
				.toArray(Integer[]::new);
	}

	public String toString(String type, String body) {

		StringBuilder result = new StringBuilder();
		result.append(type);
		result.append('{');
		if (body != null)
			result.append('[').append(body).append(']');
		for (int i = 0; i != this.getChildren().length; i++) {
			result.append(this.getChildren()[i].toString());
			if (i != this.getChildren().length - 1)
				result.append(',');
		}
		result.append('}');
		return result.toString();	
	}
	
	/**
	 * Recursively converts this RelationalTerm into logic objects.
	 * 
	 * @return
	 */
	public abstract RelationalTermAsLogic toLogic();
	/**
	 * If this term can have conditions (like joins or selects does) then it will
	 * return them as a list. Otherwise will return and empty list.
	 * 
	 * @return
	 */
	protected List<SimpleCondition> getConditions() {
		Condition c = null;
		if (this instanceof SelectionTerm) {
			c = ((SelectionTerm) this).getSelectionCondition();
		}
		if (this instanceof JoinTerm) {
			c = ((JoinTerm) this).getJoinConditions();
		}
		if (this instanceof DependentJoinTerm) {
			c = ((DependentJoinTerm) this).getJoinConditions();
		}
		if (c == null)
			return new ArrayList<>();
		if (c instanceof ConjunctiveCondition) {
			return Arrays.asList(((ConjunctiveCondition) c).getSimpleConditions());
		} else {
			return Arrays.asList(new SimpleCondition[] { (SimpleCondition) c });
		}
	}

}
