/**
 * 
 */
package uk.ac.ox.cs.pdq.workloadgen.policies.viewgen;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import uk.ac.ox.cs.pdq.workloadgen.policies.Parameter;
import uk.ac.ox.cs.pdq.workloadgen.policies.Parameter.InvalidParameterException;
import uk.ac.ox.cs.pdq.workloadgen.query.Query;
import uk.ac.ox.cs.pdq.workloadgen.query.View;
import uk.ac.ox.cs.pdq.workloadgen.schema.ForeignKey;
import uk.ac.ox.cs.pdq.workloadgen.schema.JoinableKey;
import uk.ac.ox.cs.pdq.workloadgen.schema.Schema;
import uk.ac.ox.cs.pdq.workloadgen.schema.Table;

import com.beust.jcommander.internal.Lists;

/**
 * Base class for a policy that generates join conditions for a query. This
 * class provided some useful methods that might be common to several different
 * policies.
 * 
 * @author herodotos.herodotou
 */
public abstract class VJoinPolicy implements IViewJoinPolicy {

	protected static Random random = new Random();

	private ArrayList<Table> tablesCache = null;

	/*
	 * (non-Javadoc)
	 * 
	 * @see cy.ac.cut.cs.workloadgen.policies.IPolicy#Initialize(java.util.List)
	 */
	@Override
	public abstract boolean initialize(List<Parameter> params)
			throws InvalidParameterException;

	/*
	 * (non-Javadoc)
	 * 
	 * @see cy.ac.cut.cs.workloadgen.policies.querygen.IQueryJoinPolicy#
	 * createJoinConditions(cy.ac.cut.cs.workloadgen.schema.Schema,
	 * cy.ac.cut.cs.workloadgen.query.Query)
	 */
	@Override
	public abstract boolean createJoinConditions(Schema schema, Query query, View view);

	/**
	 * Select a random table from the schema.
	 * 
	 * Optimization: This method avoids selecting a table with a small number of
	 * foreign and joinable keys (less than 2).
	 * 
	 * @return a random table
	 */
	protected Table selectRandomTable(Query query) {
		if (tablesCache == null) {
			// Create the cache of tables
			tablesCache = new ArrayList<Table>(query.getFromClause().size());
			for (Table table : query.getFromClause()) {
				if (table.getForeignKeys().size() + table.getJoinableKeys().size() >= 2) {
					tablesCache.add(table);
				}
			}
		}

		// Select a random table
		int index = random.nextInt(tablesCache.size());
		return tablesCache.get(index);
	}

	/**
	 * Add a random foreign-key join into the query. This method will expand the
	 * query by one table that is referenced from a table that already exists in
	 * the query.
	 * 
	 * @param query
	 * @return false if it is not possible to expand the query by any foreign key
	 *         join
	 */
	protected boolean addRandomForeignKeyJoin(Query query, View view) {
		List<ForeignKey> fkList = Lists.newArrayList(query.getForeignKeys());
		if (fkList.isEmpty())
			return false;
		int count = 0;
		do{
			ForeignKey fk = fkList.get(random.nextInt(fkList.size()));
			if(view.getFromClause().contains(fk.getReferencedSet().getTable()) ||
					view.getFromClause().contains(fk.getReferencingSet().getTable())) {
				view.addTable(fk.getReferencedSet().getTable());
				view.addJoinPredicate(fk);
				break;
			}
			++count;
		}while(count < fkList.size());
		return true;
	}

	/**
	 * Add a random NON-foreign-key join into the query. This method will expand
	 * the query by one table that is joinable with a table that already exists
	 * in the query.
	 * 
	 * @param query
	 * @return false if it is not possible to expand the query by any
	 *         non-foreign-key join
	 */
	protected boolean addRandomJoinableKeyJoin(Query query, View view) {
		List<JoinableKey> jkList = Lists.newArrayList(query.getJoinableKeys());
		if (jkList.isEmpty())
			return false;
		int count = 0;
		do{
			JoinableKey jk = jkList.get(random.nextInt(jkList.size()));
			if(view.getFromClause().contains(jk.getAttr1().getTable()) ||
					view.getFromClause().contains(jk.getAttr2().getTable())) {
				view.addTable(jk.getAttr1().getTable());
				view.addTable(jk.getAttr2().getTable());
				view.addJoinPredicate(jk);
				break;
			}
			++count;
		}while(count < jkList.size());
		return true;
	}
}
