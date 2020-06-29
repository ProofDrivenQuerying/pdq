// This file is part of PDQ (https://github.com/ProofDrivenQuerying/pdq) which is released under the MIT license.
// See accompanying LICENSE for copyright notice and full details.

package uk.ac.ox.cs.pdq.runtime.exec.spliterator;

import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.StreamSupport;

import jersey.repackaged.com.google.common.base.Preconditions;
import uk.ac.ox.cs.pdq.algebra.Plan;
import uk.ac.ox.cs.pdq.algebra.SelectionTerm;
import uk.ac.ox.cs.pdq.db.tuple.Tuple;
import uk.ac.ox.cs.pdq.runtime.exec.PlanDecorator;

/**
 * An executable selection plan. 
 * 
 * @author Tim Hobson
 *
 */
public class Selection extends UnaryExecutablePlan {

	private final Predicate<Tuple> filterPredicate;

	public Selection(Plan plan, PlanDecorator decorator) throws Exception {
		super(plan,decorator);
		// Check compatibility with the given Plan instance.
		Preconditions.checkArgument(plan instanceof SelectionTerm);

		// Assign the (decorated) child plan to the child field.
		this.child = decorator.decorate(this.getDecoratedPlan().getChildren()[0]);

		// Assign the filter predicate field, based on the selection condition.
		this.filterPredicate = ((SelectionTerm) this.getDecoratedPlan()).getSelectionCondition().asPredicate();
	}

	@Override
	public Spliterator<Tuple> spliterator() {
		return new SelectionSpliterator(this.child.spliterator());
	}

	public Predicate<Tuple> getFilterPredicate() {
		return this.filterPredicate;
	}

	private class SelectionSpliterator extends UnaryPlanSpliterator {

		public SelectionSpliterator(Spliterator<Tuple> childSpliterator) {
			super(childSpliterator);
		}

		@Override
		public boolean tryAdvance(Consumer<? super Tuple> action) {
			return StreamSupport.stream(this.childSpliterator, false)
					.filter(filterPredicate)
					.spliterator().tryAdvance(action);
		}
	}
}
