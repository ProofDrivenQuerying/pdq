package uk.ac.ox.cs.pdq.rest;

import org.springframework.core.io.UrlResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uk.ac.ox.cs.pdq.algebra.RelationalTerm;
import uk.ac.ox.cs.pdq.rest.jsonobjects.*;
import uk.ac.ox.cs.pdq.rest.wrappermethods.*;
import uk.ac.ox.cs.pdq.ui.io.sql.SQLLikeQueryWriter;
import uk.ac.ox.cs.pdq.fol.ConjunctiveQuery;
import uk.ac.ox.cs.pdq.db.Schema;
import uk.ac.ox.cs.pdq.io.jaxb.IOManager;
import org.springframework.core.io.Resource;
import java.nio.file.Path;
import javax.servlet.http.HttpServletRequest;
import org.springframework.http.MediaType;
import java.util.HashMap;
import java.io.File;
import org.springframework.http.HttpHeaders;
import java.nio.file.Paths;


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
  private HashMap<Integer, JsonPlan> planList;
  private HashMap<Integer, JsonRunResults> runResultList;

  public JsonController(){
    this.PATH_TO_SCHEMA_EXAMPLES = "test/demo/case_";
    this.example_names = new String[]{"001","002", "003", "004", "005", "006", "007", "008", "009" };
    this.jsonSchemaList = new SchemaName[example_names.length];
    this.schemaList = new HashMap<Integer, Schema>();
    this.queryList = new HashMap<Integer, ConjunctiveQuery>();
    this.casePropertyList = new HashMap<Integer, File>();
    this.planList = new HashMap<Integer, JsonPlan>();
    this.runResultList = new HashMap<Integer, JsonRunResults>();

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
   * Returns Entry<RelationalTerm, Cost> that shows up as a long string
   *
   * @param id
   * @return
   */
  @RequestMapping(value="/plan", method=RequestMethod.GET, produces="application/json")
  public JsonPlan plan(@RequestParam("id") int id){
    JsonPlan previousPlan = planList.get(id);

    if(previousPlan != null) return previousPlan; //if we've already planned this schema and query, return it

    Schema schema = schemaList.get(id);
    ConjunctiveQuery cq = queryList.get(id);
    File properties = casePropertyList.get(id);
    String pathToCatalog = PATH_TO_SCHEMA_EXAMPLES+example_names[id]+"/catalog.properties";

    try{
      JsonPlan plan = JsonPlanner.plan(schema, cq, properties, pathToCatalog);

      planList.put(id, plan); //if we got here, we haven't planned yet, so put the plan in the map.

      return plan;
    }catch (Throwable e) {
      e.printStackTrace();
      System.exit(-1);
      return null;
    }
  }

  @RequestMapping(value="/runPlan", method=RequestMethod.GET, produces="application/json")
  public JsonRunResults runPlan(@RequestParam("id") int id){
    JsonRunResults toReturn = this.runResultList.get(id);
    if(toReturn != null) return toReturn;

    Schema schema = schemaList.get(id);
    ConjunctiveQuery cq = queryList.get(id);
    File properties = casePropertyList.get(id);
    RelationalTerm plan = planList.get(id).plan;

    try{
      toReturn = Runner.runtime(schema, cq, properties, plan);

      runResultList.put(id, toReturn);

    }catch (Throwable e) {
      e.printStackTrace();
      System.exit(-1);
    }
    return toReturn;
  }

  @GetMapping(value="/downloadRun/{id}")
  public ResponseEntity<Resource> downloadRun(@PathVariable int id, HttpServletRequest request){
      //idea: write the csv file to the case file and read it from there to send it as a resource.
      JsonRunResults results = runResultList.get(id);
      try{
          Runner.writeOutput(results.results, PATH_TO_SCHEMA_EXAMPLES+example_names[id]+"/results.csv");

          Resource resource = loadFileAsResource(PATH_TO_SCHEMA_EXAMPLES+example_names[id]+"/results.csv");

          String contentType = "text/csv";

          return ResponseEntity.ok().contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\""+Integer.toString(id)+"_results.csv\"")
                    .body(resource);

      }catch(Exception e){
          e.printStackTrace();

      }
      return null;
  }

    public Resource loadFileAsResource(String fileName) {
        try {
            Path filePath = Paths.get(fileName);

            Resource resource = new UrlResource(filePath.toUri());

            if(resource.exists()) {
                return resource;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }
}
