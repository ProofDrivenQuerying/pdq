// This file is part of PDQ (https://github.com/ProofDrivenQuerying/pdq) which is released under the MIT license.
// See accompanying LICENSE for copyright notice and full details.

package uk.ac.ox.cs.pdq.algebra;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import uk.ac.ox.cs.pdq.db.AccessMethodDescriptor;
import uk.ac.ox.cs.pdq.db.Attribute;
import uk.ac.ox.cs.pdq.db.Relation;
import uk.ac.ox.cs.pdq.fol.Atom;
import uk.ac.ox.cs.pdq.fol.Formula;
import uk.ac.ox.cs.pdq.fol.Term;
import uk.ac.ox.cs.pdq.fol.TypedConstant;
import uk.ac.ox.cs.pdq.fol.Variable;
import uk.ac.ox.cs.pdq.util.GlobalCounterProvider;

/**
 *
 * @author Efthymia Tsamoura
 * @author Stefano
 */
public class AccessTerm extends RelationalTerm {

	protected static final long serialVersionUID = -6298959701083011594L;

	/** The accessed relation. */
	protected final Relation relation;

	/** The access method to use. */
	protected final AccessMethodDescriptor accessMethod;

	/** The constants used to call the underlying access method. */
	protected final Map<Integer, TypedConstant> inputConstants;

	/** Cached string representation. */
	protected String toString = null;

	private AccessTerm(Relation relation, AccessMethodDescriptor accessMethod) {
		super(AccessTerm.computeInputAttributes(relation, accessMethod), relation.getAttributes());
		/** Use all of the attributes of the relation being accessed as the output attributes; we do this by calling constructor of RelationalTerm */
		assert (relation != null);
		assert (accessMethod != null);
		this.relation = relation;
		this.accessMethod = accessMethod;
		this.inputConstants = new LinkedHashMap<>();
	}

	private AccessTerm(Relation relation, AccessMethodDescriptor accessMethod, Map<Integer, TypedConstant> inputConstants) {
		super(AccessTerm.computeInputAttributes(relation, accessMethod, inputConstants),
				relation.getAttributes());
		assert (relation != null);
		assert (accessMethod != null);
		this.relation = relation;
		this.accessMethod = accessMethod;
		this.inputConstants = new LinkedHashMap<>();
		if (inputConstants != null) {
			for (Integer position : inputConstants.keySet()) {
				assert (position < relation.getAttributes().length);
				assert (Arrays.asList(accessMethod.getInputs()).contains(position));
			}
			for (java.util.Map.Entry<Integer, TypedConstant> entry : inputConstants.entrySet())
				this.inputConstants.put(entry.getKey(), entry.getValue().clone());
		}
	}

	/**
	 * Gets the relation being accessed
	 *
	 * @return the accessed relation
	 * @see uk.ac.ox.cs.pdq.plan.AccessOperator#getRelation()
	 */
	public Relation getRelation() {
		return this.relation;
	}

	/**
	 * Gets the access method.
	 *
	 * @return the access method used
	 * @see uk.ac.ox.cs.pdq.plan.AccessOperator#getAccessMethod()
	 */
	public AccessMethodDescriptor getAccessMethod() {
		return this.accessMethod;
	}

	public Map<Integer, TypedConstant> getInputConstants() {
		return new LinkedHashMap<>(this.inputConstants);
	}
	
	public Map<Attribute, TypedConstant> getInputConstantsAsAttributes() {
		Map<Attribute, TypedConstant> ret = new HashMap<>();
		for (Integer index: inputConstants.keySet()) {
			ret.put(relation.getAttributes()[index], inputConstants.get(index));
		}
		return ret;
	}

	@Override
	public String toString() {
		if (this.toString == null) {
			StringBuilder result = new StringBuilder();
			result.append("Access").append('{');
			result.append(this.relation.getName());
			result.append(".");
			result.append(this.accessMethod.getName());
			result.append('[');
			int shiftBack = 0;
			for (int index = 0; index < this.accessMethod.getInputs().length; ++index) {
				result.append("#");
				result.append(this.accessMethod.getInputs()[index]);
				result.append("=");
				TypedConstant input = inputConstants.get(this.accessMethod.getInputs()[index]);
				if (input != null) {
					result.append(inputConstants.get(this.accessMethod.getInputs()[index]));
					shiftBack++;
				} else {
					if (inputAttributes.length > index - shiftBack) {
						result.append(inputAttributes[index - shiftBack]);
					}
				}
				if (index < this.accessMethod.getInputs().length - 1)
					result.append(",");
			}
			result.append(']');
			result.append('}');
			this.toString = result.toString();
		}
		return this.toString;
	}

