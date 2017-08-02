package uk.ac.ox.cs.pdq.test.planner;

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
import uk.ac.ox.cs.pdq.io.jaxb.IOManager;

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
	 * TOCOMMENT: WHAT IS THIS?.
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
		result.append(AlgebraUtilities.getAccesses(s)).append("\n\t\t");
		result.append(AlgebraUtilities.getAccesses(o)).append("\n\t");
		return result.toString();
	}
	
	public static Entry<RelationalTerm,Cost> obtainPlan(String fileName, Schema schema) {
		try(FileInputStream pis = new FileInputStream(fileName) ){
			File file = new File(fileName);
			RelationalTerm plan = IOManager.readRelationalTerm(file, schema);
			Cost cost = CostIOManager.readRelationalTermCost(file, schema);
			return new AbstractMap.SimpleEntry<RelationalTerm,Cost>(plan, cost);
		} catch (IOException | JAXBException e) {
			return null;
		}
	}

}
