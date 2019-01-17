package uk.ac.ox.cs.pdq.regression.utils;

import java.io.File;
import java.io.IOException;

import javax.xml.bind.JAXBException;

import uk.ac.ox.cs.pdq.algebra.RelationalTerm;
import uk.ac.ox.cs.pdq.cost.io.jaxb.CostIOManager;
import uk.ac.ox.cs.pdq.db.Schema;
import uk.ac.ox.cs.pdq.io.PlanPrinter;
import uk.ac.ox.cs.pdq.io.jaxb.IOManager;

public class PlanPrinterTest {

	public PlanPrinterTest() {
	}

	public static void main(String[] args) throws IOException, InterruptedException, JAXBException {
		File planFileNew = new File("..//regression//TestResults//newResults.xml");
		File planFileOld = new File("..//regression//TestResults//oldResults.xml");
		Schema schema = IOManager
				.importSchema(new File("..//regression//test//planner//linear//fast//demo//case_001//schema.xml"));
		RelationalTerm old = CostIOManager.readRelationalTermFromRelationaltermWithCost(planFileOld, schema );
		RelationalTerm newPlan= CostIOManager.readRelationalTermFromRelationaltermWithCost(planFileNew, schema );
		PlanPrinter.openPngPlan(old);
		PlanPrinter.openPngPlan(newPlan);
	}

}
