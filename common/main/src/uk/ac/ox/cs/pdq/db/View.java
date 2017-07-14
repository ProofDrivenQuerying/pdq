package uk.ac.ox.cs.pdq.db;

import java.util.Objects;

import uk.ac.ox.cs.pdq.fol.Atom;
import uk.ac.ox.cs.pdq.fol.LinearGuarded;
import uk.ac.ox.cs.pdq.fol.QuantifiedFormula;
import uk.ac.ox.cs.pdq.fol.TGD;
import uk.ac.ox.cs.pdq.util.Utility;

/**
 * @author Efthymia Tsamoura
 * @author Julien Leblay
 */
public class View extends Relation {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = -4961888228318423619L;

	/** 
	 * TOCOMMENT what is this supposed to mean, and why is it a LinearGuarded dependency?
	 *  The inverse dependency that defines the view. */
	protected LinearGuarded dependency;

	/**  
	 * TOCOMMENT So a view extends a Relation?? And does not have a declared connection to TGD except that it essentially is a TGD wrapper.
	 * The dependency that defines the view. */
	protected TGD definition;

	
	public View(String name, Attribute[] attributes) {
		super(name, attributes);
	}
	
	/**
	 * Instantiates a new view.
	 *
	 * @param dependency 		The dependency that defines the view
	 * @param accessMethods 		The binding patterns with which a view can be accessed. By default, a view has free access
	 */
	public View(LinearGuarded dependency, AccessMethod[] accessMethods) {
		super(dependency.getBody().getAtoms()[0].getPredicate().getName(), Utility.makeAttributes(dependency.getGuard()));
		this.dependency = dependency;
		this.definition = TGD.create(this.dependency.getHead(), this.dependency.getBody());
		this.setAccessMethods(accessMethods);
	}

	/**
	 * TOCOMMENT ???
	 * Gets the dependency.
	 *
	 * @return LinearGuarded
	 */
	public LinearGuarded getDependency() {
		return this.dependency;
	}

	/**
	 * Gets the definition.
	 *
	 * @return the TGD defining the view
	 */
	public TGD getDefinition() {
		return this.definition;
	}

	/**
	 * Sets the dependency.
	 *
	 * @param dependency LinearGuarded
	 */
	public void setDependency(LinearGuarded dependency) {
		this.dependency = LinearGuarded.create(
				Atom.create(this, dependency.getBody().getAtoms()[0].getTerms()),
				dependency.getHead() instanceof QuantifiedFormula ? 
						dependency.getHead().getChildren()[0] :
				dependency.getHead());
		
		this.definition = TGD.create(
				this.dependency.getHead() instanceof QuantifiedFormula ? 
						this.dependency.getHead().getChildren()[0] :
							this.dependency.getHead(), 
							this.dependency.getBody());
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null) {
			return false;
		}
		return this.getClass().isInstance(o)
				&& this.name.equals(((View) o).name);
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.name);
	}
	
}