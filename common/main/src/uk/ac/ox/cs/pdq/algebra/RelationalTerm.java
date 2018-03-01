package uk.ac.ox.cs.pdq.algebra;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.junit.Assert;

import uk.ac.ox.cs.pdq.db.Attribute;
import uk.ac.ox.cs.pdq.io.jaxb.adapters.RelationalTermAdapter;

/**
 * 
 * @author Efthymia Tsamoura
 *
 */
@XmlJavaTypeAdapter(RelationalTermAdapter.class)
public abstract class RelationalTerm implements Serializable {

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

	public Integer getNumberOfOutputAttributes() {
		return this.outputAttributes.length;
	}

	public Integer getNumberOfInputAttributes() {
		return this.inputAttributes.length;
	}

	public abstract RelationalTerm[] getChildren();

	public abstract RelationalTerm getChild(int childIndex);

	public abstract Integer getNumberOfChildren();

	public boolean isClosed() {
		return this.inputAttributes.length == 0;
	}

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
