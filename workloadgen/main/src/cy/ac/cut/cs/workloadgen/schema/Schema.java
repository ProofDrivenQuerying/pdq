/**
 * 
 */
package cy.ac.cut.cs.workloadgen.schema;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents a database schema
 * 
 * @author herodotos.herodotou
 */
public class Schema {

   private String schemaName;
   private Map<String, Table> tables;

   /**
    * @param schemaName
    */
   public Schema(String schemaName) {
      this.schemaName = schemaName;
      this.tables = new HashMap<String, Table>();
   }

   /**
    * @return the schemaName
    */
   public String getSchemaName() {
      return schemaName;
   }

   /**
    * @return all the tables in the schema
    */
   public Collection<Table> getTables() {
      return tables.values();
   }

   /**
    * @param tableName
    * @return the corresponding table or null
    */
   public Table getTable(String tableName) {
      return tables.get(tableName);
   }

   /**
    * Adds a new table in the schema
    * 
    * @param table
    *           the table to add
    * @return true if added successfully
    */
   public boolean AddTable(Table table) {
      return tables.put(table.getName(), table) == null;
   }
}
