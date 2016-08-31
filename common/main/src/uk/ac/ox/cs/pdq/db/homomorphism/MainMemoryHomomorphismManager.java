package uk.ac.ox.cs.pdq.db.homomorphism;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;

import com.google.common.base.Preconditions;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import uk.ac.ox.cs.pdq.db.Attribute;
import uk.ac.ox.cs.pdq.db.AttributeEqualities;
import uk.ac.ox.cs.pdq.db.DatabaseEGD;
import uk.ac.ox.cs.pdq.db.DatabaseEquality;
import uk.ac.ox.cs.pdq.db.DatabaseRelation;
import uk.ac.ox.cs.pdq.db.Dependency;
import uk.ac.ox.cs.pdq.db.EGD;
import uk.ac.ox.cs.pdq.db.JoinAlgorithm;
import uk.ac.ox.cs.pdq.db.MainMemoryDatabase;
import uk.ac.ox.cs.pdq.db.MainMemoryRelation;
import uk.ac.ox.cs.pdq.db.Match;
//import uk.ac.ox.cs.pdq.db.MainMemoryDatabase;
//import uk.ac.ox.cs.pdq.db.MainMemoryRelation;
import uk.ac.ox.cs.pdq.db.Relation;
import uk.ac.ox.cs.pdq.db.Schema;
import uk.ac.ox.cs.pdq.db.TGD;
import uk.ac.ox.cs.pdq.db.TypedConstant;
import uk.ac.ox.cs.pdq.db.homomorphism.DatabaseHomomorphismManager.LimitTofacts;
import uk.ac.ox.cs.pdq.db.homomorphism.HomomorphismProperty.ActiveTriggerProperty;
import uk.ac.ox.cs.pdq.db.homomorphism.HomomorphismProperty.EGDHomomorphismProperty;
import uk.ac.ox.cs.pdq.db.homomorphism.HomomorphismProperty.FactProperty;
import uk.ac.ox.cs.pdq.db.homomorphism.HomomorphismProperty.MapProperty;
import uk.ac.ox.cs.pdq.fol.Atom;
import uk.ac.ox.cs.pdq.fol.Conjunction;
import uk.ac.ox.cs.pdq.fol.ConjunctiveQuery;
import uk.ac.ox.cs.pdq.fol.Constant;
import uk.ac.ox.cs.pdq.fol.Equality;
import uk.ac.ox.cs.pdq.fol.Evaluatable;
import uk.ac.ox.cs.pdq.fol.Predicate;
import uk.ac.ox.cs.pdq.fol.Query;
import uk.ac.ox.cs.pdq.fol.Skolem;
import uk.ac.ox.cs.pdq.fol.Term;
import uk.ac.ox.cs.pdq.fol.Variable;
import uk.ac.ox.cs.pdq.io.xml.QNames;
import uk.ac.ox.cs.pdq.util.Utility;
import uk.ac.ox.cs.pdq.InconsistentParametersException;

/**
 *  @author C17Rachel.Eaton
 *  
 *  Class implementing a homomorphism manager in memory without referring to MySQL or Derby.
 *
 */
public class MainMemoryHomomorphismManager implements HomomorphismManager {
	protected Schema schema;
	protected MainMemoryDatabase database = new MainMemoryDatabase();
	private int aliasCounter = 0;
	private int counter = 0;
	/** Map schema relation to database tables. */
	protected  Map<String, DatabaseRelation> toDatabaseTables = new LinkedHashMap<>();
	protected List<Relation> relations;
	
