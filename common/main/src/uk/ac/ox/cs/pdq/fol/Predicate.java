package uk.ac.ox.cs.pdq.fol;

import com.google.common.base.Preconditions;


/**
 * A predicate's signature, associate a symbol with an arity.
 *
 * @author Julien Leblay
 */
public class Predicate {

	/**  Predicate name. */
	protected final String name;

	/**  Predicate arity. */
	protected final Integer arity;

	/**  true, if this is the signature for an equality predicate. */
	protected final Boolean isEquality;

	protected Predicate(String name, Integer arity) {
		this(name,arity,false);
	}
	
	protected Predicate(String name, Integer arity, boolean isEquality) {
		Preconditions.checkArgument(name!=null);
		Preconditions.checkArgument(!name.isEmpty());
		Preconditions.checkArgument(arity >= 0);
		this.name = name;
		this.arity = arity;
		this.isEquality = isEquality;
	}

	/**
	 * Gets the name of the predicate.
	 *
	 * @return the name of the predicate.
	 */
	public String getName() {
		return this.name;
	}

	/**
	 * Gets the arity of the predicate.
	 *
	 * @return the arity of the predicate.
	 */
	public int getArity() {
		return this.arity;
	}

	/**
	 * Checks if this is an equality predicate.
	 *
	 * @return true if the signature is of an equality predicate,
	 * false otherwise
	 */
	public boolean isEquality() {
		return this.isEquality;
	}

	@Override
	public String toString() {
		return this.name;
	}
	
    public static Predicate create(String name, Integer arity) {
        return Cache.predicate.retrieve(new Predicate(name, arity));
    }
    
    public static Predicate create(String name, Integer arity, Boolean isEquality) {
        return Cache.predicate.retrieve(new Predicate(name, arity, isEquality));
    }

}
