package uk.ac.ox.cs.pdq.ui.model;

import uk.ac.ox.cs.pdq.db.Dependency;
import uk.ac.ox.cs.pdq.db.TGD;

// TODO: Auto-generated Javadoc
/**
 * The Class ObservableDependency.
 */
public class ObservableDependency extends TGD {

	/** The Constant MAX_LENGTH. */
	private static final int MAX_LENGTH = 30;
	
	/** The dependency. */
	private Dependency dependency;
	
	/**
	 * Instantiates a new observable dependency.
	 *
	 * @param dep the dep
	 */
	public ObservableDependency(TGD dep) {
		super(dep.getLeft(), dep.getRight());
		this.dependency = dep;
	}

	/* (non-Javadoc)
	 * @see uk.ac.ox.cs.pdq.db.TGD#toString()
	 */
	@Override
	public String toString() {
		String result = String.valueOf(this.dependency);
		if (result.length() > MAX_LENGTH) {
			result = result.substring(MAX_LENGTH) + "...";
		}
		return result;
	}
}