	public MainMemoryHomomorphismManager( Schema schema ){
		this.schema = schema;
		this.relations = Lists.newArrayList(schema.getRelations());
		for(Relation relation : this.schema.getRelations() ){
			// Initializes the tables in the database. 
			Attribute fact = new Attribute( Integer.class, "Fact");
			List<Attribute> attribs = new ArrayList<Attribute>(relation.getAttributes());
			attribs.add(fact);
			Relation newrelate = new DatabaseRelation( relation.getName(), attribs );
			this.database.addNewTable( newrelate );
		}
		this.toDatabaseTables.put(QNames.EQUALITY.toString(), DatabaseRelation.DatabaseEqualityRelation);
		for (Relation relation:this.relations) {
			DatabaseRelation dbRelation = DatabaseRelation.createDatabaseRelation(relation);
			this.toDatabaseTables.put(relation.getName(), dbRelation);
		}
		
		
		
	}
	
	
	@Override
	public <Q extends Evaluatable> List<Match> getMatches(ConjunctiveQuery query, Collection<Atom> facts, LimitTofacts l) {
		
		HomomorphismProperty[] properties = new HomomorphismProperty[1];
		//properties[0] = HomomorphismProperty.createMapProperty(query.getGroundingsProjectionOnFreeVars());
		if(l.equals(LimitTofacts.THIS))
				properties[0] = HomomorphismProperty.createFactProperty(Conjunction.of(facts));
		else
			properties = new HomomorphismProperty[0];
		return this.internalGetMatches(Lists.<Query<?>>newArrayList(query),properties);
	}

	@Override
	public <Q extends Evaluatable> List<Match> getTriggers(Collection<Q> dependencies, TriggerProperty t, Collection<Atom> facts) {
		
		HomomorphismProperty[] properties = new HomomorphismProperty[1];
		if(t.equals(TriggerProperty.ACTIVE))
		{
			properties[0] = HomomorphismProperty.createActiveTriggerProperty();
		}
		return this.internalGetMatches(dependencies, properties);
		
	}
	
	
	/* Done? */
	public <Q extends Evaluatable> List<Match> internalGetMatches(Collection<Q> sources, HomomorphismProperty... constraints) {
		Preconditions.checkNotNull(sources);
		List<Match> finalMatch = new ArrayList<Match>();
		//Create a new query out of each input query that references only the cleaned predicates
		for(Q source:sources) {
			Q s = this.convert(source, constraints);
			HomomorphismProperty[] c = null;
			if(source instanceof EGD) {
				c = new HomomorphismProperty[constraints.length+1];
				System.arraycopy(constraints, 0, c, 0, constraints.length);
				c[constraints.length] = HomomorphismProperty.createEGDHomomorphismProperty();
			}
			else {
				c = constraints;
			}		
			
			/** Summary:
			 * projections: Ordered dictionary with attribute names (L) corresponding to variables (R).
			 * aliases: Dictionary representing which aliases (L) correspond to which real tables (R).
			 * egdProperties: Dictionary containing attribute names. Key attribute > Value attribute for the tuple to be selected.
			 * joinRelationsandAttributes: map between a string array representing a table1 and index1 that are equal to a table2 and index2 on the right.
			 * 		both [table1, index1]:[table2,index2] and [table2,index2]:[table1,index1] are represented.
			 **/
			Map<String, String> 			aliases 					= this.createNameNameAliasMapping( (Conjunction<Atom>)s.getBody() );
			Map<Atom, String>				stateAliases				= this.createAtomNameAliasMapping((Conjunction<Atom>)s.getBody());
			LinkedHashMap<String,Variable> 	projections 				= this.createProjections(s, stateAliases); 
			Map<String, String> 			inequalityMapping 			= this.createStatementsofInequality(s, stateAliases, c);
			//If there is NOT a meaningful join the joinRelationsandAttributes is empty
			AttributeEqualities				joinRelationsandAttributes	= this.createAttributeEqualities((Conjunction<Atom>) s.getBody(), stateAliases);
			System.out.println("Get the matches of  "+s);
			System.out.println("Compute AttributeEqualities: "+ joinRelationsandAttributes);
			MainMemoryRelation searchTable;
			JoinAlgorithm joinAlgorithm = new JoinAlgorithm();
			// Searches the database for the results of the original query without eliminating anything from nested queries
			// If no join conditions are present, the search process is cut short and only the relevant table is selected
			// TODO: Add helpful try/catch statements in case aliases is empty
			Map<String, MainMemoryRelation> aliasedTables = joinAlgorithm.renameWithAliases(aliases, this.database);
			if (!joinRelationsandAttributes.isEmpty()){
				searchTable = joinAlgorithm.join(aliasedTables, joinRelationsandAttributes);
			} else {
				searchTable = joinAlgorithm.renameRelationwithAliases(aliasedTables);				
			} 
			// Applies join conditions that are inequalities, i.e. A0.x0 < A1.x2, as applicable.
			if( !inequalityMapping.isEmpty()){
				searchTable = joinAlgorithm.applyInequalities( searchTable, inequalityMapping	);
			}
			
			// Determines whether we are dealing with an activeTrigger. If so, a second query will be run to be subtracted from the first.
			boolean activeTrigger = false;
			for(HomomorphismProperty constraint:c) {
				if(constraint instanceof ActiveTriggerProperty) {
					activeTrigger = true;
					break;
				}			
			}
			
			// If running a second query, gets input parameters and searches the database for tuples not to include in the final result
			// There was no mechanism in the original getMatches to create inequalities in the join statements of the nested query, so none exists here
			ArrayList<MainMemoryRelation> nonResults = null;
			if(s instanceof Dependency && activeTrigger) {
				nonResults = new ArrayList<MainMemoryRelation>();
				Map<String, String> from2 = this.createNameNameAliasMapping(Conjunction.of(((Dependency)s).getRight().getAtoms()), aliases);
				stateAliases = this.createAtomNameAliasMapping(Conjunction.of(((Dependency)s).getRight().getAtoms()), stateAliases );
				Map<String, MainMemoryRelation> nestedAliasedTables = joinAlgorithm.renameWithAliases(from2, this.database);
				List<AttributeEqualities> nestedAttributeEqualities = this.createNestedAttributeEqualitiesForActiveTriggers((Dependency)s, stateAliases);
				for( AttributeEqualities attrEq : nestedAttributeEqualities ){
					nonResults.add( joinAlgorithm.join(nestedAliasedTables, attrEq));
					
				}
			}
			
			// Compiles tuples to exclude from the final result into a single table
			MainMemoryRelation notThese = nonResults != null ? new MainMemoryRelation("hashTable", nonResults.get(0).getAttributes()) : null;
			if( nonResults != null) {
				for( MainMemoryRelation notThis : nonResults ){
					
					searchTable.dropTuples( Lists.newArrayList(notThis.getTuples()) );
					for( Atom tuple: notThis.getTuples()){
						notThese.addTuple(tuple);
					}
				}
			}
			
			// Selects correct attributes and maps them to the specified variable in the original search table. Set prevents duplication.
			Set<Map<Variable, Constant>> result = joinAlgorithm.finalSelect(projections, searchTable);
			// If a second query has been completed, removes its results from the search results.
			if( notThese != null){
				Set<Map<Variable, Constant>> notresult = joinAlgorithm.finalSelect(projections, notThese);
				for( Map<Variable, Constant> match : Lists.newArrayList(result)){
					if( notresult.contains(match)){
						result.remove(match);
					}
				}
			}
	
			// Assembles matches to be returned
			for( Map<Variable, Constant> mapping : result ){
				finalMatch.add(new Match(source, mapping));
			}
		}
		
		// Resets alias counter so the homomorphismManager can be called a second time
		aliasCounter = 0;
		return finalMatch;
	}
	
