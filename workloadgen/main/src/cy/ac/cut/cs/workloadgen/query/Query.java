/**
 * 
 */
package cy.ac.cut.cs.workloadgen.query;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import cy.ac.cut.cs.workloadgen.schema.Attribute;
import cy.ac.cut.cs.workloadgen.schema.AttributeSet;
import cy.ac.cut.cs.workloadgen.schema.ForeignKey;
import cy.ac.cut.cs.workloadgen.schema.JoinableKey;
import cy.ac.cut.cs.workloadgen.schema.Table;

/**
 * Represents an SPJ query.
 * 
 * @author herodotos.herodotou
 */
public class Query {

	private static String namePrefix = "Q";
	private int id;
	private List<Attribute> selectClause;
	private Set<Table> fromClause;
	private Predicates whereClause;
	private Set<ForeignKey> foreignKeys;
	private Set<JoinableKey> joinableKeys;

	/**
	 * 
	 * @param id
	 */
	public Query(int id) {
		this.id = id;
		this.selectClause = Lists.newArrayList();
		this.fromClause = Sets.newLinkedHashSet();
		this.whereClause = new Predicates();
		this.foreignKeys = Sets.newLinkedHashSet();
		this.joinableKeys = Sets.newLinkedHashSet();
	}

	public Query(int id, List<Attribute> selectClause, Set<Table> fromClause, Predicates whereClause, Set<ForeignKey> foreignKeys, Set<JoinableKey> joinableKeys) {
		Preconditions.checkNotNull(selectClause);
		Preconditions.checkNotNull(fromClause);
		Preconditions.checkNotNull(whereClause);
		Preconditions.checkNotNull(foreignKeys);
		Preconditions.checkNotNull(joinableKeys);
		this.selectClause = selectClause;
		this.fromClause = fromClause;
		this.whereClause = whereClause;
		this.foreignKeys = foreignKeys;
		this.joinableKeys = joinableKeys;

	}

	/**
	 * @return the query id
	 */
	public int getId() {
		return id;
	}

	public String getName() {
		return namePrefix + id;
	}

	/**
	 * @return the selectClause
	 */
	public List<Attribute> getSelectClause() {
		return selectClause;
	}

	/**
	 * @return the fromClause
	 */
	public Set<Table> getFromClause() {
		return fromClause;
	}

	/**
	 * @return the whereClause
	 */
	public Predicates getWhereClause() {
		return whereClause;
	}

	/**
	 * Add a table in the from clause
	 * 
	 * @param table
	 * @return true if added successfully
	 */
	public boolean addTable(Table table) {
		return fromClause.add(table);
	}

	/**
	 * Add join predicates based on the foreign key. Both tables in the foreign
	 * key should already be in the query.
	 * 
	 * @param fk
	 * @return true if added successfully
	 */
	public boolean addJoinPredicate(ForeignKey fk) {

		AttributeSet referencing = fk.getReferencingSet();
		AttributeSet referenced = fk.getReferencedSet();

		if (!fromClause.contains(referencing.getTable())
				|| !fromClause.contains(referenced.getTable()))
			return false;

		// Expecting 1-on-1 correspondence of attributes
		List<Attribute> referencingAttrs = referencing.getAttributes();
		List<Attribute> referencedAttrs = referenced.getAttributes();
		if (referencingAttrs.size() != referencedAttrs.size())
			return false;

		for (int i = 0; i < referencingAttrs.size(); ++i) {
			whereClause.addJoinPredicate(new JoinPredicate(
					referencingAttrs.get(i), referencedAttrs.get(i)));
		}

		this.foreignKeys.add(fk);
		return true;
	}

	/**
	 * Add join predicates based on the joinable key. Both tables in the joinable
	 * key should already be in the query.
	 * 
	 * @param fk
	 * @return true if added successfully
	 */
	public boolean addJoinPredicate(JoinableKey jk) {
		Attribute attr1 = jk.getAttr1();
		Attribute attr2 = jk.getAttr2();

		if (!fromClause.contains(attr1.getTable())
				|| !fromClause.contains(attr2.getTable()))
			return false;

		this.joinableKeys.add(jk);
		return whereClause.addJoinPredicate(new JoinPredicate(attr1, attr2));
	}

	/**
	 * Add a filter predicate. The table in the predicate should already be in
	 * the query.
	 * 
	 * @param filter
	 * @return true if added successfully
	 */
	public boolean addFilterPredicate(FilterPredicate filter) {
		if (!fromClause.contains(filter.getAttribute().getTable()))
			return false;

		return whereClause.addFilterPredicate(filter);
	}

	/**
	 * Add an attribute to project. The attribute's table must already be in the
	 * from list
	 * 
	 * @param attr
	 * @return true if added successfully
	 */
	public boolean addProjectAttribute(Attribute attr) {
		if (!fromClause.contains(attr.getTable()))
			return false;

		return selectClause.add(attr);
	}

	/**
	 * Print the query into valid SQL
	 * 
	 * @param ps
	 */
	public void toSQL(PrintStream ps) {
		ps.print("SELECT DISTINCT ");
		Iterator<Attribute> iterAttrs = selectClause.iterator();
		while (iterAttrs.hasNext()) {
			ps.print(iterAttrs.next().getFullName());
			if (iterAttrs.hasNext())
				ps.print(", ");
		}
		ps.println();

		ps.print("FROM ");
		Iterator<Table> iterTables = fromClause.iterator();
		while (iterTables.hasNext()) {
			ps.print(iterTables.next().getName());
			if (iterTables.hasNext())
				ps.print(", ");
		}
		ps.println();

		if(this.whereClause.getFilterPredicates().size() > 0 || this.whereClause.getJoinPredicates().size() > 0) {
			ps.print("WHERE ");
			ps.println(this.whereClause.toString());
			ps.println();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		PrintStream ps = new PrintStream(baos);
		toSQL(ps);
		return baos.toString();
	}

	/**
	 * @return the foreignKeys
	 */
	public Set<ForeignKey> getForeignKeys() {
		return this.foreignKeys;
	}

	/**
	 * @return the joinableKeys
	 */
	public Set<JoinableKey> getJoinableKeys() {
		return this.joinableKeys;
	}

	/**
	 * @param o Object
	 * @return boolean
	 */
	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null) {
			return false;
		}
		return Query.class.isInstance(o)
				&& Sets.newLinkedHashSet(this.selectClause).equals(Sets.newLinkedHashSet(((Query) o).selectClause))
				&& this.fromClause.equals(((Query) o).fromClause)
				&& this.whereClause.equals(((Query) o).whereClause)
				&& this.foreignKeys.equals(((Query) o).foreignKeys)
				&& this.joinableKeys.equals(((Query) o).joinableKeys);

	}

	/**
	 * @return int
	 */
	@Override
	public int hashCode() {
		return Objects.hash(this.selectClause, this.fromClause, this.whereClause, this.foreignKeys, this.joinableKeys);
	}

}
