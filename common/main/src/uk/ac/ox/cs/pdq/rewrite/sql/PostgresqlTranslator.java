package uk.ac.ox.cs.pdq.rewrite.sql;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.log4j.Logger;

import uk.ac.ox.cs.pdq.algebra.Operator;
import uk.ac.ox.cs.pdq.fol.Term;
import uk.ac.ox.cs.pdq.rewrite.RewriterException;
import uk.ac.ox.cs.pdq.util.TupleType;
import uk.ac.ox.cs.pdq.util.Typed;
import uk.ac.ox.cs.pdq.util.Types;

/**
 * Provide utility function for translating from/to SQL.
 *
 * TODO: This class needs a serious make-over.
 *
 * @author Julien Leblay
 */
public class PostgresqlTranslator extends SQL92Translator {

	/**  The logger. */
	public static Logger log = Logger.getLogger(PostgresqlTranslator.class);

	/**
	 * Instantiates a new postgresql translator.
	 */
	protected PostgresqlTranslator() {}

	@Override
	public String toSQL(Operator op) throws RewriterException {
		return new TreeTranslator().rewrite(op);
	}

	@Override
	public String toSQLWith(Operator op) throws RewriterException {
		return new WithTranslator().rewrite(op);
	}

	/**
	 * Helper class that specializes in translating a logical operator tree
	 * into a tree-like (nested) SQL query.
	 * @author Julien Leblay
	 *
	 */
	public class TreeTranslator extends SQL92Translator.TreeTranslator {

		/**
		 * Make aliases columns.
		 *
		 * @param s the s
		 * @param columns the columns
		 * @param type TupleType
		 * @param renaming Map<Integer,Term>
		 * @return a comma-separated list of terms prefix with the given alias.
		 */
		protected Pair<String, List<String>> makeAliasesColumns(String s, List<String> columns, TupleType type, Map<Integer, Term> renaming) {
			StringBuilder result = new StringBuilder();
			Set<String> done = new LinkedHashSet<>();
			List<String> aliases = new ArrayList<>();
			String sep = "";
			int i = 0;
			for (String colName: columns) {
				result.append(sep);
				if (!colName.contains("'")) {
					result.append(s).append('.');
					result.append(colName);
					if (renaming != null && renaming.containsKey(i)) {
						colName = String.valueOf(renaming.get(i));
					}
					if (done.contains(colName)) {
						colName = s + '_' + colName;
					}
					result.append(" AS ").append(colName);
					aliases.add(colName);
					done.add(colName);
				} else {
					result.append(cast(colName, type.getType(i)));
				}
				sep = ",";
				i++;
			}
			return Pair.of(result.toString(), aliases);
		}

	}

	/**
	 * Format the given value so as to call the proper postgresql type conversion function.
	 *
	 * @param <T> the generic type
	 * @param o Object
	 * @param targetType the target type
	 * @return a string representation a call to the given postgres-specific
	 * target type conversion function onto the given value;
	 */
	public static <T> String cast(Object o, Type targetType) {
		Type sourceType = o.getClass();
		if (o instanceof Typed) {
			sourceType = ((Typed) o).getType();
		}
		if (String.class.equals(targetType)) {
			if (String.class.equals(sourceType)) {
				return "'" + o + "'";
			}
			return "text " + o;
		} else if (Types.isNumeric(targetType)) {
			if (targetType.equals(sourceType)) {
				return String.valueOf(o);
			}
			if (Integer.class.equals(targetType)) {
				return "integer " + o;
			}
			return String.valueOf(o);
		} else {
			throw new UnsupportedOperationException("PostgresqlTranslator cast to type " + targetType + " not yet supported.");
		}
	}
}
