package uk.ac.ox.cs.pdq.reasoning.homomorphism;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.log4j.Logger;

import uk.ac.ox.cs.pdq.algebra.predicates.EqualityPredicate;
import uk.ac.ox.cs.pdq.algebra.predicates.ExtendedAttributeEqualityPredicate;
import uk.ac.ox.cs.pdq.algebra.predicates.ExtendedAttributeInEqualityPredicate;
import uk.ac.ox.cs.pdq.algebra.predicates.ExtendedConstantEqualityPredicate;
import uk.ac.ox.cs.pdq.algebra.predicates.ExtendedSetEqualityPredicate;
import uk.ac.ox.cs.pdq.db.Attribute;
import uk.ac.ox.cs.pdq.db.Relation;
import uk.ac.ox.cs.pdq.db.TGD;
import uk.ac.ox.cs.pdq.db.TypedConstant;
import uk.ac.ox.cs.pdq.fol.Conjunction;
import uk.ac.ox.cs.pdq.fol.Constant;
import uk.ac.ox.cs.pdq.fol.Evaluatable;
import uk.ac.ox.cs.pdq.fol.Formula;
import uk.ac.ox.cs.pdq.fol.Predicate;
import uk.ac.ox.cs.pdq.fol.Skolem;
import uk.ac.ox.cs.pdq.fol.Term;
import uk.ac.ox.cs.pdq.fol.Variable;
import uk.ac.ox.cs.pdq.reasoning.homomorphism.DBHomomorphismManager.DBRelation;
import uk.ac.ox.cs.pdq.reasoning.homomorphism.HomomorphismConstraint.FactConstraint;
import uk.ac.ox.cs.pdq.reasoning.homomorphism.HomomorphismConstraint.EGDHomomorphismConstraint;
import uk.ac.ox.cs.pdq.reasoning.homomorphism.HomomorphismConstraint.MapConstraint;
import uk.ac.ox.cs.pdq.util.Tuple;
import uk.ac.ox.cs.pdq.util.Utility;

import com.google.common.base.Joiner;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;

/**
 * Creates SQL statements for relation database-backed homomorphism detectors.
 *
 * @author Efthymia Tsamoura
 */
public abstract class SQLStatementBuilder {

	/** Logger. */
	private static Logger log = Logger.getLogger(SQLStatementBuilder.class);

	/** Aliases for facts **/
	protected List<Pair<Predicate, String>> aliases;

	private String aliasPrefix = "A";
	private int aliasCounter = 0;


	/**
	 * @param databaseName
	 * @return the complete list of SQL statements required to set up the fact database
	 */
	public abstract Collection<String> setupStatements(String databaseName);

	/**
	 * @param databaseName String
	 * @return the complete list of SQL statements required to clean up the fact database
	 */
	public abstract Collection<String> cleanupStatements(String databaseName);

	/**
	 *
	 * @param relation the table to drop
	 * @return a SQL statement for dropping the facts table of the given relation
	 */
	protected String dropTableStatement(Relation relation) {
		return "DROP TABLE " + relation.getName();
	}

	/**
	 * @param name
	 * @return a new String that is a copy of the given name modified such that
	 * it is acceptable for the underlying system
	 */
	public abstract String encodeName(String name);

	/**
	 * @param facts
	 * @return insert statements that add the input fact to the facts database.
	 */
	protected abstract Collection<String> makeInserts(Collection<? extends Predicate> facts, Map<String, DBRelation> aliases);
	
	/**
	 * @param facts
	 * @return delete statements that delete the input facts from the fact database.
	 */
	protected abstract Collection<String> makeDeletes(Collection<? extends Predicate> facts, Map<String, DBRelation> aliases);
	
	/**
	 * @param relation the table to create
	 * @return a SQL statement that creates the fact table of the given relation
	 */
	protected abstract String createTableStatement(Relation relation);

	/**
	 * @param relation
	 * @param columns
	 * @return a SQL statement that creates an index for the columns of the input relation
	 */
	protected String createTableIndex(Relation relation, Integer... columns) {
		StringBuilder indexName = new StringBuilder();
		StringBuilder indexColumns = new StringBuilder();
		String sep1 = "", sep2 = "";
		for (Integer i: columns) {
			indexName.append(sep1).append(relation.getAttribute(i).getName());
			indexColumns.append(sep2).append(relation.getAttribute(i).getName());
			sep1 = "_";
			sep2 = ",";
		}
		return "CREATE INDEX idx_" + this.encodeName(relation.getName()) + "_" + indexName +
				" ON " + this.encodeName(relation.getName()) + "(" + indexColumns + ")";
	}

