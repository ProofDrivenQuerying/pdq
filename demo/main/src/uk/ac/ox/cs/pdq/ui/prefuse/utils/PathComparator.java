package uk.ac.ox.cs.pdq.ui.prefuse.utils;

import java.util.Comparator;

public class PathComparator implements Comparator<Path>{

	@Override
	public int compare(Path o1, Path o2) {
		if(o1.getPlan().getCost().greaterThan(o2.getPlan().getCost())) {
			return 1;
		}
		else if(o1.getPlan().getCost().lessThan(o2.getPlan().getCost())) {
			return -1;
		}
		else {
			return 0;
		}
	}
}
