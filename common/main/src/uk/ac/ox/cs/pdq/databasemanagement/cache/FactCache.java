package uk.ac.ox.cs.pdq.databasemanagement.cache;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

import uk.ac.ox.cs.pdq.fol.Atom;

/**
 * Memory storage for a set of facts. The set can be named with an InstanceID.
 * 
 * @author Gabor
 *
 */
public class FactCache {
	/**
	 * Optional name for this set.
	 */
	private String databaseInstanceID;
	/**
	 * The actual data
	 */
	private Collection<Atom> cache;
	/**
	 * Lock object for synchronising access to the data.
	 */
	private Object LOCK = new Object();

	/**
	 * Creates a new cache.
	 * 
	 * @param databaseInstanceID
	 *            - optional name.
	 */
	public FactCache(String databaseInstanceID) {
		this.databaseInstanceID = databaseInstanceID;
		cache = new HashSet<>();
	}

	/**
	 * Stores facts in the cache.
	 * 
	 * @param toAdd
	 */
	public Collection<Atom> addFacts(Collection<Atom> toAdd) {
		Collection<Atom> results = new ArrayList<>();
		synchronized (LOCK) {
			for (Atom a:toAdd) {
				if (cache.add(a)) {
					results.add(a);
				}
			}
		}
		return results;
	}

	/**
	 * Recalls facts from the cache.
	 * 
	 * @return
	 */
	public Collection<Atom> getFacts() {
		ArrayList<Atom> result = new ArrayList<>();
		synchronized (LOCK) {
			result.addAll(cache);
		}
		return result;
	}

	/**
	 * @return optional name of this cache, could be null.
	 */
	public String getDatabaseInstanceID() {
		return databaseInstanceID;
	}

	/**
	 * @param facts
     * @return <tt>true</tt> if this collection changed as a result of the
     *         call
	 */
	public boolean removeFacts(Collection<Atom> facts) {
		synchronized (LOCK) {
			return cache.removeAll(facts);
		}
	}
	
	/**
	 *  Clears the cache.
	 */
	public void clearCache() {
		synchronized (LOCK) {
			cache.clear();
		}
	}
}