	// Below are a bunch of support functions that I've stolen from SQLStatementBuilder and repurposed to give me names of equalities, etc.
	protected List<AttributeEqualities> createNestedAttributeEqualitiesForActiveTriggers(Dependency source, Map<Atom, String> stateAliases) {
		if(source instanceof TGD) {
			return Lists.newArrayList(this.createAttributeEqualities(Conjunction.of(((Dependency)source).getAtoms()), stateAliases));
		}
		else if(source instanceof DatabaseEGD){
			ArrayList<AttributeEqualities> attributePredicates = new ArrayList<AttributeEqualities>();
			//The right atom should be an equality
			//We add additional checks to be sure that we have to do with EGDs
			for(DatabaseEquality rightAtom:((DatabaseEGD)source).getHead()) {
				Relation rightRelation = (Relation) rightAtom.getPredicate();
				String rightAlias = stateAliases.get(rightAtom);
				Map<Integer,Pair<String,Attribute>> rightToLeft = new HashMap<Integer,Pair<String,Attribute>>();
				for(Term term:rightAtom.getTerms()) {
					List<Integer> rightPositions = rightAtom.getTermPositions(term); //all the positions for the same term should be equated
					Preconditions.checkArgument(rightPositions.size() == 1);
					for(Atom leftAtom:source.getBody().getAtoms()) {
						Relation leftRelation = (Relation) leftAtom.getPredicate();
						String leftAlias = stateAliases.get(leftAtom);
						List<Integer> leftPositions = leftAtom.getTermPositions(term); 
						Preconditions.checkArgument(leftPositions.size() <= 1);
						if(leftPositions.size() == 1) {
							rightToLeft.put(rightPositions.get(0), Pair.of(leftAlias==null ? leftRelation.getName():leftAlias, leftRelation.getAttribute(leftPositions.get(0))));
						}
					}
				}
				Preconditions.checkArgument(rightToLeft.size()==2);
				Iterator<Entry<Integer, Pair<String, Attribute>>> entries;
				Entry<Integer, Pair<String, Attribute>> entry;
				
				entries = rightToLeft.entrySet().iterator();
				entry = entries.next();
				
				// first has two distinct entries. 
				// If either the first r1.i1 != r2.i2 OR the second r1.i1 != r2.i2 (where r is a relation name and i is an attribute name),
				// then the result is not included in the second query.
				// This means that if it meets the qualifications of the first query, it will be included in the result.
				AttributeEqualities first = new AttributeEqualities();
				String r1 = entry.getValue().getLeft();//).append(".").append(
				String i1 = entry.getValue().getRight().getName();//).append('=');
				String r2 = rightAlias==null ? rightRelation.getName():rightAlias;
				String i2 = rightRelation.getAttribute(0).getName();
				
				Pair left = Pair.of(r1, i1);
				Pair right = Pair.of(r2, i2);
				first.put(left, right);
				first.put(right, left);
				
				entry = entries.next();
				
				r1 = entry.getValue().getLeft();
				i1 = entry.getValue().getRight().getName();
				r2 = rightAlias==null ? rightRelation.getName():rightAlias;
				i2 = rightRelation.getAttribute(1).getName();
				
				left = Pair.of(r1, i1);
			    right = Pair.of(r2, i2);
				first.put(left,  right);
				first.put(right,  left);
				
				entries = rightToLeft.entrySet().iterator();
				entry = entries.next();
	
				// second functions in the same way as first
				AttributeEqualities second = new AttributeEqualities();
				
				r1 = entry.getValue().getLeft();
				i1 = entry.getValue().getRight().getName();
				r2 = rightAlias==null ? rightRelation.getName():rightAlias;
				i2 = rightRelation.getAttribute(1).getName();
				left = Pair.of(r1, i1);
				right = Pair.of(r2, i2);
				second.put(left, right);
				second.put(right, left);
				
				entry = entries.next();
				
				r1 = entry.getValue().getLeft();
				i1 = entry.getValue().getRight().getName();
				r2 = rightAlias==null ? rightRelation.getName():rightAlias;
				i2 = rightRelation.getAttribute(0).getName();
				left = Pair.of(r1, i1);
				right = Pair.of(r2, i2);
				second.put(left, right);
				second.put(right, left);

				attributePredicates.add(first);
				attributePredicates.add(second);
				
			}
			return attributePredicates;
		}
		else {
			throw new java.lang.IllegalArgumentException("Unsupported constraint type");
		}
	}
	protected Map<String, String> createStatementsofInequality(Evaluatable source, Map<Atom, String> stateAliases, HomomorphismProperty... constraints) {
		// Gets a part of the query and a set of constraints
		// Returns a mapping between two attributes such that the key should be less than the index.
		Map<String, String> inequality = new HashMap<String, String>();
		for(HomomorphismProperty c:constraints) {	
			if(c instanceof EGDHomomorphismProperty) {
				List<Atom> conjuncts = source.getBody().getAtoms();
				String lalias = stateAliases.get(conjuncts.get(0));
				String ralias = stateAliases.get(conjuncts.get(1));
				lalias = lalias==null ? conjuncts.get(0).getPredicate().getName():lalias;
				ralias = ralias==null ? conjuncts.get(1).getPredicate().getName():ralias;
				String eq1 = lalias + ".Fact";
				String eq2 = ralias+".Fact";
				inequality.put(eq1, eq2);
			}	
		}
		return inequality;
	}
	protected LinkedHashMap<String,Variable> createProjections(Evaluatable source, Map<Atom, String> stateAliases) {
		// Decides which attributes to map to which variables based on the cleaned source
		LinkedHashMap<String,Variable> projected = new LinkedHashMap<>();
		List<Variable> attributes = new ArrayList<>();
		for (Atom fact:source.getBody().getAtoms()) {
			String alias = stateAliases.get(fact);
			List<Term> terms = fact.getTerms();
			for (int it = 0; it < terms.size(); ++it) {
				Term term = terms.get(it);
				if (term instanceof Variable && !attributes.contains(((Variable) term).getName())) {
					projected.put(getFormattedAttributeName(it, (Relation) fact.getPredicate(), alias), (Variable)term);
					attributes.add(((Variable) term));
				}
			}
		}
		return projected;
	}
	protected String getFormattedAttributeName(int position, Relation relation, String alias) {
		Preconditions.checkNotNull(relation);
		Preconditions.checkArgument(position >= 0 && position < relation.getArity());
		StringBuilder result = new StringBuilder();
		result.append(alias==null ? relation.getName():alias).
		append(".").append(relation.getAttribute(position).getName());
		
		return result.toString();
	}
	protected AttributeEqualities createAttributeEqualities(Conjunction<Atom> source, Map<Atom, String> stateAliases) {
		Collection<Term> terms = Utility.getTerms(source.getAtoms());
		terms = Utility.removeDuplicates(terms);
		AttributeEqualities equalities = new AttributeEqualities();
		for( Term term: terms){
			Integer leftPosition = null;
			Relation leftRelation = null;
			String leftAlias = null;
			for (Atom fact:source.getAtoms()) {
				List<Integer> positions = fact.getTermPositions(term); //all the positions for the same term should be equated
				for (Integer pos:positions) {
					if(leftPosition == null) {
						leftPosition = pos;
						leftRelation = (Relation) fact.getPredicate();
						leftAlias = stateAliases.get(fact);

					} else {	
						Integer rightPosition = pos;
						Relation rightRelation = (Relation) fact.getPredicate();
						String rightAlias = stateAliases.get(fact);
						String r1 = leftAlias==null? leftRelation.getName():leftAlias;
						String r2 = rightAlias==null? rightRelation.getName():rightAlias;
						String i1 = leftRelation.getAttribute(leftPosition).getName();
						String i2 = rightRelation.getAttribute(rightPosition).getName();
						Pair ref1 = Pair.of(r1, i1);
						Pair ref2 = Pair.of(r2, i2);
						Pair equality1 = Pair.of(ref1, ref2);
						Pair equality2 = Pair.of(ref2, ref1);
						equalities.addEquality(equality1);
						equalities.addEquality(equality2);
					}
				}
			}
		}
		
		return equalities; 
	}

	
	protected Map<String, String> createNameNameAliasMapping(Conjunction<? extends Atom> predicates, Map<String, String>... stateAliases ) {
		Map<String, String> rename = new HashMap<String, String>();
		for( Map<String, String> alias :stateAliases){
			for( String key : alias.keySet()){
				rename.put(key, alias.get(key));
			}
		}
		// Assigns aliases to tables
		String aliasPrefix = "A";
		for (Atom fact:predicates) {
			String aliasName = aliasPrefix + this.aliasCounter;
			//this.aliases.put(fact, aliasName);
			rename.put(aliasName, fact.getName());
			this.counter++;
			this.aliasCounter++;
		}
		return rename;
	}	
	
