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
