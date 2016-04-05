package uk.ac.ox.cs.pdq.reasoning.homomorphism;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Collection;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedQueue;

import uk.ac.ox.cs.pdq.db.Relation;
import uk.ac.ox.cs.pdq.fol.Atom;
import uk.ac.ox.cs.pdq.fol.Predicate;
import uk.ac.ox.cs.pdq.reasoning.chase.state.EOFAtom;

import com.beust.jcommander.internal.Maps;
import com.google.common.collect.Sets;

// TODO: Auto-generated Javadoc
/**
 * Inserts facts to database asynchronously, i.e., when its inmemory cache is full.
 *  
 * @author Efthymia Tsamoura
 */
public class ExecuteAsynchronousSQLInsertThread implements Callable<Boolean> {

	/**  Connection to the database. */
	protected final Connection connection;
	
	/** Creates SQL INSERT statements**/
	protected final SQLStatementBuilder builder;
	
	/** Maps predicates to tables that store facts of this predicate **/
	protected final Map<String, DatabaseRelation> toDatabaseTables;
	
	/** Input list of atoms **/
	protected final Queue<Atom> facts = new ConcurrentLinkedQueue<>();

	/** Caches of tuples. Caches are grouped based on the fact predicates. The facts are flushed to disk when the cache is full**/
	protected final Map<Predicate,Set<Atom>> caches = Maps.newHashMap();
	
	/** Inmemory cache size**/
	protected final static int cacheSize = 100; 
	
	public ExecuteAsynchronousSQLInsertThread(SQLStatementBuilder builder, Map<String, DatabaseRelation> toDatabaseTables,
			Connection connection) {
		//TODO check input arguments
		this.connection = connection;
		this.builder = builder;
		this.toDatabaseTables = toDatabaseTables;
	}
	
	/**
	 * Appends facts to the queue for asynchronous inserts
	 * @param facts
	 */
	public void addFact(Collection<? extends Atom> facts) {
		this.facts.addAll(facts);
	}
	
	/**
	 * Call.
	 *
	 * @return Boolean
	 * @see java.util.concurrent.Callable#call()
	 */
	@Override
	public Boolean call() {		
		Atom fact; 
//		do {
			while (!((fact = this.facts.poll()) instanceof EOFAtom)) {
				//System.out.println(Thread.currentThread() + " pull tuple " + fact);
				if(!(fact instanceof EOFAtom)) {
					Set<Atom> atoms = this.caches.get(fact.getPredicate());
					if(atoms == null) {
						this.caches.put(fact.getPredicate(), Sets.newHashSet(fact));
					}
					else {
						atoms.add(fact);
					}
					if(this.caches.get(fact.getPredicate()).size() == cacheSize) {
						//System.out.println(Thread.currentThread() + " write cache " + fact.getPredicate() +  " to disk ");
						
						String query = this.builder.createBulkInsertStatement((Relation) fact.getPredicate(), this.caches.get(fact.getPredicate()),
								this.toDatabaseTables);
						try {	
							Statement sqlStatement = this.connection.createStatement();
							sqlStatement.executeUpdate(query);
							this.caches.get(fact.getPredicate()).clear();
						} catch (SQLException ex) {
							if(!ex.getCause().getMessage().contains("duplicate key value")) {
								throw new IllegalStateException(ex.getMessage(), ex);
							}
						}
					}
				}
				if(fact instanceof EOFAtom) {
					System.out.println();
				}
			}

//		}while(!(fact instanceof EOFAtom));
		
		
		System.out.println(Thread.currentThread() + " reading EOF tuple. Flushing data to disk");
		//When the thread reads the final tuple, then it should flush everything to disk
		for(Entry<Predicate, Set<Atom>> entry:this.caches.entrySet()) {		
			//TODO check if cache is empty
			String query = this.builder.createBulkInsertStatement((Relation) entry.getKey(), this.caches.get(entry.getKey()),
					this.toDatabaseTables);
			try {	
				Statement sqlStatement = this.connection.createStatement();
				sqlStatement.executeUpdate(query);
				this.caches.get(entry.getKey()).clear();
			} catch (SQLException ex) {
				if(!ex.getCause().getMessage().contains("duplicate key value")) {
					throw new IllegalStateException(ex.getMessage(), ex);
				}
			}
		}
		
		return true;
	}

}
