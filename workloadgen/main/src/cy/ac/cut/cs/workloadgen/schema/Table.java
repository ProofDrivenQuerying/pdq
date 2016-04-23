/**
 * 
 */
package cy.ac.cut.cs.workloadgen.schema;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import com.google.common.collect.Lists;

/**
 * Represents a database table.
 * 
 * @author herodotos.herodotou
 */
public class Table {

   private String name;
   private Map<String, Attribute> attributes;
   private List<Attribute> attributesList;
   private List<Attribute> primaryKey;
   private Set<ForeignKey> foreignKeys;
   private Set<JoinableKey> joinableKeys;
   private Set<Attribute> filterableAttrs;
	
   /**
    * @param name
    */
   public Table(String name) {
      this.name = name;
      this.attributes = new HashMap<String, Attribute>();
      this.attributesList = Lists.newArrayList();
      this.primaryKey = null;
      this.foreignKeys = new HashSet<ForeignKey>(2);
      this.joinableKeys = new HashSet<JoinableKey>(3);
      this.filterableAttrs = new HashSet<Attribute>(3);
   }

   /**
    * @return the table name
    */
   public String getName() {
      return name;
   }

   /**
    * @param attrName
    *           attribute name
    * @return the attribute or null
    */
   public Attribute getAttribute(String attrName) {
      return attributes.get(attrName);
   }

   /**
    * @return the attributes
    */
   public Collection<Attribute> getAttributes() {
      return attributes.values();
   }
   
   /**
    * 
    * @return the attributes
    */
   public List<Attribute> getAttributesList() {
	   return this.attributesList;
   }

   /**
    * @return the primary key (could be null if not set)
    */
   public List<Attribute> getPrimaryKey() {
      return primaryKey;
   }

   /**
    * @return the foreign keys
    */
   public Set<ForeignKey> getForeignKeys() {
      return foreignKeys;
   }

   /**
    * @return the joinable keys
    */
   public Set<JoinableKey> getJoinableKeys() {
      return joinableKeys;
   }

   /**
    * @return the filterable attributes
    */
   public Set<Attribute> getFilterableAttrs() {
      return filterableAttrs;
   }

   /**
    * @param primaryKey
    *           the primaryKey to set
    * @return true if the primary key is set successfully
    */
   public boolean setPrimaryKey(AttributeSet primaryKey) {
      if (!this.equals(primaryKey.getTable()))
         return false;

      this.primaryKey = primaryKey.getAttributes();
      return true;
   }

   /**
    * @param attribute
    *           the attribute to add
    * @return true if the attribute is added successfully
    */
   public boolean addAttribute(Attribute attribute) {
      if (!this.equals(attribute.getTable()))
         return false;

      if(attributes.put(attribute.getFullName(), attribute) == null) {
    	  this.attributesList.add(attribute);
    	  return true;
      }
      return false;
   }

   /**
    * @param foreignKey
    *           the foreignKey to add
    * @return true if the foreign key is added successfully
    */
   public boolean addForeignKey(ForeignKey foreignKey) {
      if (!this.equals(foreignKey.getReferencingSet().getTable()))
         return false;

      return this.foreignKeys.add(foreignKey);
   }

   /**
    * @param joinableKey
    *           the joinable key to add
    * @return true if the joinable key is added successfully
    */
   public boolean addJoinableKey(JoinableKey joinableKey) {
      if (!this.equals(joinableKey.getAttr1().getTable())
            && !this.equals(joinableKey.getAttr2().getTable()))
         return false;

      return this.joinableKeys.add(joinableKey);
   }

   /**
    * @param filterableAttr
    *           the attribute to set
    * @return true if the filterable is added successfully
    */
   public boolean addFilterableAttr(Attribute filterableAttr) {
      if (!this.equals(filterableAttr.getTable()))
         return false;

      return this.filterableAttrs.add(filterableAttr);
   }

   /**
    * @param attrName
    * @return true if the table contains such an attribute
    */
   public boolean hasAttribute(String attrName) {
      return attributes.containsKey(attrName);
   }

   /*
    * (non-Javadoc)
    * 
    * @see java.lang.Object#toString()
    */
   @Override
   public String toString() {
      return name;
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
		return Table.class.isInstance(o)
				&& this.toString().equals(((Table) o).toString());
	}

	/**
	 * @return int
	 */
	@Override
	public int hashCode() {
		return Objects.hash(this.toString());
	}

}
