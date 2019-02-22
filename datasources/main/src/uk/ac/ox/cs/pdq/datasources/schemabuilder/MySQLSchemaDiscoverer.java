package uk.ac.ox.cs.pdq.datasources.schemabuilder;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.tuple.Pair;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Lists;

import uk.ac.ox.cs.pdq.datasources.sql.SqlAccessMethod;
import uk.ac.ox.cs.pdq.db.AccessMethodDescriptor;
import uk.ac.ox.cs.pdq.db.Attribute;
import uk.ac.ox.cs.pdq.db.Relation;
import uk.ac.ox.cs.pdq.db.View;
import uk.ac.ox.cs.pdq.fol.Atom;
import uk.ac.ox.cs.pdq.fol.LinearGuarded;
import uk.ac.ox.cs.pdq.fol.Term;
import uk.ac.ox.cs.pdq.fol.TypedConstant;
import uk.ac.ox.cs.pdq.fol.UntypedConstant;

/**
 * MySQL-specific class for automatically discover a schema.
 *  
 * @author Julien Leblay
 *
 */
public class MySQLSchemaDiscoverer extends AbstractSQLSchemaDiscoverer {

	private Map<String, Relation> relationMap;

	/*
	 * (non-Javadoc)
	 * @see uk.ac.ox.cs.pdq.discovery.sql.AbstractSQLSchemaDiscoverer#getRelationsDiscoveryStatement(java.lang.String)
	 */
	@Override
	protected String getRelationsDiscoveryStatement(String databaseName) {
		return "SELECT table_name "
				+ " FROM information_schema.tables"
				+ " WHERE table_type LIKE 'BASE TABLE'"
				+ " AND table_schema = '" + databaseName + "'"
				+ " ORDER BY table_name";
	}

	/*
	 * (non-Javadoc)
	 * @see uk.ac.ox.cs.pdq.discovery.sql.AbstractSQLSchemaDiscoverer#getForeignKeyDiscoveryStatement(java.lang.String)
	 */
	@Override
	protected String getForeignKeyDiscoveryStatement(String relationName) {
		return "select CONSTRAINT_NAME, COLUMN_NAME, "
				+ "REFERENCED_TABLE_NAME, REFERENCED_COLUMN_NAME"
				+ " from information_schema.KEY_COLUMN_USAGE "
				+ " where TABLE_NAME = '" + relationName + "' "
				+ "AND REFERENCED_TABLE_NAME IS NOT NULL";
	}

	/*
	 * (non-Javadoc)
	 * @see uk.ac.ox.cs.pdq.discovery.sql.AbstractSQLSchemaDiscoverer#getViewsDiscoveryStatement(java.lang.String)
	 */
	@Override
	protected String getViewsDiscoveryStatement(String databaseName) {
		return "SELECT table_name "
				+ " FROM information_schema.tables"
				+ " WHERE table_type LIKE 'VIEW'"
				+ " AND table_schema = '" + databaseName + "'"
				+ " ORDER BY table_name";
	}

	/*
	 * (non-Javadoc)
	 * @see uk.ac.ox.cs.pdq.builder.discovery.sql.AbstractSQLSchemaDiscoverer#getRelationInstance(java.util.Properties, java.lang.String, java.util.List)
	 */
	@Override
	protected Relation getRelationInstance(Properties props, String relationName, Attribute[] attributes) {
		Relation relation = Relation.create(relationName, attributes,new AccessMethodDescriptor[] {new SqlAccessMethod(Relation.create(relationName, attributes), props)});
		return relation;
	}

	/**
	 * Gets the view instance.
	 *
	 * @param props Properties
	 * @param viewName String
	 * @param relationMap Map<String,Relation>
	 * @return View
	 */
	@Override
	protected View getViewInstance(Properties props, String viewName, Map<String, Relation> relationMap) {
		String definition = this.getViewDefinition(viewName);
		LinearGuarded viewToRelationDependency = this.parseViewDefinition(viewName, definition, relationMap);
		// view without access method
		View view = new View(viewName, relationMap.get(viewName).getAttributes());
		view.setViewToRelationDependency(viewToRelationDependency);
		// re create the view now with the access method.
		view = new View(viewName, relationMap.get(viewName).getAttributes(),new AccessMethodDescriptor[] {new SqlAccessMethod(view, props)});
		view.setViewToRelationDependency(viewToRelationDependency);
		return view;
	}

	/**
	 * Gets the view definition.
	 *
	 * @param viewName String
	 * @return String
	 */
	@Override
	protected String getViewDefinition(String viewName) {
		try (
				Connection connection = getConnection(this.properties);
				Statement stmt = connection.createStatement();
				ResultSet rs = stmt.executeQuery("SHOW CREATE VIEW " + viewName);
				) {
			if (rs.next()) {
				return rs.getString(2);
			}
		} catch (SQLException e) {
			log.error(e);
			throw new BuilderException(e.getMessage(), e);
		}
		return null;
	}

