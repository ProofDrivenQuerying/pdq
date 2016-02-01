package uk.ac.ox.cs.pdq.io.pretty;

 import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import uk.ac.ox.cs.pdq.db.Relation;
import uk.ac.ox.cs.pdq.db.Schema;
import uk.ac.ox.cs.pdq.db.TypedConstant;
import uk.ac.ox.cs.pdq.fol.Predicate;
import uk.ac.ox.cs.pdq.fol.Term;
import uk.ac.ox.cs.pdq.io.Reader;
import uk.ac.ox.cs.pdq.io.ReaderException;
import uk.ac.ox.cs.pdq.util.Types;

/**
 * Read facts from input stream, and returns then as Collections of Atoms.
 * 
 * @author Julien Leblay
 */
public class DataReader implements Reader<Collection<Predicate>> {

	/** The schema that the facts read must comply with */
	private final Schema schema;
	
	/**
	 * Default constructor.
	 * @param schema
	 */
	public DataReader(Schema schema) {
		this.schema = schema;
	}
	
	/**
	 * {@inheritDoc}
	 * @see uk.ac.ox.cs.pdq.io.Reader#read(java.io.InputStream)
	 */
	@Override
	public List<Predicate> read(InputStream in) {
		List<Predicate> result = new ArrayList<>();
		try (BufferedReader bif = new BufferedReader(new InputStreamReader(in))) {
			String line = null;
			int counter = 1;
			while((line = bif.readLine()) != null) {
				String trimed = line.trim();
				if (!trimed.startsWith("#")) {
					Predicate fact = this.parseFact(trimed, counter);
					if (fact != null) {
					    result.add(fact);
					}
				}
				counter++;
			}
		} catch (IOException e) {
			throw new ReaderException("I/O problem while reading facts.", e);
		}
		return result;
	}

	/**
	 * Parse a single line, return the fact is contains (expected one fact per line). 
	 * @param line
	 * @param lineNumber
	 * @return the fact read from the given line.
	 */
	private Predicate parseFact(String line, int lineNumber) {
		int open = line.indexOf('(');
		if (open > 0) {
			String relationName = line.substring(0, open);
			Relation r = this.schema.getRelation(relationName);
			if (r == null) {
				throw new ReaderException("Relation '" + relationName + "' is not part of the schema.");
			}
			List<Term> terms = new ArrayList<>();
			String[] arguments = line.substring(open + 1, line.indexOf(')')).split(",");
			if (arguments.length != r.getArity()) {
				throw new ReaderException("Fact '" + line + "' does not comply with the schema. (line: " + lineNumber + ")");
			}
			for (int i = 0, l = arguments.length; i < l; i++) {
				terms.add(new TypedConstant<>(Types.cast(r.getAttribute(i).getType(), arguments[i].trim())));
			}
			return new Predicate(r, terms);
		}
		return null;
	}
}
