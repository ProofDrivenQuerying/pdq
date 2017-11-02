package uk.ac.ox.cs.pdq.data;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import uk.ac.ox.cs.pdq.fol.ConjunctiveQuery;
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
	private Formula formula;

	protected PhysicalQuery(Formula formula) {
		this.formula = formula;
	}

	public String toString() {
		return "SQLQuery ("+formula+")";
	}

	public static PhysicalQuery create(DatabaseManager manager2, ConjunctiveQuery query) throws NoSuchMethodException, SecurityException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		Class<? extends PhysicalQuery> qclass = manager2.getQueryClass();
		Constructor<? extends PhysicalQuery> constructor = qclass.getConstructor(Formula.class);
		PhysicalQuery q = constructor.newInstance(query.getBody());
		return q;
	}

	public Formula getFormula() {
		return formula;
	}
}
