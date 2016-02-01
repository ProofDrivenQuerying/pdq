package uk.ac.ox.cs.pdq.db;

import java.util.ArrayList;
import java.util.List;

import uk.ac.ox.cs.pdq.fol.Conjunction;
import uk.ac.ox.cs.pdq.fol.Predicate;
import uk.ac.ox.cs.pdq.fol.Variable;

/**
 * A dependency of the form \delta = \forall x  \sigma(\vec{x}) --> \exists y  \tau(\vec{x}, \vec{y})
 * where \sigma is a single atim and \tau is a conjunction of atoms.
 *
 * @author Efthymia Tsamoura
 * @author Julien Leblay
 */
public class LinearGuarded extends TGD implements GuardedDependency {

	/**
	 *
	 * @param left
	 * 		The left-hand side predicate of the dependency
	 * @param right
	 * 		The right-hand side predicate of the dependency
	 */
	public LinearGuarded(Predicate left, Conjunction<Predicate> right) {
		super(Conjunction.of(left), right);
	}

	/**
	 * Constructs a guarded dependency based on the input key-foreign key
	 * dependency
	 *
	 * @param relation
	 * @param foreignKey One of the foreign keys of this relation
	 */
	public LinearGuarded(Relation relation, ForeignKey foreignKey) {
		this(createLeft(relation), createRight(relation, foreignKey));
	}

	/**
	 * @param relation Relation
	 * @return the left-hand side predicate of a linear guarded dependency for the given relation
	 */
	private static Predicate createLeft(Relation relation) {
		List<Variable> free = new ArrayList<>();
		int index = 0;
		for (int i = 0, l = relation.getArity(); i < l; i++) {
			Variable v = new Variable(Variable.DEFAULT_VARIABLE_PREFIX + (index++));
			free.add(v);
		}
		return new Predicate(relation, free);
	}

	/**
	 *
	 * @param relation
	 * @param foreignKey
	 * @return the right-hand side of a linear guarded dependency for the given relation and foreign key constraint
	 */
	private static Conjunction<Predicate> createRight(Relation relation, ForeignKey foreignKey) {
		List<Variable> free = new ArrayList<>();
		int index = 0;
		for (int i = 0, l = relation.getArity(); i < l; i++) {
			Variable v = new Variable(Variable.DEFAULT_VARIABLE_PREFIX + (index++));
			free.add(v);
		}

		List<Variable> remoteTerms = new ArrayList<>();
		for (int i = 0, l = foreignKey.getForeignRelation().getArity(); i < l; i++) {
			Variable v = new Variable(Variable.DEFAULT_VARIABLE_PREFIX + (index++));
			remoteTerms.add(v);
		}

		Reference[] references = foreignKey.getReferences();
		for (Reference rf:references) {
			int remoteTermIndex = foreignKey.getForeignRelation().getAttributeIndex(rf.getForeignAttributeName());
			int localTermIndex = relation.getAttributeIndex(rf.getLocalAttributeName());
			remoteTerms.set(remoteTermIndex, free.get(localTermIndex));
		}

		return Conjunction.of(new Predicate(foreignKey.getForeignRelation(), remoteTerms));
	}


	/**
	 * @return PredicateFormula
	 * @see uk.ac.ox.cs.pdq.db.GuardedDependency#getGuard()
	 */
	@Override
	public Predicate getGuard() {
		return this.left.iterator().next();
	}
}