package uk.ac.ox.cs.pdq.rewrite.sql;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.log4j.Logger;

import uk.ac.ox.cs.pdq.algebra.Access;
import uk.ac.ox.cs.pdq.algebra.CrossProduct;
import uk.ac.ox.cs.pdq.algebra.DependentAccess;
import uk.ac.ox.cs.pdq.algebra.Join;
import uk.ac.ox.cs.pdq.algebra.NaryOperator;
import uk.ac.ox.cs.pdq.algebra.Projection;
import uk.ac.ox.cs.pdq.algebra.RelationalOperator;
import uk.ac.ox.cs.pdq.algebra.Scan;
import uk.ac.ox.cs.pdq.algebra.Selection;
import uk.ac.ox.cs.pdq.algebra.StaticInput;
import uk.ac.ox.cs.pdq.algebra.UnaryOperator;
import uk.ac.ox.cs.pdq.algebra.predicates.AttributeEqualityPredicate;
import uk.ac.ox.cs.pdq.algebra.predicates.ConjunctivePredicate;
import uk.ac.ox.cs.pdq.algebra.predicates.ConstantEqualityPredicate;
import uk.ac.ox.cs.pdq.db.AccessMethod;
import uk.ac.ox.cs.pdq.db.AccessMethod.Types;
import uk.ac.ox.cs.pdq.db.Attribute;
import uk.ac.ox.cs.pdq.db.Relation;
import uk.ac.ox.cs.pdq.db.TypedConstant;
import uk.ac.ox.cs.pdq.fol.Conjunction;
import uk.ac.ox.cs.pdq.fol.Atom;
import uk.ac.ox.cs.pdq.fol.ConjunctiveQuery;
import uk.ac.ox.cs.pdq.fol.EGD;
import uk.ac.ox.cs.pdq.fol.Formula;
import uk.ac.ox.cs.pdq.fol.Predicate;
import uk.ac.ox.cs.pdq.fol.TGD;
import uk.ac.ox.cs.pdq.fol.Term;
import uk.ac.ox.cs.pdq.fol.Variable;
import uk.ac.ox.cs.pdq.plan.AccessOperator;
import uk.ac.ox.cs.pdq.plan.SubPlanAlias;
import uk.ac.ox.cs.pdq.rewrite.Rewriter;
import uk.ac.ox.cs.pdq.rewrite.RewriterException;
import uk.ac.ox.cs.pdq.rewrite.sql.SQL92Translator.Statement.Column;
import uk.ac.ox.cs.pdq.rewrite.sql.SQL92Translator.Statement.Column.AttributeColumn;
import uk.ac.ox.cs.pdq.util.Operator;
import uk.ac.ox.cs.pdq.util.Triple;
import uk.ac.ox.cs.pdq.util.Tuple;
import uk.ac.ox.cs.pdq.util.TupleType;
import uk.ac.ox.cs.pdq.util.Utility;

import com.google.common.base.Preconditions;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

/**
 * Provide utility function for translating from/to SQL.
 *
 * @author Julien Leblay
 */
public class SQL92Translator extends SQLTranslator {

	/**  The logger. */
	public static Logger log = Logger.getLogger(SQL92Translator.class);


	/** The Constant RELATION_ALIAS_PREFIX. */
	private static final String RELATION_ALIAS_PREFIX = "r";
	
	/** The Constant COLUMN_ALIAS_PREFIX. */
	private static final String COLUMN_ALIAS_PREFIX = "a";

	/**  Counter for generated SQL aliases. */
	private static int aliasCounter = 0;

	/**
	 * Instantiates a new SQl92 translator.
	 */
	protected SQL92Translator() {}

	/**
	 *
	 * @param q the q
	 * @return a String representation of a SQL statement for the given query * @throws ConversionException if the statement could not be generated.
	 * @throws RewriterException the rewriter exception
	 */
	@Override
	public String toSQL(Formula q) throws RewriterException {
		Preconditions.checkArgument(q != null);
		if (!(q instanceof TGD) || !(q instanceof EGD) || !(q instanceof ConjunctiveQuery)) {
			throw new UnsupportedOperationException("SQLTranslator does not yet supports queries other than conjunctive (SPJ) queries.");
		}

		StringBuilder result = new StringBuilder();
		BiMap<Atom, String> aliases = null;
		List<Term> projected = Lists.newArrayList();
		Map<Term, List<Triple<Relation, String, Integer>>> termMap = null;
		if(q instanceof TGD) {
			aliases = HashBiMap.create(makeAliases((Conjunction) ((TGD)q).getBody()));
			termMap = mapTerms((Conjunction) ((TGD)q).getBody(), aliases);
			projected.addAll(((TGD)q).getFreeVariables());
		}
		else if(q instanceof EGD) {
			aliases = HashBiMap.create(makeAliases((Conjunction) ((EGD)q).getBody()));
			termMap = mapTerms((Conjunction) ((TGD)q).getBody(), aliases);
			projected.addAll(((EGD)q).getFreeVariables());
		}
		else if(q instanceof ConjunctiveQuery) {
			aliases = HashBiMap.create(makeAliases((Conjunction) ((ConjunctiveQuery)q).getChildren().get(0)));
			termMap = mapTerms((Conjunction) ((ConjunctiveQuery)q).getChildren().get(0), aliases);
			projected.addAll(((ConjunctiveQuery)q).getFreeVariables());
		}
				
		result.append("SELECT ");
		String sep = "";
		for (Term t: projected) {
			List<Triple<Relation, String, Integer>> positions = termMap.get(t);
			if (positions.isEmpty()) {
				throw new RewriterException("Not expected boolean query in SQL translation");
			}
			Triple<Relation, String, Integer> position = positions.get(0);
			result.append(sep).append(position.getSecond());
			result.append('.').append(position.getFirst().getAttribute(position.getThird()));
			sep = ", ";
		}
		if (projected.isEmpty()) {
			result.append("true");
		}

		result.append(" FROM ");
		sep = "";
		BiMap<String, Atom> invAlias = aliases.inverse();
		for (String alias: invAlias.keySet()) {
			result.append(sep).append(invAlias.get(alias).getPredicate().getName());
			result.append(" AS ").append(alias);
			sep = ", ";
		}

		sep = " WHERE ";
		for (Term t: termMap.keySet()) {
			List<Triple<Relation, String, Integer>> positions = termMap.get(t);
			if (t instanceof Variable && positions.size() > 1) {
				for (int i = 1, l = positions.size(); i < l; i++) {
					result.append(sep).append(positions.get(i - 1).getSecond());
					result.append('.').append(positions.get(i - 1).getFirst()
							.getAttribute(positions.get(i - 1).getThird()));
					result.append('=').append(positions.get(i).getSecond());
					result.append('.').append(positions.get(i).getFirst()
							.getAttribute(positions.get(i).getThird()));
					sep = " AND ";
				}
			} else if (t instanceof TypedConstant) {
				for (int i = 0, l = positions.size(); i < l; i++) {
					Attribute attribute = positions.get(i).getFirst()
							.getAttribute(positions.get(i).getThird());
					result.append(sep).append(positions.get(i).getSecond());
					result.append('.').append(attribute);
					result.append('=').append(Utility.format(t, attribute.getType()));
					sep = " AND ";
				}
			}
		}
		if (projected.isEmpty()) {
			result.append(" LIMIT 1 ");
		}
		log.debug(q + " toSQL: " + result);
		return result.toString();
	}