	/**
	 * Parse a SQL statement featuring a conjunctive view definition and returns
	 * the corresponding linear guarded dependency.
	 *
	 * @param viewDef the view def
	 * @param relationMap Map<String,Relation>
	 * @return the corresponding linear guarded dependency representation of the
	 * given SQL view definition.
	 */
	protected LinearGuarded parseViewDefinition(String viewDef, Map<String, Relation> relationMap) {
		throw new UnsupportedOperationException();
	}

	/** The Constant TOP_PATTERN. */
	private static final String TOP_PATTERN = "select (?<select>.*) from (?<from>.*) where (?<where>.*)";

	/** The Constant ALIAS_PATTERN. */
	private static final String ALIAS_PATTERN = "((?<alias>\\w*)\\.)?(?<attribute>\\w*)(\\s*as\\s*(?<renamed>\\w*))?";

	/** The Constant NESTED_CONJUNCTION_PATTERN. */
	private static final String NESTED_CONJUNCTION_PATTERN = "\\((?<condition>.*)(\\s*and\\s*(?<rest>.*))+?\\)";

	/** The Constant CONDITION_PATTERN. */
	private static final String CONDITION_PATTERN = "\\((?<condition>.*)\\)";

	/**
	 * Parse a SQL statement featuring a conjunctive view definition and returns
	 * the corresponding linear guarded dependency.
	 *
	 * @param viewName String
	 * @param viewDef the view def
	 * @param relationMap Map<String,Relation>
	 * @return the corresponding linear guarded dependency representation of the
	 * given SQL view definition.
	 */
	protected LinearGuarded parseViewDefinition(String viewName, String viewDef, Map<String, Relation> relationMap) {
		Preconditions.checkArgument(viewDef != null && !viewDef.isEmpty());
		this.relationMap = relationMap;
		Matcher m = Pattern.compile(TOP_PATTERN, Pattern.CASE_INSENSITIVE).matcher(viewDef);
		String select = null;
		String from = null;
		String where = null;
		if (m.find()) {
			select = m.group("select").trim();
			from = m.group("from").trim();
			where = m.group("where").trim();
		}
		if (Strings.isNullOrEmpty(select) || Strings.isNullOrEmpty(from) || Strings.isNullOrEmpty(where)) {
			throw new IllegalArgumentException("Not a valid view definition " + viewDef);
		}
		BiMap<String, Atom> atoms = this.makePredicate(from, relationMap);
		this.makeJoins(where, atoms);
		Pair<List<Term>, List<Attribute>> freeTermsAndAttributes = this.makeFreeTerms(select, atoms);
		List<Term> freeTerms = freeTermsAndAttributes.getLeft();
		List<Attribute> attributes = freeTermsAndAttributes.getRight();
		Atom[] right = atoms.values().toArray(new Atom[atoms.values().size()]);
		return LinearGuarded.create(Atom.create(Relation.create(viewName, attributes.toArray(new Attribute[attributes.size()])), freeTerms.toArray(new Term[freeTerms.size()])), right);
	}

	/**
	 * Parse a SQL from clause and returns a map from relation aliases to 
	 * predicates featuring fresh variables.
	 *
	 * @param fromClause the from clause
	 * @param relationMap the relation map
	 * @return a map from relation aliases to predicates with fresh variables
	 */
	private BiMap<String, Atom> makePredicate(String fromClause, Map<String, Relation> relationMap) {
		BiMap<String, Atom> result = HashBiMap.create();
		String from = fromClause.trim();
		if (from.startsWith("(") && from.endsWith("")) {
			from = from.substring(1, from.length() - 1);
		}
		for (String token: from.trim().split("(,|join)")) {
			String[] aliased = token.trim().replace("`", "").split("(AS|\\s)");
			Relation r = relationMap.get(aliased[0].trim());
			Atom pred = Atom.create(r, createVariables(r));
			if (aliased.length == 1) {
				result.put(aliased[0].trim(), pred);
			} else {
				result.put(aliased[1].trim(), pred);
			}
		}
		return result;
	}

	/**
	 * Parse a SQL where clause and returns a map from relation aliases to 
	 * predicates featuring fresh variables.
	 *
	 * @param whereClause the where clause
	 * @param predMap BiMap<String,PredicateFormula>
	 */
	private void makeJoins(String whereClause, BiMap<String, Atom> predMap) {
		Matcher m = Pattern.compile(NESTED_CONJUNCTION_PATTERN, Pattern.CASE_INSENSITIVE).matcher(whereClause.trim());
		String nested = null;
		String rest = null;
		if (m.find()) {
			do {
				nested = m.group("condition");
				rest = m.group("rest");
				if (rest != null) {
					this.makeJoins(rest.trim(), predMap);
				}
				this.makeJoins(nested.trim(), predMap);
			} while (m.find());
		} else {
			String clause = null;
			Matcher m2 = Pattern.compile(CONDITION_PATTERN, Pattern.CASE_INSENSITIVE).matcher(whereClause.trim());
			if (m2.find()) {
				clause = m2.group("condition").trim();
			} else {
				clause = whereClause;
			}
			String[] operands = clause.replace("`", "").split("=");
			if (operands != null && operands.length == 2) {
				Triple<Atom, Integer, Attribute> t1 = this.parseAlias(operands[0], predMap);
				if (t1 == null) {
					return;
				}
				Triple<Atom, Integer, Attribute> t2 = this.parseAlias(operands[1], predMap);
				if (t2 == null) {
					return;
				}
				Atom p2 = t2.getFirst();
				List<Term> terms = Lists.newArrayList(p2.getTerms());
				terms.set(t2.getSecond(), t1.getFirst().getTerm(t1.getSecond()));
				String key = predMap.inverse().get(p2);
				predMap.forcePut(key, Atom.create(p2.getPredicate(), terms.toArray(new Term[terms.size()])));
			}
		}
	}

