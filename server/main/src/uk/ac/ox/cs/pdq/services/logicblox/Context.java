package uk.ac.ox.cs.pdq.services.logicblox;

import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import org.apache.log4j.Logger;

import uk.ac.ox.cs.pdq.db.AccessMethod;
import uk.ac.ox.cs.pdq.db.Dependency;
import uk.ac.ox.cs.pdq.db.Relation;
import uk.ac.ox.cs.pdq.db.Schema;
import uk.ac.ox.cs.pdq.db.View;
import uk.ac.ox.cs.pdq.db.builder.SchemaBuilder;
import uk.ac.ox.cs.pdq.fol.Atom;
import uk.ac.ox.cs.pdq.fol.Query;
import uk.ac.ox.cs.pdq.services.logicblox.rewrite.ProtoBufferUnwrapper;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import com.logicblox.common.protocol.CommonProto.PredicateDeclaration;
import com.logicblox.common.protocol.CommonProto.Rule;

// TODO: Auto-generated Javadoc
/**
 * The context holds information about Logicblox workspaces. It is in charge 
 * of converting and storing LB-data structures to PDQ-data structures.
 * 
 * @author Julien LEBLAY
 */
public class Context {

	/** Logger. */
	static Logger log = Logger.getLogger(Context.class);

	/**  LB workspace pointer. */
	private final Workspace workspace;

	/** Schema associated with the context's workspace. */
	private Schema schema;

	/** Schema builder. */
	private final SchemaBuilder builder;

	/** Unwrapper for incoming protobuf messages. */
	private final ProtoBufferUnwrapper proto;

	/** Stores predicate declarations that have not been committed yet. */
	private Multimap<String, PredicateDeclaration> pendingRelations = LinkedHashMultimap.create();

	/** Stores view declarations that have not been committed yet. */
	private Multimap<String, Rule> pendingViews = LinkedHashMultimap.create();

	/** Stored constraints that have not been committed yet. */
	private Multimap<String, Rule> pendingConstraints = LinkedHashMultimap.create();

	/** Index every relation or constraints by their names. */
	private Multimap<String, Object> index = LinkedHashMultimap.create();

	/** LB execution graphs's topological order. */
	private BiMap<String, Long> topoOrder = HashBiMap.create();
	
	/** Flag, set to true if the context has any uncommitted changes. */
	private boolean hasModifs = true;

	/**
	 * Gets the workspace.
	 *
	 * @return Workspace
	 */
	public Workspace getWorkspace() {
		return this.workspace;
	}
	
	/**
	 * Gets the schema.
	 *
	 * @return Schema
	 */
	public Schema getSchema() {
		log.debug("Getting schema for workspace " + this.workspace.name);
		if (this.hasModifs){
			log.trace("has modifs " + this.pendingRelations.size()
					+ ", " + this.pendingViews.size()
					+ ", " + this.pendingConstraints.size()
					);
			if (!this.pendingRelations.isEmpty())  {
				log.trace("has pending relations " + this.pendingRelations.keySet());
				this.commitPendingRelations();
			}
			if (!this.pendingViews.isEmpty())  {
				log.trace("has pending views " + this.pendingViews.keySet());
				this.commitPendingViews();
			}
			if (!this.pendingConstraints.isEmpty())  {
				log.trace("has pending contraints " + this.pendingConstraints.keySet());
				this.commitPendingConstraints();
			}
			this.schema = this.builder.build();
			this.hasModifs = false;
			log.info("Schema: " + this.schema.getRelations().size() +
					" relations, " + this.schema.getDependencies().size() +
					" constraints");
		}
		return this.schema;
	}
	
	/**
	 * Constructor for Context.
	 * @param workspace Workspace
	 */
	public Context(Workspace workspace) {
		super();
		assert workspace != null;
		this.workspace = workspace;
		this.builder = Schema.builder();
		this.proto = new ProtoBufferUnwrapper(this.builder);
	}

	/**
	 * Modifies the access of the relations in the schema under construction
	 * such that predicate preceeding the query's body predicates in the 
	 * execution graph's topological sort orders are made inaccessible, while
	 * all other relations have free access.
	 *
	 * @param query the query
	 * @return a copy of the schema, with the newly modified access methods.
	 */
	public Schema setAccesses(Query<?> query) {
		Set<String> queryPreds  = new LinkedHashSet<>();
		for (Atom p: query.getBody().getAtoms()) {
			queryPreds.add(p.getName());
		}
		
		Set<String> inaccessibles = new LinkedHashSet<>();
		TreeMap<Long, String> invTopo = new TreeMap<>(this.topoOrder.inverse());
		for (String name: queryPreds) {
			Long rank = this.topoOrder.get(name);
			if (rank != null) {
				inaccessibles.addAll(invTopo.subMap(0l, rank).values());
			}
		}

		for (Relation rel: this.builder.getRelations()) {
			if (inaccessibles.contains(rel.getName())) {
				this.builder.setAccessMethods(rel.getName());
			} else {
				this.builder.setAccessMethods(rel.getName(), new AccessMethod());
			}
		}
		return this.builder.build();
	}

	/**
	 * Removes the object(s) under the given name present in the index.
	 * The object might be relations, views, dependencies. This method
	 * has no effect if the index contains no such entry.
	 *
	 * @param name the name
	 */
	public void remove(String name) {
		log.info("Removing " + name + " from " + this.workspace.name);
		for (Object o : this.index.removeAll(name)) {
			if (o instanceof Relation) {
				Relation r = (Relation) o;
				for (Dependency ic : this.builder.getDependencies()) {
					if (ic.contains(r)) {
						log.warn("Implicit removal of dependency " + ic);
						this.builder.removeDependency(ic);
					}
				}
				this.builder.removeRelation(r);
			} else {
				this.builder.removeDependency((Dependency) o);
			}
		}
		this.pendingRelations.removeAll(name);
		this.pendingViews.removeAll(name);
		this.pendingConstraints.removeAll(name);
		this.hasModifs = true;
	}

