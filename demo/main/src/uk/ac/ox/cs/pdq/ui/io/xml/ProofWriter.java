package uk.ac.ox.cs.pdq.ui.io.xml;

import java.io.PrintStream;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import uk.ac.ox.cs.pdq.db.TGD;
import uk.ac.ox.cs.pdq.fol.Constant;
import uk.ac.ox.cs.pdq.fol.Predicate;
import uk.ac.ox.cs.pdq.fol.Skolem;
import uk.ac.ox.cs.pdq.fol.Term;
import uk.ac.ox.cs.pdq.fol.Variable;
import uk.ac.ox.cs.pdq.io.xml.AbstractXMLWriter;
import uk.ac.ox.cs.pdq.io.xml.QNames;
import uk.ac.ox.cs.pdq.planner.accessible.AccessibilityAxiom;
import uk.ac.ox.cs.pdq.ui.proof.Proof;

/**
 * Writes proofs to XML.
 * 
 * @author Julien Leblay
 * 
 */
public class ProofWriter extends AbstractXMLWriter<Proof> {

	/**
	 * Writes the given proof to the given output.
	 * @param out
	 * @param proof
	 */
	public void writeProof(PrintStream out, Proof proof) {
		Map<QNames, String> att = new LinkedHashMap<>();
		open(out, QNames.PROOF, att);
		for (Proof.State state: proof.getStates()) {
			this.writeState(out, state);
		}
//		this.writeQueryMatch(out, proof.getQueryMatch());
		close(out, QNames.PROOF);
	}

	/**
	 * Writes the given proof state to the given output.
	 * @param out
	 * @param state
	 */
	public void writeState(PrintStream out, Proof.State state) {
		Map<QNames, String> att = new LinkedHashMap<>();
		open(out, QNames.STATE, att);
		this.writeAxiom(out, state.getAxiom());
		for (Map<Variable, Constant> mapping: state.getMatches()) {
			this.writeCandidate(out, mapping);
		}
		close(out, QNames.STATE);
	}

	/**
	 * Writes the given accessibility axiom to the given output.
	 * @param out
	 * @param axiom
	 */
	public void writeAxiom(PrintStream out, AccessibilityAxiom axiom) {
		Map<QNames, String> att = new LinkedHashMap<>();
		att.put(QNames.RELATION, axiom.getBaseRelation().getName());
		att.put(QNames.ACCESS_METHOD, axiom.getAccessMethod().getName());
		openclose(out, QNames.AXIOM, att);
	}

	/**
	 * Writes the given candidate to the given output.
	 * @param out
	 * @param mapping
	 */
	public void writeCandidate(PrintStream out, Map<Variable, Constant> mapping) {
		Map<QNames, String> att = new LinkedHashMap<>();
		open(out, QNames.CANDIDATE, att);
		for (Map.Entry<Variable, Constant> entry: mapping.entrySet()) {
			this.writeEntry(out, entry);
		}
		close(out, QNames.CANDIDATE);
	}

	/**
	 * Writes the given query match to the given output.
	 * @param out
	 * @param mapping
	 */
	public void writeQueryMatch(PrintStream out, Map<Variable, Constant> mapping) {
		Map<QNames, String> att = new LinkedHashMap<>();
		open(out, QNames.MATCH, att);
		for (Map.Entry<Variable, Constant> entry: mapping.entrySet()) {
			this.writeEntry(out, entry);
		}
		close(out, QNames.MATCH);
	}

	/**
	 * Writes the given map entry to the given output.
	 * @param out
	 * @param entry
	 */
	public void writeEntry(PrintStream out, Map.Entry<Variable, Constant> entry) {
		Map<QNames, String> att = new LinkedHashMap<>();
		att.put(QNames.KEY, entry.getKey().toString());
		att.put(QNames.VALUE, entry.getValue().toString());
		openclose(out, QNames.ENTRY, att);
	}

	/**
	 * Writes the given predicate to the given output.
	 * @param out
	 * @param p
	 */
	public void writePredicate(PrintStream out, Predicate p) {
		Map<QNames, String> att = new LinkedHashMap<>();
		att.put(QNames.NAME, p.getName());
		open(out, QNames.ATOM, att);
		for (Term t: p.getTerms()) {
			Map<QNames, String> att2 = new LinkedHashMap<>();
			if (t.isVariable()) {
				att2.put(QNames.NAME, ((Variable) t).getName());
				openclose(out, QNames.VARIABLE, att2);
			} else {
				att2.put(QNames.VALUE, t.toString());
				openclose(out, QNames.CONSTANT, att2);
			}
		}
		close(out, QNames.ATOM);
	}

	/**
	 * @param out PrintStream
	 * @param o Proof
	 */
	@Override
	public void write(PrintStream out, Proof o) {
		out.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
		this.writeProof(out, o);
	}
	
	/**
	 * For test purpose only
	 * @param tgd
	 * @return Map<Variable,Term>
	 */
	private static Map<Variable, Constant> ground(TGD tgd) {
		Map<Variable, Constant> result = new LinkedHashMap<>();
		List<Variable> free = tgd.getUniversal();
		if (free != null) {
			for (Variable variable:free) {
				result.put(variable, new Skolem(Skolem.Generator.getName()));
			}
		}
		List<Variable> bound = tgd.getExistential();
		if (bound != null) {
			for (Variable variable:bound) {
				result.put(variable, new Skolem(Skolem.Generator.getName()));
			}
		}
		return result;
	}
}
