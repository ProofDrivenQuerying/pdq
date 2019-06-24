package uk.ac.ox.cs.pdq.rest.jsonwrappers;

import uk.ac.ox.cs.pdq.algebra.RelationalTerm;
import uk.ac.ox.cs.pdq.datasources.accessrepository.AccessRepository;
import uk.ac.ox.cs.pdq.fol.ConjunctiveQuery;
import uk.ac.ox.cs.pdq.db.Schema;
import uk.ac.ox.cs.pdq.datasources.tuple.Table;
import uk.ac.ox.cs.pdq.db.tuple.Tuple;
import uk.ac.ox.cs.pdq.datasources.tuple.Table.ResetableIterator;
import uk.ac.ox.cs.pdq.runtime.exec.PlanDecorator;
import uk.ac.ox.cs.pdq.runtime.exec.spliterator.ExecutablePlan;
import java.io.File;


/**
 * Runner runs a plan and returns a JsonRunResult for simple JSON visualization.
 *
 * @author Camilo Ortiz
 */
public class Runner {
    /**
     * Runs the plan and creates/returns a JSON-friendly object JsonRunResults.
     *
     * @param schema
     * @param cq
     * @param properties
     * @return
     */
    public static long runtime(Schema schema, ConjunctiveQuery cq, File properties, RelationalTerm plan){

        try{

            long start = System.currentTimeMillis();

            Table results = Runner.evaluatePlan(plan, schema);

            ResetableIterator<Tuple> it = results.iterator();
            long tupleCount = 0;

            // print output
            while(it.hasNext()) {
                tupleCount++;
                Tuple t = it.next();
            }
            System.out.println();
            System.out.println("Finished, " + tupleCount + " amount of tuples found in " + (System.currentTimeMillis() - start)/1000.0 + " sec.");

            return tupleCount;
        }catch(Throwable e){
            e.printStackTrace();
            System.exit(-1);
        }
        return 0;
    }

    private static Table evaluatePlan(RelationalTerm p, Schema schema) throws Exception {
        AccessRepository repo = AccessRepository.getRepository();
        try {
            ExecutablePlan executable = new PlanDecorator(repo,schema).decorate(p);
            System.out.println("Executing plan " + p.hashCode());
            Table res = executable.execute();
            System.out.println("plan " + p.hashCode() + " finished.");
            return res;
        }catch(Throwable t) {
            t.printStackTrace();
            throw t;
        }
    }
}
