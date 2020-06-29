// This file is part of PDQ (https://github.com/michaelbenedikt/pdq) which is released under the MIT license.
// See accompanying LICENSE for copyright notice and full details.

package uk.ac.ox.cs.pdq.runtime.exec.spliterator;

import java.util.Iterator;

import uk.ac.ox.cs.pdq.algebra.Plan;
import uk.ac.ox.cs.pdq.db.tuple.Tuple;
import uk.ac.ox.cs.pdq.runtime.exec.PlanDecorator;

/**
 * Base class for executable plans having a single child.
 * @author Tim Hobson
 *
 */
public abstract class UnaryExecutablePlan extends ExecutablePlan {

	protected ExecutablePlan child;

	public UnaryExecutablePlan(Plan plan, PlanDecorator decorator) {
		super(plan,decorator);
	}
	
	@Override
	public void close() {
		this.child.close();
	}

	// Base setInputTuples method simply delegates to the child.
	// Note that we cannot validate the input tuples against the
	// input attributes since inputTuples is an iterator.
	@Override
	public void setInputTuples(Iterator<Tuple> inputTuples) {
		this.child.setInputTuples(inputTuples);
	}
}