	/**
	 * @param relation
	 * @return a SQL statement that creates an index for the bag and fact attributes of the database tables
	 */
	protected String createTableNonJoinIndexes(Relation relation, Attribute column) {
		return "CREATE INDEX idx_" + this.encodeName(relation.getName()) + "_" + 
				column.getName() + " ON " + this.encodeName(relation.getName()) + "(" + column.getName() + ")";
	}

	/**
	 * 
	 * @param relationMap
	 * @param rule
	 * @return
	 */
	protected Collection<String> createTableIndexes(Map<String, DBRelation> relationMap, Evaluatable rule) {
		Conjunction<?> body = null;
		if (rule.getBody() instanceof Predicate) {
			body = Conjunction.of((Predicate) rule.getBody());
		} else if (rule.getBody() instanceof Conjunction<?>) {
			body = (Conjunction) rule.getBody();
		} else {
			throw new UnsupportedOperationException("Homomorphism check only supported on conjunction of atomic predicate formulas for now.");
		}
		Set<String> result = new LinkedHashSet<>();
		Multimap<Variable, Predicate> clusters = LinkedHashMultimap.create();
		for (Formula subFormula: body) {
			if (subFormula instanceof Predicate) {
				for (Term t: subFormula.getTerms()) {
					if (t instanceof Variable) {
						clusters.put((Variable) t, (Predicate) subFormula);
					}
				}
			} else {
				throw new UnsupportedOperationException("Homomorphism check only supported on conjunction of atomic predicate formulas for now.");
			}
		}
		for (Variable t: clusters.keys()) {
			Collection<Predicate> atoms = clusters.get(t);
			if (atoms.size() > 1) {
				for (Predicate atom: atoms) {
					for (int i = 0, l = atom.getTermsCount(); i < l; i++) {
						if (atom.getTerm(i).equals(t)) {
							result.add(this.createTableIndex(relationMap.get(atom.getName()), i));
						}
					}
				}
			}
		}
		return result;
	}

	protected abstract String translateLimitConstraints(Evaluatable source, HomomorphismConstraint... constraints);

	/**
	 * Creates and runs an SQL statement that detects homomorphisms of the input query to facts kept in a database
	 * @param source
	 * @param constraints
	 * 			The homomorphism constraints that should be satisfied
	 * @return homomorphisms of the input query to facts kept in a database.
	 */
	public Set<Map<Variable, Constant>> toSQL(Evaluatable source, HomomorphismConstraint[] constraints, Map<String, TypedConstant<?>> constants, Connection connection) {

		String query = "";
		List<From> from = this.toFromStatement(source);
		LinkedHashMap<Projection,Variable> projection = this.toProjectStatement(source);
		List<uk.ac.ox.cs.pdq.algebra.predicates.Predicate> predicates = Lists.newArrayList();
		List<ExtendedAttributeEqualityPredicate> where = this.toAttributeEqualityPredicates((Conjunction<Predicate>) source.getBody(), this.aliases);
		List<ExtendedConstantEqualityPredicate> constantPredicates = this.toConstantPredicates((Conjunction<Predicate>) source.getBody(), this.aliases);
		List<ExtendedSkolemEqualityPredicate> canonicalConstraints = this.translateMapConstraints(source, this.aliases, constraints);
		ExtendedAttributeInEqualityPredicate egdConstraint = this.translateEGDHomomorphismConstraints(source, this.aliases, constraints);

		/*
		 * if the target set of facts is not null, we
		 * add in the WHERE statement a predicate which limits the identifiers
		 * of the facts that satisfy any homomorphism to the
		 * identifiers of these facts
		 */
		List<ExtendedSetEqualityPredicate> factConstraints = this.translateFactConstraints(source, this.aliases, constraints);

		if(egdConstraint!=null) {
			predicates.add(egdConstraint);
		}
		predicates.addAll(where);
		predicates.addAll(constantPredicates);
		predicates.addAll(canonicalConstraints);
		predicates.addAll(factConstraints);

		//Limit the number of returned homomorphisms
		String limit = this.translateLimitConstraints(source, constraints); 

		query = "SELECT DISTINCT " 	+ Joiner.on(",").join(projection.keySet()) + "\n" +  
				"FROM " 	+ Joiner.on(",").join(from);
		if(!predicates.isEmpty()) {
			query += "\n" + "WHERE " + Joiner.on(" AND ").join(predicates);
		}	

		if(limit != null) {
			query += "\n" + limit;
		}

		log.trace(source);
		log.trace(query);
		log.trace("\n\n");

		/*
		 * For each returned homomorphism (each homomorphism corresponds to each
		 * returned tuple) we create a mapping of variables to constants.
		 * We also maintain the bag where this homomorphism is found
		 */
		Set<Map<Variable, Constant>> maps = new LinkedHashSet<>();
		try(Statement sqlStatement = connection.createStatement();
				ResultSet resultSet = sqlStatement.executeQuery(query)) {
			while (resultSet.next()) {
				int f = 1;
				Map<Variable, Constant> map = new LinkedHashMap<>();
				for(Entry<Projection, Variable> entry:projection.entrySet()) {
					String assigned = resultSet.getString(f);
					TypedConstant<?> constant = constants.get(assigned);
					Constant constantTerm = constant != null ? constant : new Skolem(assigned);
					map.put(entry.getValue(), constantTerm);
					f++;
				}
				maps.add(map);
			}
		} catch (SQLException ex) {
			log.debug(query);
			throw new IllegalStateException(ex.getMessage(), ex);
		}

		log.trace(maps + "\n\n");
		return maps;
	}

