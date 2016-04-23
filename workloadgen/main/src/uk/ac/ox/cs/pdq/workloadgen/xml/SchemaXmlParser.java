/**
 * 
 */
package uk.ac.ox.cs.pdq.workloadgen.xml;

import java.io.File;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import uk.ac.ox.cs.pdq.workloadgen.schema.Attribute;
import uk.ac.ox.cs.pdq.workloadgen.schema.AttributeSet;
import uk.ac.ox.cs.pdq.workloadgen.schema.ForeignKey;
import uk.ac.ox.cs.pdq.workloadgen.schema.JoinableKey;
import uk.ac.ox.cs.pdq.workloadgen.schema.Schema;
import uk.ac.ox.cs.pdq.workloadgen.schema.Table;
import uk.ac.ox.cs.pdq.workloadgen.schema.Attribute.AttrType;

/**
 * Used for parsing the XML representation of a database schema
 * 
 * @author herodotos.herodotou
 */
public class SchemaXmlParser {

   // Element and attribute names in XML
   private static String ELEMENT_SCHEMA = "schema";
   private static String ELEMENT_TABLE = "table";
   private static String ELEMENT_ATTRIBUTE = "attribute";
   private static String ELEMENT_PRIMARY_KEY = "primaryKey";
   private static String ELEMENT_FOREIGN_KEY = "foreignKey";
   private static String ELEMENT_JOINABLE_KEY = "joinableKey";
   private static String ELEMENT_FILTERED = "filtered";
   private static String ATTRIBUTE_NAME = "name";
   private static String ATTRIBUTE_TYPE = "type";
   private static String ATTRIBUTE_ATTR_NAME = "attrName";
   private static String ATTRIBUTE_ATTR_NAME_1 = "attrName1";
   private static String ATTRIBUTE_ATTR_NAME_2 = "attrName2";
   private static String ATTRIBUTE_ATTR_NAMES = "attrNames";
   private static String ATTRIBUTE_REFERENCING = "referencing";
   private static String ATTRIBUTE_REFERENCED = "referenced";

   /**
    * Parse the provided XML file to create a database schema
    * 
    * @param xmlFile
    * @return a schema
    * @throws SchemaXmlParsingException
    */
   static public Schema ImportSchema(File xmlFile)
         throws SchemaXmlParsingException {
      Document doc = null;

      // Parse the XML file
      try {
         DocumentBuilderFactory dbFactory = DocumentBuilderFactory
               .newInstance();
         DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
         doc = dBuilder.parse(xmlFile);
      } catch (Exception e) {
         throw new SchemaXmlParsingException(
               "Failed to parse the XML input file for the schema", e);
      }

      // Get the root element, which should be 'schema'
      doc.getDocumentElement().normalize();
      Element root = doc.getDocumentElement();
      if (!root.getTagName().equalsIgnoreCase(ELEMENT_SCHEMA))
         throw new SchemaXmlParsingException("Invalid XML file: "
               + "expected root element to be 'schema'");

      if (!root.hasAttribute(ATTRIBUTE_NAME))
         throw new SchemaXmlParsingException("Invalid XML file: "
               + "missing schema name");

      Schema schema = new Schema(root.getAttribute(ATTRIBUTE_NAME));

      // Create all the tables in the schema
      NodeList tableList = root.getElementsByTagName(ELEMENT_TABLE);
      for (int i = 0; i < tableList.getLength(); ++i) {
         Element eTable = (Element) tableList.item(i);
         Table table = ParseTable1(eTable);
         if (!schema.AddTable(table))
            throw new SchemaXmlParsingException("Invalid XML file: "
                  + "unable to add table " + table + " in the schema");
      }

      // Re-parse tables to generate foreign keys, joinable keys, and filtered
      // attributes (required for cross-reference)
      for (int i = 0; i < tableList.getLength(); ++i) {
         Element eTable = (Element) tableList.item(i);
         ParseTable2(eTable, schema);
      }

      return schema;
   }

   /**
    * Parse the table XML element to generate a table instance with its
    * attributes and its primary key.
    * 
    * @param eTable
    * @return a table
    * @throws SchemaXmlParsingException
    */
   private static Table ParseTable1(Element eTable)
         throws SchemaXmlParsingException {

      // Create the new table
      if (!eTable.hasAttribute(ATTRIBUTE_NAME))
         throw new SchemaXmlParsingException("Invalid XML file: "
               + "missing table name");
      Table table = new Table(eTable.getAttribute(ATTRIBUTE_NAME));

      // Create all the attributes of the table
      NodeList attrList = eTable.getElementsByTagName(ELEMENT_ATTRIBUTE);
      for (int i = 0; i < attrList.getLength(); ++i) {
         Element eAttr = (Element) attrList.item(i);
         Attribute attr = ParseAttribute(eAttr, table);
         if (!table.addAttribute(attr))
            throw new SchemaXmlParsingException("Invalid XML file: "
                  + "unable to add attribute " + attr + " to table " + table);
      }

      // Create the primary key
      NodeList primKeyList = eTable.getElementsByTagName(ELEMENT_PRIMARY_KEY);
      if (primKeyList.getLength() != 1)
         throw new SchemaXmlParsingException("Invalid XML file: "
               + "expected exactly 1 primary key element");
      Element ePrimKey = (Element) primKeyList.item(0);
      table.setPrimaryKey(ParsePrimaryKey(ePrimKey, table));

      return table;
   }

