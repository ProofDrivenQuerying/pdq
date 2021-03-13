// This file is part of PDQ (https://github.com/ProofDrivenQuerying/pdq) which is released under the MIT license.
// See accompanying LICENSE for copyright notice and full details.

package uk.ac.ox.cs.pdq.rest;

import org.springframework.core.io.UrlResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uk.ac.ox.cs.pdq.algebra.RelationalTerm;
import uk.ac.ox.cs.pdq.rest.jsonobjects.schema.*;
import uk.ac.ox.cs.pdq.rest.util.*;
import uk.ac.ox.cs.pdq.rest.jsonobjects.plan.Plan;
import uk.ac.ox.cs.pdq.rest.jsonobjects.run.RunResults;
import uk.ac.ox.cs.pdq.ui.io.sql.SQLLikeQueryReader;
import uk.ac.ox.cs.pdq.ui.io.sql.SQLLikeQueryWriter;
import uk.ac.ox.cs.pdq.fol.ConjunctiveQuery;
import uk.ac.ox.cs.pdq.db.Schema;
import uk.ac.ox.cs.pdq.io.jaxb.IOManager;
import org.springframework.core.io.Resource;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.servlet.http.HttpServletRequest;
import org.springframework.http.MediaType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.io.File;

import org.springframework.http.HttpHeaders;

import java.nio.file.Paths;

/**
 * Entry point for Rest application. Controller defines and computes all the REST calls of the API.
 *
 * @author Camilo Ortiz
 */

@RestController
public class Controller {
    private final String workingDirectory = "demo/";
    private final HashMap<Integer, String> paths;

    private final HashMap<Integer, Schema> schemaList;
    private final HashMap<Integer, HashMap<Integer, ConjunctiveQuery>> commonQueries;
    private final HashMap<Integer, File> casePropertyList;
    private final HashMap<Integer, String> catalogPaths;
    private final boolean localMode; //change to config file

