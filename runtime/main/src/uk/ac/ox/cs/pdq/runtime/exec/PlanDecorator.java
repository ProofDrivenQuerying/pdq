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
import uk.ac.ox.cs.pdq.db.Schema;
import uk.ac.ox.cs.pdq.runtime.exec.spliterator.Access;
import uk.ac.ox.cs.pdq.runtime.exec.spliterator.CartesianProduct;
import uk.ac.ox.cs.pdq.runtime.exec.spliterator.DependentJoin;
import uk.ac.ox.cs.pdq.runtime.exec.spliterator.ExecutablePlan;
import uk.ac.ox.cs.pdq.runtime.exec.spliterator.Projection;
import uk.ac.ox.cs.pdq.runtime.exec.spliterator.Selection;
import uk.ac.ox.cs.pdq.runtime.exec.spliterator.SymmetricMemoryHashJoin;

/**
 * The decorator converts RelationalTerm objects to ExecutablePlan objects. The
 * decoration is done recursively by passing the decorator to the constructor of
 * the executablePlan to make sure it will use the same parameters/access
 * repository to decorate the children as well.
 * 
 * @author gabor
 * @author ATI project (Efi, Tim etc)
 */
public class PlanDecorator {
	/**
	 * Known list of Executable accesses
	 */
	private AccessRepository repository;
	/**
	 * Every executable access needs to be "updated" with the corresponding schema relation.
	 */
	private Schema schema;

	/**
	 * Currently the only extra information needed for the decoration is the
	 * accessRepository that holds the executable access methods. The decoration
	 * process will replace the access method descriptors to an executable one from
	 * the repository.
	 * 
	 * @param repository
	 */
	public PlanDecorator(AccessRepository repository) {
		this.repository = repository;
	}

	public PlanDecorator(AccessRepository repository, Schema schema) {
		this.schema = schema;
		this.repository = repository;
	}

	/**
	 * The decoration itself. The input plan can be an executablePlan (in that case
	 * we do nothing) or a Relationalterm. Relationalterms will be enwrapped in the
	 * corresponding executable objects.
	 * 
	 * This function is a false recursive function. The implementation of each
	 * executable plan's constructor have to make sure that this decorator function
	 * is called on each children as well.
	 * 
	 * The access method descriptors will be replaced with an executable access
	 * method from the repository.
	 * 
	 * @param plan
	 * @return
	 * @throws Exception
	 */
	public ExecutablePlan decorate(Plan plan) throws Exception {
		Preconditions.checkNotNull(plan);

		// If the plan is already decorated, do nothing.
		if (plan instanceof ExecutablePlan)
			return (ExecutablePlan) plan;
		if (plan instanceof RenameTerm) {
			AccessTerm newAccess = replaceAccess((AccessTerm) plan.getChild(0));
			RenameTerm newRename = RenameTerm.create(((RenameTerm) plan).getRenamings(),newAccess);
			return new Access(newRename, this);
		} if (plan instanceof AccessTerm) {
			return new Access(replaceAccess((AccessTerm) plan), this);
		} else if (plan instanceof SelectionTerm)
			return new Selection(plan, this);
		else if (plan instanceof ProjectionTerm)
			return new Projection(plan, this);
		else if (plan instanceof DependentJoinTerm)
			return new DependentJoin(plan, this);
		else if (plan instanceof JoinTerm)
			return new SymmetricMemoryHashJoin(plan, this); // IMP TODO: support other join implementations - how?
		else if (plan instanceof CartesianProductTerm)
			return new CartesianProduct(plan, this);
		else
			throw new IllegalArgumentException("Unsupported logical plan: " + plan);
	}

	/**
	 * Creates a new AccessTerm that is identical to the input accessTerm, but the
	 * access method descriptor is replaced with an executable version from the
	 * repository.
	 * 
	 * The name of the access method descriptor is used to identify the executable
	 * version to use.
	 * 
	 * @param plan
	 * @return
	 * @throws Exception
	 */
	private AccessTerm replaceAccess(AccessTerm plan) throws Exception {
		AccessMethodDescriptor amDesc = plan.getAccessMethod();
		if (amDesc instanceof ExecutableAccessMethod) {
			// plan is already executable.
			return plan;
		}
		ExecutableAccessMethod newAccess = repository.getAccess(amDesc.getName());
		if (newAccess == null) {
			throw new Exception("AccessMethod \"" + amDesc.getName() + "\" not found in repository: " + repository);
		}
		newAccess.updateRelation(schema.getRelation(newAccess.getRelation().getName()));
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
