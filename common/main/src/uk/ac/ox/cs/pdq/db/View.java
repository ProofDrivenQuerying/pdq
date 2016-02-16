package uk.ac.ox.cs.pdq.db;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import uk.ac.ox.cs.pdq.fol.Formula;
import uk.ac.ox.cs.pdq.fol.Predicate;
import uk.ac.ox.cs.pdq.fol.Rule;
import uk.ac.ox.cs.pdq.fol.Signature;
import uk.ac.ox.cs.pdq.fol.Term;

import com.google.common.collect.Lists;

// TODO: Auto-generated Javadoc
/**
 * A view.
 *
 * @author Efthymia Tsamoura
 * @author Julien Leblay
 */
public class View extends Relation implements Rule<Formula, Predicate> {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = -4961888228318423619L;

	/** The view id. */
	protected int viewId;

	/**  The inverse dependency that defines the view. */
	protected LinearGuarded dependency;

	/**  The dependency that defines the view. */
	protected TGD definition;

	/**
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
	public View(String name, List<Attribute> attributes, List<AccessMethod> bindings) {
		super(name, attributes, bindings);
	}

	/**
	 * Instantiates a new view.
	 *
	 * @param dependency 		The dependency that defines the view
	 * @param binding 		A binding with which we can access the view. By default, a view has free access
	 */
	public View(LinearGuarded dependency, AccessMethod binding) {
		this(dependency, Lists.newArrayList(binding));
	}


	/**
	 * Instantiates a new view.
	 *
	 * @param dependency 		The dependency that defines the view
	 * @param bindings 		The binding patterns with which a view can be accessed. By default, a view has free access
	 */
	public View(LinearGuarded dependency, List<AccessMethod> bindings) {
		super(dependency.getGuard().getName(), makeAttributes(dependency.getGuard()));
		this.viewId = globalId++;
		this.dependency = new LinearGuarded(
				new Predicate(this, dependency.getUniversal()),
				dependency.getRight());
		this.definition = this.dependency.invert();
		this.setAccessMethods(bindings);
	}

	/**
	 * Make attributes.
	 *
	 * @param fact An input fact
	 * @return The list of schema attributes that correspond to this fact
	 */
	private static List<Attribute> makeAttributes(Predicate fact) {
		Signature s = fact.getSignature();
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
	 * Gets the id.
	 *
	 * @return int
	 */
	@Override
	public int getId() {
		return this.viewId;
	}

	/**
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

	/* (non-Javadoc)
	 * @see uk.ac.ox.cs.pdq.fol.Rule#getHead()
	 */
	@Override
	public Predicate getHead() {
		return this.dependency.getGuard();
	}

	/* (non-Javadoc)
	 * @see uk.ac.ox.cs.pdq.fol.Rule#getBody()
	 */
	@Override
	public Formula getBody() {
		return this.dependency.getHead();
	}

	/* (non-Javadoc)
	 * @see uk.ac.ox.cs.pdq.fol.Rule#contains(uk.ac.ox.cs.pdq.fol.Signature)
	 */
	@Override
	public boolean contains(Signature s) {
		return this.dependency.contains(s);
	}
}