	/**
	 * Parse a SQL select clause and returns a list of corresponding free 
	 * variables from the given predicates collection.
	 *
	 * @param selectClause the select clause
	 * @param predMap Map<String,PredicateFormula>
	 * @return a map from relation aliases to predicates with fresh variables
	 */
	private Pair<List<Term>, List<Attribute>> makeFreeTerms(String selectClause, Map<String, Atom> predMap) {
		List<Term> terms = new ArrayList<>();
		List<Attribute> attributes = new ArrayList<>();
		for (String token: selectClause.trim().split(",")) {
			Triple<Atom, Integer, Attribute> triple = this.parseAlias(token.trim().replace("`", ""), predMap);
			if (triple == null) {
				Pair<Term, Attribute> pair = this.parseConstant(token, predMap.values());
				terms.add(pair.getLeft());
				attributes.add(pair.getRight());
			} else {
				terms.add(triple.getFirst().getTerm(triple.getSecond()));
				attributes.add(triple.getThird());
			}
		}
		return Pair.of(terms, attributes);
	}

	/**
	 * Parses the alias.
	 *
	 * @param token String
	 * @param predMap Map<String,PredicateFormula>
	 * @return Triple<PredicateFormula,Integer,Attribute>
	 */
	private Triple<Atom, Integer, Attribute> parseAlias(String token, Map<String, Atom> predMap) {
		String alias = null;
		String attribute = null;
		String renamed = null;
		Matcher m = Pattern.compile(ALIAS_PATTERN, Pattern.CASE_INSENSITIVE).matcher(token.trim());
		if (m.find()) {
			alias = m.group("alias");
			attribute = m.group("attribute");
			renamed = m.group("renamed");
		}
		alias = alias != null ? alias.trim() : null;
		attribute = attribute != null ? attribute.trim() : null;
		renamed = renamed != null ? renamed.trim() : null;
		if (Strings.isNullOrEmpty(attribute)) {
			throw new IllegalArgumentException("Not a valid alias clause in view definition " + token);
		}

		Atom pred = null;
		Integer index = null;
		Attribute att = null;
		if (alias == null || alias.isEmpty()){
			Iterator<Atom> it = predMap.values().iterator();
			while (it.hasNext()) {
				pred = it.next();
				index = relationMap.get(pred.getPredicate().getName()).getAttributePosition(attribute);
				if (index >= 0) {
					break;
				}
			}
		} else {
			pred = predMap.get(alias);
			if (pred != null) {
				index = relationMap.get(pred.getPredicate().getName()).getAttributePosition(attribute);
			}
		}
		if (renamed != null && !renamed.isEmpty()){
			att = Attribute.create(relationMap.get(pred.getPredicate().getName()).getAttribute(index).getType(), renamed);
		}
		return new Triple<>(pred, index, att);
	}

	/**
	 * Parses the constant of an SQL query string.
	 *
	 * @param token String
	 * @param atoms the predicates
	 * @return the first predicate found in the given collection featuring the
	 * given attribute and its position.
	 */
	private Pair<Term, Attribute> parseConstant(String token, Collection<Atom> atoms) {
		String attribute = null;
		String renamed = null;
		Matcher m = Pattern.compile(ALIAS_PATTERN, Pattern.CASE_INSENSITIVE).matcher(token);
		if (m.find()) {
			attribute = m.group("attribute").trim();
			renamed = m.group("renamed").trim();
		}
		if (Strings.isNullOrEmpty(attribute)) {
			throw new IllegalArgumentException("Not a valid select clause in view definition " + token);
		}

		Term term = null;
		Attribute att = null;
		if (attribute != null
				&& ((attribute.startsWith("\"") && attribute.endsWith("\""))
						|| (attribute.startsWith("'") && attribute.endsWith("'")))) {
			term = TypedConstant.create(attribute.substring(1, attribute.length() - 1));
		}
		if (renamed != null && !renamed.isEmpty()) 
			att = Attribute.create(String.class, String.valueOf(UntypedConstant.getFreshConstant()));
		return Pair.of(term, att);
	}
}
