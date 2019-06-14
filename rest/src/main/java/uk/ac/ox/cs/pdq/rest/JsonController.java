package uk.ac.ox.cs.pdq.rest;

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




@RestController  //marks class as Spring MVC controller
public class JsonController{
  private final String PATH_TO_SCHEMA_EXAMPLES;
  private String[] SCHEMA_NAMES;

  private HashMap<Integer, Schema> schemaList;
  private HashMap<Integer, ConjunctiveQuery> queryList;
  private SchemaName[] jsonSchemaList;

  public JsonController(){
    this.PATH_TO_SCHEMA_EXAMPLES = "test/demo/case_";
    this.SCHEMA_NAMES = new String[]{"001", "002", "003"};
    this.jsonSchemaList = new SchemaName[SCHEMA_NAMES.length];
    this.schemaList = new HashMap<Integer, Schema>();
    this.queryList = new HashMap<Integer, ConjunctiveQuery>();

    for (int i = 0; i < SCHEMA_NAMES.length; i++){
      try {
        //get schema
        String pathToSchema = PATH_TO_SCHEMA_EXAMPLES+SCHEMA_NAMES[i]+"/schema.xml";
        String pathToQuery = PATH_TO_SCHEMA_EXAMPLES+SCHEMA_NAMES[i]+"/query.xml";
        Schema schema = IOManager.importSchema(new File(pathToSchema));
        ConjunctiveQuery query = IOManager.importQuery(new File(pathToQuery));

        //put schema in hash map
        schemaList.put(i, schema);

        //put query in hash map
        queryList.put(i, query);

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
    public String getQueries(@RequestParam(value="id") int id){

      ConjunctiveQuery query = queryList.get(id);
      Schema schema = schemaList.get(id);

      String query_string = SQLLikeQueryWriter.convert(query, schema);

      if (query_string.equals("")){
        return query.toString();
      }
      return query_string;
    }
}
