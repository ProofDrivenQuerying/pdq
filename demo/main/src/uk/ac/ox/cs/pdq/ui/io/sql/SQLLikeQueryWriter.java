package uk.ac.ox.cs.pdq.ui.io.sql;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import uk.ac.ox.cs.pdq.db.Attribute;
import uk.ac.ox.cs.pdq.db.Relation;
import uk.ac.ox.cs.pdq.fol.ConjunctiveQuery;
import uk.ac.ox.cs.pdq.fol.Atom;
import uk.ac.ox.cs.pdq.fol.Query;
import uk.ac.ox.cs.pdq.fol.Term;
import uk.ac.ox.cs.pdq.io.Writer;
import uk.ac.ox.cs.pdq.io.pretty.PrettyWriter;

import com.google.common.base.Preconditions;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;

// TODO: Auto-generated Javadoc
/**
 * Writes a concise representation of a query to the given output.
 *
 * @author Julien Leblay
 */
public class SQLLikeQueryWriter extends PrettyWriter<Query<?>> implements Writer<Query<?>> {

	/** The Constant ALIAS_PREFIX. */
	private static final String ALIAS_PREFIX = "a";

	/**
	 * The default out to which queries should be written, if not 
	 * explicitly provided at write time.
	 */
	private PrintStream out;

	/**
	 * Instantiates a new SQL like query writer.
	 *
	 * @param out the default output
	 */
	private SQLLikeQueryWriter(PrintStream out) {
		this.out = out;
	}
	
	/**
	 * Fluent pretty writer provider.
	 *
	 * @param out the out
	 * @return a new SQLLikeQueryWriter with the given default output.
	 */
	public static SQLLikeQueryWriter to(PrintStream out) {
		return new SQLLikeQueryWriter(out);
	}
	
	/*
	 * (non-Javadoc)
	 * @see uk.ac.ox.cs.pdq.builder.io.PrettyWriter#write(java.lang.Object)
	 */
	@Override
	public void write(Query<?> q) {
		this.write(this.out, q);
	}
	
	/*
	 * (non-Javadoc)
	 * @see uk.ac.ox.cs.pdq.provider.io.Writer#write(java.io.PrintStream, java.lang.Object)
	 */
	@Override
	public void write(PrintStream out, Query<?> q) {
		out.print(this.toString(q));
	}
	
	/**
	 * To string.
	 *
	 * @param query the query
	 * @return the string
	 */
	/*
	 * 
	 */
	private String toString(Query<?> query) {
		Preconditions.checkArgument(query instanceof ConjunctiveQuery, "Non conjunctive queries not yet supported.");
		
		ConjunctiveQuery q = (ConjunctiveQuery) query;
		StringBuilder result = new StringBuilder();
		// Make predicate aliases and join clusters.
		int counter = 0;
		Map<Atom, String> aliases = new LinkedHashMap<>();
		Multimap<Term, Atom> joins = LinkedHashMultimap.create();
		for (Atom p: q.getBody()) {
			aliases.put(p, ALIAS_PREFIX + (counter++));
			for (Term t: p.getTerms()) {
				joins.put(t, p);
			}
		}
		// Make SELECT clause
		result.append("SELECT ");
		Collection<Term> head = q.getHead().getTerms();
		if (head.isEmpty()) {
			result.append("*");
		} else {
			String sep = "";
			for (Term t: head) {
				Atom p = joins.get(t).iterator().next();
				int pos = p.getTermPositions(t).iterator().next();
				Attribute a = ((Relation) p.getPredicate()).getAttribute(pos);
				result.append(sep).append(aliases.get(p)).append('.').append(a.getName());
				sep = ", ";
			}
		}
		
		// Remove non-join clusters
		for (Iterator<Term> i = joins.keySet().iterator(); i.hasNext(); ) {
			Collection<Atom> cluster = joins.get(i.next());
			if (cluster.size() <= 1) {
				i.remove();
			}
		}
		
		// Make FROM clause
		result.append("\nFROM ");
		String sep = "";
		Set<Atom> joined = new LinkedHashSet<>();
//		Set<PredicateFormula> unused = new LinkedHashSet<>(aliases.keySet());
		for (Term t : joins.keySet()) {
			Iterator<Atom> i = joins.get(t).iterator();
			for (Atom curr: joins.get(t)) {
				if (!joined.contains(curr)) {
					result.append(sep).append(curr.getPredicate().getName())
						.append(" AS ").append(aliases.get(curr));
					String sep2 = " ON ";
					for (Atom other: joined) {
						for (Term u : joins.keySet()) {
							if (joins.get(u).contains(other) && joins.get(u).contains(curr)) {
								result.append(sep2).append(aliases.get(curr)).append('.')
									.append(((Relation) curr.getPredicate()).getAttribute(curr.getTermPositions(u).get(0)))
									.append("=").append(aliases.get(other)).append('.')
									.append(((Relation) other.getPredicate()).getAttribute(other.getTermPositions(u).get(0)));
								sep2 = " AND ";
							}
						}
					}
					joined.add(curr);
				}
				Atom next = i.next();
				curr = next;
				sep = "\n\tJOIN ";
			} 
		}
		sep = joined.isEmpty() ? "" : ", ";
		for (Atom p : aliases.keySet()) {
			if (!joined.contains(p)) {
				result.append(sep).append(p.getPredicate().getName())
				.append(" AS ").append(aliases.get(p));
			}
		}
		
		
		// Make WHERE clause
		sep = "\nWHERE ";
		for (Atom p: q.getBody()) {
			List<Term> terms = p.getTerms();
			for (int i = 0, l = terms.size(); i < l; i++) {
				if (!terms.get(i).isVariable() && !terms.get(i).isSkolem()) {
					result.append(sep).append(aliases.get(p)).append('.')
						.append(((Relation) p.getPredicate()).getAttribute(i))
						.append('=').append("'").append(terms.get(i)).append("'"); 
					sep = "\nAND ";
				}
			}
		}
		
		return result.toString();
	}
	
	/**
	 * Returns a short String representation of the given dependency. This
	 * by-passes toString which is too verbose for non-debug purpose.
	 *
	 * @param t the t
	 * @return a short String representation of the dependency.
	 */
	public static String convert(Query<?> t) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		PrintStream ps = new PrintStream(baos);
		SQLLikeQueryWriter.to(ps).write(t);
		return baos.toString();
	}
	
//	public static void main(String... args) {
//		System.setProperty("user.dir", "/auto/users/leblay/.pdq/");
//		try(InputStream sin = new FileInputStream("/auto/users/leblay/.pdq/schemas/3.s");
//			InputStream qin = new FileInputStream("/auto/users/leblay/.pdq/queries/3_1.q");
//			InputStream pin = new FileInputStream("/auto/users/leblay/.pdq/plans/3_1_1.p")) {
//			Schema s = Readers.from(sin).read();
//			ConjunctiveQuery q = Readers.from(qin).with(s).read();
//			Plan p = Readers.from(pin).with(s).read();
//			RuntimeParameters params = new RuntimeParameters("/auto/users/leblay/.pdq/plans/3_1_1.properties");
//			params.setExecutorType(ExecutorTypes.PIPELINED);
//			uk.ac.ox.cs.pdq.runtime.Runtime runtime = new uk.ac.ox.cs.pdq.runtime.Runtime(params, s);
//			runtime.registerEventHandler(new TuplePrinterTest(System.out));
//			runtime.evaluatePlan(p, q);
//		} catch (IOException e) {
//			e.printStackTrace();
//		} catch (EvaluationException e) {
//			e.printStackTrace();
//		}
//	}
}
