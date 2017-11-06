package uk.ac.ox.cs.pdq.data;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import com.google.common.base.Preconditions;

import uk.ac.ox.cs.pdq.data.memory.MemoryQuery;
import uk.ac.ox.cs.pdq.data.sql.DatabaseException;
import uk.ac.ox.cs.pdq.data.sql.SQLQuery;
import uk.ac.ox.cs.pdq.data.sql.SqlDatabaseInstance;
import uk.ac.ox.cs.pdq.fol.ConjunctiveQuery;
import uk.ac.ox.cs.pdq.fol.Dependency;
import uk.ac.ox.cs.pdq.fol.Formula;

/**
 * This is a query that was created from formulas such as ConjunctiveQuery or
 * a dependency activeness check, but converted to the language of a physical database such as
 * SQL.
 * 
 * @author Gabor
 *
 */
public class PhysicalQuery extends PhysicalDatabaseCommand {
	protected Formula formula;

	/** The formula must be conjunctiveQuery or Dependency
	 * @param formula
	 */
	protected PhysicalQuery(Formula formula) {
		Preconditions.checkArgument(formula instanceof ConjunctiveQuery || formula instanceof Dependency);
		this.formula = formula;
	}

	public String toString() {
		return "PhysicalQuery ("+formula+")";
	}

	public static PhysicalQuery create(DatabaseManager manager, ConjunctiveQuery query) throws DatabaseException {
		try {
			if (manager.getQueryClass() == SQLQuery.class) {
				SQLQuery q = SQLQuery.createSQLQuery(query, (SqlDatabaseInstance)manager.databaseInstance);
				return q;
			}
			if (manager.getQueryClass() == MemoryQuery.class) {
				MemoryQuery q = new MemoryQuery(query);
				return q;
			} 
			
			Class<? extends PhysicalQuery> qclass = manager.getQueryClass();
			Constructor<? extends PhysicalQuery> constructor;
			constructor = qclass.getConstructor(Formula.class);
			PhysicalQuery q = constructor.newInstance(query);
			return q;
		} catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			throw new DatabaseException("Failed to create query from : "+ query + " using manager: "+ manager + ".",e);
		}
	}

	public Formula getFormula() {
		return formula;
	}
}
