package uk.ac.ox.cs.pdq.io.pretty;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.io.IOUtils;

import uk.ac.ox.cs.pdq.db.Relation;
import uk.ac.ox.cs.pdq.db.Schema;
import uk.ac.ox.cs.pdq.db.TGD;
import uk.ac.ox.cs.pdq.db.TypedConstant;
import uk.ac.ox.cs.pdq.db.builder.DependencyBuilder;
import uk.ac.ox.cs.pdq.fol.Conjunction;
import uk.ac.ox.cs.pdq.fol.LogicalSymbols;
import uk.ac.ox.cs.pdq.fol.Predicate;
import uk.ac.ox.cs.pdq.fol.Term;
import uk.ac.ox.cs.pdq.fol.Variable;
import uk.ac.ox.cs.pdq.io.Reader;
import uk.ac.ox.cs.pdq.io.ReaderException;
import uk.ac.ox.cs.pdq.util.Types;

import com.google.common.base.Preconditions;

// TODO: Auto-generated Javadoc
/**
 * A pretty reader for dependencies.
 * 
 * @author Julien Leblay
 */
public class PrettyDependencyReader implements Reader<TGD> {

	/** The dependency being built. */
	private DependencyBuilder builder = null;
	
	/** The schema the dependency is based on . */
	private final Schema schema;

	/**
	 * Constructor for PrettyDependencyReader.
	 * @param schema Schema
	 */
	public PrettyDependencyReader(Schema schema) {
		this.schema = schema;
	}
	
	/**
	 * Read a TGD from the input stream.
	 *
	 * @param in InputStream
	 * @return TGD
	 * @see uk.ac.ox.cs.pdq.io.Reader#read(InputStream)
	 */
	@Override
	public TGD read(InputStream in) {
		this.builder = new DependencyBuilder();
		try {
			StringWriter writer = new StringWriter();
			IOUtils.copy(in, writer, "UTF-8");
			String s = writer.toString();
			return this.parse(s); 
		} catch (IOException e) {
			throw new ReaderException("Could read input stream");
		}
	}

	/**
	 * Parses a TGD from the input String.
	 *
	 * @param s String
	 * @return TGD
	 */
	private TGD parse(String s) {
		Preconditions.checkArgument(s != null);
		String[] sides = s.split(LogicalSymbols.IMPLIES.toString());
		if (sides.length != 2) {
			throw new ReaderException("Dependency must have exactly one head and one body");
		}
		for (Predicate p: this.parseConjunction(sides[0])) {
			this.builder.addLeftAtom(p);
		}
		for (Predicate p: this.parseConjunction(sides[1])) {
			this.builder.addRightAtom(p);
		}
		return this.builder.build();
	}
	
	/**
	 * Parses a conjunction of atoms from the given String.
	 *
	 * @param s String
	 * @return Conjunction<Predicate>
	 */
	public Conjunction<Predicate> parseConjunction(String s) {
		String[] sAtoms = s.split(LogicalSymbols.AND.toString());
		if (sAtoms == null || sAtoms.length == 0) {
			throw new ReaderException("Atom list cannot be empty.");
		}
		Collection<Predicate> atoms = new LinkedList<>();
		for (String atom: sAtoms) {
			String trimmed = atom.trim();
			int openIndex = trimmed.indexOf('(');
			int closeIndex = trimmed.indexOf(')');
			if (openIndex < 0 || closeIndex < 0 || closeIndex <= openIndex) {
				throw new ReaderException("Each atom must consistent opening and closing parenthesis.");
			}
			if (trimmed.length() > closeIndex + 1) {
				throw new ReaderException("Atom can only be followed by a logical operator.");
			}
			String prefix = trimmed.substring(0, openIndex);
			Relation relation = this.schema.getRelation(prefix);
			if (relation == null) {
				throw new ReaderException("Relation " + prefix + " is not part of the schema.");
			}
			String[] sTerms = trimmed.substring(openIndex + 1, closeIndex).split(",");
			if (sTerms.length != relation.getArity()) {
				throw new ReaderException("Relation " + prefix + " must have exactly " + relation.getArity() + " arguments.");
			}
			List<Term> terms = new ArrayList<>();
			for (int i = 0, l = sTerms.length; i < l; i++) {
				String t = sTerms[i].trim();
				if (t.startsWith("'")) {
					if (t.endsWith("'")) {
						try {
							Object o  = Types.cast(relation.getAttribute(i).getType(), t.substring(1, t.length() - 1));
							terms.add(new TypedConstant<>(o));
						} catch (ClassCastException e) {
							throw new ReaderException(e.getMessage());
						}
					} else {
						throw new ReaderException("Constant declaration not properly closed " + t);
					}
				} else if (t.endsWith("'")) {
					throw new ReaderException("Constant declaration not properly opened " + t);
				} else {
					terms.add(new Variable(t));
				}
			}
			atoms.add(new Predicate(relation, terms));
		}
		return Conjunction.of(atoms);
	}
}
