/**
 * 
 */
package uk.ac.ox.cs.pdq.workloadgen.policies.querygen;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;

import uk.ac.ox.cs.pdq.workloadgen.policies.Parameter;
import uk.ac.ox.cs.pdq.workloadgen.policies.Parameter.InvalidParameterException;
import uk.ac.ox.cs.pdq.workloadgen.query.Query;
import uk.ac.ox.cs.pdq.workloadgen.schema.ForeignKey;
import uk.ac.ox.cs.pdq.workloadgen.schema.JoinableKey;
import uk.ac.ox.cs.pdq.workloadgen.schema.Schema;
import uk.ac.ox.cs.pdq.workloadgen.schema.Table;

/**
 * Base class for a policy that generates join conditions for a query. This
 * class provided some useful methods that might be common to several different
 * policies.
 * 
 * @author herodotos.herodotou
 */
public abstract class QJoinPolicy implements IQueryJoinPolicy {

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
   public abstract boolean createJoinConditions(Schema schema, Query query);

   /**
    * Select a random table from the schema.
    * 
    * Optimization: This method avoids selecting a table with a small number of
    * foreign and joinable keys (less than 2).
    * 
    * @return a random table
    */
   protected Table selectRandomTable(Schema schema) {

      if (tablesCache == null) {
         // Create the cache of tables
         tablesCache = new ArrayList<Table>(schema.getTables().size());
         for (Table table : schema.getTables()) {
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
   protected boolean addRandomForeignKeyJoin(Query query) {
      List<ForeignKey> fkList = extractForeignKeys(query);
      if (fkList.isEmpty())
         return false;

      ForeignKey fk = fkList.get(random.nextInt(fkList.size()));
      query.addTable(fk.getReferencedSet().getTable());
      query.addJoinPredicate(fk);

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
   protected boolean addRandomJoinableKeyJoin(Query query) {
      List<JoinableKey> jkList = extractJoinableKeys(query);
      if (jkList.isEmpty())
         return false;

      JoinableKey jk = jkList.get(random.nextInt(jkList.size()));
      query.addTable(jk.getAttr1().getTable());
      query.addTable(jk.getAttr2().getTable());
      query.addJoinPredicate(jk);

      return true;
   }

   /**
    * Find all foreign keys where the referencing table is in the query and the
    * referenced table is not in the query
    * 
    * @param query
    * @return a list of foreign keys
    */
   protected List<ForeignKey> extractForeignKeys(Query query) {
      List<ForeignKey> fkSet = new ArrayList<ForeignKey>();

      Set<Table> tables = query.getFromClause();
      for (Table table : tables) {
         Set<ForeignKey> tableFkSet = table.getForeignKeys();
         for (ForeignKey fk : tableFkSet) {
            if (!tables.contains(fk.getReferencedSet().getTable())) {
               fkSet.add(fk);
            }
         }
      }

      return fkSet;
   }

   /**
    * Find all joinable keys where one table is in the query and the other table
    * is not in the query
    * 
    * @param query
    * @return a list of joinable keys
    */
   protected List<JoinableKey> extractJoinableKeys(Query query) {
      List<JoinableKey> jkSet = new ArrayList<JoinableKey>();

      Set<Table> tables = query.getFromClause();
      for (Table table : tables) {
         Set<JoinableKey> tableJkSet = table.getJoinableKeys();
         for (JoinableKey jk : tableJkSet) {
            if ((tables.contains(jk.getAttr1().getTable()) && !tables
                  .contains(jk.getAttr2().getTable()))
                  || (tables.contains(jk.getAttr2().getTable()) && !tables
                        .contains(jk.getAttr1().getTable()))) {
               jkSet.add(jk);
            }
         }
      }

      return jkSet;
   }

}
