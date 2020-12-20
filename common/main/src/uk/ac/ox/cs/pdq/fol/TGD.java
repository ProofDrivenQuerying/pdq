// This file is part of PDQ (https://github.com/ProofDrivenQuerying/pdq) which is released under the MIT license.
// See accompanying LICENSE for copyright notice and full details.

package uk.ac.ox.cs.pdq.fol;

import java.util.Arrays;
import java.util.List;

/**
 *
 * @author Efthymia Tsamoura, Mark Ridler
 * @author Stefano
 */
public class TGD extends Dependency {

	private static final long serialVersionUID = 2745278271063580698L;

	protected TGD(Atom[] body, Atom[] head) {
		super(body, head);
	}

	protected TGD(Atom[] body, Atom[] head, String name) {
		super(body, head, name);
	}

	@Override
	public String toString() {
		return this.body.toString() + " " + LogicalSymbols.IMPLIES + " " + this.head.toString();
	}

	public boolean isLinear() {
		return this.body.getAtoms().length == 1;
	}

	public boolean isGuarded() {
		List<Variable> universalList = Arrays.asList(getTopLevelQuantifiedVariables());
		for (Atom atom : getBodyAtoms())
			if (Arrays.asList(atom.getTerms()).containsAll(universalList))
				return true;
		return false;
	}

	public static TGD create(Atom[] body, Atom[] head) {
		return Cache.tgd.retrieve(new TGD(body, head));
	}

	public static TGD create(Atom[] body, Atom[] head, String name) {
		return Cache.tgd.retrieve(new TGD(body, head, name));
	}
}
