package uk.ac.ox.cs.pdq.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.log4j.Logger;

import uk.ac.ox.cs.pdq.fol.Atom;
import uk.ac.ox.cs.pdq.fol.Conjunction;
import uk.ac.ox.cs.pdq.fol.Disjunction;
import uk.ac.ox.cs.pdq.fol.Formula;
import uk.ac.ox.cs.pdq.fol.Implication;
import uk.ac.ox.cs.pdq.fol.Literal;
import uk.ac.ox.cs.pdq.fol.Negation;
import uk.ac.ox.cs.pdq.fol.QuantifiedFormula;
import uk.ac.ox.cs.pdq.fol.Variable;

import com.google.common.collect.Lists;

// TODO: Auto-generated Javadoc
/**
 * Provide utility function, that don't fit anywhere else.
 *
 * @author Efthymia Tsamoura
 * @author Julien Leblay
 * @author George Konstantinidis
 */
public class Utility {

	/**  The logger. */
	public static Logger log = Logger.getLogger(Utility.class);

	/**
	 * Search.
	 *
	 * @param <T> the generic type
	 * @param collection the collection
	 * @param object the object
	 * @return 		the positions where the input object appears in collection.
	 * 		If object does not appear in source, then an empty list is returned
	 */
	public static <T> List<Integer> search(Collection<? extends T> collection, T object) {
		List<Integer> result = new ArrayList<>();
		int index = 0;
		for (T obj : collection) {
			if (obj.equals(object)) {
				result.add(index);
			}
			index++;
		}
		return result;
	}
	
	public static List<Variable> getVariables(Formula formula) {
		List<Variable> variables = Lists.newArrayList();
		if(formula instanceof Conjunction) {
			variables.addAll(getVariables(((Conjunction)formula).getChildren().get(0)));
			variables.addAll(getVariables(((Conjunction)formula).getChildren().get(1)));
		}
		else if(formula instanceof Disjunction) {
			variables.addAll(getVariables(((Disjunction)formula).getChildren().get(0)));
			variables.addAll(getVariables(((Disjunction)formula).getChildren().get(1)));
		}
		else if(formula instanceof Negation) {
			variables.addAll(getVariables(((Negation)formula).getChildren().get(0)));
		}
		else if(formula instanceof Atom) {
			variables.addAll(((Atom)formula).getVariables());
		}
		else if(formula instanceof Implication) {
			variables.addAll(getVariables(((Implication)formula).getChildren().get(0)));
			variables.addAll(getVariables(((Implication)formula).getChildren().get(1)));
		}
		else if(formula instanceof QuantifiedFormula) {
			variables.addAll(getVariables(((QuantifiedFormula)formula).getChildren().get(0)));
		}
		return variables;
	}
	
	public static boolean isConjunctionOfAtoms(Formula formula) {
		if(formula instanceof Conjunction) {
			return isConjunctionOfAtoms(formula.getChildren().get(0)) && isConjunctionOfAtoms(formula.getChildren().get(1));
		}
		if(formula instanceof Atom || formula instanceof Literal) {
			return true;
		}
		if(formula instanceof Negation) {
			if(((Negation) formula).getChildren().get(0) instanceof Atom ||
					((Negation) formula).getChildren().get(0) instanceof Literal) {
				return true;
			}
		}
		return false;
	}

	public static boolean isDisjunctionOfAtoms(Formula formula) {
		if(formula instanceof Disjunction) {
			return isDisjunctionOfAtoms(formula.getChildren().get(0)) && isDisjunctionOfAtoms(formula.getChildren().get(1));
		}
		if(formula instanceof Atom || formula instanceof Literal) {
			return true;
		}
		if(formula instanceof Negation) {
			if(((Negation) formula).getChildren().get(0) instanceof Atom ||
					((Negation) formula).getChildren().get(0) instanceof Literal) {
				return true;
			}
		}
		return false;
	}
}