   /**
    * Parse the table XML element a second time to generate the foreign keys,
    * joinable keys, and filtered attributes for the given table
    * 
    * @param eTable
    * @param schema
    * @throws SchemaXmlParsingException
    */
   private static void ParseTable2(Element eTable, Schema schema)
         throws SchemaXmlParsingException {

      Table table = schema.getTable(eTable.getAttribute(ATTRIBUTE_NAME));

      // Create all the foreign keys of the table
      NodeList nodeList = eTable.getElementsByTagName(ELEMENT_FOREIGN_KEY);
      for (int i = 0; i < nodeList.getLength(); ++i) {
         Element eForKey = (Element) nodeList.item(i);
         ForeignKey forKey = ParseForeignKey(eForKey, table, schema);
         if (!table.addForeignKey(forKey))
            throw new SchemaXmlParsingException("Invalid XML file: "
                  + "unable to add " + forKey + " to table " + table);

      }

      // Create all the join keys of the table
      nodeList = eTable.getElementsByTagName(ELEMENT_JOINABLE_KEY);
      for (int i = 0; i < nodeList.getLength(); ++i) {
         Element eJoinableKey = (Element) nodeList.item(i);
         JoinableKey joinableKey = ParseJoinableKey(eJoinableKey, table, schema);
         if (!table.addJoinableKey(joinableKey))
            throw new SchemaXmlParsingException("Invalid XML file: "
                  + "unable to add " + joinableKey + " to table " + table);

      }

      // Create all the filtered attributes of the table
      nodeList = eTable.getElementsByTagName(ELEMENT_FILTERED);
      for (int i = 0; i < nodeList.getLength(); ++i) {
         Element eFiltered = (Element) nodeList.item(i);
         Attribute attr = ParseFiltered(eFiltered, table);
         if (!table.addFilterableAttr(attr))
            throw new SchemaXmlParsingException("Invalid XML file: "
                  + "unable to add filter " + attr + " to table " + table);

      }
   }

   /**
    * Parse the attribute XML element to generate an attribute instance
    * 
    * @param eAttr
    * @return an attribute
    * @throws SchemaXmlParsingException
    */
   private static Attribute ParseAttribute(Element eAttr, Table table)
         throws SchemaXmlParsingException {

      // Validate and get the attribute name
      if (!eAttr.hasAttribute(ATTRIBUTE_NAME))
         throw new SchemaXmlParsingException("Invalid XML file: "
               + "missing attribute name for table " + table);

      String fullName = eAttr.getAttribute(ATTRIBUTE_NAME);
      if (!fullName.startsWith(table.getName()) && !fullName.contains("."))
         fullName = table.getName() + "." + fullName;
      else if (!fullName.startsWith(table.getName()) || !fullName.contains("."))
         throw new SchemaXmlParsingException("Invalid XML file: "
               + "invalid attribute name=" + fullName);

      // Validate and get the attribute type
      if (!eAttr.hasAttribute(ATTRIBUTE_TYPE))
         throw new SchemaXmlParsingException("Invalid XML file: "
               + "missing attribute type for " + fullName);

      AttrType type = AttrType.getEnum(eAttr.getAttribute(ATTRIBUTE_TYPE));
      if (type == AttrType.AT_UNKNOWN)
         throw new SchemaXmlParsingException("Invalid XML file: "
               + "unknown attribute type=" + eAttr.getAttribute(ATTRIBUTE_TYPE));

      return new Attribute(table, fullName, type);
   }

   /**
    * Parse the primaryKey XML element to generate an attribute set representing
    * the primary key of a table
    * 
    * @param ePrimKey
    * @param table
    * @return
    * @throws SchemaXmlParsingException
    */
   private static AttributeSet ParsePrimaryKey(Element ePrimKey, Table table)
         throws SchemaXmlParsingException {
      // Validate the attributes
      if (!ePrimKey.hasAttribute(ATTRIBUTE_ATTR_NAMES))
         throw new SchemaXmlParsingException("Invalid XML file: "
               + "missing PK attribute names");

      AttributeSet attrSet = ParseAttributeSet(
            ePrimKey.getAttribute(ATTRIBUTE_ATTR_NAMES), table);

      return attrSet;
   }