    /**
     * Constructor. Reads in demo schema/query/property information found in ./demo/ and stores it in HashMaps for
     * quick access during runtime.
     */
    public Controller() {
        this.paths = new HashMap<Integer, String>();
        this.schemaList = new HashMap<Integer, Schema>();
        this.commonQueries = new HashMap<Integer, HashMap<Integer, ConjunctiveQuery>>();
        this.casePropertyList = new HashMap<Integer, File>();


        this.catalogPaths = new HashMap<Integer, String>();
        this.localMode = false;

        // The directory we store our example folders in
        // This is externalizable
        File testDirectory = new File(workingDirectory);
        // All the example folders
        File[] examples = testDirectory.listFiles();

        if (examples != null) {
            int i = 0;
            for (File folder : examples) {
                if (folder.isDirectory()) {
                    paths.put(i, folder.getPath());
                    File[] info = folder.listFiles();

                    for (File file : info) {
                        switch (file.getName()) {
                            case "schema.xml":
                                try {

                                    Schema schema = IOManager.importSchema(file);
                                    schemaList.put(i, schema);

                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                break;
                            case "queries":
                                File[] queries = file.listFiles();
                                HashMap<Integer, ConjunctiveQuery> queryHashMap = new HashMap<Integer, ConjunctiveQuery>();
                                int j = 0;
                                for (File query : queries) {
                                    try {

                                        ConjunctiveQuery CQs = IOManager.importQuery(query);
                                        queryHashMap.put(j, CQs);

                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                    j++;
                                }

                                commonQueries.put(i, queryHashMap);

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
     * Returns an initial list of schemas that only contains their respective id and name.
     *
     * @return SchemaName[]
     */
    @CrossOrigin(origins = "http://localhost:3000")
    @RequestMapping(value = "/initSchemas", method = RequestMethod.GET, produces = "application/json")
    public SchemaArray initSchemas() {

        SchemaName[] jsonSchemaList = new SchemaName[this.schemaList.size()];

        int i = 0;
        for (Integer id : this.schemaList.keySet()) {
            HashMap<Integer, ConjunctiveQuery> CQList = this.commonQueries.get(id);


            ArrayList<Query> JQList = new ArrayList<Query>();
            Schema schema = this.schemaList.get(id);

            int n = CQList.size();
            for (int j = 0; j < n; j++) {
                try {

                    String query_string = SQLLikeQueryWriter.convert(CQList.get(j), schema);

                    Query query = new Query(j, query_string);

                    JQList.add(query);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            SchemaName jsonSchema = new SchemaName(schema, i, JQList);
            jsonSchemaList[i] = jsonSchema;
            i++;
        }

        return new SchemaArray(jsonSchemaList);
    }

    /**
     * Returns relations associated with specific schema. Schema is identified thanks to the provided id.
     *
     * @param id
     * @return JsonRelationList
     */
    @CrossOrigin(origins = "http://localhost:3000")
    @RequestMapping(value = "/getRelations", method = RequestMethod.GET, produces = "application/json")
    public RelationArray getRelations(@RequestParam(value = "id") int id) {

        Schema schema = schemaList.get(id);

        return new RelationArray(schema, id);
    }

    /**
     * Returns dependencies associated with specific schema
     *
     * @param id
     * @return JsonRelationList
     */
    @CrossOrigin(origins = "http://localhost:3000")
    @RequestMapping(value = "/getDependencies", method = RequestMethod.GET, produces = "application/json")
    public Dependencies getDependencies(@RequestParam(value = "id") int id) {

        Schema schema = schemaList.get(id);

        return new Dependencies(schema, id);
    }

    /**
     * Verifies whether an SQL string can be converted to Conjunctive Query
     *
     * @param schemaID
     * @param queryID
     * @param SQL
     * @return boolean based on whether the SQL string is translatable
     */
    @CrossOrigin(origins = "http://localhost:3000")
    @GetMapping(value = "/verifyQuery/{schemaID}/{queryID}/{SQL:.+}")
    public boolean verifyQuery(@PathVariable Integer schemaID, @PathVariable Integer queryID, @PathVariable String SQL) {
        Schema schema = this.schemaList.get(schemaID);

        boolean validQuery = false;

        try {
            SQLLikeQueryReader reader = new SQLLikeQueryReader(schema);
            ConjunctiveQuery newQuery = reader.fromString(SQL);

            //validation goes here
            validQuery = true;

            // localMode means we save new queries to file
            if (this.localMode) {
                File query = new File(paths.get(schemaID) + "/queries/" + "query" + queryID.toString() + ".xml");
                IOManager.exportQueryToXml(newQuery, query);
                HashMap<Integer, ConjunctiveQuery> updatedList = this.commonQueries.get(schemaID);
                updatedList.put(queryID, newQuery);
            }
        } catch (Exception | ExceptionInInitializerError | NoClassDefFoundError e) {
            e.printStackTrace();
            return false;
        }

        System.out.println("Returned in validQuery " + validQuery);
        return validQuery;
    }

    /**
     * Generates a plan based on schemaID and queryID
     *
     * @param schemaID
     * @param queryID
     * @param SQL
     * @return
     */
    @CrossOrigin(origins = "http://localhost:3000")
    @GetMapping(value = "/plan/{schemaID}/{queryID}/{SQL}")
    public Plan plan(@PathVariable Integer schemaID, @PathVariable Integer queryID, @PathVariable String SQL) {

        Schema schema = schemaList.get(schemaID);
        File properties = casePropertyList.get(schemaID);
        String pathToCatalog = catalogPaths.get(schemaID);
        Plan plan = null;
        try {

            SQLLikeQueryReader reader = new SQLLikeQueryReader(schema);
            ConjunctiveQuery cq = reader.fromString(SQL);


            plan = JsonPlanner.plan(schema, cq, properties, pathToCatalog);
            plan.getGraphicalPlan().setType("ORIGIN");

            // If our plan has already been written to file, don't write it out again
            if (Files.exists(Paths.get(paths.get(schemaID) + "/computed-plan" + queryID + ".xml")) && commonQueries.get(schemaID).get(queryID) != null){
                return plan;
            }

            File planFile = new File(paths.get(schemaID) + "/computed-plan" + queryID + ".xml");

            RelationalTerm relationalTermPlan = plan.getPlan();
            IOManager.writeRelationalTerm(relationalTermPlan, planFile);

        } catch (Throwable e) {
            e.printStackTrace();
        }
        return plan;
    }

    /**
     * Load plan xml from its respective directory and sends it to the client.
     *
     * @param schemaID
     * @param queryID
     * @param SQL
     * @param request
     * @return a ResponseEntity that contains a Resource file (for downloading)
     */
    @CrossOrigin(origins = "http://localhost:3000")
    @GetMapping(value = "/downloadPlan/{schemaID}/{queryID}/{SQL}")
    public ResponseEntity<Resource> downloadPlan(@PathVariable int schemaID, @PathVariable int queryID,
                                                 @PathVariable String SQL, HttpServletRequest request) {

        try {
            Resource resource = loadFileAsResource(paths.get(schemaID) + "/computed-plan" + queryID + ".xml");

            String contentType = "application/xml";

            return ResponseEntity.ok().contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"computed-plan.xml\"")
                    .body(resource);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Generates and runs plan based on schemaID and queryID
     *
     * @param schemaID
     * @param queryID
     * @param SQL
     * @return
     */
    @CrossOrigin(origins = "http://localhost:3000")
    @GetMapping(value = "/run/{schemaID}/{queryID}/{SQL}")
    public RunResults run(@PathVariable Integer schemaID, @PathVariable Integer queryID, @PathVariable String SQL){


        Schema schema = schemaList.get(schemaID);
        File properties = casePropertyList.get(schemaID);
        String pathToCatalog = catalogPaths.get(schemaID);
        RunResults result = null;

        try {
            SQLLikeQueryReader reader = new SQLLikeQueryReader(schema);
            ConjunctiveQuery cq = reader.fromString(SQL);

            Plan jsonPlan = JsonPlanner.plan(schema, cq, properties, pathToCatalog);
            RelationalTerm plan = jsonPlan.getPlan();

            result = JsonRunner.runtime(schema, cq, properties, plan);

            // If our run has already been written to file, don't write it out again.
            if (Files.exists(Paths.get(paths.get(schemaID) + "/results" + queryID + ".csv")) && commonQueries.get(schemaID).get(queryID) != null){
                return result;
            }

            JsonRunner.writeOutput(result.results, paths.get(schemaID) + "/results" + queryID + ".csv");

        } catch (Throwable e) {
            e.printStackTrace();
        }

        return result;

    }

    /**
     * Load run table from its respective directory and sends it to the client.
     *
     * @param schemaID
     * @param queryID
     * @param request
     * @return a ResponseEntity that contains a Resource file (for downloading)
     */
    @CrossOrigin(origins = "http://localhost:3000")
    @GetMapping(value = "/downloadRun/{schemaID}/{queryID}/{SQL}")
    public ResponseEntity<Resource> downloadRun(@PathVariable int schemaID, @PathVariable int queryID,
                                                @PathVariable String SQL, HttpServletRequest request) {

        try {

            Resource resource = loadFileAsResource(paths.get(schemaID) + "/results" + queryID + ".csv");
            String contentType = "text/csv";

            return ResponseEntity.ok().contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"results.csv\"")
                    .body(resource);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Loads fileName and converts it into a Resource
     *
     * @param fileName as a string
     * @return Resource
     */
    public Resource loadFileAsResource(String fileName) {
        try {
            Path filePath = Paths.get(fileName);

            Resource resource = new UrlResource(filePath.toUri());

            if (resource.exists()) {
                return resource;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public HashMap<Integer, Schema> getSchemaList() {
        return this.schemaList;
    }

    public HashMap<Integer, HashMap<Integer, ConjunctiveQuery>> getCommonQueries() {
        return this.commonQueries;
    }

    public HashMap<Integer, File> getCasePropertyList() {
        return casePropertyList;
    }
}
