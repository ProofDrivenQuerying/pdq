package uk.ac.ox.cs.pdq.test.cost.estimators;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Map.Entry;

import uk.ac.ox.cs.pdq.algebra.RelationalTerm;
import uk.ac.ox.cs.pdq.cost.Cost;
import uk.ac.ox.cs.pdq.cost.io.jaxb.CostIOManager;
import uk.ac.ox.cs.pdq.db.Schema;
import uk.ac.ox.cs.pdq.io.jaxb.IOManager;

// TODO: Auto-generated Javadoc
/**
 * Tests the cost estimators.
 *
 * @author Efthymia Tsamoura
 */
public class CostEstimatorTest {

	/**
	 * Obtain plan.
	 *
	 * @param fileName the file name
	 * @param schema Schema
	 * @return Plan
	 */
	protected Entry<RelationalTerm,Cost> obtainPlan(String fileName, Schema schema) {
		try(FileInputStream pis = new FileInputStream(fileName) ){
//				BufferedInputStream bis = new BufferedInputStream(pis)) {
//			try {
//				bis.mark(1024);
//				return new LeftDeepPlanReader(schema).read(bis); 
//			} catch (Exception re) {
//				bis.reset();
//			}
//			return new DAGPlanReader(schema).read(bis); 
			File file = new File(fileName);
			RelationalTerm plan = IOManager.readRelationalTermCost(file, schema);
			Cost cost = CostIOManager.readRelationalTermCost(file, schema);
		} catch (IOException e) {
			return null;
		}
	}

}