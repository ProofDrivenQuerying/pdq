// This file is part of PDQ (https://github.com/michaelbenedikt/pdq) which is released under the MIT license.
// See accompanying LICENSE for copyright notice and full details.

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
 * Class of chase constants (i.e. nulls) that are inferred to be equal under the schema 
 * constraints.
 * For each such class, we pick one constant as the representative of the class; if a fact
 * F(...n...) holds of a constant n, then the fact will be propagated to
 * F(...r....) where r is the representative of the equivalence class of n.
 * 
 * @author Efthymia Tsamoura
 * @author Gabor
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
	 * Instantiates a new constant equality class.
	 *
	 * @param equality the equality
	 * @throws ChaseException the chase exception
	 */
	public EqualConstantsClass(Atom equality) throws ChaseException{
		this.constants = new TreeSet<>();
		Term[] terms = equality.getTerms();
		Preconditions.checkArgument(equality.isEquality());
		Preconditions.checkArgument(terms.length == 2);
		Preconditions.checkArgument(terms[0]!=null);
		Preconditions.checkArgument(terms[1]!=null);
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
		else {
			//If only one or none of the inputs is schema constant
			this.constants.add(terms[0]);
			this.constants.add(terms[1]);
			
			if(terms[0] instanceof TypedConstant) {
				this.schemaConstant = (TypedConstant) terms[0];
			}
			else if(terms[1] instanceof TypedConstant) {
				this.schemaConstant = (TypedConstant) terms[1];
			}
			else {
				//If both inputs are chase constants we can't set the schema constant field.
			}
		}
		//Find out the representative. 
		this.setRepresentative();
	}

	/**
	 * Instantiates a new constant equality class.
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
	 * Tries to add a constant to an equivalence class. Equivalence classes
	 * are not allowed to contain two schema constants (since we assume the
	 * Unique Name Assumption); thus the add will fail if it would result
	 * in two such constants being equivalent.
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
	 * 		Schema constant in the class, if there is one
	 * 		Constants from the canonical database
	 * 		Other labelled nulls not produced after firing EGDs.
	 */
	private void setRepresentative() {
		this.representative = null;
		if(this.schemaConstant != null) {
			this.representative = this.schemaConstant;
		}
		else {
			if(this.representative == null || this.representative instanceof UntypedConstant && !((UntypedConstant) this.representative).isCannonicalConstant()) {
				for(Term constant:this.constants) {
					if(constant instanceof UntypedConstant && ((UntypedConstant) constant).isCannonicalConstant()) {
						this.representative = constant;
						break;
					}
				}
			}
			if(this.representative == null) {
				for(Term constant:this.constants) {
					if(constant instanceof UntypedConstant && ((UntypedConstant) constant).isNonCannonicalConstant()) {
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
	 * @param constant the constant
	 * @return true, if class contains this constant
	 */
	public boolean contains(Term constant) {
		return (this.schemaConstant!= null && this.schemaConstant.equals(constant)) 
				|| this.constants.contains(constant);
	}

	/**
	 * Gets the schema constant that is in the class, if there is one (otherwise will return null)
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
	 * Gets all members of the class.
	 *
	 * @return the constants in the class
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
		// trivially equal, do not need to check further
		if (this == o) return true;
		if (o == null) return false;
		if (!this.getClass().isInstance(o)) return false;
		
		// Schema Constant can be null, needs to be checked.
		if (this.schemaConstant == null && ((EqualConstantsClass) o).schemaConstant != null) return false;
		if (this.schemaConstant != null && ((EqualConstantsClass) o).schemaConstant == null) return false;
		
		if (this.schemaConstant != null && !this.schemaConstant.equals(((EqualConstantsClass) o).schemaConstant)) return false;
		//everything is not null, so check whether they have exactly the same members, and the same representative
		return this.constants.equals(((EqualConstantsClass) o).constants)
				&& this.representative.equals(((EqualConstantsClass) o).representative);
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