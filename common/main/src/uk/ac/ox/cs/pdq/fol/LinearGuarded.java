package uk.ac.ox.cs.pdq.fol;

import java.util.ArrayList;
import java.util.List;

import uk.ac.ox.cs.pdq.db.ForeignKey;
import uk.ac.ox.cs.pdq.db.Reference;
import uk.ac.ox.cs.pdq.db.Relation;

/**
 * TOCOMMENT this is not well readable in javadoc we need to find out how to write formulas in javadoc (maybe html?)
 * A dependency of the form \delta = \forall x  \sigma(\vec{x}) --> \exists y  \tau(\vec{x}, \vec{y})
 * where \sigma is a single atim and \tau is a conjunction of atoms.
 *
 * @author Efthymia Tsamoura
 * @author Julien Leblay
 */
public class LinearGuarded extends TGD {

	public LinearGuarded(Formula body, Formula head) {
		super(body,head);
	}
	
	/**
	 * Constructs a guarded dependency based on the input key-foreign key
	 * dependency.
	 *
	 * @param relation the relation
	 * @param foreignKey One of the foreign keys of this relation
	 */
	public LinearGuarded(Relation relation, ForeignKey foreignKey) {
		super(createBody(relation), createHead(relation, foreignKey));
	}

	/**
	 *
	 * @param relation Relation
	 * @return the body formula of a linear guarded dependency for the given relation
	 */
	private static Formula createBody(Relation relation) {
		List<Variable> free = new ArrayList<>();
		int index = 0;
		for (int i = 0, l = relation.getArity(); i < l; i++) {
			Variable v = new Variable(Variable.DEFAULT_VARIABLE_PREFIX + (index++));
			free.add(v);
		}
		return new Atom(relation, free);
	}

	/**
	 *
	 * @param relation the relation
	 * @param foreignKey the foreign key
	 * @return the head formula of a linear guarded dependency for the given relation and foreign key constraint
	 */
	private static Formula createHead(Relation relation, ForeignKey foreignKey) {
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
		return new Atom(foreignKey.getForeignRelation(), remoteTerms);
	}

	/**
	 * Gets the guard atom.
	 *
	 */
	public Atom getGuard() {
		return this.getBody().getAtoms().get(0);
	}
	
	@Override
	public boolean isGuarded() {
		return true;
	}
}