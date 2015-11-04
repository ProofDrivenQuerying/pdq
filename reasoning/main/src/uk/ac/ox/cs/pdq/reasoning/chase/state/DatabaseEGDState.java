package uk.ac.ox.cs.pdq.reasoning.chase.state;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import uk.ac.ox.cs.pdq.db.Constraint;
import uk.ac.ox.cs.pdq.db.EGD;
import uk.ac.ox.cs.pdq.db.TypedConstant;
import uk.ac.ox.cs.pdq.fol.Constant;
import uk.ac.ox.cs.pdq.fol.Equality;
import uk.ac.ox.cs.pdq.fol.Formula;
import uk.ac.ox.cs.pdq.fol.Predicate;
import uk.ac.ox.cs.pdq.fol.Query;
import uk.ac.ox.cs.pdq.fol.Term;
import uk.ac.ox.cs.pdq.fol.Variable;
import uk.ac.ox.cs.pdq.reasoning.Match;
import uk.ac.ox.cs.pdq.reasoning.chase.ChaseException;
import uk.ac.ox.cs.pdq.reasoning.chase.FiringGraph;
import uk.ac.ox.cs.pdq.reasoning.chase.MapFiringGraph;
import uk.ac.ox.cs.pdq.reasoning.homomorphism.DBHomomorphismManager;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;

/**
 * 
 * @author Efthymia Tsamoura
 *
 */
public class DatabaseEGDState extends DatabaseListState {


	private boolean _isFailed = false;

	/** Keeps the classes of equal constants **/
	protected EqualConstantsClasses constantClasses;

	/**
	 * 
	 * @param query
	 * @param manager
	 */
	public DatabaseEGDState(Query<?> query, DBHomomorphismManager manager) {
		this(manager, query.getCanonical().getPredicates(), new MapFiringGraph());
		//this.manager.addFacts(this.facts);
	}

	public DatabaseEGDState(
			DBHomomorphismManager manager,
			Collection<Predicate> facts) {
		this(manager, facts, new MapFiringGraph());
		//this.manager.addFacts(this.facts);
	}

	/**
	 * 
	 * @param query
	 * @param manager
	 * @param facts
	 * @param graph
	 */
	public DatabaseEGDState(
			DBHomomorphismManager manager,
			Collection<Predicate> facts,
			FiringGraph graph) {
		super(manager, facts, graph);
		this.constantClasses = new EqualConstantsClasses();
		for(Predicate fact:facts) {
			if(fact instanceof Equality) {
				this.constantClasses.add((Equality) fact);
			}
		}
	}

	/**
	 * Updates that state given the input match. 
	 * @param match
	 * @return
	 */
	@Override
	public boolean chaseStep(Match match) {	
		return this.chaseStep(Sets.newHashSet(match));
	}

	@Override
	public boolean chaseStep(Collection<Match> matches) {
		Preconditions.checkNotNull(matches);
		Collection<Predicate> created = new LinkedHashSet<>();
		for(Match match:matches) {
			Constraint dependency = (Constraint) match.getQuery();
			Map<Variable, Constant> mapping = match.getMapping();
			Constraint grounded = dependency.fire(mapping, true);
			Formula left = grounded.getLeft();
			Formula right = grounded.getRight();
			if(dependency instanceof EGD) {
				for(Predicate equality:right.getPredicates()) {
					if(!this.constantClasses.add((Equality)equality)) {
						this._isFailed = true;
						break;
					}
				}
			}	
			this.graph.put(dependency, Sets.newHashSet(left.getPredicates()), Sets.newHashSet(right.getPredicates()));
			created.addAll(right.getPredicates());
		}
		this.addFacts(created);

		if(this.constantClasses.size()==1 || this.constantClasses.size()==0) {
			this._isFailed = false;
		}
		else {
			this._isFailed = true;
		}
		return !this._isFailed;
	}

	public EqualConstantsClasses getConstantClasses() {
		return this.constantClasses;
	}

	/**
	 * Class of equal constants
	 * @author Efthymia Tsamoura
	 *
	 */
	public static class EqualConstantsClass {

		/** Collection of equal constants **/
		private final Collection<Term> constants = new HashSet<>();

		/** The schema constant that belongs to this class **/
		private TypedConstant<?> schemaConstant; 

		public EqualConstantsClass(Equality equality) throws ChaseException{
			List<Term> terms = equality.getTerms();
			Preconditions.checkArgument(terms.get(0) instanceof Constant && terms.get(1) instanceof Constant);
			//If both inputs are schema constants
			if(terms.get(0) instanceof TypedConstant && terms.get(1) instanceof TypedConstant) {
				if(!terms.get(0).equals(terms.get(1))) {
					throw new ChaseException("Trying to add different schema constants in the same class");
				}
				else {
					//Create a class that keeps the input schema constant 
					this.schemaConstant = (TypedConstant<?>) terms.get(0);
					this.constants.add(terms.get(0));
				}
			}
			//If only one of the inputs is schema constant
			else if(terms.get(0) instanceof TypedConstant) {
				this.schemaConstant = (TypedConstant<?>) terms.get(0);
				this.constants.addAll(terms);
			}
			else if(terms.get(1) instanceof TypedConstant) {
				this.schemaConstant = (TypedConstant<?>) terms.get(1);
				this.constants.addAll(terms);
			}
			else {
				//If both inputs are chase constants
				this.constants.addAll(equality.getTerms());
			}
		}

