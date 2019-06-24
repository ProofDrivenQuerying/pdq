package uk.ac.ox.cs.pdq.rest;

import uk.ac.ox.cs.pdq.cost.Cost;
import uk.ac.ox.cs.pdq.algebra.RelationalTerm;
import uk.ac.ox.cs.pdq.rest.jsonwrappers.*;
import uk.ac.ox.cs.pdq.ui.io.sql.SQLLikeQueryWriter;
import uk.ac.ox.cs.pdq.fol.ConjunctiveQuery;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMethod;
import uk.ac.ox.cs.pdq.db.Schema;
import uk.ac.ox.cs.pdq.io.jaxb.IOManager;
import java.util.HashMap;
import java.io.File;
import java.util.Map.Entry;

/*
  @info:
  `@RequestMapping` tag ensures that HTTP requests to /<my-request>
  are mapped to the <my-request>() method

  `@RestController` tag marks the class as a Spring MVC controller
  */

/**
 * Entry point for Rest application. JsonController defines and computes all the REST calls of the API.
 *
 * @author Camilo Ortiz
 */
@RestController
public class JsonController{
  private final String PATH_TO_SCHEMA_EXAMPLES;
  private String[] example_names;

  private HashMap<Integer, Schema> schemaList;
  private HashMap<Integer, ConjunctiveQuery> queryList;
  private SchemaName[] jsonSchemaList;
  private HashMap<Integer, File> casePropertyList;

  public JsonController(){
    this.PATH_TO_SCHEMA_EXAMPLES = "test/demo/case_";
    this.example_names = new String[]{"001","002", "003", "004", "005", "006" };
    this.jsonSchemaList = new SchemaName[example_names.length];
    this.schemaList = new HashMap<Integer, Schema>();
    this.queryList = new HashMap<Integer, ConjunctiveQuery>();
    this.casePropertyList = new HashMap<Integer, File>();

    for (int i = 0; i < example_names.length; i++){
      try {
        //get schema
        String pathToSchema = PATH_TO_SCHEMA_EXAMPLES+example_names[i]+"/schema.xml";
        String pathToQuery = PATH_TO_SCHEMA_EXAMPLES+example_names[i]+"/query.xml";
        String pathToCaseProperties = PATH_TO_SCHEMA_EXAMPLES+example_names[i]+"/case.properties";

        Schema schema = IOManager.importSchema(new File(pathToSchema));
        ConjunctiveQuery queries = IOManager.importQuery(new File(pathToQuery));
        File caseProperties = new File(pathToCaseProperties);



        //put schema in hash map
        schemaList.put(i, schema);

        //put query in hash map
        queryList.put(i, queries);

        //put case properties in hash map
        casePropertyList.put(i, caseProperties);

        //make JsonSchema and put it in JsonSchema array
        SchemaName jsonSchema = new SchemaName(schema, i);
        jsonSchemaList[i] = jsonSchema;

      }catch (Throwable e) {
        e.printStackTrace();
        System.exit(-1);
      }
    }
  }

  /**
   * Returns an initial list of schemas that only contains their id and names.
   *
   * @return SchemaName[]
   */
  @RequestMapping(value="/init_schemas", method=RequestMethod.GET, produces="application/json")
  public SchemaName[] init_schemas(){
    return this.jsonSchemaList;
  }
  /**
   * Returns relations associated with specific schema.
   *
   * @param id
   * @return JsonRelationList
   */
  @RequestMapping(value="/getRelations", method=RequestMethod.GET, produces="application/json")
  public JsonRelationList getRelations(@RequestParam(value="id") int id){

    Schema schema = schemaList.get(id);
    JsonRelationList toReturn = new JsonRelationList(schema, id);

    return toReturn;
  }
  /**
   * Returns Query associated with schema. As of now, each schema has only one query.
   *
   * @param id
   * @return JsonQuery
   */
  @RequestMapping(value="/getQueries", method=RequestMethod.GET, produces="application/json")
  public JsonQuery getQueries(@RequestParam(value="id") int id){

    ConjunctiveQuery query = queryList.get(id);
    Schema schema = schemaList.get(id);

    String query_string = SQLLikeQueryWriter.convert(query, schema);

    JsonQuery toReturn = new JsonQuery(id, query_string);
    return toReturn;
  }
  /**
   * Returns properties of the schema (DOES NOT WORK YET)
   *
   * @param id
   * @return File
   */
  @RequestMapping(value="/getProperties", method=RequestMethod.GET, produces="application/json")
  public File getProperties(@RequestParam(value="id") int id){

    File properties = casePropertyList.get(id);

    // JsonQuery toReturn = new JsonQuery(id, query_string);
    return properties;
  }
  /**
   * Returns Entry<RelationalTerm, Cost> that shows up as a long string
   *
   * @param id
   * @return
   */
  @RequestMapping(value="/plan", method=RequestMethod.GET, produces="application/json")
  public Entry<RelationalTerm, Cost> plan(@RequestParam("id") int id){
    Schema schema = schemaList.get(id);
    ConjunctiveQuery cq = queryList.get(id);
    File properties = casePropertyList.get(id);
    String pathToCatalog = PATH_TO_SCHEMA_EXAMPLES+example_names[id]+"/catalog.properties";

    try{
      Entry<RelationalTerm, Cost> plan = JsonPlanner.plan(schema, cq, properties, pathToCatalog);

      return plan;
    }catch (Throwable e) {
      e.printStackTrace();
      System.exit(-1);
      return null;
    }
  }
  /**
   * Returns JsonGraphicalPlan for use by the vx and d3.js libraries
   *
   * @param id
   * @return
   */
  @RequestMapping(value="/getGraphicalPlan", method=RequestMethod.GET, produces="application/json")
  public JsonGraphicalPlan getGraphicalPlan(@RequestParam("id") int id){
    Schema schema = schemaList.get(id);
    ConjunctiveQuery cq = queryList.get(id);
    File properties = casePropertyList.get(id);
    String pathToCatalog = PATH_TO_SCHEMA_EXAMPLES+example_names[id]+"/catalog.properties";

    JsonGraphicalPlan toReturn = null;
    try{

      toReturn = JsonPlanner.search(schema, cq, properties, pathToCatalog);

    }catch (Throwable e) {
      e.printStackTrace();
      System.exit(-1);

    }
    return toReturn;
  }

  @RequestMapping(value="/runPlan", method=RequestMethod.GET, produces="application/json")
  public JsonRunResults runPlan(@RequestParam("id") int id){
    Schema schema = schemaList.get(id);
    ConjunctiveQuery cq = queryList.get(id);
    File properties = casePropertyList.get(id);
    String pathToCatalog = PATH_TO_SCHEMA_EXAMPLES+example_names[id]+"/catalog.properties";

    JsonRunResults toReturn = null;

    try{
      RelationalTerm plan = JsonPlanner.planToObject(schema, cq, properties, pathToCatalog);
      toReturn = new JsonRunResults(Runner.runtime(schema, cq, properties, plan));

    }catch (Throwable e) {
      e.printStackTrace();
      System.exit(-1);
    }
    return toReturn;
  }
}
