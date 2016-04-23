/**
 * 
 */
package cy.ac.cut.cs.workloadgen.xml;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import cy.ac.cut.cs.workloadgen.schema.AttrStatsList;
import cy.ac.cut.cs.workloadgen.schema.AttrStatsRange;
import cy.ac.cut.cs.workloadgen.schema.Attribute;
import cy.ac.cut.cs.workloadgen.schema.Schema;
import cy.ac.cut.cs.workloadgen.schema.Table;
import cy.ac.cut.cs.workloadgen.schema.Attribute.AttrType;

/**
 * Used for parsing XML representation of the attribute statistics
 * 
 * @author herodotos.herodotou
 */
public class StatsXmlParser {

   // Element and attribute names in XML
   private static String ELEMENT_SCHEMA_STATS = "schemaStats";
   private static String ELEMENT_TABLE_STATS = "tableStats";
   private static String ELEMENT_ATTR_STATS = "attrStats";
   private static String ELEMENT_LIST_VALUES = "listValues";
   private static String ELEMENT_RANGE_VALUES = "rangeValues";
   private static String ELEMENT_VALUE = "value";
   private static String ELEMENT_MIN = "min";
   private static String ELEMENT_MAX = "max";
   private static String ATTRIBUTE_NAME = "name";
   private static String ATTRIBUTE_TYPE = "type";

   private static SimpleDateFormat DateFormatter = new SimpleDateFormat(
         "yyyy-MM-dd");

   /**
    * Import attribute statistics from the XML file into the schema
    * 
    * @param xmlFile
    * @param schema
    * @throws StatsXmlParsingException
    */
   static public void ImportAttrStats(File xmlFile, Schema schema)
         throws StatsXmlParsingException {
      Document doc = null;

      // Parse the XML file
      try {
         DocumentBuilderFactory dbFactory = DocumentBuilderFactory
               .newInstance();
         DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
         doc = dBuilder.parse(xmlFile);
      } catch (Exception e) {
         throw new StatsXmlParsingException(
               "Failed to parse the XML input file for the stats", e);
      }

      // Get the root element, which should be 'schemaStats'
      doc.getDocumentElement().normalize();
      Element root = doc.getDocumentElement();
      if (!root.getTagName().equalsIgnoreCase(ELEMENT_SCHEMA_STATS))
         throw new StatsXmlParsingException("Invalid XML file: "
               + "expected root element to be 'schemaStats'");

      // Validate schema name
      if (!root.hasAttribute(ATTRIBUTE_NAME))
         throw new StatsXmlParsingException("Invalid XML file: "
               + "missing schema name");

      String schemaName = root.getAttribute(ATTRIBUTE_NAME);
      if (!schemaName.equalsIgnoreCase(schema.getSchemaName()))
         throw new StatsXmlParsingException("Invalid XML file: "
               + "incorrect schema name, expected " + schema.getSchemaName());

      // Process all the table stats
      NodeList nodeList = root.getElementsByTagName(ELEMENT_TABLE_STATS);
      for (int i = 0; i < nodeList.getLength(); ++i) {
         Element eTableStats = (Element) nodeList.item(i);
         ParseTableStats(eTableStats, schema);
      }
   }

   /**
    * Parse the tableStats XML element and create the table statistics
    * 
    * @param eTableStats
    * @param schema
    * @throws StatsXmlParsingException
    */
   private static void ParseTableStats(Element eTableStats, Schema schema)
         throws StatsXmlParsingException {

      // Get the table
      if (!eTableStats.hasAttribute(ATTRIBUTE_NAME))
         throw new StatsXmlParsingException("Invalid XML file: "
               + "missing table name");

      Table table = schema.getTable(eTableStats.getAttribute(ATTRIBUTE_NAME));
      if (table == null)
         throw new StatsXmlParsingException("Invalid XML file: "
               + "schema does not contain table "
               + eTableStats.getAttribute(ATTRIBUTE_NAME));

      // Get all the attributes stats of the table
      NodeList attrList = eTableStats.getElementsByTagName(ELEMENT_ATTR_STATS);
      for (int i = 0; i < attrList.getLength(); ++i) {
         Element eAttrStats = (Element) attrList.item(i);
         ParseAttributeStats(eAttrStats, table);
      }
   }

   /**
    * Parse the attrStats XML element and create the attribute statistics
    * 
    * @param eAttrStats
    * @param table
    * @throws StatsXmlParsingException
    */
   private static void ParseAttributeStats(Element eAttrStats, Table table)
         throws StatsXmlParsingException {

      // Validate and get the attribute name
      if (!eAttrStats.hasAttribute(ATTRIBUTE_NAME))
         throw new StatsXmlParsingException("Invalid XML file: "
               + "missing attribute name for table " + table);

      String attrName = eAttrStats.getAttribute(ATTRIBUTE_NAME);
      if (!table.hasAttribute(attrName))
         throw new StatsXmlParsingException("Invalid XML file: table "
               + table.getName() + " does not contain attribute " + attrName);

      // Validate and get the attribute type
      if (!eAttrStats.hasAttribute(ATTRIBUTE_TYPE))
         throw new StatsXmlParsingException("Invalid XML file: "
               + "missing attribute type for attribute " + attrName);

      AttrType type = AttrType.getEnum(eAttrStats.getAttribute(ATTRIBUTE_TYPE));
      if (type == AttrType.AT_UNKNOWN)
         throw new StatsXmlParsingException("Invalid XML file: "
               + "unknown attribute type="
               + eAttrStats.getAttribute(ATTRIBUTE_TYPE));

      Attribute attribute = table.getAttribute(attrName);
      if (attribute.getType() != type)
         throw new StatsXmlParsingException("Invalid XML file: "
               + "incorrect type for attribute " + attrName);

      NodeList listValues = eAttrStats
            .getElementsByTagName(ELEMENT_LIST_VALUES);
      NodeList rangeValues = eAttrStats
            .getElementsByTagName(ELEMENT_RANGE_VALUES);
      if (listValues.getLength() == 1) {
         // Get the list of values
         Element eListValues = (Element) listValues.item(0);
         ParseListValues(eListValues, attribute);
      } else if (rangeValues.getLength() == 1) {
         // Get the range of values
         Element eRangeValues = (Element) rangeValues.item(0);
         ParseRangeValues(eRangeValues, attribute);
      } else {
         throw new StatsXmlParsingException("Invalid XML file: "
               + "expected listValues or rangeValues for attribute " + attrName);
      }
   }

