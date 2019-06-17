package uk.ac.ox.cs.pdq.rest;

import uk.ac.ox.cs.pdq.cost.Cost;
import uk.ac.ox.cs.pdq.algebra.RelationalTerm;
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



@RestController  //marks class as Spring MVC controller
public class JsonController{
  private final String PATH_TO_SCHEMA_EXAMPLES;
  private String[] example_names;

  private HashMap<Integer, Schema> schemaList;
  private HashMap<Integer, ConjunctiveQuery> queryList;
  private SchemaName[] jsonSchemaList;
  private HashMap<Integer, File> casePropertyList;

  public JsonController(){
    this.PATH_TO_SCHEMA_EXAMPLES = "test/demo/case_";
    this.example_names = new String[]{"001", "002", "003"};
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


/*
  `@RequestMapping` ensures that HTTP requests to /init_schemas
  are mapped to the init_schemas() method
*/
  @RequestMapping(value="/init_schemas", method=RequestMethod.GET, produces="application/json")

  public SchemaName[] init_schemas(){
    return this.jsonSchemaList;
  }

  @RequestMapping(value="/getRelations", method=RequestMethod.GET, produces="application/json")
  public JsonRelationList getRelations(@RequestParam(value="id") int id){

    Schema schema = schemaList.get(id);
    JsonRelationList toReturn = new JsonRelationList(schema, id);

    //change this so that it only sends a list of names and id...
    //when you click on a relation it should send relation info
    return toReturn;
  }

  @RequestMapping(value="/getQueries", method=RequestMethod.GET, produces="application/json")
  public JsonQuery getQueries(@RequestParam(value="id") int id){

    ConjunctiveQuery query = queryList.get(id);
    Schema schema = schemaList.get(id);

    String query_string = SQLLikeQueryWriter.convert(query, schema);

    JsonQuery toReturn = new JsonQuery(id, query_string);
    return toReturn;
  }

  @RequestMapping(value="/plan", method=RequestMethod.GET, produces="application/json")
  public Entry<RelationalTerm, Cost> plan(@RequestParam("id") int id){
    Schema schema = schemaList.get(id);
    ConjunctiveQuery cq = queryList.get(id);
    File properties = casePropertyList.get(id);

    try{
      Entry<RelationalTerm, Cost> plan = JsonPlanner.plan(schema, cq, properties);

      return plan;
      // return plan.toString();
    }catch (Throwable e) {
      e.printStackTrace();
      System.exit(-1);
      return null;
    }
  }
}
