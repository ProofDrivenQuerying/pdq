package uk.ac.ox.cs.pdq.rest;

import org.springframework.core.io.UrlResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uk.ac.ox.cs.pdq.algebra.RelationalTerm;
import uk.ac.ox.cs.pdq.rest.jsonobjects.*;
import uk.ac.ox.cs.pdq.rest.eventhandlers.*;
import uk.ac.ox.cs.pdq.rest.jsonobjects.plan.JsonPlan;
import uk.ac.ox.cs.pdq.rest.jsonobjects.run.JsonRunResults;
import uk.ac.ox.cs.pdq.rest.jsonobjects.schema.JsonDependencyList;
import uk.ac.ox.cs.pdq.rest.jsonobjects.schema.JsonRelationList;
import uk.ac.ox.cs.pdq.rest.jsonobjects.schema.SchemaName;
import uk.ac.ox.cs.pdq.ui.io.sql.SQLLikeQueryWriter;
import uk.ac.ox.cs.pdq.fol.ConjunctiveQuery;
import uk.ac.ox.cs.pdq.db.Schema;
import uk.ac.ox.cs.pdq.io.jaxb.IOManager;
import org.springframework.core.io.Resource;
import java.nio.file.Path;
import javax.servlet.http.HttpServletRequest;
import org.springframework.http.MediaType;
import java.util.ArrayList;
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
  private HashMap<Integer, String> paths;

  private HashMap<Integer, Schema> schemaList;
  private HashMap<Integer, ArrayList<ConjunctiveQuery>> queryList;
  private HashMap<Integer, File> casePropertyList;
  private HashMap<Integer, ArrayList<JsonPlan>> planList;
  private HashMap<Integer, ArrayList<JsonRunResults>> runResultList;
  private HashMap<Integer, String> catalogPaths;
  private boolean demoMode; //change to config file

  public JsonController(){
    this.paths = new HashMap<Integer, String>();
    this.schemaList = new HashMap<Integer, Schema>();
    this.queryList = new HashMap<Integer, ArrayList<ConjunctiveQuery>>();
    this.casePropertyList = new HashMap<Integer, File>();
    this.planList = new HashMap<Integer, ArrayList<JsonPlan>>();
    this.runResultList = new HashMap<Integer, ArrayList<JsonRunResults>>();
    this.catalogPaths = new HashMap<Integer, String>();
    this.demoMode = true;

    File testDirectory = new File("test/demo/");
    File[] examples = testDirectory.listFiles();

    if(examples != null){
        int i = 0;
        for(File folder : examples){
            if (folder.isDirectory()){
                paths.put(i, folder.getPath());
                File[] info = folder.listFiles();

                for(File file : info){
                    switch(file.getName()){
                        case "schema.xml":
                            try{

                                Schema schema = IOManager.importSchema(file);
                                schemaList.put(i, schema);

                            }catch(Exception e){
                                e.printStackTrace();
                            }
                            break;
                        case "queries":
                            File[] queries = file.listFiles();
                            ArrayList<ConjunctiveQuery> queryArrayList = new ArrayList<ConjunctiveQuery>();
                            for(File query : queries){
                                try{

                                    ConjunctiveQuery CQs = IOManager.importQuery(query);
                                    queryArrayList.add(CQs);

                                }catch (Exception e){
                                    e.printStackTrace();
                                }
                            }

                            queryList.put(i, queryArrayList);

                            break;

                        case "case.properties":
                            casePropertyList.put(i, file);
                            break;

                        case "catalog.properties":
                            String pathToCatalog = file.getPath();
                            catalogPaths.put(i, pathToCatalog);
                            break;

                        default:
                            break;
                    }

                }
                i++;
            }

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
      SchemaName[] jsonSchemaList = new SchemaName[this.schemaList.size()];
      int i = 0;
      for(Integer id : this.schemaList.keySet()){
          ArrayList<ConjunctiveQuery> CQList = this.queryList.get(id);
          ArrayList<JsonQuery> JQList = new ArrayList<JsonQuery>();
          Schema schema = this.schemaList.get(id);

          int j = 0;
          for(ConjunctiveQuery CQ : CQList){
              try{

                  String query_string = SQLLikeQueryWriter.convert(CQ, schema);

                  JsonQuery query = new JsonQuery(j, query_string);

                  JQList.add(query);

              }catch(Exception e){
                  e.printStackTrace();
              }
              j++;
          }

          SchemaName jsonSchema = new SchemaName(schema, i, JQList);
          jsonSchemaList[i] = jsonSchema;
          i++;
      }

    return jsonSchemaList;
  }
  /**
   * Returns relations associated with specific schema. Schema is identified thanks to the provided id.
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
   * Returns dependencies associated with specific schema.
   *
   * @param id
   * @return JsonRelationList
   */
  @RequestMapping(value="/getDependencies", method=RequestMethod.GET, produces="application/json")
  public ResponseEntity<JsonDependencyList> getDependencies(@RequestParam(value="id") int id){

    Schema schema = schemaList.get(id);
    JsonDependencyList toReturn = new JsonDependencyList(schema, id);

    String contentType = "application/json";

    return ResponseEntity.ok().contentType(MediaType.parseMediaType(contentType))
              .header(contentType)
              .body(toReturn);
  }

    @GetMapping(value="/verifyQuery/{schemaID}/{queryID}/{SQL:.+}")
    public boolean verifyQuery(@PathVariable Integer schemaID, @PathVariable Integer queryID, @PathVariable String SQL, HttpServletRequest request){
        Schema schema = this.schemaList.get(schemaID);

        boolean validQuery = false;

        try{
            SQLQueryReader reader = new SQLQueryReader(schema);
            ConjunctiveQuery newQuery = reader.fromString(SQL);

            //validation goes here
            validQuery = true;

            if(! this.demoMode){
                ArrayList<ConjunctiveQuery> updatedList = queryList.get(schemaID);
                updatedList.add(newQuery);
                queryList.put(schemaID, updatedList);

                File query = new File(paths.get(schemaID)+"/queries/"+"query"+queryID.toString()+".xml");
                IOManager.exportQueryToXml(newQuery, query);
            }

            return validQuery;
        }catch(Exception e){
            e.printStackTrace();
        }
        return validQuery;
    }

  /**
   * Returns Entry<RelationalTerm, Cost> that shows up as a long string
   *
   * @param schemaID, queryID
   * @return
   */
  @GetMapping(value="/plan/{schemaID}/{queryID}/{SQL}")
  public JsonPlan plan(@PathVariable Integer schemaID, @PathVariable Integer queryID, @PathVariable String SQL){
      if(demoMode && queryID != 0){
          Schema schema = schemaList.get(schemaID);
          File properties = casePropertyList.get(schemaID);
          String pathToCatalog = catalogPaths.get(schemaID);

          try{
              SQLQueryReader reader = new SQLQueryReader(schema);
              ConjunctiveQuery cq = reader.fromString(SQL);

              JsonPlan plan = JsonPlanner.plan(schema, cq, properties, pathToCatalog);

              return plan;
          }catch (Throwable e) {
              e.printStackTrace();
              System.exit(-1);
              return null;
          }
      }

      ArrayList<JsonPlan> previousPlans= planList.get(schemaID);
      JsonPlan previousPlan = null;

      if(previousPlans == null){
          previousPlans = new ArrayList<JsonPlan>();
      }else if (previousPlans.size() > queryID){
              previousPlan = previousPlans.get(queryID);
      }


      if(previousPlan != null) return previousPlan; //if we've already planned this schema and query, return it

      Schema schema = schemaList.get(schemaID);
      ArrayList<ConjunctiveQuery> queryArrayList = queryList.get(schemaID);
      ConjunctiveQuery cq = queryArrayList.get(queryID);
      File properties = casePropertyList.get(schemaID);
      String pathToCatalog = catalogPaths.get(schemaID);

      try{
          JsonPlan plan = JsonPlanner.plan(schema, cq, properties, pathToCatalog);

          if(plan != null){
              plan.getGraphicalPlan().setType("ORIGIN"); //manually set type of origin node

              previousPlans.add(plan);

              planList.put(schemaID, previousPlans);
          }
          return plan;
      }catch (Throwable e) {
          e.printStackTrace();
          System.exit(-1);
          return null;
      }
  }

  @GetMapping(value="/run/{schemaID}/{queryID}/{SQL}")
  public JsonRunResults run(@PathVariable Integer schemaID, @PathVariable Integer queryID, @PathVariable String SQL){
      if(demoMode && queryID != 0){
          Schema schema = schemaList.get(schemaID);
          File properties = casePropertyList.get(schemaID);
          String pathToCatalog = catalogPaths.get(schemaID);
          JsonRunResults toReturn = null;

          try{

              SQLQueryReader reader = new SQLQueryReader(schema);
              ConjunctiveQuery cq = reader.fromString(SQL);

              JsonPlan jsonPlan = JsonPlanner.plan(schema, cq, properties, pathToCatalog);
              RelationalTerm plan = jsonPlan.getPlan();

              toReturn = Runner.runtime(schema, cq, properties, plan);

          }catch (Throwable e) {
              e.printStackTrace();
              System.exit(-1);
          }

          return toReturn;
      }

      ArrayList<JsonRunResults> results = this.runResultList.get(schemaID);
      JsonRunResults toReturn = null;
      if (results == null){
          results = new ArrayList<JsonRunResults>();
      }else if(results != null && results.size() > queryID){
          toReturn = results.get(queryID);
      }

      if(toReturn != null) return toReturn;

      Schema schema = schemaList.get(schemaID);

    ArrayList<ConjunctiveQuery> CQList = queryList.get(schemaID);
    ConjunctiveQuery cq = CQList.get(queryID);

    File properties = casePropertyList.get(schemaID);

    ArrayList<JsonPlan> plans = planList.get(schemaID);
    RelationalTerm plan = plans.get(queryID).getPlan();

    try{
      toReturn = Runner.runtime(schema, cq, properties, plan);

      //update HashMap
      results.add(toReturn);
      runResultList.put(schemaID, results);

    }catch (Throwable e) {
      e.printStackTrace();
      System.exit(-1);
    }
    return toReturn;
  }

    /**
     * Write run table to its associated example folder, load it, and send it to the client.
     *
     * @param schemaID
     * @param queryID
     * @param request
     * @return
     */
  @GetMapping(value="/downloadRun/{schemaID}/{queryID}/{SQL}")
  public ResponseEntity<Resource> downloadRun(@PathVariable int schemaID,
                                              @PathVariable int queryID,
                                              @PathVariable String SQL,
                                              HttpServletRequest request){
      if(demoMode && queryID != 0){

          try{
              Schema schema = schemaList.get(schemaID);
              File properties = casePropertyList.get(schemaID);
              String pathToCatalog = catalogPaths.get(schemaID);
              SQLQueryReader reader = new SQLQueryReader(schema);
              ConjunctiveQuery cq = reader.fromString(SQL);

              JsonPlan jsonPlan = JsonPlanner.plan(schema, cq, properties, pathToCatalog);
              RelationalTerm plan = jsonPlan.getPlan();
              JsonRunResults result = Runner.runtime(schema, cq, properties, plan);
              Runner.writeOutput(result.results, paths.get(schemaID)+"/results.csv");

              Resource resource = loadFileAsResource(paths.get(schemaID)+"/results.csv");
              String contentType = "text/csv";

              return ResponseEntity.ok().contentType(MediaType.parseMediaType(contentType))
                      .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"results.csv\"")
                      .body(resource);

          }catch(Exception e){
              e.printStackTrace();

          }
      }

      ArrayList<JsonRunResults> results = this.runResultList.get(schemaID);
      JsonRunResults result = null;
      if (results == null){
          results = new ArrayList<JsonRunResults>();
      }else if(results != null && results.size() > queryID){
          result = results.get(queryID);
      }

      try{
          Runner.writeOutput(result.results, paths.get(schemaID)+"/results.csv");

          Resource resource = loadFileAsResource(paths.get(schemaID)+"/results.csv");

          String contentType = "text/csv";

          return ResponseEntity.ok().contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"results.csv\"")
                    .body(resource);

      }catch(Exception e){
          e.printStackTrace();

      }
      return null;
  }

  @GetMapping(value="/downloadPlan/{schemaID}/{queryID}/{SQL}")
  public ResponseEntity<Resource> downloadPlan(@PathVariable int schemaID,
                                               @PathVariable int queryID,
                                               @PathVariable String SQL, HttpServletRequest request){
      if(demoMode && queryID != 0){
          try{
              Schema schema = schemaList.get(schemaID);
              File properties = casePropertyList.get(schemaID);
              String pathToCatalog = catalogPaths.get(schemaID);
              SQLQueryReader reader = new SQLQueryReader(schema);
              ConjunctiveQuery cq = reader.fromString(SQL);
              File planFile = new File(paths.get(schemaID)+"/computed-plan.xml");

              JsonPlan jsonPlan = JsonPlanner.plan(schema, cq, properties, pathToCatalog);
              RelationalTerm plan = jsonPlan.getPlan();

              IOManager.writeRelationalTerm(plan, planFile);
              Resource resource = loadFileAsResource(paths.get(schemaID)+"/computed-plan.xml");

              String contentType = "application/xml";

              return ResponseEntity.ok().contentType(MediaType.parseMediaType(contentType))
                      .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"computed-plan.xml\"")
                      .body(resource);

          }catch(Exception e){
              e.printStackTrace();
          }
      }

      ArrayList<JsonPlan> previousPlans= planList.get(schemaID);
      RelationalTerm plan = previousPlans.get(queryID).getPlan();

      File planFile = new File(paths.get(schemaID)+"/computed-plan.xml");
      try{
          IOManager.writeRelationalTerm(plan, planFile);

          Resource resource = loadFileAsResource(paths.get(schemaID)+"/computed-plan.xml");

          String contentType = "application/xml";

          return ResponseEntity.ok().contentType(MediaType.parseMediaType(contentType))
                  .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"computed-plan.xml\"")
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

            if (resource.exists()) {
                return resource;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }
}
