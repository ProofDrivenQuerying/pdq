package uk.ac.ox.cs.pdq.test.cost.estimators;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;

import uk.ac.ox.cs.pdq.db.Schema;
import uk.ac.ox.cs.pdq.io.xml.DAGPlanReader;
import uk.ac.ox.cs.pdq.io.xml.LeftDeepPlanReader;
import uk.ac.ox.cs.pdq.plan.Plan;

/**
 * Tests the cost estimators
 * @author Efthymia Tsamoura
 *
 */
public class CostEstimatorTest {

	/**
	 * @param directory File
	 * @param schema Schema
	 * @param query Query
	 * @return Plan
	 */
	protected Plan obtainPlan(String fileName, Schema schema) {
		try(FileInputStream pis = new FileInputStream(fileName);
				BufferedInputStream bis = new BufferedInputStream(pis)) {
			try {
				bis.mark(1024);
				return new LeftDeepPlanReader(schema).read(bis); 
			} catch (Exception re) {
				bis.reset();
			}
			return new DAGPlanReader(schema).read(bis); 
		} catch (IOException e) {
			return null;
		}
	}

}