	/**
	 * TOCOMMENT ???
	 * 
	 * Make aliases.
	 *
	 * @param q the q
	 * @return a map from each predication in the input query to an alias.
	 * @throws RewriterException if any atomic formula in the query is not
	 * a predicate formula.
	 */
	private static Map<Atom, String> makeAliases(Conjunction q) throws RewriterException {
		Map<Atom, String> result = new LinkedHashMap<>();
		int alias = 0;
		for (Atom a:q.getAtoms()) {
			result.put(a, RELATION_ALIAS_PREFIX + (alias++));
		}
		return result;
	}
	
	/**
	 * TOCOMMENT ???
	 * 
	 * Map terms.
	 *
	 * @param q the q
	 * @param aliases the aliases
	 * @return a map from each term of the input query to a triple
	 * featuring the corresponding relation, its alias in the final query and
	 * the index of the term with the relation attributes.
	 * @throws RewriterException if a problem occurred during the creation of the map.
	 */
	private static Map<Term, List<Triple<Relation, String, Integer>>> mapTerms(
			Conjunction q, Map<Atom, String> aliases)
					throws RewriterException {
		Map<Term, List<Triple<Relation, String, Integer>>> result = new LinkedHashMap<>();
		for (Atom a: q.getAtoms()) {//getbody
			Atom f = a;
			Predicate sig = f.getPredicate();
			if (sig instanceof Relation) {
				int i = 0;
				for (Term t: f.getTerms()) {
					List<Triple<Relation, String, Integer>> positions = result.get(t);
					if (positions == null) {
						positions = new ArrayList<>();
						result.put(t, positions);
					}
					positions.add(new Triple<>((Relation) f.getPredicate(),
							aliases.get(f), i));
					i++;
				}
			} else {
				throw new RewriterException();
			}
		}
		return result;
	}

	/**
	 * Translates the operator to SQL.
	 *
	 * @param op the op
	 * @return a SQL statement equivalent to the given plan
	 * @throws RewriterException the rewriter exception
	 */
	@Override
	public String toSQL(Operator op) throws RewriterException {
		return new TreeTranslator().rewrite(op);
	}

	/**
	 * TOCOMMENT Translates to SQL-with: what is that?
	 *
	 * @param op the op
	 * @return a SQL statement equivalent to the given plan
	 * @throws RewriterException the rewriter exception
	 */
	@Override
	public String toSQLWith(Operator op) throws RewriterException {
		return new WithTranslator().rewrite(op);
	}

	private static List<String> toString(List<? extends Term> terms){
		List<String> result = new ArrayList<>();
		for (Term t: terms) {
			result.add(String.valueOf(t));
		}
		return result;
	}

	/**
	 * Gets the positions of TOCOMMENT ???.
	 *
	 * @param projected the projected
	 * @param fullList the full list
	 * @param renaming Map<Integer,Term>
	 * @return a list of position of the projected terms in fullList.
	 */
	private static Pair<List<Term>, List<Integer>> getPositions(List<? extends Term> projected, List<? extends Term> fullList, Map<Integer, Term> renaming) {
		List<Term> terms = new ArrayList<>(projected.size());
		List<Integer> positions = new ArrayList<>(projected.size());
		for (Term t: projected) {
			if (t.isVariable() || t.isUntypedConstant())  {
				Integer i = fullList.indexOf(t);
				if (i < 0) {
					throw new IllegalStateException("Could not find projected '"
							+ t + "' in " + fullList);
				}
				positions.add(i);
				if (renaming.containsKey(i)) {
					terms.add(renaming.get(i));
				} else {
					terms.add(t);
				}
			} else {
				positions.add(-1);
				terms.add(t);
			}
		}
		return Pair.of(terms, positions);
	}

	/**
	 * Gets the positions of TOCOMMENT ???.
	 *
	 * @param projected the projected
	 * @param fullList the full list
	 * @return a list of position of the projected terms in fullList.
	 */
	private static Pair<List<Term>, List<Integer>> getPositions(List<? extends Term> projected, List<? extends Term> fullList) {
		return getPositions(projected, fullList, new LinkedHashMap<Integer, Term>());
	}

	/**
	 * TOCOMMENT ???.
	 * Project by positions.
	 *
	 * @param columns the columns
	 * @param positions the positions
	 * @return a list that is a projection of columns on the given positions.
	 */
	private static List<String> projectByPositions(List<String> columns, Pair<List<Term>, List<Integer>> positions) {
		List<String> result = new ArrayList<>();
		int i = 0;
		for (Term t: positions.getLeft()) {
			if (t.isVariable() || t.isUntypedConstant()) {
				result.add(columns.get(positions.getRight().get(i)));
			} else {
				result.add("'" + t + "'");
			}
			i++;
		}
		return result;
	}

	/**
	 * Helper class that specializes in translating a logical operator tree
	 * into a tree-like (nested) SQL query.
	 *
	 * @author Julien Leblay
	 */
	public class TreeTranslator implements Rewriter<Operator, String> {

		/**  List of column aliases. */
		private Map<String, String> aliases = new LinkedHashMap<>();

		/**  List of column that have being re-aliased due to duplicate names. */
		private Map<String, String> reAliased = new LinkedHashMap<>();