   /**
    * Parse the foreignKey XML element to generate a foreignKey instance
    * 
    * @param eForKey
    * @param table
    * @param schema
    * @return a foreign key
    * @throws SchemaXmlParsingException
    */
   private static ForeignKey ParseForeignKey(Element eForKey, Table table,
         Schema schema) throws SchemaXmlParsingException {

      // Get the referencing attributes
      if (!eForKey.hasAttribute(ATTRIBUTE_REFERENCING))
         throw new SchemaXmlParsingException("Invalid XML file: "
               + "missing FK referencing attribute names");

      AttributeSet attrSet1 = ParseAttributeSet(
            eForKey.getAttribute(ATTRIBUTE_REFERENCING), table);

      // Get the referenced attributes
      if (!eForKey.hasAttribute(ATTRIBUTE_REFERENCED))
         throw new SchemaXmlParsingException("Invalid XML file: "
               + "missing FK referenced attribute names");

      String referenced = eForKey.getAttribute(ATTRIBUTE_REFERENCED);
      int i = referenced.indexOf('.');
      if (i < 0)
         throw new SchemaXmlParsingException("Invalid XML file: "
               + "FK referenced=" + referenced + " are missing table name");

      String refTableName = referenced.substring(0, i);
      Table refTable = schema.getTable(refTableName);
      if (refTable == null)
         throw new SchemaXmlParsingException("Invalid XML file: "
               + "invalid table name in FK referenced=" + referenced);

      AttributeSet attrSet2 = ParseAttributeSet(referenced, refTable);

      return new ForeignKey(attrSet1, attrSet2);
   }

   /**
    * Parse the joinableKey XML element to generate a joinableKey instance
    * (which represents two joinable attributes)
    * 
    * @param eForKey
    * @param table
    * @param schema
    * @return a joinable key
    * @throws SchemaXmlParsingException
    */
   private static JoinableKey ParseJoinableKey(Element eJoinableKey,
         Table table, Schema schema) throws SchemaXmlParsingException {

      // Get the first attribute
      if (!eJoinableKey.hasAttribute(ATTRIBUTE_ATTR_NAME_1))
         throw new SchemaXmlParsingException("Invalid XML file: "
               + "missing JK attribute name 1");

      String attrName1 = eJoinableKey.getAttribute(ATTRIBUTE_ATTR_NAME_1);
      Attribute attr1 = table.getAttribute(attrName1);
      if (attr1 == null)
         throw new SchemaXmlParsingException("Invalid XML file: "
               + "invalid attribute name=" + attrName1 + " for table " + table);

      // Get the second attribute (from a different table)
      if (!eJoinableKey.hasAttribute(ATTRIBUTE_ATTR_NAME_2))
         throw new SchemaXmlParsingException("Invalid XML file: "
               + "missing JK attribute name 2");

      String attrName2 = eJoinableKey.getAttribute(ATTRIBUTE_ATTR_NAME_2);
      int i = attrName2.indexOf('.');
      if (i < 0)
         throw new SchemaXmlParsingException("Invalid XML file: "
               + "JK attrName2=" + attrName2 + " is missing table name");

      String refTableName = attrName2.substring(0, i);
      Table refTable = schema.getTable(refTableName);
      if (refTable == null)
         throw new SchemaXmlParsingException("Invalid XML file: "
               + "invalid table name in JK attrName2=" + attrName2);

      Attribute attr2 = refTable.getAttribute(attrName2);
      if (attr2 == null)
         throw new SchemaXmlParsingException("Invalid XML file: "
               + "invalid attribute name=" + attrName2 + " for table "
               + refTable);

      return new JoinableKey(attr1, attr2);
   }

   /**
    * Parse the Filtered XML element to generate a filterable attribute
    * 
    * @param eFiltered
    * @param table
    * @return an attribute
    * @throws SchemaXmlParsingException
    */
   private static Attribute ParseFiltered(Element eFiltered, Table table)
         throws SchemaXmlParsingException {

      // Get the attribute
      if (!eFiltered.hasAttribute(ATTRIBUTE_ATTR_NAME))
         throw new SchemaXmlParsingException("Invalid XML file: "
               + "missing filtered attribute name");

      String attrName = eFiltered.getAttribute(ATTRIBUTE_ATTR_NAME);
      Attribute attr = table.getAttribute(attrName);
      if (attr == null)
         throw new SchemaXmlParsingException("Invalid XML file: "
               + "invalid attribute name=" + attrName + " for table " + table);

      return attr;
   }

   /**
    * Parse a space-separated string of attributes to generate an attribute set
    * belonging to the provided table
    * 
    * @param attrNames
    * @param table
    * @return an attribute set
    * @throws SchemaXmlParsingException
    */
   private static AttributeSet ParseAttributeSet(String attrNames, Table table)
         throws SchemaXmlParsingException {
      String[] arrNames = attrNames.trim().split("\\s+");
      ArrayList<Attribute> attrs = new ArrayList<Attribute>(arrNames.length);
      for (String attrName : arrNames) {
         Attribute attr = table.getAttribute(attrName);
         if (attr == null)
            throw new SchemaXmlParsingException("Invalid XML file: "
                  + "unknown attribute " + attrName + "for table" + table);
         attrs.add(attr);
      }

      return new AttributeSet(table, attrs);
   }

   /**
    * Custom XML parsing exception
    * 
    * @author herodotos.herodotou
    */
   public static class SchemaXmlParsingException extends Exception {

      private static final long serialVersionUID = -1676626763226297647L;

      public SchemaXmlParsingException(String message) {
         super(message);
      }

      public SchemaXmlParsingException(String message, Throwable cause) {
         super(message, cause);
      }

   }

}
