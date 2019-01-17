package uk.ac.ox.cs.pdq.reasoning.chase.schemaconstantequality;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.TreeSet;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;

import uk.ac.ox.cs.pdq.fol.Atom;
import uk.ac.ox.cs.pdq.fol.Constant;
import uk.ac.ox.cs.pdq.fol.Term;
import uk.ac.ox.cs.pdq.fol.TypedConstant;
import uk.ac.ox.cs.pdq.fol.UntypedConstant;
import uk.ac.ox.cs.pdq.reasoning.chase.ChaseException;

/**
 * Class of chase constants that are equal under the schema constraints.
 * @author Efthymia Tsamoura
 *
 */
public class EqualConstantsClass {

	/**  Collection of equal constants *. */
	private final Collection<Term> constants;

	/**  The schema constant that belongs to this class *. */
	private TypedConstant schemaConstant; 

	/** The representative term. If this.schemaConstant <> null the representative should equal this.schemaConstant**/
	private Term representative;

	/**
	 * Instantiates a new equal constants class.
	 *
	 * @param equality the equality
	 * @throws ChaseException the chase exception
	 */
	public EqualConstantsClass(Atom equality) throws ChaseException{
		this.constants = new TreeSet<>();
		Term[] terms = equality.getTerms();
		Preconditions.checkArgument(terms[0] instanceof Constant && terms[1] instanceof Constant);
		//If both inputs are schema constants
		if(terms[0] instanceof TypedConstant && terms[1] instanceof TypedConstant) {
			if(!terms[0].equals(terms[1])) {
				throw new ChaseException("Trying to add different schema constants in the same class");
			}
			else {
				//Create a class that keeps the input schema constant 
				this.schemaConstant = (TypedConstant) terms[0];
				this.constants.add(terms[0]);
			}
		}
		//If only one of the inputs is schema constant
		else if(terms[0] instanceof TypedConstant) {
			this.schemaConstant = (TypedConstant) terms[0];
			this.constants.add(terms[0]);
			this.constants.add(terms[1]);
		}
		else if(terms[1] instanceof TypedConstant) {
			this.schemaConstant = (TypedConstant) terms[1];
			this.constants.add(terms[0]);
			this.constants.add(terms[1]);
		}
		else {
			//If both inputs are chase constants
			this.constants.add(terms[0]);
			this.constants.add(terms[1]);
		}
		//Find out the representative. 
		this.setRepresentative();
	}

	/**
	 * Instantiates a new equal constants class.
	 *
	 * @param constants the constants
	 * @param representative the representative
	 * @param schemaConstant the schema constant
	 */
	private EqualConstantsClass(Collection<Term> constants, Term representative, TypedConstant schemaConstant) {
		Preconditions.checkNotNull(constants);
		this.constants = new LinkedHashSet<>();
		this.constants.addAll(constants);
		this.schemaConstant = schemaConstant;
		this.representative = representative;
	}

	/**
	 * Adds the.
	 *
	 * @param input the input
	 * @param inputClass 		The class of the input constant
	 * @return 		true if the input constant has been successfully added.
	 * 		An input constant is successfully added only if we do not come up with cases
	 * 		where two different schema constants should be equal
	 */
	public boolean add(Term input, EqualConstantsClass inputClass) {
		Preconditions.checkArgument(input instanceof Constant);
		//If the input constant already belongs to a class
		if(inputClass != null) {
			TypedConstant inputSchemaConstant = inputClass.getSchemaConstant();
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
				this.schemaConstant = (TypedConstant) input;
			}
			else if(!(input instanceof TypedConstant)) {
				this.constants.add(input);

			}
		}
		//Find out the representative. 
		this.setRepresentative();
		return true;
	}

	
	/**
	 * Picks the representative of this class
	 * 		The representatives are picked in the following priority:
	 * 		Schema constants
	 * 		Constants from the canonical database
	 * 		Other labelled nulls not produced after firing EGDs.
	 */
	private void setRepresentative() {
		this.representative = null;
		if(this.schemaConstant != null) {
			this.representative = this.schemaConstant;
		}
		else {
			if(this.representative == null || this.representative instanceof UntypedConstant && !((UntypedConstant) this.representative).getSymbol().startsWith("c")) {
				for(Term constant:this.constants) {
					if(constant instanceof UntypedConstant && ((UntypedConstant) constant).getSymbol().startsWith("c")) {
						this.representative = constant;
						break;
					}
				}
			}
			if(this.representative == null) {
				for(Term constant:this.constants) {
					if(constant instanceof UntypedConstant && ((UntypedConstant) constant).getSymbol().startsWith("k")) {
						this.representative = constant;
						break;
					}
				}
				if(this.representative == null) {
					this.representative = this.constants.iterator().next();
				}
			}
		}	
	}
	
	/**
	 * Contains.
	 *
	 * @param constant the constant
	 * @return true, if successful
	 */
	public boolean contains(Term constant) {
		return (this.schemaConstant!= null && this.schemaConstant.equals(constant)) 
				|| this.constants.contains(constant);
	}

	/**
	 * Gets the schema constant.
	 *
	 * @return the schema constant
	 */
	public TypedConstant getSchemaConstant() {
		return this.schemaConstant;
	}

	/**
	 * Merge.
	 *
	 * @param target the target
	 * @return 		true if the input class of equal constants has been successfully merged with this class.
	 * 		Two classes of equal constants fail to merge if they contain different schema constants.
	 */
	public boolean merge(EqualConstantsClass target) {
		if(this.schemaConstant != null && target.getSchemaConstant() != null &&
				!this.schemaConstant.equals(target.getSchemaConstant())) {
			return false;
		} else {
			this.constants.addAll(target.getConstants());
			this.schemaConstant = target.schemaConstant;
			//Update the representative
			this.setRepresentative();
			return true;
		}
	}

	/**
	 * Gets the constants.
	 *
	 * @return the constants
	 */
	public Collection<Term> getConstants() {
		return this.constants;
	}

	/**
	 * Equals.
	 *
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
				&& this.schemaConstant.equals(((EqualConstantsClass) o).schemaConstant)
				&& this.representative.equals(((EqualConstantsClass) o).representative);
//				&& ( (this.schemaConstant == null && ((EqualConstantsClass) o).schemaConstant == null) ||
//						this.schemaConstant != null && ((EqualConstantsClass) o).schemaConstant != null &&
//						this.schemaConstant.equals(((EqualConstantsClass) o).schemaConstant) );
	}

	/**
	 * Hash code.
	 *
	 * @return int
	 */
	@Override
	public int hashCode() {
		return Objects.hash(this.constants, this.schemaConstant, this.representative);
	}

	/**
	 * To string.
	 *
	 * @return String
	 */
	@Override
	public String toString() {
		return "[" + Joiner.on(",").join(this.constants) + " =" + this.schemaConstant + "]";
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	@Override
	public EqualConstantsClass clone() {
		return new EqualConstantsClass(Sets.newHashSet(this.constants), this.representative, this.schemaConstant);
	}

	/**
	 * Gets the representative.
	 *
	 * @return the representative term of this class.
	 * If this.schemaConstant <> null the representative should equal this.schemaConstant.
	 * If all constants are labelled nulls, then return a labelled null.
	 * Otherwise, the first (in order of appearance) canonical constant
	 */
	public Term getRepresentative() {
		return this.representative;
	}
}