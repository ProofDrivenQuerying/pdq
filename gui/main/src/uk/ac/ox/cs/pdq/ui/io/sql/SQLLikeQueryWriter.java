// This file is part of PDQ (https://github.com/ProofDrivenQuerying/pdq) which is released under the MIT license.
// See accompanying LICENSE for copyright notice and full details.

package uk.ac.ox.cs.pdq.ui.io.sql;

import com.google.common.base.Preconditions;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import uk.ac.ox.cs.pdq.db.Attribute;
import uk.ac.ox.cs.pdq.db.Relation;
import uk.ac.ox.cs.pdq.db.Schema;
import uk.ac.ox.cs.pdq.fol.Atom;
import uk.ac.ox.cs.pdq.fol.ConjunctiveQuery;
import uk.ac.ox.cs.pdq.fol.Formula;
import uk.ac.ox.cs.pdq.fol.Term;
import uk.ac.ox.cs.pdq.io.Writer;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.*;
//import uk.ac.ox.cs.pdq.io.pretty.PrettyWriter;

// TODO: Auto-generated Javadoc
/**
 * Writes a concise representation of a query to the given output.
 *
 * @author Julien Leblay
 */
public class SQLLikeQueryWriter implements Writer<Formula> {

	/** The Constant ALIAS_PREFIX. */
	private static final String ALIAS_PREFIX = "a";

	/**
	 * The default out to which queries should be written, if not 
	 * explicitly provided at write time.
	 */
	private PrintStream out;
	private Schema schema;

	/**
	 * Instantiates a new SQL like query writer.
	 *
	 * @param out the default output
	 */
	private SQLLikeQueryWriter(PrintStream out, Schema s) {
		this.out = out;
		this.schema = s;
	}
	
	/**
	 * Fluent pretty writer provider.
	 *
	 * @param out the out
	 * @return a new SQLLikeQueryWriter with the given default output.
	 */
	public static SQLLikeQueryWriter to(PrintStream out, Schema s) {
		return new SQLLikeQueryWriter(out, s);
	}
	
	/*
	 * (non-Javadoc)
	 * @see uk.ac.ox.cs.pdq.builder.io.PrettyWriter#write(java.lang.Object)
	 */
	public void write(Formula f) {
		this.write(this.out, f);
	}
	
	/*
	 * (non-Javadoc)
	 * @see uk.ac.ox.cs.pdq.provider.io.Writer#write(java.io.PrintStream, java.lang.Object)
	 */
	public void write(PrintStream out, Formula f) {
		out.print(this.toString(f));
	}
	
	/**
	 * To string.
	 *
	 * @param f Formula
	 * @return the string
	 */
	/*
	 * 
	 */
	private String toString(Formula f) {
		Preconditions.checkArgument(f instanceof ConjunctiveQuery, "Non conjunctive queries not yet supported.");
		
		ConjunctiveQuery q = (ConjunctiveQuery) f;
		StringBuilder result = new StringBuilder();
		// Make predicate aliases and join clusters.
		int counter = 0;
		Map<Atom, String> aliases = new LinkedHashMap<>();
		Multimap<Term, Atom> joins = LinkedHashMultimap.create();
		for(Atom p : q.getBody().getAtoms()) {
			aliases.put(p, ALIAS_PREFIX + (counter++));
			for (Term t: p.getTerms()) {
				joins.put(t, p);
			}
		}
		// Make SELECT clause
		result.append("SELECT ");
		Term[] head = q.getFreeVariables();
		if (head.length == 0) {
			result.append("*");
		} else {
			String sep = "";
			for (Term t: head) {
				Atom p = joins.get(t).iterator().next();
				int pos = p.getTermPosition(t);
				Relation r = this.schema.getRelation(p.getPredicate().getName());
				if(r != null)
				{
					Attribute a = r.getAttribute(pos);
					result.append(sep).append(aliases.get(p)).append('.').append(a.getName());
					sep = ", ";
				}
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
								Relation r = this.schema.getRelation(curr.getPredicate().getName());
								Relation r2 = this.schema.getRelation(other.getPredicate().getName());
								if((r != null) && (r2 != null))
								{
									result.append(sep2).append(aliases.get(curr)).append('.')
										.append(r.getAttribute(curr.getTermPosition(u)))
										.append("=").append(aliases.get(other)).append('.')
										.append(r2.getAttribute(other.getTermPosition(u)));
								}
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
		Atom[] atoms = q.getBody().getAtoms();
		for (int a = 0; a < atoms.length; a++) {
			Atom p = atoms[a];
			Term[] terms = p.getTerms();
			for (int i = 0, l = terms.length; i < l; i++) {
				if (!terms[i].isVariable()) {
					Relation r = this.schema.getRelation(p.getPredicate().getName());
					if(r != null){
						Attribute aa = r.getAttribute(i);
						result.append(sep).append(aliases.get(p)).append('.')
								.append(aa)
						.append('=').append("'").append(terms[i]).append("'");
					}else{
						System.out.println("something has happened");
					}

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
	 * @param f the Formula
	 * @param s the Schema
	 * @return a short String representation of the dependency.
	 */
	public static String convert(Formula f, Schema s) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		PrintStream ps = new PrintStream(baos);
		if(f instanceof ConjunctiveQuery)
		{
			SQLLikeQueryWriter.to(ps, s).write(f);
		}	
		return baos.toString();
	}
}