		/**
		 * 
		 * @param input
		 * @param inputClass
		 * 		The class of the input constant
		 * @return
		 * 		true if the input constant has been successfully added.
		 * 		An input constant is successfully added only if we do not come up with cases
		 * 		where two different schema constants should be equal
		 */
		public boolean add(Term input, EqualConstantsClass inputClass) {
			Preconditions.checkArgument(input instanceof Constant);
			//If the input constant already belongs to a class
			if(inputClass != null) {
				TypedConstant<?> inputSchemaConstant = inputClass.getSchemaConstant();
				if(this.schemaConstant != null && inputSchemaConstant != null) {
					if(!this.schemaConstant.equals(inputSchemaConstant)) {
						return false;
					}
					else {
						this.constants.addAll(inputClass.getConstants());
					}
				}
				else if(this.schemaConstant == null && inputSchemaConstant == null) {
					this.constants.addAll(inputClass.getConstants());
					this.constants.add(input);
				}
				else if(this.schemaConstant == null) {
					this.constants.addAll(inputClass.getConstants());
					this.constants.add(input);
					this.schemaConstant = inputClass.getSchemaConstant();
				}
				else if(inputSchemaConstant == null) {
					this.constants.addAll(inputClass.getConstants());
				}
			}
			//Otherwise
			else {
				if(this.schemaConstant != null && input instanceof TypedConstant) {
					if(!this.schemaConstant.equals(input)) {
						return false;
					}
				}
				else if(this.schemaConstant == null && !(input instanceof TypedConstant)) {
					this.constants.add(input);
				}
				else if(this.schemaConstant == null) {
					this.constants.add(input);
					this.schemaConstant = (TypedConstant<?>) input;
				}
				else if(!(input instanceof TypedConstant)) {
					this.constants.add(input);

				}
			}
			return true;
		}

		public boolean contains(Term constant) {
			return (this.schemaConstant!= null && this.schemaConstant.equals(constant)) 
					|| this.constants.contains(constant);
		}

		public TypedConstant<?> getSchemaConstant() {
			return schemaConstant;
		}

		public Collection<Term> getConstants() {
			return this.constants;
		}
		
		/**
		 * @param o Object
		 * @return boolean
		 */
		@Override
		public boolean equals(Object o) {
			if (this == o) {
				return true;
			}
			if (o == null) {
				return false;
			}
			return this.getClass().isInstance(o)
					&& this.constants.equals(((EqualConstantsClass) o).constants)
					&& this.schemaConstant.equals(((EqualConstantsClass) o).schemaConstant);
		}

		/**
		 * @return int
		 */
		@Override
		public int hashCode() {
			return Objects.hash(this.constants, this.schemaConstant);
		}

		/**
		 * @return String
		 */
		@Override
		public String toString() {
			return "[" + Joiner.on(",").join(this.constants) + "," + this.schemaConstant + "]";
		}
	}


	/**
	 * Keeps the different classes of equal constants
	 * @author Efthymia Tsamoura
	 *
	 */
	public static class EqualConstantsClasses {

		/** The classes of equal constants**/
		private final Set<EqualConstantsClass> classes = new HashSet<>();

		/**
		 * 
		 * @param equality
		 * @return
		 * 		true if the input equality does not cause chase failure
		 */
		public boolean add(Equality equality) { 
			List<Term> terms = equality.getTerms();
			EqualConstantsClass c0 = this.getClass(terms.get(0));
			EqualConstantsClass c1 = this.getClass(terms.get(1));

			if(c0 != null && c1 != null) {
				if(c0.add(terms.get(1), c1)) {
					this.classes.remove(c1);
				}
				else {
					return false;
				}
			}
			if(c0 != null) {
				return c0.add(terms.get(1), c1);
			}
			if(c1 != null) {
				return c1.add(terms.get(0), c0);
			}
			if(c0 == null && c1 == null) {
				try {
					this.classes.add(new EqualConstantsClass(equality));
				} catch (ChaseException e) {
					return false;
				}
			}
			return true;
		}

		public int size() {
			return this.classes.size();
		}

		public EqualConstantsClass getClass(Term term) {
			for(EqualConstantsClass c:this.classes) {
				if(c.contains(term)) {
					return c;
				}
			}
			return null;
		}
		
		/**
		 * @param o Object
		 * @return boolean
		 */
		@Override
		public boolean equals(Object o) {
			if (this == o) {
				return true;
			}
			if (o == null) {
				return false;
			}
			return this.getClass().isInstance(o)
					&& this.classes.equals(((EqualConstantsClasses) o).classes);
		}

		/**
		 * @return int
		 */
		@Override
		public int hashCode() {
			return Objects.hash(this.classes);
		}

		/**
		 * @return String
		 */
		@Override
		public String toString() {
			return Joiner.on("\n").join(this.classes);
		}
	}	

	@Override
	public boolean isFailed() {
		return this._isFailed;
	}

}
