package uk.ac.ox.cs.pdq.rewrite.sql;

import org.apache.log4j.Logger;

import uk.ac.ox.cs.pdq.fol.Evaluatable;
import uk.ac.ox.cs.pdq.rewrite.RewriterException;
import uk.ac.ox.cs.pdq.util.Operator;

// TODO: Auto-generated Javadoc
/**
 * Provide utility function for translating from/to SQL.
 *
 * TODO: This class needs a serious make-over.
 *
 * @author Julien Leblay
 */
public abstract class SQLTranslator {

	/**
	 * The Enum SupportedDialect.
	 */
	public static enum SupportedDialect{ 
 /** The SQ l92. */
 SQL92, 
 /** The postgresql. */
 POSTGRESQL }

	/**  The logger. */
	public static Logger log = Logger.getLogger(SQLTranslator.class);

	/**
	 * Target.
	 *
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
	 * Target.
	 *
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
	 * Generic.
	 *
	 * @return SQLTranslator<S>
	 */
	public static SQLTranslator generic() {
		return target(SupportedDialect.SQL92);
	}

	/**
	 * To sql.
	 *
	 * @param q the q
	 * @return a String representation of a SQL statement for the given query
	 * @throws RewriterException if the statement could not be generated.
	 */
	public abstract String toSQL(Evaluatable q) throws RewriterException;


	/**
	 * To sql with.
	 *
	 * @param op the op
	 * @return a SQL statement equivalent to the given plan
	 * @throws RewriterException the rewriter exception
	 */
	public abstract String toSQLWith(Operator op) throws RewriterException ;


	/**
	 * To sql.
	 *
	 * @param op the op
	 * @return a SQL statement equivalent to the given relational expression
	 * @throws RewriterException the rewriter exception
	 */
	public abstract String toSQL(Operator op) throws RewriterException ;
}
