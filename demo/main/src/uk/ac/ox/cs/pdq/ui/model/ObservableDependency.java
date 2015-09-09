package uk.ac.ox.cs.pdq.ui.model;

import uk.ac.ox.cs.pdq.db.Constraint;
import uk.ac.ox.cs.pdq.db.TGD;

public class ObservableDependency extends TGD {

	private static final int MAX_LENGTH = 30;
	
	private Constraint dependency;
	
	public ObservableDependency(TGD dep) {
		super(dep.getLeft(), dep.getRight());
		this.dependency = dep;
	}

	@Override
	public String toString() {
		String result = String.valueOf(this.dependency);
		if (result.length() > MAX_LENGTH) {
			result = result.substring(MAX_LENGTH) + "...";
		}
		return result;
	}
}
