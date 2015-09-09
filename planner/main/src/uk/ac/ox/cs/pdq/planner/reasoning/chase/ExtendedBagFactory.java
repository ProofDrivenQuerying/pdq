package uk.ac.ox.cs.pdq.planner.reasoning.chase;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;

import uk.ac.ox.cs.pdq.fol.AcyclicQuery;
import uk.ac.ox.cs.pdq.fol.ConjunctiveQuery;
import uk.ac.ox.cs.pdq.fol.Predicate;
import uk.ac.ox.cs.pdq.fol.Query;
import uk.ac.ox.cs.pdq.planner.db.access.AccessibleSchema;

import com.google.common.base.Preconditions;


/**
 * Creates bags of facts.
 * The returned bags keep the sub-queries and inferred accessible dependencies satisfied in the current bag. 
 *
 * @author Efthymia Tsamoura
 */
public final class ExtendedBagFactory extends uk.ac.ox.cs.pdq.reasoning.chase.BagFactory{

	protected static Logger log = Logger.getLogger(ExtendedBagFactory.class);

	/** Input query*/
	private final Query<?> query;

	/** Accessible sub-queries of the input query*/
	private final List<Query<?>> queries;


	/**
	 * Constructor for BagFactory.
	 * @param schema Schema
	 * @param accessibleSchema AccessibleSchema
	 * @param query Query<?>
	 */
	public ExtendedBagFactory(AccessibleSchema accessibleSchema, Query<?> query) {
		this(accessibleSchema, query, createImportantQueries(query, accessibleSchema));
	}

	/**
	 * Constructor for BagFactory.
	 * @param schema Schema
	 * @param accessibleSchema AccessibleSchema
	 * @param query Query<?>
	 * @param queries List<Query<?>>
	 */
	private ExtendedBagFactory(AccessibleSchema accessibleSchema, Query<?> query, List<Query<?>> queries) {
		super(accessibleSchema);
		Preconditions.checkNotNull(accessibleSchema);
		Preconditions.checkNotNull(query);
		this.query = query;
		this.queries = queries;
	}

	/**
	 * @return BagFactory
	 */
	@Override
	public ExtendedBagFactory clone() {
		return new ExtendedBagFactory((AccessibleSchema) this.schema, this.query, this.queries);
	}

	/**
	 *
	 * @param facts
	 * @return
	 * 		a bag initialised with the input set of atoms
	 */
	@Override
	public ExtendedBag createBag(Collection<Predicate> facts) {
		return new ExtendedBag(
				facts, this.schema.getDependencies(),
				((AccessibleSchema)this.schema).getInferredAccessibilityAxioms(),
				this.queries);
	}


	/**
	 * Creates the accessible sub-queries that must be checked during blocking.
	 * If the input query is an acyclic one, then the sub-queries to be checked are the prefixes of the input query.
	 * Otherwise, every sub-query for every combination of free and existential variable must be checked
	 * @param query Query<?>
	 * @param accessibleSchema AccessibleSchema
	 * @return List<Query<?>>
	 */
	private static List<Query<?>> createImportantQueries(Query<?> query, AccessibleSchema accessibleSchema) {
		log.debug("Creating important sub-queries...");
		List<Query<?>> result = new LinkedList<>();
		if (query instanceof AcyclicQuery) {
			for (Query<?> q: ((AcyclicQuery) query).getSuffixQueries()) {
				result.add(accessibleSchema.accessible(q));
			}
		}
		else if (query instanceof ConjunctiveQuery) {
			for (Query<?> q: query.getImportantSubqueries()) {
				result.add(accessibleSchema.accessible(q));
			}
		}
		log.debug("Important sub-queries created. (" + result.size() + ")");
		return result;
	}
}
