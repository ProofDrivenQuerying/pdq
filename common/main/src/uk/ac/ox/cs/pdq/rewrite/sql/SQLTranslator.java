package uk.ac.ox.cs.pdq.rewrite.sql;

import org.apache.log4j.Logger;

import uk.ac.ox.cs.pdq.fol.Evaluatable;
import uk.ac.ox.cs.pdq.rewrite.RewriterException;
import uk.ac.ox.cs.pdq.util.Operator;

/**
 * Provide utility function for translating from/to SQL.
 *
 * TODO: This class needs a serious make-over.
 *
 * @author Julien Leblay
 */
public abstract class SQLTranslator {

	/** */
	public static enum SupportedDialect{ SQL92, POSTGRESQL }

	/** The logger */
	public static Logger log = Logger.getLogger(SQLTranslator.class);

	/**
	 * @param dialect SupportedDialect
	 * @return SQLTranslator<S>
	 */
	public static SQLTranslator target(SupportedDialect dialect) {
		switch(dialect) {
		case POSTGRESQL:
			return new PostgresqlTranslator();
		default:
			return new SQL92Translator();
		}
	}

	/**
	 * @param dialect String
	 * @return SQLTranslator<S>
	 */
	public static SQLTranslator target(String dialect) {
		if (dialect == null) {
			return generic();
		}
		return target(SupportedDialect.valueOf(String.valueOf(dialect).toUpperCase()));
	}

	/**
	 * @return SQLTranslator<S>
	 */
	public static SQLTranslator generic() {
		return target(SupportedDialect.SQL92);
	}

	/**
	 * @param q
	 * @return a String representation of a SQL statement for the given query
	 * @throws RewriterException if the statement could not be generated.
	 */
	public abstract String toSQL(Evaluatable q) throws RewriterException;


	/**
	 * @param plan
	 * @return a SQL statement equivalent to the given plan
	 */
	public abstract String toSQLWith(Operator op) throws RewriterException ;


	/**
	 * @param plan Plan
	 * @return a SQL statement equivalent to the given relational expression
	 */
	public abstract String toSQL(Operator op) throws RewriterException ;
}
