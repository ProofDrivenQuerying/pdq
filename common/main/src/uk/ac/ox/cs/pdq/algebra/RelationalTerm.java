package uk.ac.ox.cs.pdq.algebra;

import java.io.Serializable;

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

	protected RelationalTerm(Attribute[] inputAttributes, Attribute[] outputAttributes) {
		Assert.assertTrue(outputAttributes != null && outputAttributes.length > 0);
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
	
	public boolean isClosed() {
		return this.inputAttributes.length == 0;
	}
}
