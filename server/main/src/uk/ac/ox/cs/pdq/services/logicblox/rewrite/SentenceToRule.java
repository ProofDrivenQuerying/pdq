package uk.ac.ox.cs.pdq.services.logicblox.rewrite;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import uk.ac.ox.cs.pdq.db.TGD;
import uk.ac.ox.cs.pdq.fol.Conjunction;
import uk.ac.ox.cs.pdq.fol.Formula;
import uk.ac.ox.cs.pdq.fol.Negation;
import uk.ac.ox.cs.pdq.fol.Atom;
import uk.ac.ox.cs.pdq.rewrite.Rewriter;
import uk.ac.ox.cs.pdq.rewrite.RewriterException;

/**
 * Attempts to rewrite a sentence into a TGD. If the sentence of found
 * not to be top-level conjunction, with a single negation sub-formula,
 * the rewrite operation is aborted and null is returned
 *
 * @author Julien Leblay
 * @param <T> the generic type
 */
public class SentenceToRule<T extends Formula> implements Rewriter<T, TGD>{

	/**
	 * Rewrite.
	 *
	 * @param input T
	 * @return if the input formula is a conjunction with a single negated atom,
	 * returns an equivalent TGD, otherwise returns null.
	 * @throws RewriterException the rewriter exception
	 */
	@Override
	public TGD rewrite(T input) throws RewriterException {
		if (input instanceof Conjunction) {
			Conjunction<Formula> conjunction = (Conjunction) input;
			Atom head = null;
			List<Atom> body = new LinkedList<>();
			for (Formula f: conjunction) {
				if (f instanceof Negation) {
					if (head == null) {
						Collection<T> subFormula = ((Negation) f).getChildren();
						Formula h = subFormula.iterator().next();
						if (!(h instanceof Atom)) {
							throw new RewriterException("Not a supported constraint sentence: " + input);
						}
						head = (Atom) h;
					} else {
						throw new RewriterException("Not a valid constraint sentence: " + input);
					}
				} else if (f instanceof Atom) {
					body.add((Atom) f);
				} else {
					throw new RewriterException("Not a valid constraint sentence: " + input);
				}
			}
			if (head == null) {
				throw new RewriterException("Contradiction in head currently not supported in PDQ: " + input);
			}
			return new TGD(Conjunction.of(body), Conjunction.of(head));
		}
		throw new RewriterException("Not a supported constraint sentence: " + input);
	}
}
