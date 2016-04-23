/**
 * 
 */
package uk.ac.ox.cs.pdq.workloadgen.xml;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import uk.ac.ox.cs.pdq.workloadgen.policies.IPolicy;
import uk.ac.ox.cs.pdq.workloadgen.policies.Parameter;
import uk.ac.ox.cs.pdq.workloadgen.policies.Parameter.InvalidParameterException;
import uk.ac.ox.cs.pdq.workloadgen.policies.querygen.IQueryFilterPolicy;
import uk.ac.ox.cs.pdq.workloadgen.policies.querygen.IQueryJoinPolicy;
import uk.ac.ox.cs.pdq.workloadgen.policies.querygen.IQueryProjectPolicy;
import uk.ac.ox.cs.pdq.workloadgen.policies.querygen.QueryGenPolicy;
import uk.ac.ox.cs.pdq.workloadgen.policies.viewgen.IViewFilterPolicy;
import uk.ac.ox.cs.pdq.workloadgen.policies.viewgen.IViewJoinPolicy;
import uk.ac.ox.cs.pdq.workloadgen.policies.viewgen.IViewProjectPolicy;
import uk.ac.ox.cs.pdq.workloadgen.policies.viewgen.ViewGenPolicy;

/**
 * Used for parsing the XML representation of the policies and their parameters
 * 
 * @author herodotos.herodotou
 */
public class PoliciesXmlParser {

   // Element and attribute names in XML
   private static String ELEMENT_POLICIES = "Policies";
   private static String ELEMENT_QUERY_GEN_POLICY = "QueryGenPolicy";
   private static String ELEMENT_VIEW_GEN_POLICY = "ViewGenPolicy";
   private static String ELEMENT_POLICY = "Policy";
   private static String ELEMENT_PARAMETER = "Parameter";
   private static String ELEMENT_NAME = "Name";
   private static String ELEMENT_VALUE = "Value";
   private static String ATTRIBUTE_NUM_QUERIES = "numQueries";
   private static String ATTRIBUTE_NUM_VIEWS = "numViews";
   private static String ATTRIBUTE_CLASS = "class";

   /**
    * Import the query generation policy and its parameters from the XML file
    * 
    * @param xmlFile
    * @return the query generation policy
    * @throws PoliciesXmlParsingException
    */
   static public QueryGenPolicy ImportQueryGenPolicy(File xmlFile)
         throws PoliciesXmlParsingException {

      // Get the root element
      Element root = GetPoliciesRootElement(xmlFile);

      // Create the high-level query gen policy
      NodeList nodeList = root.getElementsByTagName(ELEMENT_QUERY_GEN_POLICY);
      if (nodeList.getLength() != 1)
         throw new PoliciesXmlParsingException("Invalid XML file: "
               + "expected 1 QueryGenElement");

      // Get the number of queries
      Element eQueryGen = (Element) nodeList.item(0);
      if (!eQueryGen.hasAttribute(ATTRIBUTE_NUM_QUERIES))
         throw new PoliciesXmlParsingException("Invalid XML file: "
               + "missing number of queries for QueryGenElement");

      int numQueries = 0;

      try {
         numQueries = Integer.parseInt(eQueryGen
               .getAttribute(ATTRIBUTE_NUM_QUERIES));
      } catch (NumberFormatException e) {
         throw new PoliciesXmlParsingException("Invalid XML file: "
               + "number of queries is not a number", e);
      }

      QueryGenPolicy queryGen = new QueryGenPolicy(numQueries);

      // Create the policies
      nodeList = eQueryGen.getElementsByTagName(ELEMENT_POLICY);
      for (int i = 0; i < nodeList.getLength(); ++i) {
         IPolicy policy = ParsePolicy((Element) nodeList.item(i));

         if (IQueryJoinPolicy.class.isAssignableFrom(policy.getClass())) {
            queryGen.setJoinPolicy((IQueryJoinPolicy) policy);
         } else if (IQueryFilterPolicy.class
               .isAssignableFrom(policy.getClass())) {
            queryGen.setFilterPolicy((IQueryFilterPolicy) policy);
         } else if (IQueryProjectPolicy.class.isAssignableFrom(policy
               .getClass())) {
            queryGen.setProjectPolicy((IQueryProjectPolicy) policy);
         } else {
            throw new PoliciesXmlParsingException("Invalid XML file: "
                  + "policy class does not implement correct interface: "
                  + policy.getClass().getName());
         }
      }

      if (!queryGen.validatePolicies()) {
         throw new PoliciesXmlParsingException("Invalid XML file: "
               + "missing a policy for query generation");
      }

      return queryGen;
   }
   