	/**
	 * 
	 * @param source
	 * @return
	 * 		the tables that will be queried
	 */
	protected List<From> toFromStatement(Evaluatable source) {
		this.aliasCounter = 0;
		List<From> relations = new ArrayList<>();
		this.aliases = Lists.newArrayList();
		for (Predicate fact:source.getBody().getPredicates()) {
			String aliasName = this.aliasPrefix + this.aliasCounter;
			relations.add(new From(aliasName, (Relation) fact.getSignature()));
			this.aliases.add(Pair.of(fact, aliasName));
			this.aliasCounter++;
		}
		return relations;
	}

	/**
	 * 
	 * @param source
	 * @return
	 * 		the attributes that will be projected. 
	 * 		If the input is an egd or tgd we project the attributes that map to universally quantified variables.
	 * 		If the input is a query we project the attributes that map to its free variables. 
	 */
	protected LinkedHashMap<Projection,Variable> toProjectStatement(Evaluatable source) {
		LinkedHashMap<Projection,Variable> projected = new LinkedHashMap<>();
		List<Variable> attributes = new ArrayList<>();
		int f = 0;
		for (Predicate fact:source.getBody().getPredicates()) {
			String alias = this.aliases.get(f).getRight();
			List<Term> terms = fact.getTerms();
			for (int it = 0; it < terms.size(); ++it) {
				Term term = terms.get(it);
				if (term instanceof Variable && !attributes.contains(((Variable) term).getName())) {
					projected.put(new Projection(it, (Relation) fact.getSignature(), alias), (Variable)term);
					attributes.add(((Variable) term));
				}
			}
			++f;
		}
		return projected;
	}

	/**
	 * 
	 * @param source
	 * @return
	 * 		the equality predicates of the input conjunction
	 */
	protected List<ExtendedAttributeEqualityPredicate> toAttributeEqualityPredicates(Conjunction<Predicate> source, List<Pair<Predicate, String>> aliases) {
		List<ExtendedAttributeEqualityPredicate> attributePredicates = new ArrayList<>();
		Collection<Term> terms = Utility.getTerms(source.getPredicates());
		terms = Utility.removeDuplicates(terms);
		for (Term term:terms) {
			int f = 0;
			Integer leftPosition = null;
			Relation left = null;
			String leftAlias = null;
			for (Predicate fact:source.getPredicates()) {
				List<Integer> positions = fact.getTermPositions(term);
				for (Integer it:positions) {
					if(leftPosition == null) {
						leftPosition = it;
						left = (Relation) fact.getSignature();
						leftAlias = aliases.get(f).getRight();
					}
					else {
						attributePredicates.add(new ExtendedAttributeEqualityPredicate(leftPosition, it, left, leftAlias, 
								(Relation) fact.getSignature(), aliases.get(f).getRight()));
					}
				}
				++f;
			}
		}
		return attributePredicates;
	}


