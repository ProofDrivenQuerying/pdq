// This file is part of PDQ (https://github.com/ProofDrivenQuerying/pdq) which is released under the MIT license.
// See accompanying LICENSE for copyright notice and full details.

package uk.ac.ox.cs.pdq.runtime.exec.spliterator;

import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.StreamSupport;

import jersey.repackaged.com.google.common.base.Preconditions;
import uk.ac.ox.cs.pdq.algebra.Plan;
import uk.ac.ox.cs.pdq.algebra.ProjectionTerm;
import uk.ac.ox.cs.pdq.db.tuple.Tuple;
import uk.ac.ox.cs.pdq.runtime.exec.PlanDecorator;

/**
 * An executable projection plan. 
 * 
 * @author Tim Hobson
 *
 */
public class Projection extends UnaryExecutablePlan {

	private final Function<Tuple, Tuple> projectionFunction;

	public Projection(Plan plan, PlanDecorator decorator) throws Exception {
		super(plan, decorator);
		// Check compatibility with the given Plan instance.
		Preconditions.checkArgument(plan instanceof ProjectionTerm);
		
		// Assign the (decorated) child plan to the child field.
		this.child = decorator.decorate(this.decoratedPlan.getChildren()[0]); 
		
		// Assign the projection function (as a closure).
		this.projectionFunction = ExecutablePlan.tupleProjector(plan.getChildren()[0].getOutputAttributes(), 
				((ProjectionTerm) plan).getProjections());
	}
	
	@Override
	public Spliterator<Tuple> spliterator() {
		return new ProjectionSpliterator(this.child.spliterator());
	}

	public Function<Tuple, Tuple> getProjectionFunction() {
		return this.projectionFunction;
	}
	
	private class ProjectionSpliterator extends UnaryPlanSpliterator {

		public ProjectionSpliterator(Spliterator<Tuple> childSpliterator) {
			super(childSpliterator);
		}

		@Override
		public boolean tryAdvance(Consumer<? super Tuple> action) {
			return StreamSupport.stream(this.childSpliterator, false)
					.map(projectionFunction)
					.spliterator().tryAdvance(action);
		}
	}
}
