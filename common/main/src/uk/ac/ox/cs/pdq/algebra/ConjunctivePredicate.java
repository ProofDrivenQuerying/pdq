package uk.ac.ox.cs.pdq.algebra;

import org.junit.Assert;

import uk.ac.ox.cs.pdq.InterningManager;


public class ConjunctivePredicate implements Predicate {
	private static final long serialVersionUID = 3482096951862132845L;
	
	protected final SimplePredicate[] predicates;

	protected ConjunctivePredicate(SimplePredicate[] predicates) {
		Assert.assertNotNull(predicates);
		this.predicates = predicates.clone();
	}

	public int getNumberOfConjuncts() {
		return this.predicates.length;
	}

	@Override
	public String toString() {
		StringBuilder result = new StringBuilder();
		String sep = "(";
		if (this.predicates.length > 0) {
			for (Predicate p: this.predicates) {
				result.append(sep).append(p);
				sep = "&";
			}
			result.append(')');
		}
		return result.toString();
	}
	
    protected static final InterningManager<ConjunctivePredicate> s_interningManager = new InterningManager<ConjunctivePredicate>() {
        protected boolean equal(ConjunctivePredicate object1, ConjunctivePredicate object2) {
            if (object1.predicates.length != object2.predicates.length)
                return false;
            for (int index = object1.predicates.length - 1; index >= 0; --index)
                if (!object1.predicates[index].equals(object2.predicates[index]))
                    return false;
            return true;
        }

        protected int getHashCode(ConjunctivePredicate object) {
            int hashCode = 0;
            for (int index = object.predicates.length - 1; index >= 0; --index)
                hashCode = hashCode * 7 + object.predicates[index].hashCode();
            return hashCode;
        }
    };
    
    protected Object readResolve() {
        return s_interningManager.intern(this);
    }

    public static ConjunctivePredicate create(SimplePredicate[] predicates) {
        return s_interningManager.intern(new ConjunctivePredicate(predicates));
    }
    
}