   /**
    * Import the query generation policy and its parameters from the XML file
    * 
    * @param xmlFile
    * @return the query generation policy
    * @throws PoliciesXmlParsingException
    */
   static public ViewGenPolicy ImportViewGenPolicy(File xmlFile)
         throws PoliciesXmlParsingException {

      // Get the root element
      Element root = GetPoliciesRootElement(xmlFile);

      // Create the high-level query gen policy
      NodeList nodeList = root.getElementsByTagName(ELEMENT_VIEW_GEN_POLICY);
      if (nodeList.getLength() != 1)
         throw new PoliciesXmlParsingException("Invalid XML file: "
               + "expected 1 QueryGenElement");

      // Get the number of queries
      Element eQueryGen = (Element) nodeList.item(0);
      if (!eQueryGen.hasAttribute(ATTRIBUTE_NUM_VIEWS))
         throw new PoliciesXmlParsingException("Invalid XML file: "
               + "missing number of queries for QueryGenElement");

      int numQueries = 0;

      try {
         numQueries = Integer.parseInt(eQueryGen
               .getAttribute(ATTRIBUTE_NUM_VIEWS));
      } catch (NumberFormatException e) {
         throw new PoliciesXmlParsingException("Invalid XML file: "
               + "number of queries is not a number", e);
      }

      ViewGenPolicy queryGen = new ViewGenPolicy(numQueries);

      // Create the policies
      nodeList = eQueryGen.getElementsByTagName(ELEMENT_POLICY);
      for (int i = 0; i < nodeList.getLength(); ++i) {
         IPolicy policy = ParsePolicy((Element) nodeList.item(i));

         if (IViewJoinPolicy.class.isAssignableFrom(policy.getClass())) {
            queryGen.setJoinPolicy((IViewJoinPolicy) policy);
         } else if (IViewFilterPolicy.class
               .isAssignableFrom(policy.getClass())) {
            queryGen.setFilterPolicy((IViewFilterPolicy) policy);
         } else if (IViewProjectPolicy.class.isAssignableFrom(policy
               .getClass())) {
            queryGen.setProjectPolicy((IViewProjectPolicy) policy);
         } else {
            throw new PoliciesXmlParsingException("Invalid XML file: "
                  + "policy class does not implement correct interface: "
                  + policy.getClass().getName());
         }
      }

      if (!queryGen.validatePolicies()) {
         throw new PoliciesXmlParsingException("Invalid XML file: "
               + "missing a policy for query generation");
      }

      return queryGen;
   }

   /**
    * Get the root element of the XML document, which should be 'policies'
    * 
    * @param xmlFile
    * @return the root element
    * @throws PoliciesXmlParsingException
    */
   private static Element GetPoliciesRootElement(File xmlFile)
         throws PoliciesXmlParsingException {
      Document doc = null;

      // Parse the XML file
      try {
         DocumentBuilderFactory dbFactory = DocumentBuilderFactory
               .newInstance();
         DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
         doc = dBuilder.parse(xmlFile);
      } catch (Exception e) {
         throw new PoliciesXmlParsingException(
               "Failed to parse the XML input file for the query policy", e);
      }

      // Get the root element, which should be 'policies'
      doc.getDocumentElement().normalize();
      Element root = doc.getDocumentElement();
      if (!root.getTagName().equalsIgnoreCase(ELEMENT_POLICIES))
         throw new PoliciesXmlParsingException("Invalid XML file: "
               + "expected root element to be 'policies'");

      return root;
   }

   /**
    * Parse a policy XML element. This method will dynamically create an
    * instance of an object with the class name provided in the policy. This
    * instance must implement the IPolicy interface.
    * 
    * @param ePolicy
    * @return a policy
    * @throws PoliciesXmlParsingException
    */
   private static IPolicy ParsePolicy(Element ePolicy)
         throws PoliciesXmlParsingException {

      if (!ePolicy.hasAttribute(ATTRIBUTE_CLASS))
         throw new PoliciesXmlParsingException("Invalid XML file: "
               + "missing policy class name");

      // Create the new policy dynamically
      Object obj = null;
      String className = ePolicy.getAttribute(ATTRIBUTE_CLASS);

      try {
         obj = Class.forName(className).newInstance();
      } catch (Exception e) {
         throw new PoliciesXmlParsingException("Invalid XML file: "
               + "failed to create instance for class " + className, e);
      }

      if (!IPolicy.class.isAssignableFrom(obj.getClass())) {
         throw new PoliciesXmlParsingException("Invalid XML file: "
               + "the class " + className + " does not implement the IPolicy");
      }

      IPolicy policy = (IPolicy) obj;

      // Get the parameters for the policy
      try {
         if (!policy.initialize(ParseParameterList(ePolicy)))
            throw new PoliciesXmlParsingException(
                  "Failed to initialize the policy");
      } catch (InvalidParameterException e) {
         throw new PoliciesXmlParsingException(
               "Failed to initialize the policy", e);
      }

      return policy;
   }

   /**
    * Parse the parameter sub-elements of the input XML element
    * 
    * @param elem
    * @return a list of parameters (name/value pairs)
    * @throws PoliciesXmlParsingException
    */
   private static List<Parameter> ParseParameterList(Element elem)
         throws PoliciesXmlParsingException {

      NodeList nodeList = elem.getElementsByTagName(ELEMENT_PARAMETER);
      List<Parameter> params = new ArrayList<Parameter>(nodeList.getLength());

      // Process all parameters
      for (int i = 0; i < nodeList.getLength(); ++i) {
         Element param = (Element) nodeList.item(i);

         // Get the name/value of the parameter
         NodeList nameList = param.getElementsByTagName(ELEMENT_NAME);
         if (nameList.getLength() != 1)
            throw new PoliciesXmlParsingException("Invalid XML file: "
                  + "missing parameter name");

         NodeList valueList = param.getElementsByTagName(ELEMENT_VALUE);
         if (valueList.getLength() != 1)
            throw new PoliciesXmlParsingException("Invalid XML file: "
                  + "missing parameter name");

         params.add(new Parameter(nameList.item(0).getTextContent().trim(),
               valueList.item(0).getTextContent().trim()));
      }

      return params;
   }

   /**
    * Custom XML parsing exception
    * 
    * @author herodotos.herodotou
    */
   public static class PoliciesXmlParsingException extends Exception {

      private static final long serialVersionUID = -5437005857881893007L;

      public PoliciesXmlParsingException(String message) {
         super(message);
      }

      public PoliciesXmlParsingException(String message, Throwable cause) {
         super(message, cause);
      }

   }

}
