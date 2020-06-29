// This file is part of PDQ (https://github.com/ProofDrivenQuerying/pdq) which is released under the MIT license.
// See accompanying LICENSE for copyright notice and full details.

package uk.ac.ox.cs.pdq.reasoningdatabase;

import java.io.IOException;
import java.util.Collection;

import uk.ac.ox.cs.pdq.fol.Atom;

/**
 * This interface describes any object that can store facts in a bufferered write structure.
 * @author gabor
 */
public interface DataSink {
	public void addFacts(Collection<Atom> facts) throws IOException;
}
