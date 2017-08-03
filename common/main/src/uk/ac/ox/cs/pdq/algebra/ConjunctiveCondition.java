package uk.ac.ox.cs.pdq.algebra;

import org.junit.Assert;

import uk.ac.ox.cs.pdq.InterningManager;


public class ConjunctiveCondition extends Condition {
	private static final long serialVersionUID = 3482096951862132845L;
	
	protected final SimpleCondition[] predicates;

	private ConjunctiveCondition(SimpleCondition[] predicates) {
		Assert.assertNotNull(predicates);
		this.predicates = predicates.clone();
	}
	
	public SimpleCondition[] getSimpleConditions() {
		return this.predicates.clone();
	}

	public int getNumberOfConjuncts() {
		return this.predicates.length;
	}

	@Override
	public String toString() {
		StringBuilder result = new StringBuilder();
		String sep = "(";
		if (this.predicates.length > 0) {
			for (Condition p: this.predicates) {
				result.append(sep).append(p);
				sep = "&";
			}
			result.append(')');
		}
		return result.toString();
	}
	
    protected static final InterningManager<ConjunctiveCondition> s_interningManager = new InterningManager<ConjunctiveCondition>() {
        protected boolean equal(ConjunctiveCondition object1, ConjunctiveCondition object2) {
            if (object1.predicates.length != object2.predicates.length)
                return false;
            for (int index = object1.predicates.length - 1; index >= 0; --index)
                if (!object1.predicates[index].equals(object2.predicates[index]))
                    return false;
            return true;
        }

        protected int getHashCode(ConjunctiveCondition object) {
            int hashCode = 0;
            for (int index = object.predicates.length - 1; index >= 0; --index)
                hashCode = hashCode * 7 + object.predicates[index].hashCode();
            return hashCode;
        }
    };
    
    protected Object readResolve() {
        return s_interningManager.intern(this);
    }

    public static ConjunctiveCondition create(SimpleCondition[] predicates) {
        return s_interningManager.intern(new ConjunctiveCondition(predicates));
    }
    
}
