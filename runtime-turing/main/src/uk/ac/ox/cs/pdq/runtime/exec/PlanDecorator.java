package uk.ac.ox.cs.pdq.runtime.exec;

import com.google.common.base.Preconditions;

import uk.ac.ox.cs.pdq.algebra.AccessTerm;
import uk.ac.ox.cs.pdq.algebra.DependentJoinTerm;
import uk.ac.ox.cs.pdq.algebra.JoinTerm;
import uk.ac.ox.cs.pdq.algebra.Plan;
import uk.ac.ox.cs.pdq.algebra.ProjectionTerm;
import uk.ac.ox.cs.pdq.algebra.SelectionTerm;
import uk.ac.ox.cs.pdq.runtime.exec.spliterator.Access;
import uk.ac.ox.cs.pdq.runtime.exec.spliterator.Projection;
import uk.ac.ox.cs.pdq.runtime.exec.spliterator.DependentJoin;
import uk.ac.ox.cs.pdq.runtime.exec.spliterator.ExecutablePlan;
import uk.ac.ox.cs.pdq.runtime.exec.spliterator.Selection;
import uk.ac.ox.cs.pdq.runtime.exec.spliterator.SymmetricMemoryHashJoin;

public class PlanDecorator {

	public static ExecutablePlan decorate(Plan plan) {
		Preconditions.checkNotNull(plan);
		
		// If the plan is already decorated, do nothing.
		if (plan instanceof ExecutablePlan)
			return (ExecutablePlan) plan;
	
		if (plan instanceof AccessTerm)
			return new Access(plan);
		else if (plan instanceof SelectionTerm)
			return new Selection(plan);
		else if (plan instanceof ProjectionTerm)
			return new Projection(plan);
		else if (plan instanceof DependentJoinTerm)
			return new DependentJoin(plan);
		else if (plan instanceof JoinTerm)
			return new SymmetricMemoryHashJoin(plan); // IMP TODO: support other join implementations - how?
		else
			throw new IllegalArgumentException("Unsupported logical plan: " + plan);
	}
}
