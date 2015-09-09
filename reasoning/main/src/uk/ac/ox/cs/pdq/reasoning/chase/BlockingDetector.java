package uk.ac.ox.cs.pdq.reasoning.chase;

import java.util.Collection;
import java.util.List;

import org.apache.log4j.Logger;

import com.google.common.collect.Lists;

/**
 * Blocking implementation which reconsiders the status of a bag every time new
 * facts are propagated upwards the tree.
 * A bag B' blocks another bag B if
 *  -in the initial configuration, B' satisfies as many dependencies as B does
 *  -in the next configurations, B' satisfies as many inferred accessible dependencies as B does
 *  plus as many inferred accessible sub-queries of the original query as B does.
 *
 * @author Efthymia Tsamoura
 *
 */
public class BlockingDetector {

	protected static Logger log = Logger.getLogger(BlockingDetector.class);

	/**
	 *	Updates the status of the input bags.
	 * 	This function is called during chasing (either every time we fire a dependency or at specific time intervals).
	 * @param bags
	 * 	 	Input bags.
	 */
	public void doBlocking(Collection<? extends Bag> bags) {
		List<? extends Bag> b = Lists.newArrayList(bags);
		for (int i = 0; i < b.size() - 1; ++i) {
			for (int j = i + 1; j < b.size(); ++j) {
				b.get(i).isBlocked(b.get(j));
			}
		}
	}
}