	protected BiMap<Atom, String> createAtomNameAliasMapping(Conjunction<? extends Atom> predicates, Map<Atom, String>... stateAliases ){
		String aliasPrefix = "A";
		this.aliasCounter -= this.counter;
		this.counter = 0;
		BiMap<Atom, String> aliases = HashBiMap.create();
		for( Map<Atom, String> alias : stateAliases ){
			for( Atom key : alias.keySet()){
				aliases.put(key, alias.get(key));
			}
		}
	
		
		for( Atom fact: predicates){
			String aliasName = aliasPrefix + this.aliasCounter;
			aliases.put(fact, aliasName);
			this.aliasCounter++;
		}
		return aliases;
	}
	/**
	 * Convert.
	 *
	 * @param <Q> the generic type
	 * @param source 		An input formula
	 * @param toDatabaseTables 		Map of schema relation names to *clean* names
	 * @param constraints 		A set of constraints that should be satisfied by the homomorphisms of the input formula to the facts of the database 
	 * @return 		a formula that uses the input *clean* names
	 */
	private <Q extends Evaluatable> Q convert(Q source, HomomorphismProperty... constraints) {
		// Converts the source into a useable form according to the constraints
		if(source instanceof TGD) {
			int f = 0;
			List<Atom> left = Lists.newArrayList();
			for(Atom atom:((TGD) source).getLeft()) {
				Relation relation = this.toDatabaseTables.get(atom.getName());
				List<Term> terms = Lists.newArrayList(atom.getTerms());
				terms.add(new Variable(DatabaseRelation.Fact.getName() + f++));
				left.add(new Atom(relation, terms));
			}
			List<Atom> right = Lists.newArrayList();
			for(Atom atom:((TGD) source).getRight()) {
				Relation relation = this.toDatabaseTables.get(atom.getName());
				List<Term> terms = Lists.newArrayList(atom.getTerms());
				terms.add(new Variable(DatabaseRelation.Fact.getName() + f++));
				right.add(new Atom(relation, terms));
			}
			return (Q) new TGD(Conjunction.of(left), Conjunction.of(right));
		}
		else if (source instanceof EGD) {
			int f = 0;
			List<Atom> left = Lists.newArrayList();
			for(Atom atom:((EGD) source).getLeft()) {
				Relation relation = this.toDatabaseTables.get(atom.getName());
				List<Term> terms = Lists.newArrayList(atom.getTerms());
				terms.add(new Variable(DatabaseRelation.Fact.getName() + f++));
				left.add(new Atom(relation, terms));
			}
			List<DatabaseEquality> right = Lists.newArrayList();
			for(Equality atom:((EGD) source).getRight()) {
				List<Term> terms = Lists.newArrayList(atom.getTerms());
				terms.add(new Variable(DatabaseRelation.Fact.getName() + f++));
				right.add(new DatabaseEquality(terms));
			}
			return (Q) new DatabaseEGD(Conjunction.of(left), Conjunction.of(right));
		}
		else if(source instanceof Query) {
			int f = 0;
			List<Atom> body = Lists.newArrayList();
			for(Atom atom:((Query<?>) source).getBody().getAtoms()) {
				Relation relation = this.toDatabaseTables.get(atom.getName());
				List<Term> terms = Lists.newArrayList(atom.getTerms());
				terms.add(new Variable(DatabaseRelation.Fact.getName() + f++));
				body.add(new Atom(relation, terms));
			}
			return (Q) new ConjunctiveQuery(((Query) source).getHead(), Conjunction.of(body));
		}
		else {
			throw new java.lang.UnsupportedOperationException();
		}
	}
	
