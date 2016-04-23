/**
 * 
 */
package cy.ac.cut.cs.workloadgen.query;

import java.io.PrintStream;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.beust.jcommander.internal.Lists;
import com.google.common.collect.Sets;

import cy.ac.cut.cs.workloadgen.schema.Attribute;
import cy.ac.cut.cs.workloadgen.schema.ForeignKey;
import cy.ac.cut.cs.workloadgen.schema.JoinableKey;
import cy.ac.cut.cs.workloadgen.schema.Table;

/**
 * Represents an SPJ query.
 * 
 * @author herodotos.herodotou
 */
public class View extends Query{

	private static String namePrefix = "V";

	/**
	 * 
	 * @param id
	 */
	public View(int id) {
		super(id);
	}

	public View(int id, List<Attribute> selectClause, Set<Table> fromClause, Predicates whereClause, Set<ForeignKey> foreignKeys, Set<JoinableKey> joinableKeys) {
		super(id, Lists.newArrayList(selectClause), Sets.newLinkedHashSet(fromClause), whereClause.clone(), Sets.newLinkedHashSet(foreignKeys), Sets.newLinkedHashSet(joinableKeys));
	}

	public String getName() {
		return namePrefix + this.getId();
	}


	/**
	 * @param attrName
	 *           attribute name
	 * @return the attribute or null
	 */
	public Attribute getAttribute(String attrName) {
		for(Attribute attribute:this.getSelectClause()) {
			if(attribute.getName().equals(attrName)) {
				return attribute;
			}
		}
		return null;
	}

	/**
	 * Print the query into valid SQL
	 * 
	 * @param ps
	 */
	public void toSQL(PrintStream ps) {
		ps.print("SELECT DISTINCT ");
		Iterator<Attribute> iterAttrs = this.getSelectClause().iterator();
		while (iterAttrs.hasNext()) {
			ps.print(iterAttrs.next().getFullName());
			if (iterAttrs.hasNext())
				ps.print(", ");
		}
		ps.println();

		ps.print("FROM ");
		Iterator<Table> iterTables = this.getFromClause().iterator();
		while (iterTables.hasNext()) {
			ps.print(iterTables.next().getName());
			if (iterTables.hasNext())
				ps.print(", ");
		}
		ps.println();

		if(this.getWhereClause().getFilterPredicates().size() > 0 || this.getWhereClause().getJoinPredicates().size() > 0) {
			ps.print("WHERE ");
			ps.println(this.getWhereClause().toString());
			ps.println();
		}
	}

}
