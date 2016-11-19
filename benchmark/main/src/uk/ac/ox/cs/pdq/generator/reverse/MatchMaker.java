package uk.ac.ox.cs.pdq.generator.reverse;

import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import uk.ac.ox.cs.pdq.EventHandler;
import uk.ac.ox.cs.pdq.fol.Conjunction;
import uk.ac.ox.cs.pdq.fol.ConjunctiveQuery;
import uk.ac.ox.cs.pdq.fol.Atom;
import uk.ac.ox.cs.pdq.fol.Term;
import uk.ac.ox.cs.pdq.fol.Variable;
import uk.ac.ox.cs.pdq.io.pretty.VeryPrettyQueryWriter;
import uk.ac.ox.cs.pdq.io.xml.QueryWriter;
import uk.ac.ox.cs.pdq.planner.accessibleschema.AccessibleSchema.AccessibleRelation;
import uk.ac.ox.cs.pdq.planner.accessibleschema.AccessibleSchema.InferredAccessibleRelation;
import uk.ac.ox.cs.pdq.util.Utility;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import com.google.common.eventbus.Subscribe;

// TODO: Auto-generated Javadoc
/**
 * This class collects facts derived through some reasoning process, and 
 * attempts to find interesting answerable queries based on series of given
 * criteria.
 *  
 * @author Julien Leblay
 */
public class MatchMaker implements EventHandler {

	/** Logger. */
	private static Logger log = Logger.getLogger(ReverseQueryGenerator.class);
	
	/** The interval between which the program outputs results. */
	private static final int INTERVAL = 10;
	
	/** A counter. */
	private int counter;
	
	/** A symbol representing in the output what kind results who bind found in the last round
	 * BY default '-' means nothing happened, '*' means some new infacc atom has been added,
	 * '+' means some new constant has been made accessible. */
	private char toPrint = '-';
	
	/** The out. */
	private final PrintStream out;
	
	/** The accessible constants. */
	private final LinkedHashSet<Term> accConstants = new LinkedHashSet<>();

	/** The inferred-accessible facts. */
	private final LinkedHashMap<FactSignature, Atom> infAccFacts = new LinkedHashMap<>();
	
	/** The clusters. */
	private final Multimap<Term, Atom> clusters = LinkedHashMultimap.create();
	
	/** The selectors. */
	private final List<QuerySelector> selectors = new LinkedList<>();
	
	/**
	 * Instantiates a new match maker.
	 *
	 * @param selectors the selectors
	 */
	public MatchMaker(QuerySelector... selectors) {
		this(System.out, selectors);
	}
	
	/**
	 * Instantiates a new match maker.
	 *
	 * @param out the out
	 * @param selectors the selectors
	 */
	public MatchMaker(PrintStream out, QuerySelector... selectors) {
		this.out = out;
		for (QuerySelector s: selectors) {
			this.selectors.add(s);
		}
	}
	
	/**
	 * Handle inferred-accessible facts.
	 *
	 * @param facts the facts
	 */
	@Subscribe
	public void handleInfAccFacts(Collection<Atom> facts) {
		if (facts != null) {
			for (Atom p: facts) {
				if (p.getPredicate() instanceof InferredAccessibleRelation) {
					Atom f = new Atom(
							((InferredAccessibleRelation) p.getPredicate()).getBaseRelation(),
							p.getTerms());
					FactSignature sig = FactSignature.make(f);
					if (!this.infAccFacts.containsKey(sig)) {
						this.infAccFacts.put(sig, f);
						this.toPrint = '*';
						for (Term t: f.getTerms()) {
							this.clusters.get(t).add(f);
						}
					}
					continue;
				}
				if (p.getPredicate() instanceof AccessibleRelation) {
					if (this.accConstants.addAll(p.getTerms())) {
						if (this.toPrint == '-') {
							this.toPrint = '+';
						}
					}
				}
			}
			this.counter++;
			if (this.counter%INTERVAL==0) {
				this.out.print(this.toPrint);
				this.toPrint = '-';
			}
		}
	}
	
	/**
	 * Checks wether the given query passes all selection tests.
	 *
	 * @param q the q
	 * @return true if all the matcher's selectors have accepted the query q
	 */
	protected boolean accept(ConjunctiveQuery q) {
		for (QuerySelector sel: this.selectors) {
			if (!sel.accept(q)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Prints a report of the matcher's status.
	 */
	public void report() {
		this.out.println();
		if (this.clusters.isEmpty()) {
			this.out.println("No match");
			return;
		}
		this.out.println("Accessible (" + this.accConstants.size() + ") :" + this.accConstants);
		this.out.println("Clusters (" + this.clusters. keySet().size() + ") :");
		Set<Term> clTerms = new LinkedHashSet<>();
		List<Set<Atom>> clusters2 = new LinkedList<>();
		for (Term t: this.clusters.keySet()) {
			Collection<Atom> cluster = this.clusters.get(t);
			if (cluster.size() > 1) {
				this.out.println(t + " (" + cluster.size() + ") : " + cluster);
				clTerms.add(t);
			}
			clusters2.add(Sets.newHashSet(cluster));
		}
		
		int i = 1;
		Collection<Set<Atom>> connectedComponents = Utility.connectedComponents(clusters2);
		this.out.println("\nConnected component (" + connectedComponents.size() + "):");
		for (Set<Atom> component: connectedComponents) {
			this.out.println(component.size() + " : " + component);
			Set<Variable> terms = new HashSet<>(Utility.getVariables(component));
			terms.retainAll(this.accConstants);
			this.out.println("\tacc constants: " + terms);

			Set<Atom> restricted = Sets.newHashSet(component);
			while (restricted.size() > 30) {
				for (Iterator<Atom> it = restricted.iterator(); it.hasNext();) {
					Atom p = it.next();
					if (Utility.getTypedConstants(p).isEmpty()) {
						log.warn("Removing from connected component: " + p);
						it.remove();
						break;
					}
				}
			}
			
			for (Set<Atom> body: Sets.powerSet(restricted)) {
				//Set<Term> head = new LinkedHashSet<>(Utility.getTerms(body));
				ConjunctiveQuery candidate = new ConjunctiveQuery(
						Utility.getVariables(body),
						Conjunction.of(body));
				if (this.accept(candidate)) {
					VeryPrettyQueryWriter.to(this.out).write(candidate);
					this.out.println();
					
					try(PrintStream ps = new PrintStream("test/output/candidate-" + "Q" + ".xml")) {
						new QueryWriter().write(ps, candidate);
					} catch (FileNotFoundException e) {
						log.warn(e);
					}
				}
			}
		}
	}
}