	/**
	 * 
	 * @param tgd
	 * @return
	 * 		equality predicates between the left and the right hand side conjuncts of the input tgd
	 */
	protected List<ExtendedAttributeEqualityPredicate> toAttributeEqualityPredicates(TGD tgd, List<Pair<Predicate, String>> aliases) {
		List<ExtendedAttributeEqualityPredicate> attributePredicates = new ArrayList<>();
		//For each universally quantified variable
		for (Term term:tgd.getUniversal()) {
			int f = 0;
			//Find its occurrences in the body of the dependency
			for (Predicate lfact:tgd.getLeft().getPredicates()) {
				Integer leftPosition = null;
				Relation leftRelation = null;
				String leftAlias = null;
				List<Integer> lpositions = lfact.getTermPositions(term);
				if(!lpositions.isEmpty()) {

					leftPosition = lpositions.get(0);
					leftRelation = (Relation) lfact.getSignature();
					leftAlias = aliases.get(f).getRight();

					int rf = tgd.getLeft().size();
					//Find also its occurrences in the head of the dependency
					for(Predicate rfact:tgd.getRight().getPredicates()) {
						List<Integer> rpositions = rfact.getTermPositions(term);
						if(!rpositions.isEmpty()) {
							//Make the corresponding variables unequal
							Integer rightPosition = rpositions.get(0);
							attributePredicates.add(new ExtendedAttributeEqualityPredicate(leftPosition, rightPosition, leftRelation, leftAlias, 
									(Relation) rfact.getSignature(), aliases.get(rf).getRight()));
						}
						++rf;
					}
				}
				++f;
			}
		}
		return attributePredicates;
	}

	/**
	 * 
	 * @param source
	 * @return
	 * 		constant equality predicates 
	 */
	protected List<ExtendedConstantEqualityPredicate> toConstantPredicates(Conjunction<Predicate> source, List<Pair<Predicate, String>> aliases) {
		List<ExtendedConstantEqualityPredicate> constantPredicates = new ArrayList<>();
		int f = 0;
		for (Predicate fact:source.getPredicates()) {
			try{
				String alias = aliases.get(f).getRight();
				List<Term> terms = fact.getTerms();
				for (int it = 0; it < terms.size(); ++it) {
					Term term = terms.get(it);
					if (!term.isVariable() && !term.isSkolem()) {
						constantPredicates.add(new ExtendedConstantEqualityPredicate(it, (TypedConstant) term, (Relation) fact.getSignature(), alias));
					}
				}
				++f;
			}
			catch(Exception ex) {
			}
		}
		return constantPredicates;
	}

	/**
	 * 
	 * @param source
	 * @param constraints
	 * @return
	 * 		predicates that correspond to fact constraints 
	 */
	protected List<ExtendedSetEqualityPredicate> translateFactConstraints(Evaluatable source, List<Pair<Predicate, String>> aliases, HomomorphismConstraint... constraints) {
		List<ExtendedSetEqualityPredicate> setPredicates = new ArrayList<>();
		for(HomomorphismConstraint c:constraints) {
			if(c instanceof FactConstraint) {
				List<Object> facts = new ArrayList<>();
				for (Predicate atom:((FactConstraint) c).atoms) {
					facts.add(atom.getId());
				}
				int f = 0;
				for(Predicate fact:source.getBody().getPredicates()) {
					String alias = aliases.get(f).getRight();
					setPredicates.add(new ExtendedSetEqualityPredicate(fact.getTermsCount()-1, facts, (Relation) fact.getSignature(), alias));
					++f;
				}
			}
		}
		return setPredicates;
	}

	/**
	 * 
	 * @param source
	 * @param constraints
	 * @return
	 * 		predicates that correspond to fact constraints 
	 */
	protected ExtendedAttributeInEqualityPredicate translateEGDHomomorphismConstraints(Evaluatable source, List<Pair<Predicate, String>> aliases, HomomorphismConstraint... constraints) {
		for(HomomorphismConstraint c:constraints) {
			if(c instanceof EGDHomomorphismConstraint) {
				List<Predicate> conjuncts = source.getBody().getPredicates();
				String lalias = aliases.get(0).getRight();
				String ralias = aliases.get(1).getRight();
				return new ExtendedAttributeInEqualityPredicate(conjuncts.get(0).getTermsCount()-1, conjuncts.get(1).getTermsCount()-1, 
						(Relation) conjuncts.get(0).getSignature(), lalias, (Relation) conjuncts.get(1).getSignature(), ralias);
			}
		}
		return null;
	}


	/**
	 * 
	 * @param source
	 * @param constraints
	 * @return
	 * 		predicates that correspond to canonical constraints
	 */
	protected List<ExtendedSkolemEqualityPredicate> translateMapConstraints(Evaluatable source, List<Pair<Predicate, String>> aliases, HomomorphismConstraint... constraints) {
		List<ExtendedSkolemEqualityPredicate> constantPredicates = new ArrayList<>();
		for(HomomorphismConstraint c:constraints) {
			if(c instanceof MapConstraint) {
				Map<Variable, Constant> m = ((MapConstraint) c).mapping;
				for(Entry<Variable, Constant> pair:m.entrySet()) {
					int f = 0;
					for (Predicate fact:source.getBody().getPredicates()) {
						int it = fact.getTerms().indexOf(pair.getKey());
						if(it != -1) {
							constantPredicates.add(
									new ExtendedSkolemEqualityPredicate(it, (Skolem) pair.getValue(), (Relation) fact.getSignature(), aliases.get(f).getRight()));
						}
						++f;
					}
				}
			}
		}
		return constantPredicates;
	}

