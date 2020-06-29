// This file is part of PDQ (https://github.com/michaelbenedikt/pdq) which is released under the MIT license.
// See accompanying LICENSE for copyright notice and full details.

package uk.ac.ox.cs.pdq.runtime.exec.spliterator;

import java.util.Spliterator;

import uk.ac.ox.cs.pdq.db.tuple.Tuple;

public abstract class BinaryPlanSpliterator implements Spliterator<Tuple> {

	protected Spliterator<Tuple> leftChildSpliterator;
	protected Spliterator<Tuple> rightChildSpliterator;

	public BinaryPlanSpliterator(Spliterator<Tuple> leftChildSpliterator, 
			Spliterator<Tuple> rightChildSpliterator) {
		this.leftChildSpliterator = leftChildSpliterator;
		this.rightChildSpliterator = rightChildSpliterator;
	}

	// Default size estimation for a binary executable plan.
	@Override
	public long estimateSize() {
		return this.leftChildSpliterator.estimateSize() * this.rightChildSpliterator.estimateSize();
	}

	// TODO: This is untested (and merely educated guesswork)!
	@Override
	public int characteristics() {
		return this.leftChildSpliterator.characteristics() & this.rightChildSpliterator.characteristics();
	}
}