	/* Done! */
	@Override
	public MainMemoryHomomorphismManager clone() {
		// Returns a copy of the homomorphism manager
		MainMemoryHomomorphismManager copy = new MainMemoryHomomorphismManager(this.schema);
		copy.database = this.database;
		//copy.aliases = this.aliases;
		copy.toDatabaseTables = this.toDatabaseTables;
		copy.relations = this.relations;
		//copy.rename = this.rename;
		return copy;
	}
	/* Done? */
	@Override
	public void close() throws Exception {
		// TODO Auto-generated method stub
		
	}
	/* Done! */
	@Override
	public void addFacts(Collection<? extends Atom> facts) {
		// adds facts to the database
		for( Atom fact: facts) {
			Predicate pred = new Predicate(fact.getName(), fact.getPredicate().getArity()+1);
			List<Term> terms = new ArrayList<Term>();
			terms.addAll(fact.getTerms());
			Skolem id = new Skolem(String.valueOf(fact.getId()));
			terms.add( id );
			Atom newfact = new Atom(pred, terms);
			this.database.addTuples( Lists.newArrayList(newfact) );
		}
	}
	/* Done! */
	@Override
	public void deleteFacts(Collection<? extends Atom> facts) {
		for( Atom fact: facts ){
			MainMemoryRelation updatedTable = this.database.getTable(fact.getName());
			updatedTable.dropTuple(fact);
			this.database.editTable( updatedTable );
		}
		
	}
	/* Done! */
	@Override
	public void initialize() {
		// TODO Auto-generated method stub	
	}

	@Override
	public void addQuery(Query<?> query) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void clearQuery() {
		throw new UnsupportedOperationException();
		
	}
	
	public String toString(){
		return this.database.toString();
	}

}