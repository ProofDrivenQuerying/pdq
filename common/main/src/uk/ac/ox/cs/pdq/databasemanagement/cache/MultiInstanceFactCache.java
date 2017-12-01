package uk.ac.ox.cs.pdq.databasemanagement.cache;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import uk.ac.ox.cs.pdq.fol.Atom;

/**
 * Manages many FactCache instances. Used by the VirtualMultiInstance Database
 * manager to store facts.
 * 
 * @author Gabor
 *
 */
public class MultiInstanceFactCache {
	/**
	 * The actual data
	 */
	private Map<Integer, FactCache> multiCache;

	public MultiInstanceFactCache() {
		multiCache = new HashMap<>();
	}

	/**
	 * Stores facts in the cache.
	 */
	public Collection<Atom> addFacts(Collection<Atom> facts, int instanceId) {
		if (!multiCache.containsKey(instanceId)) {
			multiCache.put(instanceId, new FactCache(instanceId));
		}
		return multiCache.get(instanceId).addFacts(facts);
	}

	/**
	 * Retrieve facts from the cache.
	 */
	public Collection<Atom> getFacts(int instanceId) {
		if (!multiCache.containsKey(instanceId)) {
			multiCache.put(instanceId, new FactCache(instanceId));
		}
		return multiCache.get(instanceId).getFacts();
	}

	/**
	 * get all facts from a certain relation in the given instance.
	 * 
	 * @param relationName
	 * @param instanceId
	 * @return
	 */
	public List<Atom> getFactsOfRelation(String relationName, int instanceId) {
		if (!multiCache.containsKey(instanceId)) {
			multiCache.put(instanceId, new FactCache(instanceId));
		}
		return multiCache.get(instanceId).getFactsOfRelation(relationName);
	}

	/**
	 * Delete facts from the cache.
	 */
	public boolean deleteFacts(Collection<Atom> facts, int instanceId) {
		if (!multiCache.containsKey(instanceId)) {
			multiCache.put(instanceId, new FactCache(instanceId));
		}
		return multiCache.get(instanceId).removeFacts(facts);
	}

	public void clearCache(int instanceId) {
		multiCache.get(instanceId).clearCache();
	}

}