		/**
		 * To sql.
		 *
		 * @param logOp the log op
		 * @return a SQL statement equivalent to the given relational expression
		 */
		private Pair<String, List<String>> toSQL(Operator logOp) {
			StringBuilder result = new StringBuilder();
			List<String> columnNames = new ArrayList<>();
			if (logOp instanceof Scan || logOp instanceof DependentAccess) {
				Relation relation = ((AccessOperator) logOp).getRelation();
				result.append("SELECT ");
				String sep = "";
				for (Term t: ((AccessOperator) logOp).getColumns()) {
					//					String alias = getColumnAlias(t, relAlias);
					result.append(sep).append(t);
					columnNames.add(String.valueOf(t));
					sep = ",";
				}
				result.append(" FROM ").append(relation.getName());
				return Pair.of(result.toString(), columnNames);

			} else if (logOp instanceof UnaryOperator) {
				if (logOp instanceof Access) {
					return this.accessToSQL((Access) logOp);
				} else if (logOp instanceof Projection) {
					return this.projectionToSQL((Projection) logOp);
				} else if (logOp instanceof Selection) {
					return this.selectionToSQL((Selection) logOp);
				} else {
					throw new UnsupportedOperationException(logOp + " translation to SQL, currently not supported.");
				}

			} else if (logOp instanceof Join) {
				return this.joinToSQL((Join) logOp);

			} else if (logOp instanceof CrossProduct) {
				return this.productToSQL((CrossProduct) logOp);

			} else if (logOp instanceof SubPlanAlias) {
				return this.toSQL((RelationalOperator) ((SubPlanAlias) logOp).getPlan().getOperator());

			} else {
				throw new UnsupportedOperationException(logOp + " translation to SQL, currently not supported.");
			}
		}

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
				} else {
					result.append(Utility.format(colName, type.getType(i)));
				}
				if (renaming != null && renaming.containsKey(i)) {
					colName = String.valueOf(renaming.get(i));
				}
				if (done.contains(colName)) {
					colName = s + '_' + colName;
				}
				result.append(" AS ").append(colName);
				aliases.add(colName);
				done.add(colName);
				sep = ",";
				i++;
			}
			return Pair.of(result.toString(), aliases);
		}

		/**
		 * Access to sql.
		 *
		 * @param access the access
		 * @return the SQL statement for an access command
		 */
		private Pair<String, List<String>> accessToSQL(Access access) {
			StringBuilder result = new StringBuilder();

			RelationalOperator child = access.getChild();

			Relation relation = access.getRelation();
			AccessMethod binding = access.getAccessMethod();
			String childAlias = RELATION_ALIAS_PREFIX + (SQL92Translator.aliasCounter++);
			Pair<String, List<String>> columns = this.makeAliasesColumns(childAlias, SQL92Translator.toString(access.getColumns()), access.getType());
			result.append("SELECT ").append(columns.getLeft())
			.append(" FROM ").append(relation.getName())
			.append(" AS ").append(childAlias);
			if (binding.getType() != Types.FREE
					&& binding.getInputs() != null
					&& !binding.getInputs().isEmpty()) {
				String sep = " WHERE (";
				for (Integer i: binding.getInputs()) {
					result.append(sep).append(relation.getAttribute(i - 1));
					sep = ",";
				}
				result.append(") IN (");
			}

			if (child instanceof StaticInput) {
				String sep = "";
				for (Tuple t: ((StaticInput) child).getTuples()) {
					TupleType type = t.getType();
					result.append(sep);
					if (t.size() > 0) {
						String sep2 = "(";
						for (int i = 0, l = t.size(); i < l; i++) {

							result.append(sep2).append(Utility.format(t.getValue(i), type.getType(i)));
							sep2 = ",";
						}
						result.append(')');
					}
					sep = "),(";
				}

			} else {
				Pair<String, List<String>> subQuery = this.toSQL(child);
				result.append(subQuery.getLeft());
			}
			result.append(')');
			return Pair.of(result.toString(), columns.getRight());
		}

		/**
		 * Projection to sql.
		 *
		 * @param proj the proj
		 * @return the SQL statement for an projection command
		 */
		private Pair<String, List<String>> projectionToSQL(Projection proj) {
			StringBuilder result = new StringBuilder();

			RelationalOperator child = proj.getChild();
			String childAlias = RELATION_ALIAS_PREFIX + (SQL92Translator.aliasCounter++);
			Pair<String, List<String>> subQuery = this.toSQL(child);

			Pair<List<Term>, List<Integer>> positions = getPositions(proj.getProjected(), child.getColumns());
			Pair<String, List<String>> columns = this.makeAliasesColumns(childAlias, projectByPositions(subQuery.getRight(), positions), proj.getType(), proj.getRenaming());
			result.append("SELECT ").append(columns.getLeft());
			result.append(" FROM (").append(subQuery.getLeft())
			.append(") AS ").append(childAlias);
			return Pair.of(result.toString(), columns.getRight());
		}

		/**
		 * Selection to sql.
		 *
		 * @param select Selection
		 * @return the SQL statement for an selection command
		 */
		private Pair<String, List<String>> selectionToSQL(Selection select) {
			StringBuilder result = new StringBuilder();

			RelationalOperator child = select.getChild();
			String childAlias = RELATION_ALIAS_PREFIX + (SQL92Translator.aliasCounter++);
			Pair<String, List<String>> subQuery = this.toSQL(child);

			result.append("SELECT ").append(this.makeAliasesColumns(childAlias, subQuery.getRight(), select.getType()).getLeft());
			result.append(" FROM (").append(subQuery.getLeft()).append(") AS ").append(childAlias);
			uk.ac.ox.cs.pdq.algebra.predicates.Predicate pred = select.getPredicate();
			if (pred != null) {
				String where = this.predicateToSQL(pred, subQuery.getRight(),
						child.getType(), childAlias);
				if (where != null && !where.trim().isEmpty()) {
					result.append(" WHERE ").append(where);
				}
			}
			return Pair.of(result.toString(), subQuery.getRight());
		}

		/**
		 * Join to sql.
		 *
		 * @param join the join
		 * @return the SQL statement for an join command
		 */
		private Pair<String, List<String>> joinToSQL(Join join) {
			StringBuilder result = new StringBuilder();

			List<RelationalOperator> children = join.getChildren();
			String lAlias = RELATION_ALIAS_PREFIX + (SQL92Translator.aliasCounter++);
			String rAlias = RELATION_ALIAS_PREFIX + (SQL92Translator.aliasCounter++);
			Pair<String, List<String>> leftSub = this.toSQL(children.get(0));
			Pair<String, List<String>> rightSub = this.toSQL(children.get(1));

			Pair<String, List<String>> leftCols = this.makeAliasesColumns(lAlias, leftSub.getRight(), children.get(0).getType());
			Pair<String, List<String>> rightCols = this.makeAliasesColumns(rAlias, leftSub.getRight(), rightSub.getRight());
			result.append("SELECT ").append(leftCols.getLeft())
				.append(',').append(rightCols.getLeft())
				.append(" FROM (").append(leftSub.getLeft()).append(") AS ")
				.append(lAlias).append(", (").append(rightSub.getLeft())
				.append(") AS ").append(rAlias);

			uk.ac.ox.cs.pdq.algebra.predicates.Predicate pred = join.getPredicate();
			if (pred != null) {
				String whereClause = this.predicateToSQL(pred, leftSub.getRight(), rightSub.getRight(), lAlias, rAlias);
				if (!whereClause.trim().isEmpty()) {
					result.append(" WHERE ").append(whereClause);
				}
			}

			List<String> columns = new ArrayList<>();
			columns.addAll(leftCols.getRight());
			columns.addAll(rightCols.getRight());
			return Pair.of(result.toString(), columns);
		}

		/**
		 * Product to sql.
		 *
		 * @param product the product
		 * @return the SQL statement for an Cartesian product command
		 */
		private Pair<String, List<String>> productToSQL(CrossProduct product) {
			StringBuilder result = new StringBuilder();

			List<RelationalOperator> children = product.getChildren();
			String lAlias = RELATION_ALIAS_PREFIX + (SQL92Translator.aliasCounter++);
			String rAlias = RELATION_ALIAS_PREFIX + (SQL92Translator.aliasCounter++);
			Pair<String, List<String>> leftSub = this.toSQL(children.get(0));
			Pair<String, List<String>> rightSub = this.toSQL(children.get(1));

			Pair<String, List<String>> leftCols = this.makeAliasesColumns(lAlias, leftSub.getRight(), children.get(0).getType());
			Pair<String, List<String>> rightCols = this.makeAliasesColumns(rAlias, leftSub.getRight(), rightSub.getRight());
			result.append("SELECT ").append(leftCols.getLeft())
			.append(',').append(rightCols.getLeft())
			.append(" FROM (").append(leftSub.getLeft()).append(") AS ")
			.append(lAlias).append(", (").append(rightSub.getLeft())
			.append(") AS ").append(rAlias);

			List<String> columns = new ArrayList<>();
			columns.addAll(leftCols.getRight());
			columns.addAll(rightCols.getRight());
			return Pair.of(result.toString(), columns);
		}

		/**
		 * Atom to sql.
		 *
		 * @param uk.ac.ox.cs.pdq.algebra.predicates.Predicate the predicate
		 * @param columns the columns
		 * @param type the type
		 * @param relAlias String
		 * @return the SQL clause for the given predicate (currently only conjunctive predicates are supported).
		 */
		private String predicateToSQL(uk.ac.ox.cs.pdq.algebra.predicates.Predicate  pred,
				List<String> columns, TupleType type, String relAlias) {
			StringBuilder result = new StringBuilder();
			if (pred instanceof ConjunctivePredicate) {
				String sep = " ";
				for (uk.ac.ox.cs.pdq.algebra.predicates.Predicate subPred: ((ConjunctivePredicate<uk.ac.ox.cs.pdq.algebra.predicates.Predicate>) pred)) {
					result.append(sep).append(this.predicateToSQL(subPred, columns, type, relAlias));
					sep = " AND ";
				}
			} else if (pred instanceof AttributeEqualityPredicate) {
				AttributeEqualityPredicate p = (AttributeEqualityPredicate) pred;
				if (this.reAliased.containsKey(columns.get(p.getPosition()))) {
					result.append(this.reAliased.get(columns.get(p.getPosition())));
				} else {
					result.append(relAlias).append('.').append(columns.get(p.getPosition()));
				}
				result.append('=');
				if (this.reAliased.containsKey(columns.get(p.getOther()))) {
					result.append(this.reAliased.get(columns.get(p.getOther())));
				} else {
					result.append(relAlias).append('.').append(columns.get(p.getOther()));
				}
			} else if (pred instanceof ConstantEqualityPredicate) {
				ConstantEqualityPredicate p = (ConstantEqualityPredicate) pred;
				if (this.reAliased.containsKey(columns.get(p.getPosition()))) {
					result.append(this.reAliased.get(columns.get(p.getPosition())));
				} else {
					result.append(relAlias).append('.').append(columns.get(p.getPosition()));
				}
				result.append('=');
				result.append(Utility.format(p.getValue(), type.getType(p.getPosition())));
			}
			return result.toString();
		}

		/**
		 * Atom to sql.
		 *
		 * @param predicate the predicate
		 * @param lColumns the l columns
		 * @param rColumns the r columns
		 * @param lAlias the l alias
		 * @param rAlias the r alias
		 * @return the SQL clause for the given predicate referring the relations
		 * aliases as lAlias (left) and rAlias (right).
		 * Currently only conjunctive predicates are supported.
		 */
		private String predicateToSQL(uk.ac.ox.cs.pdq.algebra.predicates.Predicate  pred,
				List<String> lColumns, List<String> rColumns,
				String lAlias, String rAlias) {
			StringBuilder result = new StringBuilder();
			if (pred instanceof ConjunctivePredicate) {
				String sep = " ";
				for (uk.ac.ox.cs.pdq.algebra.predicates.Predicate subPred: ((ConjunctivePredicate<uk.ac.ox.cs.pdq.algebra.predicates.Predicate>) pred)) {
					result.append(sep).append(
							this.predicateToSQL(subPred,
									lColumns, rColumns,
									lAlias, rAlias));
					sep = " AND ";
				}
			} else if (pred instanceof AttributeEqualityPredicate) {
				AttributeEqualityPredicate p = (AttributeEqualityPredicate) pred;
				result.append(lAlias).append('.').append(lColumns.get(p.getPosition()));
				result.append('=');
				result.append(rAlias).append('.').append(rColumns.get(p.getOther() - lColumns.size()));
			}
			return result.toString();
		}

		/**
		 * Gets the column alias.
		 *
		 * @param t the t
		 * @return a fresh column alias for the given column, and stores the
		 * relationship in an instance map.
		 */
		private String getColumnAlias(String t) {
			String a = this.aliases.get(t);
			if (a == null) {
				a = COLUMN_ALIAS_PREFIX + (SQL92Translator.aliasCounter++);
				this.aliases.put(t, a);
			}
			return a;
		}

		/**
		 * Make aliases columns.
		 *
		 * @param s the s
		 * @param columns the columns
		 * @param rColumns List<String>
		 * @return a comma-separated list of terms prefix with the given alias,
		 * when redundant columns are renamed.
		 */
		private Pair<String, List<String>> makeAliasesColumns(String s, List<String> columns, List<String> rColumns) {
			StringBuilder result = new StringBuilder();
			List<String> aliases = new ArrayList<>();
			String sep = "";
			Set<String> done = new LinkedHashSet<>(columns);
			for (String colName: rColumns) {
				result.append(sep).append(s).append('.').append(colName);
				if (done.contains(colName)) {
					String reAlias = s + '_' + colName;
					this.reAliased.put(reAlias, s + '.' + colName);
					colName = reAlias;
					result.append(" AS ").append(colName);
				}
				sep = ",";
				done.add(colName);
				aliases.add(colName);
			}
			return Pair.of(result.toString(), aliases);
		}

		/**
		 * Make aliases columns.
		 *
		 * @param s the s
		 * @param columns the columns
		 * @param type TupleType
		 * @return a comma-separated list of terms prefix with the given alias.
		 */
		private Pair<String, List<String>> makeAliasesColumns(String s, List<String> columns, TupleType type) {
			return this.makeAliasesColumns(s, columns, type, new LinkedHashMap<Integer, Term>());
		}

		/**
		 * Process attribute name.
		 *
		 * @param s the s
		 * @return the given string, prefixed with a "_" if it does not start with
		 * a letter of a underscore bar.
		 */
		private String processAttributeName(String s) {
			String result = s;
			if (!s.matches("[a-zA-Z_].*")) {
				result = "_" + result;
			}
			if (s.contains(".")) {
				result = result.replace(".", "_");
			}
			if (s.contains(" ")) {
				result = result.replace(" ", "_");
			}
			if (result.length() >= 64) {
				result = COLUMN_ALIAS_PREFIX + (SQL92Translator.aliasCounter++);
			}
			return result;
		}

		/* (non-Javadoc)
		 * @see uk.ac.ox.cs.pdq.rewrite.Rewriter#rewrite(java.lang.Object)
		 */
		@Override
		public String rewrite(Operator input) throws RewriterException {
			return toSQL(input).getLeft();
		}
	}

	/**
	 * Helper class that specializes in translating a logical operator SQL-With
	 * type of SQL statement.
	 * @author Julien Leblay
	 *
	 */
	public class WithTranslator implements Rewriter<Operator, String> {

		/** The op2stmt. */
		private Map<RelationalOperator, Statement.Builder> op2stmt = Maps.newLinkedHashMap();
		
		/** The stmt2alias. */
		private Map<Statement.Builder, String> stmt2alias = Maps.newLinkedHashMap();
		
		/** The alias2rel. */
		private Map<String, String> alias2rel = Maps.newLinkedHashMap();

		/**
		 * Rewrite.
		 *
		 * @param logOp the log op
		 * @return a SQL-With statement equivalent to the given logical operator,
		 * where the nesting reflects that of the given logical operator
		 */
		public String rewrite(Operator logOp) {
			// TODO: make this generic enough to support more operator types
			Preconditions.checkArgument(logOp instanceof RelationalOperator);
			StringBuilder str = new StringBuilder();
			Statement.Builder builder = this.toSQL((RelationalOperator) logOp, str);
			if (str.length() > 0) {
				str.replace(0, 1, "WITH ");
			}
			str.append(' ').append(builder.build());
			return str.toString();
		}

		/**
		 * Temp relation.
		 *
		 * @param stmt the stmt
		 * @return a temporary relation corresponding to the (materialization
		 * of the) given statement.
		 */
		private Relation tempRelation(Statement.Builder stmt) {
			String name = this.stmt2alias.get(stmt);
			List<Attribute> attributes = stmt.getAttributes();
			return new Relation(name, attributes) {};
		}

		/**
		 * Recursive SQL construction.
		 *
		 * @param logOp the log op
		 * @param str the str
		 * @return Statement.Builder
		 */
		private Statement.Builder toSQL(RelationalOperator logOp, StringBuilder str) {
			if (this.op2stmt.containsKey(logOp)) {
				return this.op2stmt.get(logOp);
			}
			Statement.Builder result = Statement.builder();
			if (logOp instanceof Access) {
				RelationalOperator child = ((Access) logOp).getChild();
				Relation accessedRelation = ((Access) logOp).getRelation();
				result.from(accessedRelation);
				AccessMethod accessMethod = ((Access) logOp).getAccessMethod();
				Column[] inputColumns = new Column[accessMethod.getInputs().size()];
				for (int i = 0, l = inputColumns.length; i < l; i++) {
					inputColumns[i] = result.getColumn(accessMethod.getInputs().get(i) - 1);
				}

				if (child instanceof StaticInput) {
					result.where(inputColumns).belongsTo(((StaticInput) child).getTuples());

				} else {
					Statement.Builder childStatement = this.op2stmt.get(child);
					if (childStatement == null) {
						childStatement = this.toSQL(child, str);
					}
					String childAlias = this.stmt2alias.get(childStatement);

					if (!this.alias2rel.containsKey(childAlias)) {
						String nestedSql = childStatement.build().toString();
						str.append(", ").append(childAlias).append(" AS (").append(nestedSql).append(')');
						this.alias2rel.put(childAlias, nestedSql);
					}

					Statement.Builder b = Statement.builder();
					b.from(this.tempRelation(childStatement));
					result.where(inputColumns).belongsTo(b.build().toString());
				}

			} else if (logOp instanceof Scan || logOp instanceof DependentAccess) {
				Relation accessedRelation = ((AccessOperator) logOp).getRelation();
				result.from(accessedRelation);

			} else if (logOp instanceof Selection) {
				result.copy(this.toSQL(((Selection) logOp).getChild(), str));
				uk.ac.ox.cs.pdq.algebra.predicates.Predicate predicate = ((Selection) logOp).getPredicate();
				result.where(predicate);

			} else if (logOp instanceof Projection) {
				RelationalOperator child = ((Projection) logOp).getChild();
				result.copy(this.toSQL(child, str));
				Projection proj = ((Projection) logOp);
				result.project(proj.getProjected(), proj.getRenaming(), child.getColumns());

			} else if (logOp instanceof Join || logOp instanceof CrossProduct) {
				RelationalOperator leftChild = ((NaryOperator) logOp).getChildren().get(0);
				RelationalOperator rightChild = ((NaryOperator) logOp).getChildren().get(1);
				Statement.Builder c1 = this.toSQL(leftChild, str);
				if (!this.alias2rel.containsKey(this.stmt2alias.get(c1))) {
					String leftAlias = this.stmt2alias.get(c1);
					String nestedSql = c1.build().toString();
					str.append(", ").append(leftAlias).append(" AS (").append(nestedSql).append(')');
					this.alias2rel.put(leftAlias, nestedSql);
				}
				Statement.Builder c2 = this.toSQL(rightChild, str);
				if (!this.alias2rel.containsKey(this.stmt2alias.get(c2))) {
					String rightAlias = this.stmt2alias.get(c2);
					String nestedSql = c2.build().toString();
					str.append(", ").append(rightAlias).append(" AS (").append(nestedSql).append(')');
					this.alias2rel.put(rightAlias, nestedSql);
				}
				Preconditions.checkNotNull(c1);
				Preconditions.checkNotNull(c2);
				Relation l = this.tempRelation(c1);
				Relation r = this.tempRelation(c2);
				result.from(l, r);
				if (logOp instanceof Join) {
					result.where(((Join) logOp).getPredicate());
				}

			} else if (logOp instanceof SubPlanAlias) {
				return this.toSQL((RelationalOperator) ((SubPlanAlias) logOp).getPlan().getOperator(), str);

			} else {
				throw new UnsupportedOperationException("Unsupported operator encountered in SQL-With translation. " + logOp);
			}
			this.op2stmt.put(logOp, result);
			if (!this.stmt2alias.containsKey(result)) {
				this.stmt2alias.put(result, RELATION_ALIAS_PREFIX + (SQL92Translator.aliasCounter++));
			}
			return result;
		}
	}

	/**
	 * Helper class for building SQL statements.
	 *
	 * @author Julien Leblay
	 *
	 */
	public static final class Statement {

		/** The rel alias counter. */
		private static int relAliasCounter = 0;
		
		/** The col alias counter. */
		private static int colAliasCounter = 0;

		/**  List of column alias to be projected (as will appear in the SELECT clause). */
		private final List<String> projection = Lists.newLinkedList();
		
		/**  Bijective map from column aliases to the representation). */
		private final BiMap<String, Column> columns = HashBiMap.create();
		
		/**  Bijective map from columns the term they origination from). */
		private final BiMap<Column, Term> columnTerms = HashBiMap.create();
		
		/**  Map from relation aliases to their representations. */
		private final Map<String, Relation> relations = new LinkedHashMap<>();
		
		/**  Collection of conditions to be applied to the statement. */
		private final Collection<Condition> conditions = Sets.newLinkedHashSet();

		/**
		 * Gets the column.
		 *
		 * @param index int
		 * @return Column
		 */
		public Column getColumn(int index) {
			return this.columns.get(this.projection.get(index));
		}

		/**
		 * Adds the select.
		 *
		 * @param alias String
		 * @param att Attribute
		 */
		private void addSelect(String alias, Attribute att) {
			Relation pred = this.relations.get(alias);
			Preconditions.checkArgument(pred != null);
			int position = pred.getAttributeIndex(att.getName());
			Preconditions.checkArgument(position >= 0);
			String columnAlias = COLUMN_ALIAS_PREFIX + (colAliasCounter++);
			Column col = new AttributeColumn(alias, att);
			this.columns.put(columnAlias, col);
			this.columnTerms.forcePut(col, new Variable(att.getName()));
			this.projection.add(columnAlias);
		}

		/**
		 * Adds the from.
		 *
		 * @param rel Relation
		 * @return String
		 */
		private String addFrom(Relation rel) {
			String alias = RELATION_ALIAS_PREFIX + (relAliasCounter++);
			this.relations.put(alias, rel);
			for (Attribute att: rel.getAttributes()) {
				this.addSelect(alias, att);
			}
			return alias;
		}

		/**
		 * Adds the condition.
		 *
		 * @param cond Condition
		 */
		private void addCondition(Condition cond) {
			this.conditions.add(cond);
		}

		/**
		 * Adds the conditions.
		 *
		 * @param conds Collection<Condition>
		 */
		private void addConditions(Collection<Condition> conds) {
			this.conditions.addAll(conds);
		}

		/**
		 * Clone.
		 *
		 * @return Statement
		 */
		@Override
		public Statement clone() {
			Statement result = new Statement();
			result.projection.addAll(this.projection);
			result.columns.putAll(this.columns);
			result.relations.putAll(this.relations);
			result.conditions.addAll(this.conditions);
			return result;
		}

		/**
		 * To string.
		 *
		 * @return String
		 */
		@Override
		public String toString() {
			StringBuilder result = new StringBuilder("SELECT");
			String sep = " ";
			if (this.projection.isEmpty()) {
				result.append(" * ");
			} else {
				Set<String> done = new LinkedHashSet<>();
				for (String a: this.projection) {
					String b = a;
					if (!this.columns.containsKey(a)) {
						throw new IllegalStateException();
					}
					if (done.contains(a)) {
						b = a + '_' + aliasCounter++;
					}
					result.append(sep).append(this.columns.get(a)).append(" AS ").append(b);
					done.add(a);
					done.add(b);
					sep = ", ";
				}
			}
			sep = " FROM ";
			for (String a: this.relations.keySet()) {
				result.append(sep).append(this.relations.get(a).getName()).append(" AS ").append(a);
				sep = ", ";
			}
			sep = " WHERE ";
			for (Condition c: this.conditions) {
				result.append(sep).append(c);
				sep = " AND ";
			}
			return result.toString();
		}

		/**
		 * Builder.
		 *
		 * @return Statement.Builder
		 */
		private static Statement.Builder builder() {
			return new Statement.Builder();
		}

		/**
		 * Help class for building statement incrementally.
		 *
		 * @author Julien Leblay
		 */
		private static final class Builder implements uk.ac.ox.cs.pdq.builder.Builder<Statement> {

			/** The result. */
			private Statement result = new Statement();
			
			/** The conditions. */
			private Collection<Condition.Builder> conditions = new LinkedHashSet<>();

			/**
			 * To string.
			 *
			 * @return String
			 */
			@Override
			public String toString() {
				return this.build().toString();
			}

			/**
			 * Gets the column.
			 *
			 * @param index int
			 * @return Column
			 */
			public Column getColumn(int index) {
				return this.result.getColumn(index);
			}

			/**
			 * Gets the attributes.
			 *
			 * @return List<Attribute>
			 */
			public List<Attribute> getAttributes() {
				List<Attribute> result = new ArrayList<>(this.result.projection.size());
				for (String c: this.result.projection) {
					result.add(new Attribute(this.result.columns.get(c).getType(), c));
				}
				return result;
			}

			/**
			 * Copy.
			 *
			 * @param other Statement.Builder
			 * @return Builder
			 */
			public Builder copy(Statement.Builder other) {
				this.result.columns.putAll(other.result.columns);
				this.result.projection.addAll(other.result.projection);
				this.result.columnTerms.putAll(other.result.columnTerms);
				this.result.relations.putAll(other.result.relations);
				this.conditions.addAll(other.conditions);
				return this;
			}

			/**
			 * Project.
			 *
			 * @param projected List<Term>
			 * @param renaming Map<Integer,Term>
			 * @param columns List<Term>
			 * @return Builder
			 */
			public Builder project(List<Term> projected, Map<Integer, Term> renaming, List<Term> columns) {
				List<String> result = new ArrayList<>(columns.size());
				for (Term t: projected) {
					int i = columns.indexOf(t);
					String a = null;
					if (i < 0) {
						if (!(t.isVariable() || t.isUntypedConstant())) {
							a = COLUMN_ALIAS_PREFIX + (aliasCounter++);
							Column c = new Column.ConstantColumn((TypedConstant) t);
							this.result.columns.forcePut(a, c);
							this.result.columnTerms.forcePut(c, t);
						} else {
							throw new IllegalStateException(t + " out of bounds.\n" + t.getClass() + "\n" + columns);
						}
					} else {
						a = this.result.projection.get(i);
						Column c = this.result.columnTerms.inverse().get(t);
						if ((t.isVariable() || t.isUntypedConstant())
								&& renaming != null && renaming.containsKey(i)) {
							Term newTerm = renaming.get(i);
							if (newTerm.isUntypedConstant() || newTerm.isUntypedConstant()) {
								a = newTerm.toString();
								this.result.columns.forcePut(a, c);
								this.result.columnTerms.forcePut(c, newTerm);
							}
						}
					}
					result.add(a);
				}
				this.result.projection.clear();
				this.result.projection.addAll(result);
				return this;
			}

			/**
			 * From.
			 *
			 * @param pred Relation
			 * @return Builder
			 */
			public Builder from(Relation pred) {
				this.result.addFrom(pred);
				return this;
			}

			/**
			 * From.
			 *
			 * @param preds Relation[]
			 * @return Builder
			 */
			public Builder from(Relation... preds) {
				return this.from(Lists.newArrayList(preds));
			}

			/**
			 * From.
			 *
			 * @param preds Collection<Relation>
			 * @return Builder
			 */
			public Builder from(Collection<Relation> preds) {
				for (Relation p: preds) {
					this.result.addFrom(p);
				}
				return this;
			}

			/**
			 * Where.
			 *
			 * @param pred Atom
			 * @return Builder
			 */
			public Builder where(uk.ac.ox.cs.pdq.algebra.predicates.Predicate pred) {
				if (pred instanceof ConjunctivePredicate) {
					return this.where((ConjunctivePredicate) pred);
				}
				if (pred instanceof AttributeEqualityPredicate) {
					return this.where((AttributeEqualityPredicate) pred);
				}
				if (pred instanceof ConstantEqualityPredicate) {
					return this.where((ConstantEqualityPredicate) pred);
				}
				return this;
			}

			/**
			 * Where.
			 *
			 * @param pred ConjunctivePredicate<Atom>
			 * @return Builder
			 */
			public Builder where(ConjunctivePredicate<uk.ac.ox.cs.pdq.algebra.predicates.Predicate> pred) {
				for (uk.ac.ox.cs.pdq.algebra.predicates.Predicate p: pred) {
					this.where(p);
				}
				return this;
			}

			/**
			 * Where.
			 *
			 * @param pred AttributeEqualityPredicate
			 * @return Builder
			 */
			public Builder where(AttributeEqualityPredicate pred) {
				Column c1 = this.result.getColumn(pred.getPosition());
				Column c2 = this.result.getColumn(pred.getOther());
				this.conditions.add(Condition.columns(c1).equalsTo(c2));
				return this;
			}

			/**
			 * Where.
			 *
			 * @param pred ConstantEqualityPredicate
			 * @return Builder
			 */
			public Builder where(ConstantEqualityPredicate pred) {
				Column c1 = this.result.getColumn(pred.getPosition());
				this.conditions.add(Condition.columns(c1).equalsTo(pred.getValue()));
				return this;
			}

			/**
			 * Where.
			 *
			 * @param column Column[]
			 * @return Condition.Builder
			 */
			public Condition.Builder where(Column... column) {
				Condition.Builder builder = new Condition.Builder(column);
				this.conditions.add(builder);
				return builder;
			}

			/**
			 * Builds the.
			 *
			 * @return Statement
			 * @see uk.ac.ox.cs.pdq.builder.Builder#build()
			 */
			@Override
			public Statement build() {
				Statement result = this.result.clone();
				for (Condition.Builder cb: this.conditions) {
					result.addConditions(cb.build());
				}
				return result;
			}
		}

		/**
		 * Representation of a column. A column may or may not be projected.
		 *
		 * @author Julien Leblay
		 *
		 */
		protected static abstract class Column {
			
			/**
			 * Gets the type.
			 *
			 * @return Type
			 */
			public abstract Type getType();

			/**
			 * A column that refers to a relation attribute.
			 * Two column with the same name but different relation aliases are
			 * considered distinct.
			 *
			 * @author Julien Leblay
			 */
			protected static class AttributeColumn extends Column {
				
				/** The relation alias. */
				final String relationAlias;
				
				/** The attribute. */
				final Attribute attribute;
				/**
				 * Constructor for AttributeColumn.
				 * @param ra String
				 * @param a Attribute
				 */
				public AttributeColumn(String ra, Attribute a) {
					this.relationAlias = ra;
					this.attribute = a;
				}
				
				/**
				 * To string.
				 *
				 * @return String
				 */
				@Override
				public String toString() {
					StringBuilder result = new StringBuilder();
					if (this.relationAlias != null) {
						result.append(this.relationAlias).append('.');
					}
					result.append(this.attribute.getName());
					return result.toString();
				}
				
				/**
				 * Gets the type.
				 *
				 * @return Type
				 */
				@Override
				public Type getType() {
					return this.attribute.getType();
				}
			}
			/**
			 * A column that corresponds to a constant value.
			 *
			 * @author Julien Leblay
			 */
			protected static class ConstantColumn extends Column {
				
				/** The constant. */
				final TypedConstant<?> constant;

				/**
				 * Constructor for ConstantColumn.
				 * @param c TypedConstant<?>
				 */
				public ConstantColumn(TypedConstant<?> c) {
					this.constant = c;
				}

				/**
				 * To string.
				 *
				 * @return String
				 */
				@Override
				public String toString() {
					StringBuilder result = new StringBuilder();
					result.append(this.constant);
					return result.toString();
				}

				/**
				 * Gets the type.
				 *
				 * @return Type
				 */
				@Override
				public Type getType() {
					return this.constant.getType();
				}
			}
		}

		/**
		 * Representation of a statement condition.
		 *
		 * @author Julien Leblay
		 */
		private static abstract class Condition {
			
			/** The column. */
			final Column[] column;

			/**
			 * Constructor for Condition.
			 * @param col Column[]
			 */
			protected Condition(Column... col) {
				this.column = col;
			}

			/**
			 * To string.
			 *
			 * @return String
			 */
			@Override
			public String toString() {
				StringBuilder result = new StringBuilder();
				String sep = "";
				for (Column c: this.column) {
					result.append(sep).append(c);
					sep = ",";
				}
				if (this.column.length > 1) {
					result.insert(0, "(").append(')');
				}
				return result.toString();
			}

			/**
			 * A equality-type of condition.
			 *
			 * @author Julien Leblay
			 */
			protected static final class Equality extends Condition {
				
				/** The target. */
				final Object target;

				/**
				 * Constructor for Equality.
				 * @param o Object
				 * @param col Column[]
				 */
				protected Equality(Object o, Column... col) {
					super(col);
					this.target = o;
				}

				/**
				 * To string.
				 *
				 * @return String
				 */
				@Override
				public String toString() {
					StringBuilder result = new StringBuilder(super.toString());
					result.append('=');
					if (this.target instanceof TypedConstant) {
						result.append(Utility.format((TypedConstant) this.target));
					} else {
						result.append(this.target);
					}
					return result.toString();
				}
			}
			/**
			 * A set membership-type of condition.
			 *
			 * @author Julien Leblay
			 */
			protected static final class Membership extends Condition {
				
				/** The targets. */
				final Collection<Object> targets;

				/**
				 * Constructor for Membership.
				 * @param o Collection<Object>
				 * @param col Column[]
				 */
				protected Membership(Collection<Object> o, Column... col) {
					super(col);
					this.targets = o;
				}

				/**
				 * To string.
				 *
				 * @return String
				 */
				@Override
				public String toString() {
					StringBuilder result = new StringBuilder(super.toString());
					result.append(" IN ");
					String sep = "(";
					String sep2;
					for (Object o: this.targets) {
						if (o instanceof Tuple) {
							Tuple t = (Tuple) o;
							String tuple = "";
							sep2 = "";
							for (int i = 0, l = t.size(); i < l; i++) {
								tuple += sep2 + Utility.format(t.getValue(i), t.getType().getType(i));
								sep2 = ",";
							}
							if (t.size() > 1) {
								tuple = "(" + tuple + ")";
							}
							result.append(sep).append(tuple);
						} else if (o instanceof TypedConstant){
							result.append(sep).append(Utility.format((TypedConstant) o));
						} else {
							result.append(sep).append(o);
						}
						sep = ",";
					}
					result.append(')');
					return result.toString();
				}
			}

			/**
			 * Columns.
			 *
			 * @param cols Column[]
			 * @return Condition.Builder
			 */
			public static Condition.Builder columns(Column... cols) {
				return new Condition.Builder(cols);
			}

			/**
			 * Helper class for building condition.
			 * @author Julien Leblay
			 *
			 */
			private static final class Builder implements uk.ac.ox.cs.pdq.builder.Builder<Collection<Condition>> {

				/** The column. */
				final Column[] column;
				
				/** The equals to. */
				final Collection<Object> equalsTo = new LinkedList<>();
				
				/** The belongs to. */
				final Collection<Object> belongsTo = new LinkedList<>();

				/**
				 * Constructor for Builder.
				 * @param c Column[]
				 */
				public Builder(Column... c) {
					this.column = c;
				}

				/**
				 * Equals to.
				 *
				 * @param o Object
				 * @return Builder
				 */
				public Builder equalsTo(Object o) {
					this.equalsTo.add(o);
					return this;
				}

				/**
				 * Belongs to.
				 *
				 * @param o Object
				 * @return Builder
				 */
				public Builder belongsTo(Object o) {
					if (o instanceof Collection) {
						this.belongsTo.addAll((Collection) o);
						return this;
					}
					this.belongsTo.add(o);
					return this;
				}

				/**
				 * Builds the.
				 *
				 * @return Collection<Condition>
				 * @see uk.ac.ox.cs.pdq.builder.Builder#build()
				 */
				@Override
				public Collection<Condition> build() {
					Collection<Condition> result = new LinkedList<>();
					for (Object e: this.equalsTo) {
						result.add(new Equality(e, this.column));
					}
					if (!this.belongsTo.isEmpty()) {
						result.add(new Membership(this.belongsTo, this.column));
					}
					return result;
				}

				/**
				 * To string.
				 *
				 * @return String
				 */
				@Override
				public String toString() {
					return this.build().toString();
				}
			}
		}
	}
}
