package uk.ac.ox.cs.pdq.io.pretty;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.LinkedHashMap;
import java.util.Map;

import uk.ac.ox.cs.pdq.db.Attribute;
import uk.ac.ox.cs.pdq.db.Relation;
import uk.ac.ox.cs.pdq.fol.LogicalSymbols;
import uk.ac.ox.cs.pdq.fol.Atom;
import uk.ac.ox.cs.pdq.fol.Dependency;
import uk.ac.ox.cs.pdq.fol.LinearGuarded;
import uk.ac.ox.cs.pdq.fol.Term;
import uk.ac.ox.cs.pdq.io.Writer;

// TODO: Auto-generated Javadoc
/**
 * Writes a concise representation of a dependency to the given output.
 *
 * @author Julien Leblay
 * @param <T> the generic type
 */
public class VeryShortDependencyWriter<T extends Dependency> 
		extends PrettyWriter<T> implements Writer<T> {

	/**
	 * The default out to which dependencies should be written, if not 
	 * explicitly provided at write time.
	 */
	private PrintStream out;

	/**
	 * Instantiates a new very short dependency writer.
	 *
	 * @param out the default output
	 */
	private VeryShortDependencyWriter(PrintStream out) {
		this.out = out;
	}
	
	/**
	 * Fluent pretty writer provider.
	 *
	 * @param out the out
	 * @return a new SQLLikeQueryWriter with the given default output.
	 */
	public static VeryShortDependencyWriter to(PrintStream out) {
		return new VeryShortDependencyWriter<>(out);
	}

	/**
	 * {@inheritDoc}
	 * @see uk.ac.ox.cs.pdq.io.Writer#write(java.io.PrintStream, java.lang.Object)
	 */
	@Override
	public void write(PrintStream out, T tgd) {
		out.print(this.toString(tgd));
	}

	/**
	 * {@inheritDoc}
	 * @see uk.ac.ox.cs.pdq.io.pretty.PrettyWriter#write(java.lang.Object)
	 */
	@Override
	public void write(T t) {
		this.write(this.out, t);
	}

	/**
	 * Returns a short String representation of the given dependency. This
	 * by-passes toString which is too verbose for non-debug purpose.
	 *
	 * @param <T> the generic type
	 * @param t the t
	 * @return a short String representation of the dependency.
	 */
	public static <T extends Dependency> String convert(T t) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		PrintStream ps = new PrintStream(baos);
		VeryShortDependencyWriter.to(ps).write(t);
		return baos.toString();
	}
	
	/**
	 * Gets a string representation of the given TGD.
	 *
	 * @param <T> the generic type
	 * @param tgd the tgd
	 * @return the string representation of the given TGD.
	 */
	private <T extends Dependency> String toString(T tgd) {
		if (tgd instanceof LinearGuarded) {
			return this.toString((LinearGuarded) tgd);
		}
		StringBuilder result = new StringBuilder();
		String sep = "";
		for (Atom a : tgd.getLeft().getAtoms()) {
			result.append(sep);
			Atom f = a;
			result.append(f.getPredicate().getName());
			result.append('(' + joinTerms(f.getTerms(), ", ") + ')');
			sep = " " + LogicalSymbols.AND + ' ';
		}
		result.append(" " + LogicalSymbols.IMPLIES + ' ');
		sep = "";
		for (Atom a : tgd.getRight().getAtoms()) {
			result.append(sep);
			Atom f = a;
			result.append(f.getPredicate().getName());
			result.append('(' + joinTerms(f.getTerms(), ", ") + ')');
			sep = " " + LogicalSymbols.AND + ' ';
		}
		return result.toString();
	}
	
	/**
	 * Gets a string representation of the given LinearGuarded TGD.
	 *
	 * @param tgd the tgd
	 * @return the string representation of the given LinearGuarded TGD.
	 */
	private String toString(LinearGuarded tgd) {
		try {
			StringBuilder result = new StringBuilder();
			Map<Term, Attribute> leftAttributes = new LinkedHashMap<>();
			for (Atom p: tgd.getLeft()) {
				for (int i = 0, l = p.getTermsCount(); i < l; i++) {
					leftAttributes.put(p.getTerm(i), ((Relation) p.getPredicate()).getAttribute(i));
				}
			}
			Map<Term, Attribute> rightAttributes = new LinkedHashMap<>();
			for (Atom p: tgd.getRight()) {
				for (int i = 0, l = p.getTermsCount(); i < l; i++) {
					rightAttributes.put(p.getTerm(i), ((Relation) p.getPredicate()).getAttribute(i));
				}
			}
			leftAttributes.keySet().retainAll(rightAttributes.keySet());
			rightAttributes.keySet().retainAll(leftAttributes.keySet());

			result.append(tgd.getLeft().iterator().next().getPredicate().getName());
			String sep = "(";
			for (Attribute a: leftAttributes.values()) {
				result.append(sep).append(a.getName());
				sep = ",";
			}
			result.append("): ");
			result.append(tgd.getRight().iterator().next().getPredicate().getName());
			sep = "(";
			for (Attribute a: rightAttributes.values()) {
				result.append(sep).append(a.getName());
				sep = ",";
			}
			result.append(")");
			return result.toString();
		} catch (Exception e) {
			throw new IllegalStateException();
		}
	}
}