	/**
	 * Add a new PredicateDeclaration to the collection of pending ones.
	 *
	 * @param name the name
	 * @param predDecl the pred decl
	 */
	public void putRelation(String name, PredicateDeclaration predDecl) {
		if (!this.index.containsKey(name)) {
			log.debug("Putting relation " + name + " to " + this.workspace.name);
			this.pendingRelations.put(name, predDecl);
			this.hasModifs = true;
		}
	}

	/**
	 * Add a new View to the collection of pending ones.
	 *
	 * @param name the name
	 * @param rule the rule
	 */
	public void putView(String name, Rule rule) {
		if (!this.index.containsKey(name)) {
			log.debug("Putting view " + name + " to " + this.workspace.name);
			this.pendingViews.put(name, rule);
			this.hasModifs = true;
		}
	}

	/**
	 * Add a new Constraint to the collection of pending ones.
	 *
	 * @param name the name
	 * @param rule the rule
	 */
	public void putDependency(String name, Rule rule) {
		if (!this.index.containsKey(name)) {
			log.debug("Putting constraint " + name + " to " + this.workspace.name);
			this.pendingConstraints.put(name, rule);
			this.hasModifs = true;
		}
	}

	/**
	 * Commits all pending relations, and removes successfully commited ones
	 * from the collection of pending relations.
	 */
	private void commitPendingRelations() {
		boolean hasChanged = false;
		int initialSize = this.pendingRelations.size();
		do {
			hasChanged = false;
			for (Iterator<Entry<String, PredicateDeclaration>> i =
					this.pendingRelations.entries().iterator(); i.hasNext();) {
				Entry<String, PredicateDeclaration> entry = i.next();
				try {
					Relation r = this.proto.unwrapPredicateDeclaration(entry.getValue());
					this.builder.addRelation(r);
					this.index.put(entry.getKey(), r);
					i.remove();
					hasChanged = true;
				} catch (Exception e) {
					log.debug(entry.getKey() + " relation not committed.");
//					e.printStackTrace();
				}
			}
		} while (hasChanged && !this.pendingRelations.isEmpty());
		if (!this.pendingRelations.isEmpty()) {
			log.warn(this.pendingRelations.size() + " relations out of " + 
					initialSize + " could not be committed: "
					+ this.pendingRelations.keySet()
					);
		}
	}

	/**
	 * Commits all pending views, and removes successfully committed ones
	 * from the collection of pending views.
	 */
	private void commitPendingViews() {
		boolean hasChanged = false;
		int initialSize = this.pendingViews.size();
		do {
			hasChanged = false;
			for (Iterator<Entry<String, Rule>> i =
					this.pendingViews.entries().iterator(); i.hasNext();) {
				Entry<String, Rule> entry = i.next();
				try {
					View v = (View) this.proto.ruleToView(entry.getValue());
					this.builder.addRelation(v);
					this.index.put(entry.getKey(), v);
					i.remove();
					hasChanged = true;
				} catch (Exception e) {
					log.debug(entry.getKey() + " view not committed.");
//					e.printStackTrace();
				}
			}
		} while (hasChanged && !this.pendingViews.isEmpty());
		if (!this.pendingViews.isEmpty()) {
			log.warn(this.pendingViews.size() + " views out of " + 
					initialSize + " could not be committed: "
					+ this.pendingViews.keySet()
					);
		}
	}

	/**
	 * Commits all pending constraints, and removes successfully committed ones
	 * from the collection of pending relations.
	 */
	private void commitPendingConstraints() {
		boolean hasChanged = false;
		int initialSize = this.pendingConstraints.size();
		do {
			hasChanged = false;
			for (Iterator<Entry<String, Rule>> i =
					this.pendingConstraints.entries().iterator(); i.hasNext();) {
				Entry<String, Rule> entry = i.next();
				try {
					Dependency c = (Dependency) this.proto.ruleToConstraint(entry.getValue());
					if (c != null) {
						this.builder.addDependency(c);
						this.index.put(entry.getKey(), c);
					} else {
						log.warn("Invalid constraint " + entry.getKey());
					}
					i.remove();
					hasChanged = true;
				} catch (Exception e) {
					log.debug(entry.getKey() + " constraint not committed.");
//					e.printStackTrace();
				}
			}
		} while (hasChanged && !this.pendingViews.isEmpty());
		if (!this.pendingConstraints.isEmpty()) {
			log.warn(this.pendingConstraints.size() + " constraints out of " + 
					initialSize + " could not be committed: "
					+ this.pendingConstraints.keySet()
					);
		}
	}
	
	/**
	 * Removes the rank of the object by the given from the topological order.
	 *
	 * @param name the name
	 */
	public void removeRank(String name) {
		this.topoOrder.remove(name);
	}

	/**
	 * Updates the rank of the object by the given from the topological order.
	 *
	 * @param name the name
	 * @param rank the rank
	 */
	public void updateRank(String name, Long rank) {
		this.topoOrder.forcePut(name, rank);
	}
	
	/**
	 * Handle on a Logicblox workspace.
	 */
	public static class Workspace {

		/** The name. */
		private final String name;

		/**
		 * Constructor for Workspace.
		 * @param name String
		 */
		public Workspace(String name) {
			super();
			this.name = name;
		}

		/**
		 * Gets the name.
		 *
		 * @return String
		 */
		public String getName() {
			return this.name;
		}
	}
}
