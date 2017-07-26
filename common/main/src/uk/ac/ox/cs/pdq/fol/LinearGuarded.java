package uk.ac.ox.cs.pdq.fol;

import org.junit.Assert;

import uk.ac.ox.cs.pdq.InterningManager;
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

	private static final long serialVersionUID = -6527657738786432098L;

	private LinearGuarded(Formula body, Formula head) {
		super(body,head);
		Assert.assertTrue(body.getAtoms().length == 1);
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
		Variable[] free = new Variable[relation.getArity()];
		int index = 0;
		for (int variableIndex = 0; variableIndex < relation.getArity(); variableIndex++) 
			free[variableIndex] = Variable.create(Variable.DEFAULT_VARIABLE_PREFIX + (index++));
		return Atom.create(relation, free);
	}

	/**
	 *
	 * @param relation the relation
	 * @param foreignKey the foreign key
	 * @return the head formula of a linear guarded dependency for the given relation and foreign key constraint
	 */
	private static Formula createHead(Relation relation, ForeignKey foreignKey) {
		Variable[] free = new Variable[relation.getArity()];
		int index = 0;
		for (int variableIndex = 0; variableIndex < relation.getArity(); variableIndex++) 
			free[variableIndex] = Variable.create(Variable.DEFAULT_VARIABLE_PREFIX + (index++));

		Variable[] remoteTerms = new Variable[foreignKey.getForeignRelation().getArity()];
		for (int variableIndex = 0; variableIndex < foreignKey.getForeignRelation().getArity(); variableIndex++) 
			remoteTerms[variableIndex] = Variable.create(Variable.DEFAULT_VARIABLE_PREFIX + (index++));
		
		Reference[] references = foreignKey.getReferences();
		for (Reference rf:references) {
			int remoteTermIndex = foreignKey.getForeignRelation().getAttributePosition(rf.getForeignAttributeName());
			int localTermIndex = relation.getAttributePosition(rf.getLocalAttributeName());
			remoteTerms[remoteTermIndex] = free[localTermIndex];
		}
		return Atom.create(foreignKey.getForeignRelation(), remoteTerms);
	}

	/**
	 * Gets the guard atom.
	 *
	 */
	public Atom getGuard() {
		return this.getBodyAtom(0);
	}
	
	@Override
	public boolean isGuarded() {
		return true;
	}
	
    protected Object readResolve() {
        return s_interningManager.intern(this);
    }
    protected static final InterningManager<LinearGuarded> s_interningManager = new InterningManager<LinearGuarded>() {
        protected boolean equal(LinearGuarded object1, LinearGuarded object2) {
            if (!object1.head.equals(object2.head) || !object1.body.equals(object2.body) || object1.variables.length != object2.variables.length) 
                return false;
            for (int index = object1.variables.length - 1; index >= 0; --index)
                if (!object1.variables[index].equals(object2.variables[index]))
                    return false;
            return true;
        }
        
        protected int getHashCode(LinearGuarded object) {
            int hashCode = object.head.hashCode() + object.body.hashCode() * 7;
            for (int index = object.variables.length - 1; index >= 0; --index)
                hashCode = hashCode * 8 + object.variables[index].hashCode();
            return hashCode;
        }
    };
    
    public static LinearGuarded create(Formula head, Formula body) {
        return s_interningManager.intern(new LinearGuarded(head, body));
    }
}