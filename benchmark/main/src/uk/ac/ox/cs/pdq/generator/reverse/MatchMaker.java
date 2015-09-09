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
import uk.ac.ox.cs.pdq.fol.Predicate;
import uk.ac.ox.cs.pdq.fol.Query;
import uk.ac.ox.cs.pdq.fol.Term;
import uk.ac.ox.cs.pdq.fol.Variable;
import uk.ac.ox.cs.pdq.io.pretty.VeryPrettyQueryWriter;
import uk.ac.ox.cs.pdq.io.xml.QueryWriter;
import uk.ac.ox.cs.pdq.planner.db.access.AccessibleSchema.AccessibleRelation;
import uk.ac.ox.cs.pdq.planner.db.access.AccessibleSchema.InferredAccessibleRelation;
import uk.ac.ox.cs.pdq.util.Utility;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import com.google.common.eventbus.Subscribe;

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
	
	private static final int INTERVAL = 10;
	private int counter;
	private char toPrint = '-';
	
	private final PrintStream out;
	private final LinkedHashSet<Term> accConstants = new LinkedHashSet<>();
	private final LinkedHashMap<FactSignature, Predicate> infAccFacts = new LinkedHashMap<>();
	private final Multimap<Term, Predicate> clusters = LinkedHashMultimap.create();
	private final List<QuerySelector> selectors = new LinkedList<>();
	
	public MatchMaker(QuerySelector... selectors) {
		this(System.out, selectors);
	}
	
	public MatchMaker(PrintStream out, QuerySelector... selectors) {
		this.out = out;
		for (QuerySelector s: selectors) {
			this.selectors.add(s);
		}
	}
	
	@Subscribe
	public void handleInfAccFacts(Collection<Predicate> facts) {
		if (facts != null) {
			for (Predicate p: facts) {
				if (p.getSignature() instanceof InferredAccessibleRelation) {
					Predicate f = new Predicate(
							((InferredAccessibleRelation) p.getSignature()).getBaseRelation(),
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
				if (p.getSignature() instanceof AccessibleRelation) {
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
	 * @param q
	 * @return true if all the matcher's selectors have accepted the query q
	 */
	protected boolean accept(Query<?> q) {
		for (QuerySelector sel: this.selectors) {
			if (!sel.accept(q)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Prints a report of the matcher's status 
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
		List<Set<Predicate>> clusters2 = new LinkedList<>();
		for (Term t: this.clusters.keySet()) {
			Collection<Predicate> cluster = this.clusters.get(t);
			if (cluster.size() > 1) {
				this.out.println(t + " (" + cluster.size() + ") : " + cluster);
				clTerms.add(t);
			}
			clusters2.add(Sets.newHashSet(cluster));
		}
		
		int i = 1;
		Collection<Set<Predicate>> connectedComponents = Utility.connectedComponents(clusters2);
		this.out.println("\nConnected component (" + connectedComponents.size() + "):");
		for (Set<Predicate> component: connectedComponents) {
			this.out.println(component.size() + " : " + component);
			Set<Variable> terms = new HashSet<>(Utility.getVariables(component));
			terms.retainAll(this.accConstants);
			this.out.println("\tacc constants: " + terms);

			Set<Predicate> restricted = Sets.newHashSet(component);
			while (restricted.size() > 30) {
				for (Iterator<Predicate> it = restricted.iterator(); it.hasNext();) {
					Predicate p = it.next();
					if (p.getSchemaConstants().isEmpty()) {
						log.warn("Removing from connected component: " + p);
						it.remove();
						break;
					}
				}
			}
			
			for (Set<Predicate> body: Sets.powerSet(restricted)) {
				Set<Term> head = new LinkedHashSet<>(Utility.getTerms(body));
				ConjunctiveQuery candidate = new ConjunctiveQuery("Q" + (i++),
						new ArrayList<>(head),
						Conjunction.of(body));
				if (this.accept(candidate)) {
					VeryPrettyQueryWriter.to(this.out).write(candidate);
					this.out.println();
					
					try(PrintStream ps = new PrintStream("test/output/candidate-" + candidate.getHead().getName() + ".xml")) {
						new QueryWriter().write(ps, candidate);
					} catch (FileNotFoundException e) {
						log.warn(e);
					}
				}
			}
		}
	}
}
