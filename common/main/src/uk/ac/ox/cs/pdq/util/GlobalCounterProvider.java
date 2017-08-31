package uk.ac.ox.cs.pdq.util;

import java.util.HashMap;
import java.util.Map;

/** 
 * Provides an auto-incremented number. This can be used for example to generate access method names, or canonical data such as "c1","c2" and so on.
 * The main functionality is that these numbers can be reseted to zero to make sure we can restart test cases or able to do multiple runs with the same initial conditions. 
 * Therefore every static counter should use this counter provider.
 *  
 * @author Gabor
 *
 */
public class GlobalCounterProvider {
	private static Map<String, Integer> counters = new HashMap<>();
	
	/** 
	 * Returns an auto-incrementing number for each key it has been called with. 
	 * The first time it is called with a key it will return zero, each following calls with the same key will return an incrementing value.
	 *  
	 * @param key
	 * @return
	 */
	public static int getNext(String key) {
		Integer current = counters.get(key);
		if (current==null) {
			counters.put(key,0);
			return 0;
		}
		counters.put(key,current+1);
		return current+1;
	}

	/**
	 *  Returns the current value of the counter under the given key. Returns null if the getNext was never called with this key.
	 * @param key
	 * @return
	 */
	public static Integer getCurrent(String key) {
		return counters.get(key);
	}
	
	public static void resetCounters() {
		counters.clear();
	}

}