	@Override
	public RelationalTerm[] getChildren() {
		return new RelationalTerm[] {};
	}

	public static AccessTerm create(Relation relation, AccessMethodDescriptor accessMethod) {
		return Cache.accessTerm.retrieve(new AccessTerm(relation, accessMethod));
	}

	public static AccessTerm create(Relation relation, AccessMethodDescriptor accessMethod,
			Map<Integer, TypedConstant> inputConstants) {
		return Cache.accessTerm.retrieve(new AccessTerm(relation, accessMethod, inputConstants));
	}

	@Override
	public RelationalTerm getChild(int childIndex) {
		return null;
	}

	@Override
	public Integer getNumberOfChildren() {
		return 0;
	}

	/**
	 * 1) RelationalTerm T is an access term for relation R with attributes a1 ...
	 * an. Let p1 ... pk be the positions that include constants c1.. ck in them.
	 * 
	 * tologic() produces:
	 * 
	 * phi=R(tau1...tau_n) where tau_i=c_i for each p_i and tau_i= a variable x_i
	 * for other position
	 * 
	 * mapping M takes ai to tau_i (we need to access the schema of R to figure out
	 * the positions of each attribute).
	 * </pre>
	 */
	@Override
	public RelationalTermAsLogic toLogic() {
		AccessTerm at = (AccessTerm) this;
		Relation R = at.getRelation();
		Term[] tau = new Term[R.getArity()];
		Map<Attribute, Term> mapping = new HashMap<>();
		for (int index = 0; index < R.getArity(); index++) {
			if (at.getInputConstants().containsKey(index)) {
				tau[index] = at.getInputConstants().get(index);
			} else {
				tau[index] = Variable.create("x_" + index + "_" + GlobalCounterProvider.getNext("VariableName"));
			}
			mapping.put(at.getOutputAttribute(index), tau[index]);
		}
		Formula phi = Atom.create(R, tau);
		return new RelationalTermAsLogic(phi, mapping);
	}

	/**
	 * The access method descriptor contains the index of the attribute only. This
	 * method maps those indexes to the actual Attribute objects.
	 * 
	 * @param relation
	 * @param accessMethod
	 * @return
	 */
	public static Attribute[] computeInputAttributes(Relation relation, AccessMethodDescriptor accessMethod) {
		assert (relation != null);
		assert (accessMethod != null);
		if (accessMethod.getInputs().length == 0) {
			return new Attribute[] {};
		}
		List<Attribute> inputs = new ArrayList<>();
		for (Integer i : accessMethod.getInputs()) {
			inputs.add(relation.getAttributes()[i]);
		}
		return inputs.toArray(new Attribute[inputs.size()]);
	}

	/**
	 * Tests if the given access method and relation is compatible or not. Generates
	 * a list of attributes by filtering out the provided input constants from the
	 * accessMethod's inputs.
	 * 
	 * @param relation
	 * @param accessMethod
	 * @param inputConstants
	 * @return
	 */
	public static Attribute[] computeInputAttributes(Relation relation, AccessMethodDescriptor accessMethod,
			Map<Integer, TypedConstant> inputConstants) {
		assert (relation != null);
		if (!(accessMethod != null && accessMethod.getInputs().length > 0)
				&& (inputConstants == null || inputConstants.isEmpty())) {
			return new Attribute[0];
		}
		assert (accessMethod != null && accessMethod.getInputs().length > 0);
		assert (inputConstants != null);
		for (Integer position : inputConstants.keySet()) {
			assert (position < relation.getAttributes().length);
			assert (Arrays.asList(accessMethod.getInputs()).contains(position));
		}
		List<Attribute> inputs = new ArrayList<>();
		for (Integer i : accessMethod.getInputs()) {
			if (!inputConstants.containsKey(i)) {
				inputs.add(relation.getAttributes()[i]);
			}
		}
		return inputs.toArray(new Attribute[inputs.size()]);
	}
	
}