	/**
	 * @return SQLStatementBuilder
	 */
	@Override
	public abstract SQLStatementBuilder clone();

	/**
	 * 
	 * @author Efthymia Tsamoura
	 *
	 */
	protected static class Projection {
		private final Relation relation;
		private final int position; 
		private final String alias;

		public Projection(int position, Relation relation, String alias) {
			Preconditions.checkNotNull(relation);
			Preconditions.checkArgument(position >= 0 && position < relation.getArity());
			this.relation = relation;
			this.position = position;
			this.alias = alias;
		}

		public Attribute getAttribute() {
			return this.relation.getAttribute(this.getPosition());
		}

		public Relation getRelation() {
			return this.relation;
		}

		public int getPosition() {
			return this.position;
		}

		/*
		 * (non-Javadoc)
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			StringBuilder result = new StringBuilder();
			result.append(this.alias==null ? this.relation.getName():this.alias).
			append(".").append(this.getAttribute().getName());
			return result.toString();
		}
	}

	/**
	 * 
	 * @author Efthymia Tsamoura
	 *
	 */
	protected static class From {
		private final Relation relation;
		private final String alias; 

		public From(String alias, Relation relation) {
			Preconditions.checkNotNull(relation);
			this.relation = relation;
			this.alias = alias;
		}

		public Relation getRelation() {
			return this.relation;
		}

		public String getAlias() {
			return this.alias;
		}

		/*
		 * (non-Javadoc)
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			StringBuilder result = new StringBuilder();
			result.append(this.getRelation().getName()).append(" AS ");
			result.append(this.alias==null ? this.relation.getName():this.alias);
			return result.toString();
		}
	}


	/**
	 * 
	 * @author Efthymia Tsamoura
	 *
	 */
	protected static class ExtendedSkolemEqualityPredicate implements EqualityPredicate{

		private final Relation relation;
		private final String alias;
		private final int position;
		private final Skolem constant;

		public ExtendedSkolemEqualityPredicate(int position, Skolem constant, Relation relation,  String alias) {
			Preconditions.checkNotNull(constant);
			Preconditions.checkNotNull(relation);
			Preconditions.checkArgument(position >= 0 && position < relation.getArity());
			this.relation = relation;
			this.alias = alias;
			this.constant = constant;
			this.position = position;
		}

		public Attribute getAttribute() {
			return this.relation.getAttribute(this.getPosition());
		}

		public Relation getRelation() {
			return this.relation;
		}

		/**
		 * @param predicate the (possibly nested) predicate to flatten, if null the empty collection is returned.
		 * @return a collection of predicate remove the nesting of conjunction that
		 * it may contain.
		 */
		public Collection<uk.ac.ox.cs.pdq.algebra.predicates.Predicate> flatten() {
			Collection<uk.ac.ox.cs.pdq.algebra.predicates.Predicate> result = new LinkedList<>();
			result.add(this);
			return result;
		}

		/*
		 * (non-Javadoc)
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		@Override
		public boolean equals(Object o) {
			if (this == o) {
				return true;
			}
			if (o == null) {
				return false;
			}
			return this.getClass().isInstance(o)
					&& this.getPosition() == ((ExtendedSkolemEqualityPredicate) o).getPosition()
					&& this.getConstant() == ((ExtendedSkolemEqualityPredicate) o).getConstant()
					&& this.getRelation() == ((ExtendedSkolemEqualityPredicate) o).getRelation();
		}

		/*
		 * (non-Javadoc)
		 * @see java.lang.Object#hashCode()
		 */
		@Override
		public int hashCode() {
			return Objects.hashCode(this.getPosition(), this.getConstant(), this.getRelation());
		}

		/*
		 * (non-Javadoc)
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			StringBuilder result = new StringBuilder();
			result.append(this.alias==null ? this.relation.getName():this.alias).append(".").append(this.getAttribute().getName()).append('=');
			result.append("'").append(this.getConstant()).append("'");
			return result.toString();
		}

		public int getPosition() {
			return this.position;
		}

		public Skolem getConstant() {
			return this.constant;
		}

		@Override
		public boolean isSatisfied(Tuple t) {
			throw new java.lang.UnsupportedOperationException();
		}

	}
}
