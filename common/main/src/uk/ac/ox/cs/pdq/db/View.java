package uk.ac.ox.cs.pdq.db;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import uk.ac.ox.cs.pdq.fol.Formula;
import uk.ac.ox.cs.pdq.fol.Atom;
import uk.ac.ox.cs.pdq.fol.Predicate;
import uk.ac.ox.cs.pdq.fol.Term;

import com.google.common.collect.Lists;

/**
 * TOCOMMENT I still have a hard time understanding the hierarchy between Formulas, Dependencies, Implications, Rules, Views, etc.
 * A view.
 *
 * @author Efthymia Tsamoura
 * @author Julien Leblay
 */
public class View extends Relation //implements Rule<Formula, Atom> 
{

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = -4961888228318423619L;

	/** The view id. */
	protected int viewId;

	/** 
	 * TOCOMMENT what is this supposed to mean, and why is it a LinearGuarded dependency?
	 *  The inverse dependency that defines the view. */
	protected Dependency dependency;

	/**  
	 * TOCOMMENT So a view extends a Relation?? And does not have a declared connection to TGD except that it essentially is a TGD wrapper.
	 * The dependency that defines the view. */
	protected Dependency definition;

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
	 * @param bindings 		The binding patterns with which a view can be accessed. By default, a view has free access
	 */
	//TOCOMMENT: the term "binding" is used in many places for variable names, instead of "binding pattern", or "access method/restriction"
	//biding is something else and this might be confusing
	public View(String name, List<Attribute> attributes, List<AccessMethod> bindings) {
		super(name, attributes, bindings);
	}

	/**
	 * Instantiates a new view.
	 *
	 * @param dependency 		The dependency that defines the view
	 * @param binding 		A binding with which we can access the view. By default, a view has free access
	 */
	public View(Dependency dependency, AccessMethod binding) {
		this(dependency, Lists.newArrayList(binding));
	}


	/**
	 * Instantiates a new view.
	 *
	 * @param dependency 		The dependency that defines the view
	 * @param bindings 		The binding patterns with which a view can be accessed. By default, a view has free access
	 */
	public View(Dependency dependency, List<AccessMethod> bindings) {
		super(dependency.getGuard().getName(), makeAttributes(dependency.getGuard()));
		this.viewId = globalId++;
		this.dependency = new LinearGuarded(
				new Atom(this, dependency.getUniversal()),
				dependency.getRight());
		this.definition = this.dependency.invert();
		this.setAccessMethods(bindings);
	}

	/**
	 * TOCOMMENT how is this method relevant to a View?
	 * Make attributes.
	 *
	 * @param fact An input fact
	 * @return The list of schema attributes that correspond to this fact
	 */
	private static List<Attribute> makeAttributes(Atom fact) {
		Predicate s = fact.getPredicate();
		if (s instanceof Relation) {
			return ((Relation) s).getAttributes();
		}
		List<Attribute> result = new ArrayList<>();
		for (Term t : fact.getTerms()) {
			result.add(new Attribute(String.class, t.toString()));
		}
		return result;
	}

	/**
	 * Gets the id of this view.
	 *
	 * @return int
	 */
	@Override
	public int getId() {
		return this.viewId;
	}

	/**
	 * TOCOMMENT ???
	 * Gets the dependency.
	 *
	 * @return LinearGuarded
	 */
	public Dependency getDependency() {
		return this.dependency;
	}

	/**
	 * Gets the definition.
	 *
	 * @return the TGD defining the view
	 */
	public Dependency getDefinition() {
		return this.definition;
	}

	/**
	 * Sets the dependency.
	 *
	 * @param d LinearGuarded
	 */
	public void setDependency(Dependency d) {
		this.dependency = d;
		this.definition = d.invert();
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

//	/* (non-Javadoc)
//	 * @see uk.ac.ox.cs.pdq.fol.Rule#getHead()
//	 */
//	@Override
//	public Atom getHead() {
//		return this.dependency.getGuard();
//	}
//
//	/* (non-Javadoc)
//	 * @see uk.ac.ox.cs.pdq.fol.Rule#getBody()
//	 */
//	@Override
//	public Formula getBody() {
//		return this.dependency.getHead();
//	}
//
//	/* (non-Javadoc)
//	 * @see uk.ac.ox.cs.pdq.fol.Rule#contains(uk.ac.ox.cs.pdq.fol.Predicate)
//	 */
//	@Override
//	public boolean contains(Predicate s) {
//		return this.dependency.contains(s);
//	}
}