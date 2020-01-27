package uk.ac.ox.cs.pdq.rest.util;

import uk.ac.ox.cs.pdq.algebra.RelationalTerm;
import uk.ac.ox.cs.pdq.datasources.accessrepository.AccessRepository;
import uk.ac.ox.cs.pdq.fol.ConjunctiveQuery;
import uk.ac.ox.cs.pdq.db.Schema;
import uk.ac.ox.cs.pdq.datasources.tuple.Table;
import uk.ac.ox.cs.pdq.db.tuple.Tuple;
import uk.ac.ox.cs.pdq.datasources.tuple.Table.ResetableIterator;
import uk.ac.ox.cs.pdq.rest.jsonobjects.run.RunResults;
import uk.ac.ox.cs.pdq.runtime.exec.PlanDecorator;
import uk.ac.ox.cs.pdq.runtime.exec.spliterator.ExecutablePlan;
import java.io.File;
import java.io.FileWriter;


/**
 * Runner is the hub for running plans for other JSON wrapper classes.
 *
 * @author Camilo Ortiz
 */

public class JsonRunner {
    /**
     * Runs the plan and creates/returns a JSON-friendly object JsonRunResults.
     *
     * @param schema
     * @param cq
     * @param properties
     * @return
     */
    public static RunResults runtime(Schema schema, ConjunctiveQuery cq, File properties, RelationalTerm plan){

        try{
            long start = System.currentTimeMillis();

            Table results = JsonRunner.evaluatePlan(plan, schema);

            ResetableIterator<Tuple> it = results.iterator();

            long tupleCount = 0;

            while(it.hasNext()) {
                tupleCount++;
                Tuple t = it.next();
            }

            double computationTime = (System.currentTimeMillis() - start)/1000.0;


            return new RunResults(tupleCount, results, computationTime, cq);

        }catch(Throwable e){
            e.printStackTrace();
        }
        return null;
    }

    private static Table evaluatePlan(RelationalTerm p, Schema schema) throws Exception {
        AccessRepository repo = AccessRepository.getRepository("./services");
        try {
            ExecutablePlan executable = new PlanDecorator(repo, schema).decorate(p);
            Table res = executable.execute();
            return res;
        }catch(Throwable t) {
            t.printStackTrace();
            throw t;
        }
    }

    /** This will print exception if we don't have every executable access method that is necessary to execute this plan.
     *  It will return null if we can't run the plan and will otherwise return an executable.
     * @param p
     * @return
     * @throws Exception
     */
    public static ExecutablePlan decoratePlan(RelationalTerm p, Schema schema) throws Exception {
        AccessRepository repo = AccessRepository.getRepository("./services");

        ExecutablePlan executable = null;
        try{
            executable = new PlanDecorator(repo,schema).decorate(p);
        }catch(Exception e){
            e.printStackTrace();
        }
        return executable;
    }

    /**
     * This util writes the run's table out to a file.
     * @param results
     * @param path
     * @throws Exception
     */
    public static void writeOutput(Table results, String path) throws Exception{

        File target = new File(path);


        int tupleCount = 0;

        try (FileWriter fw = new FileWriter(target,false)) {
            // Header
            StringBuilder builder = null;
            for (uk.ac.ox.cs.pdq.db.Attribute attribute : results.getHeader()) {
                if (builder == null) {
                    builder = new StringBuilder();
                } else {
                    builder.append(",");
                }
                builder.append(attribute.getName());
            }
            builder.append("\r\n");
            fw.write(builder.toString());

            //Data
            ResetableIterator<Tuple> it = results.iterator();
            while(it.hasNext()) {
                tupleCount++;
                Tuple t = it.next();

                builder = null;
                int attributeCounter = 0;
                for (Object value : t.getValues()) {
                    if (builder == null) {
                        builder = new StringBuilder();
                    } else {
                        builder.append(",");
                    }
                    if (results.getHeader()[attributeCounter].getType().equals(String.class))
                        builder.append(value.toString().replaceAll(",", "/c"));
                    else
                        builder.append(value);
                    attributeCounter++;
                }
                builder.append("\r\n");
                fw.write(builder.toString());
            }
            fw.close();
        }
    }
}
