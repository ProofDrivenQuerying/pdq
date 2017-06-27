package uk.ac.ox.cs.pdq.algebra;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.Assert;

import uk.ac.ox.cs.pdq.InterningManager;
import uk.ac.ox.cs.pdq.db.AccessMethod;
import uk.ac.ox.cs.pdq.db.Attribute;
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
	protected final Map<Integer, TypedConstant<?>> inputConstants;

	/**  Cashed string representation. */
	protected String toString = null;

	protected AccessTerm(Relation relation, AccessMethod accessMethod) {
		super(AlgebraUtilities.getInputAttributes(relation, accessMethod), relation.getAttributes().toArray(new Attribute[relation.getAttributes().size()]));
		Assert.assertNotNull(relation);
		Assert.assertNotNull(accessMethod);
		this.relation = relation;
		this.accessMethod = accessMethod;
		this.inputConstants = new LinkedHashMap<>();
	}

	protected AccessTerm(Relation relation, AccessMethod accessMethod, Map<Integer, TypedConstant<?>> inputConstants) {
		super(AlgebraUtilities.getInputAttributes(relation, accessMethod, inputConstants), relation.getAttributes().toArray(new Attribute[relation.getAttributes().size()]));
		Assert.assertNotNull(relation);
		Assert.assertNotNull(accessMethod);
		for(Integer position:inputConstants.keySet()) {
			Assert.assertTrue(position < relation.getArity());
			Assert.assertTrue(Arrays.asList(accessMethod.getInputs()).contains(position));
		}
		this.relation = relation;
		this.accessMethod = accessMethod;
		this.inputConstants = new LinkedHashMap<>();
		for(java.util.Map.Entry<Integer, TypedConstant<?>> entry:inputConstants.entrySet()) 
			this.inputConstants.put(entry.getKey(), entry.getValue().clone());
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

	@Override
	public String toString() {
		if(this.toString == null) {
			StringBuilder result = new StringBuilder();
			result.append("Access").append('{');
			result.append(this.relation.getName());
			result.append('[');
			for(int index = 0; index < this.accessMethod.getInputs().length; ++index) {
				result.append(this.accessMethod.getInputs()[index]);
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

    protected static final InterningManager<AccessTerm> s_interningManager = new InterningManager<AccessTerm>() {
        protected boolean equal(AccessTerm object1, AccessTerm object2) {
            if (!object1.relation.equals(object2.relation) || !object1.accessMethod.equals(object2.accessMethod) || 
            		object1.inputConstants.size() != object2.inputConstants.size())
                return false;
            for(java.util.Map.Entry<Integer, TypedConstant<?>> entry:object1.inputConstants.entrySet()) {
            	if(!object2.inputConstants.containsKey(entry.getKey()) || object2.inputConstants.get(entry.getKey()).equals(entry.getValue())) 
            		return false;
            }
            return true;
        }

        protected int getHashCode(AccessTerm object) {
            int hashCode = object.relation.hashCode() + object.accessMethod.hashCode() * 7;
            for(java.util.Map.Entry<Integer, TypedConstant<?>> entry:object.inputConstants.entrySet()) 
                hashCode = hashCode * 8 + entry.getKey().hashCode() * 9 + entry.getValue().hashCode() * 10;
            return hashCode;
        }
    };
    
    protected Object readResolve() {
        return s_interningManager.intern(this);
    }

    public static AccessTerm create(Relation relation, AccessMethod accessMethod) {
        return s_interningManager.intern(new AccessTerm(relation, accessMethod));
    }
    
    public static AccessTerm create(Relation relation, AccessMethod accessMethod, Map<Integer, TypedConstant<?>> inputConstants) {
        return s_interningManager.intern(new AccessTerm(relation, accessMethod, inputConstants));
    }

}