   /**
    * Parse the listValues XML element and get the list of values for the
    * attribute
    * 
    * @param eListValues
    * @param attribute
    * @throws StatsXmlParsingException
    */
   private static void ParseListValues(Element eListValues, Attribute attribute)
         throws StatsXmlParsingException {

      NodeList nodeList = eListValues.getElementsByTagName(ELEMENT_VALUE);
      if (nodeList.getLength() == 0)
         throw new StatsXmlParsingException("Invalid XML file: "
               + "missing list of values for " + attribute);

      // Get the list of values and parse them based on the attribute type
      List<Object> values = new ArrayList<Object>(nodeList.getLength());
      String value = null;
      AttrType type = attribute.getType();

      for (int i = 0; i < nodeList.getLength(); ++i) {
         Element eValue = (Element) nodeList.item(i);
         value = eValue.getTextContent().trim();

         try {
            switch (type) {
            case AT_CHAR:
               values.add(value.toCharArray()[0]);
               break;
            case AT_DATE:
               values.add(DateFormatter.parse(value));
               break;
            case AT_DOUBLE:
               values.add(Double.parseDouble(value));
               break;
            case AT_INTEGER:
               values.add(Integer.parseInt(value));
               break;
            case AT_STRING:
               values.add(value);
               break;
            case AT_UNKNOWN:
            default:
               throw new StatsXmlParsingException("Invalid XML file: "
                     + "unexpected type for " + attribute);
            }
         } catch (Exception e) {
            throw new StatsXmlParsingException("Invalid XML file: "
                  + "error parsing value " + value, e);
         }
      }

      // Set the appropriate statistics instance based on type
      attribute.setStats(new AttrStatsList(type, values));
   }

   /**
    * Parse the rangeValues XML element and get the range of values for the
    * attribute
    * 
    * @param eRangeValues
    * @param attribute
    * @throws StatsXmlParsingException
    */
   private static void ParseRangeValues(Element eRangeValues,
         Attribute attribute) throws StatsXmlParsingException {

      // Get the min and max value
      NodeList minList = eRangeValues.getElementsByTagName(ELEMENT_MIN);
      if (minList.getLength() != 1)
         throw new StatsXmlParsingException("Invalid XML file: "
               + "problematic min value for " + attribute);

      NodeList maxList = eRangeValues.getElementsByTagName(ELEMENT_MAX);
      if (maxList.getLength() != 1)
         throw new StatsXmlParsingException("Invalid XML file: "
               + "problematic max value for " + attribute);

      AttrType type = attribute.getType();
      Element eMin = (Element) minList.item(0);
      Element eMax = (Element) maxList.item(0);
      String strMin = eMin.getTextContent().trim();
      String strMax = eMax.getTextContent().trim();

      // Set the statistics instance based on type
      AttrStatsRange attrStats = null;
      try {
         switch (type) {
         case AT_CHAR:
            attrStats = new AttrStatsRange(type, strMin.toCharArray()[0],
                  strMax.toCharArray()[0]);
            break;
         case AT_DATE:
            attrStats = new AttrStatsRange(type, DateFormatter.parse(strMin),
                  DateFormatter.parse(strMax));
            break;
         case AT_DOUBLE:
            attrStats = new AttrStatsRange(type, Double.parseDouble(strMin),
                  Double.parseDouble(strMax));
            break;
         case AT_INTEGER:
            attrStats = new AttrStatsRange(type, Integer.parseInt(strMin),
                  Integer.parseInt(strMax));
            break;
         case AT_STRING:
            attrStats = new AttrStatsRange(type, strMin, strMax);
            break;
         case AT_UNKNOWN:
         default:
            throw new StatsXmlParsingException("Invalid XML file: "
                  + "unexpected type for " + attribute);
         }
      } catch (Exception e) {
         throw new StatsXmlParsingException("Invalid XML file: "
               + "error parsing range for " + attribute, e);
      }

      // Set attribute stats
      attribute.setStats(attrStats);
   }

   /**
    * Custom XML parsing exception
    * 
    * @author herodotos.herodotou
    */
   public static class StatsXmlParsingException extends Exception {

      private static final long serialVersionUID = 472976341729536260L;

      public StatsXmlParsingException(String message) {
         super(message);
      }

      public StatsXmlParsingException(String message, Throwable cause) {
         super(message, cause);
      }

   }

}
