package uk.ac.ox.cs.pdq.algebra;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.Assert;

import uk.ac.ox.cs.pdq.db.AccessMethod;
import uk.ac.ox.cs.pdq.db.Relation;
import uk.ac.ox.cs.pdq.db.TypedConstant;


/**
 *
 * @author Efthymia Tsamoura
 */
public class AccessTerm extends RelationalTerm {

	protected static final long serialVersionUID = -6298959701083011594L;

	/** The accessed relation. */
	protected final Relation relation;

	/** The access method to use. */
	protected final AccessMethod accessMethod;

	/**  The constants used to call the underlying access method. */
	protected final Map<Integer, TypedConstant> inputConstants;

	/**  Cashed string representation. */
	protected String toString = null;

	private AccessTerm(Relation relation, AccessMethod accessMethod) {
		super(AlgebraUtilities.computeInputAttributes(relation, accessMethod), relation.getAttributes());
		Assert.assertNotNull(relation);
		Assert.assertNotNull(accessMethod);
		this.relation = relation;
		this.accessMethod = accessMethod;
		this.inputConstants = new LinkedHashMap<>();
	}

	private AccessTerm(Relation relation, AccessMethod accessMethod, Map<Integer, TypedConstant> inputConstants) {
		super(AlgebraUtilities.computeInputAttributes(relation, accessMethod, inputConstants), relation.getAttributes());
		Assert.assertNotNull(relation);
		Assert.assertNotNull(accessMethod);
		this.relation = relation;
		this.accessMethod = accessMethod;
		this.inputConstants = new LinkedHashMap<>();
		if (inputConstants!=null) {
			for(Integer position:inputConstants.keySet()) {
				Assert.assertTrue(position < relation.getArity());
				Assert.assertTrue(Arrays.asList(accessMethod.getInputs()).contains(position));
			}
			for(java.util.Map.Entry<Integer, TypedConstant> entry:inputConstants.entrySet()) 
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
	public AccessMethod getAccessMethod() {
		return this.accessMethod;
	}
	
	public Map<Integer, TypedConstant> getInputConstants() {
		return new LinkedHashMap<>(this.inputConstants);
	}

	@Override
	public String toString() {
		if(this.toString == null) {
			StringBuilder result = new StringBuilder();
			result.append("Access").append('{');
			result.append(this.relation.getName());
			result.append(".");
			result.append(this.accessMethod.getName());
			result.append('[');
			int shiftBack = 0;
			for(int index = 0; index < this.accessMethod.getInputs().length; ++index) {
				result.append("#");
				result.append(this.accessMethod.getInputs()[index]);
				result.append("=");
				TypedConstant input = inputConstants.get(this.accessMethod.getInputs()[index]);
				if (input!=null) {
					result.append(inputConstants.get(this.accessMethod.getInputs()[index]));
					shiftBack++;
				} else {
					if (inputAttributes.length > index-shiftBack) {
						result.append(inputAttributes[index-shiftBack]);
					}
				}
				if(index < this.accessMethod.getInputs().length - 1)
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
		 return new RelationalTerm[]{};
	}

    public static AccessTerm create(Relation relation, AccessMethod accessMethod) {
        return Cache.accessTerm.retrieve(new AccessTerm(relation, accessMethod));
    }
    
    public static AccessTerm create(Relation relation, AccessMethod accessMethod, Map<Integer, TypedConstant> inputConstants) {
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
}