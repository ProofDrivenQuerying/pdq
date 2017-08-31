package uk.ac.ox.cs.pdq.db;

import java.io.Serializable;

import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.junit.Assert;

import uk.ac.ox.cs.pdq.io.jaxb.adapters.AccessMethodAdapter;

/**
 * An access method defines the positions of a relation's attributes whose values are required to access the relation.
 *
 * @author Efthymia Tsamoura
 * @author Julien Leblay
 */
@XmlJavaTypeAdapter(AccessMethodAdapter.class)
public class AccessMethod implements Serializable {

	protected static final long serialVersionUID = -5821292665848480210L;

	/** A Constant DEFAULT_PREFIX for all automatically generated access methods names */
	public static final String DEFAULT_PREFIX = "mt_";

	/** A global counter appended to the default prefix in order to create a new automatically generated access methods name. */
	protected static int globalCounter = 0;

	/**  Input attribute positions. */
	protected final Integer[] inputs;

	/**  Name of the access method. */
	protected final String name;

	/**  String representation of the object. */
	protected String toString = null;
	
	protected AccessMethod(Integer[] inputs) {
		this(DEFAULT_PREFIX + globalCounter++, inputs);
	}

	protected AccessMethod(String name, Integer[] inputs) {
		this.name = name;
		this.inputs = inputs.clone();
	}

	public Integer[] getInputs() {
		return this.inputs.clone();
	}
	
	public int getNumberOfInputs() {
		return this.inputs.length;
	}
	
	public int getInputPosition(int index) {
		Assert.assertTrue(index >=0 && index < this.inputs.length);
		return this.inputs[index];
	}
	
	public String getName() {
		return this.name;
	}

	public static AccessMethod create(String name, Integer[] inputs) {
		return Cache.accessMethod.retrieve(new AccessMethod(name, inputs));
	}
	
	public static AccessMethod create(Integer[] inputs) {
		return Cache.accessMethod.retrieve(new AccessMethod(inputs));
	}

	@Override
	public String toString() {
		if (this.toString == null) {
			StringBuilder result = new StringBuilder();
			result.append(this.name);
			if(this.inputs.length > 0) {
				result.append(':');
				char sep = '[';
				for (int i:this.inputs) {
					result.append(sep).append(i);
					sep = ',';
				}
				result.append(']');
			}
			this.toString = result.toString();
		}
		return this.toString;
	}
}
