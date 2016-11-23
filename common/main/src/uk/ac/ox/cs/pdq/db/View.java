package uk.ac.ox.cs.pdq.db;

import java.util.List;
import java.util.Objects;

import uk.ac.ox.cs.pdq.fol.Atom;
import uk.ac.ox.cs.pdq.fol.LinearGuarded;
import uk.ac.ox.cs.pdq.fol.QuantifiedFormula;
import uk.ac.ox.cs.pdq.fol.TGD;
import uk.ac.ox.cs.pdq.util.Utility;

import com.google.common.collect.Lists;

/**
 * TOCOMMENT I still have a hard time understanding the hierarchy between Formulas, Dependencies, Implications, Rules, Views, etc.
 * A view.
 *
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

	/**
	 * TOCOMMENT Instantiates a new view by instantiating a Relation?
	 * Instantiates a new view.
	 *
	 * @param name 		The name of the view
	 * @param attributes 		The view's attributes
	 */
	public View(String name, List<Attribute> attributes) {
		super(name, attributes);
	}

	/**
	 * Instantiates a new view.
	 *
	 * @param name 		The name of the view
	 * @param attributes 		The view's attributes
	 * @param accessMethods 		The binding patterns with which a view can be accessed. By default, a view has free access
	 */
	//TOCOMMENT: the term "binding" is used in many places for variable names, instead of "binding pattern", or "access method/restriction"
	//biding is something else and this might be confusing
	public View(String name, List<Attribute> attributes, List<AccessMethod> accessMethods) {
		super(name, attributes, accessMethods);
	}

	/**
	 * Instantiates a new view.
	 *
	 * @param dependency 		The dependency that defines the view
	 * @param accessMethod 		A binding with which we can access the view. By default, a view has free access
	 */
	public View(LinearGuarded dependency, AccessMethod accessMethod) {
		this(dependency, Lists.newArrayList(accessMethod));
	}


	/**
	 * Instantiates a new view.
	 *
	 * @param dependency 		The dependency that defines the view
	 * @param accessMethods 		The binding patterns with which a view can be accessed. By default, a view has free access
	 */
	public View(LinearGuarded dependency, List<AccessMethod> accessMethods) {
		super(dependency.getBody().getAtoms().get(0).getPredicate().getName(), Utility.makeAttributes(dependency.getGuard()));
		this.dependency = new LinearGuarded(
				new Atom(this, dependency.getBody().getAtoms().get(0).getTerms()),
				dependency.getHead() instanceof QuantifiedFormula ? 
						dependency.getHead().getChildren().get(0) :
				dependency.getHead());
		
		this.definition = new TGD(
				this.dependency.getHead() instanceof QuantifiedFormula ? 
						this.dependency.getHead().getChildren().get(0) :
							this.dependency.getHead(), 
							this.dependency.getBody());
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
	 * @param d LinearGuarded
	 */
	public void setDependency(LinearGuarded d) {
		this.dependency = new LinearGuarded(
				new Atom(this, d.getBody().getAtoms().get(0).getTerms()),
				d.getHead() instanceof QuantifiedFormula ? 
						d.getHead().getChildren().get(0) :
				d.getHead());
		
		this.definition = new TGD(
				this.dependency.getHead() instanceof QuantifiedFormula ? 
						this.dependency.getHead().getChildren().get(0) :
							this.dependency.getHead(), 
							this.dependency.getBody());
	}
	
	/*
	 * (non-Javadoc)
	 * @see uk.ac.ox.cs.pdq.dbschema.Relation#equals(java.lang.Object)
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
				&& this.name.equals(((View) o).name)
				&& this.attributes.equals(((View) o).attributes);
	}

	/*
	 * (non-Javadoc)
	 * @see uk.ac.ox.cs.pdq.dbschema.Relation#hashCode()
	 */
	@Override
	public int hashCode() {
		return Objects.hash(this.name, this.attributes);
	}

	
}