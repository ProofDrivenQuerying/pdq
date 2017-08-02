package uk.ac.ox.cs.pdq.db;

import java.io.Serializable;

import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import org.junit.Assert;
import uk.ac.ox.cs.pdq.InterningManager;
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

	/**
	 * Gets the zero based inputs.
	 *
	 */
	public Integer[] getZeroBasedInputPositions() {
		Integer[] zero = new Integer[this.inputs.length];
		for(int index = 0; index < this.inputs.length; ++index) 
			zero[index] = this.inputs[index] - 1;
		return zero;
	}
	
	public String getName() {
		return this.name;
	}

	protected Object readResolve() {
		return s_interningManager.intern(this);
	}

	protected static final InterningManager<AccessMethod> s_interningManager = new InterningManager<AccessMethod>() {
		protected boolean equal(AccessMethod object1, AccessMethod object2) {
			if (!object1.name.equals(object2.name) || object1.inputs.length != object2.inputs.length)
				return false;
			for (int index = object1.inputs.length - 1; index >= 0; --index)
				if (!object1.inputs[index].equals(object2.inputs[index]))
					return false;
			return true;
		}

		protected int getHashCode(AccessMethod object) {
			int hashCode = object.name.hashCode();
			for (int index = object.inputs.length - 1; index >= 0; --index)
				hashCode = hashCode * 7 + object.inputs[index].hashCode();
			return hashCode;
		}
	};

	public static AccessMethod create(String name, Integer[] inputs) {
		return s_interningManager.intern(new AccessMethod(name, inputs));
	}
	
	public static AccessMethod create(Integer[] inputs) {
		return s_interningManager.intern(new AccessMethod(inputs));
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
