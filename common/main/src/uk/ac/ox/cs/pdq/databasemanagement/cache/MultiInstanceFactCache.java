package uk.ac.ox.cs.pdq.databasemanagement.cache;

import java.util.ArrayList;
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

	public Collection<Atom> checkExistsInOtherInstances(Collection<Atom> isThisNew, int instanceId) {
		Collection<Atom> newToThisInstance = new ArrayList<>();
		newToThisInstance.addAll(isThisNew);
		for (Integer iId : multiCache.keySet()) {
			if (iId != instanceId) {
				newToThisInstance = multiCache.get(iId).contains(newToThisInstance);
			}
		}
		return newToThisInstance;
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

	/**
	 * Deletes the given facts under the given instanceId, and then checks each
	 * deleted fact if it is appearing in other instances. When a fact does not used
	 * anywhere anymore it will add it to the return list.
	 * 
	 * @param facts
	 *            to delete
	 * @param instanceId
	 *            delete from this instance only.
	 * @return facts that are never used in any instance.
	 */
	public Collection<Atom> deleteFactsAndListUnusedFacts(Collection<Atom> facts, int instanceId) {
		boolean changed = deleteFacts(facts, instanceId);
		Collection<Atom> results = new ArrayList<>();
		if (changed) {
			for (Atom f : facts) {
				boolean found = false;
				for (Integer iId : multiCache.keySet()) {
					if (iId!=instanceId && multiCache.get(iId).containsFact(f)) {
						found = true;
						break;
					}
				}
				if (!found)
					results.add(f);
			}
		}
		return results;
	}

}
