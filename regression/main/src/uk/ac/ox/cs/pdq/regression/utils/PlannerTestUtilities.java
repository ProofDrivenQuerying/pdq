package uk.ac.ox.cs.pdq.regression.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.AbstractMap;
import java.util.Map.Entry;

import javax.xml.bind.JAXBException;

import uk.ac.ox.cs.pdq.algebra.AlgebraUtilities;
import uk.ac.ox.cs.pdq.algebra.RelationalTerm;
import uk.ac.ox.cs.pdq.cost.Cost;
import uk.ac.ox.cs.pdq.cost.io.jaxb.CostIOManager;
import uk.ac.ox.cs.pdq.db.Schema;

public class PlannerTestUtilities {

	/**
	 * The Enum Levels.
	 */
	public static enum Levels {
		/** The identical. */
		IDENTICAL, 
		/** The equivalent. */
		EQUIVALENT, 
		/** The different. */
		DIFFERENT}

	/**
	 * <pre>
	 * This is an advanced equals method that can return
	 *  - Levels.DIFFERENT in case they have different cost 
	 *  - Levels.IDENTICAL in case the two relationalTerm is the same 
	 *  - Levels.EQUIVALENT in case the two relationalTerm is different but have the same cost. 
	 * </pre>
	 *
	 * @param o Plan
	 * @return Levels
	 */
	public static Levels howDifferent(RelationalTerm s, Cost sCost, RelationalTerm o, Cost oCost) {
		if (o == null) {
			return Levels.DIFFERENT;
		}
		if (sCost.equals(oCost)) {
			if (s.equals(o)) {
				return Levels.IDENTICAL;
			}
			//PlanPrinter.openPngPlan(o);
			return Levels.EQUIVALENT;
		}
		return Levels.DIFFERENT;
	}

	/**
	 * Diff.
	 *
	 * @param o Plan
	 * @return String
	 */
	public static String diff(RelationalTerm s, Cost sCost, RelationalTerm o, Cost oCost) {
		StringBuilder result = new StringBuilder();
		result.append("\n\tCosts: ").append(sCost).append(" <-> ").append(oCost);
		result.append("\n\tLeaves:\n\t\t");
		String accessesS = "" + AlgebraUtilities.getAccesses(s);
		String accessesO = "" + AlgebraUtilities.getAccesses(o);
		if (accessesS.equals(accessesO)) {
			result.append("Same accesses, but plan differs.");
		} else {
			result.append(AlgebraUtilities.getAccesses(s)).append("\n\t\t");
			result.append(AlgebraUtilities.getAccesses(o)).append("\n\t");
		}
		return result.toString();
	}
	
	public static Entry<RelationalTerm,Cost> obtainPlan(String fileName, Schema schema) {
		try(FileInputStream pis = new FileInputStream(fileName) ){
			File file = new File(fileName);
			RelationalTerm plan = CostIOManager.readRelationalTermFromRelationaltermWithCost(file, schema);
			Cost cost = CostIOManager.readRelationalTermCost(file, schema);
			return new AbstractMap.SimpleEntry<RelationalTerm,Cost>(plan, cost);
		} catch (IOException | JAXBException e) {
			return null; 
		}
	}

}
