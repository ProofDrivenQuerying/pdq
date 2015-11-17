package uk.ac.ox.cs.pdq.reasoning.utility;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;

import uk.ac.ox.cs.pdq.db.TypedConstant;
import uk.ac.ox.cs.pdq.fol.Constant;
import uk.ac.ox.cs.pdq.fol.Equality;
import uk.ac.ox.cs.pdq.fol.Skolem;
import uk.ac.ox.cs.pdq.fol.Term;
import uk.ac.ox.cs.pdq.reasoning.chase.ChaseException;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;

/**
 * Class of equal constants created during EGD chasing
 * @author Efthymia Tsamoura
 *
 */
public class EqualConstantsClass {

	/** Collection of equal constants **/
	private final Collection<Term> constants;

	/** The schema constant that belongs to this class **/
	private TypedConstant<?> schemaConstant; 

	/** The representative term. If this.schemaConstant <> null the representative should equal this.schemaConstant**/
	private Term representative;

	public EqualConstantsClass(Equality equality) throws ChaseException{
		this.constants = new LinkedHashSet<>();
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

	private EqualConstantsClass(Collection<Term> constants, Term representative, TypedConstant<?> schemaConstant) {
		Preconditions.checkNotNull(constants);
		this.constants = new LinkedHashSet<>();
		this.constants.addAll(constants);
		this.schemaConstant = schemaConstant;
		this.representative = representative;
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
		return this.schemaConstant;
	}

	/**
	 * 
	 * @param target
	 * @return
	 * 		true if the input class of equal constants has been successfully merged with this class.
	 * 		Two classes of equal constants fail to merge if they contain different schema constants.
	 */
	public boolean merge(EqualConstantsClass target) {
		if(this.schemaConstant != null && target.getSchemaConstant() != null &&
				!this.schemaConstant.equals(target.getSchemaConstant())) {
			return false;
		} else {
			this.constants.addAll(target.getConstants());
			this.schemaConstant = target.schemaConstant;
			return true;
		}
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
				&& ( (this.schemaConstant == null && ((EqualConstantsClass) o).schemaConstant == null) ||
						this.schemaConstant != null && ((EqualConstantsClass) o).schemaConstant != null &&
						this.schemaConstant.equals(((EqualConstantsClass) o).schemaConstant) );
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

	@Override
	public EqualConstantsClass clone() {
		return new EqualConstantsClass(Sets.newHashSet(this.constants), this.representative, this.schemaConstant);
	}

	/**
	 * 
	 * @return the representative term of this class.
	 * If this.schemaConstant <> null the representative should equal this.schemaConstant.
	 * If all constants are labelled nulls, then return a labelled null.
	 * Otherwise, the first (in order of appearance) canonical constant
	 */
	public Term getRepresentative() {
		if(this.representative == null) {
			if(this.schemaConstant != null) {
				this.representative = this.schemaConstant;
			}
			else {
				for(Term constant:this.constants) {
					if(constant instanceof Skolem && ((Skolem) constant).getName().startsWith("c")) {
						this.representative = constant;
						return this.representative;
					}
				}
				this.representative = this.constants.iterator().next();
			}	
		}
		else {
			if(this.schemaConstant != null) {
				this.representative = this.schemaConstant;
				return this.representative;
			}
			if(this.representative instanceof TypedConstant) {
				return this.representative;
			}
			else if(this.representative instanceof Skolem && ((Skolem) this.representative).getName().startsWith("c")) {
				return this.representative;
			}
			else {
				for(Term constant:this.constants) {
					if(constant instanceof Skolem && ((Skolem) constant).getName().startsWith("c")) {
						this.representative = constant;
						return this.representative;
					}
				}
			}
		}
		return this.representative;
	}
}