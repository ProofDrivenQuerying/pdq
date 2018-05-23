package uk.ac.ox.cs.pdq.runtime.exec;

import com.google.common.base.Preconditions;

import uk.ac.ox.cs.pdq.algebra.AccessTerm;
import uk.ac.ox.cs.pdq.algebra.CartesianProductTerm;
import uk.ac.ox.cs.pdq.algebra.DependentJoinTerm;
import uk.ac.ox.cs.pdq.algebra.JoinTerm;
import uk.ac.ox.cs.pdq.algebra.Plan;
import uk.ac.ox.cs.pdq.algebra.ProjectionTerm;
import uk.ac.ox.cs.pdq.algebra.RenameTerm;
import uk.ac.ox.cs.pdq.algebra.SelectionTerm;
import uk.ac.ox.cs.pdq.datasources.ExecutableAccessMethod;
import uk.ac.ox.cs.pdq.datasources.accessrepository.AccessRepository;
import uk.ac.ox.cs.pdq.db.AccessMethodDescriptor;
import uk.ac.ox.cs.pdq.runtime.exec.spliterator.Access;
import uk.ac.ox.cs.pdq.runtime.exec.spliterator.CartesianProduct;
import uk.ac.ox.cs.pdq.runtime.exec.spliterator.DependentJoin;
import uk.ac.ox.cs.pdq.runtime.exec.spliterator.ExecutablePlan;
import uk.ac.ox.cs.pdq.runtime.exec.spliterator.Projection;
import uk.ac.ox.cs.pdq.runtime.exec.spliterator.Selection;
import uk.ac.ox.cs.pdq.runtime.exec.spliterator.SymmetricMemoryHashJoin;

public class PlanDecorator {
	private AccessRepository repository;
	
	public PlanDecorator(AccessRepository repository) {
		this.repository = repository;
	}
	
	public ExecutablePlan decorate(Plan plan) throws Exception {
		Preconditions.checkNotNull(plan);
		
		// If the plan is already decorated, do nothing.
		if (plan instanceof ExecutablePlan)
			return (ExecutablePlan) plan;
		if (plan instanceof RenameTerm)
			return this.decorate(plan.getChild(0));
		if (plan instanceof AccessTerm) {
			return new Access(replaceAccess((AccessTerm)plan),this);
		}else if (plan instanceof SelectionTerm)
			return new Selection(plan,this);
		else if (plan instanceof ProjectionTerm)
			return new Projection(plan,this);
		else if (plan instanceof DependentJoinTerm)
			return new DependentJoin(plan,this);
		else if (plan instanceof JoinTerm)
			return new SymmetricMemoryHashJoin(plan,this); // IMP TODO: support other join implementations - how?
		else if (plan instanceof CartesianProductTerm)
			return new CartesianProduct(plan,this); 
		else
			throw new IllegalArgumentException("Unsupported logical plan: " + plan);
	}
	
	private Plan replaceAccess(AccessTerm plan) throws Exception {
		AccessMethodDescriptor amDesc = plan.getAccessMethod();
		if (amDesc instanceof ExecutableAccessMethod) {
			// plan is already executable.
			return plan;
		}
		ExecutableAccessMethod newAccess = repository.getAccess(amDesc.getName());
		if (newAccess==null) {
			throw new Exception("AccessMethod \"" + amDesc.getName()+"\" not found in repository: " + repository);
		}
		AccessTerm newAccessTerm;
		if (plan.getInputConstants() != null) {
			newAccessTerm = AccessTerm.create(plan.getRelation(), newAccess, plan.getInputConstants());
		} else {
			newAccessTerm = AccessTerm.create(plan.getRelation(), newAccess);
		}
		return newAccessTerm;
	}

	public AccessRepository getAccessRepository() {
		return repository;
	}
}
