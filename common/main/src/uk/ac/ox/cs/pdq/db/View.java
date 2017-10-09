package uk.ac.ox.cs.pdq.db;

import java.util.Objects;

import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import uk.ac.ox.cs.pdq.fol.LinearGuarded;
import uk.ac.ox.cs.pdq.fol.TGD;
import uk.ac.ox.cs.pdq.io.jaxb.adapters.ViewAdapter;
import uk.ac.ox.cs.pdq.util.Utility;

/**
 * @author Efthymia Tsamoura
 * @author Julien Leblay
 * @author Gabor
 */
@XmlJavaTypeAdapter(ViewAdapter.class)
public class View extends Relation {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = -4961888228318423619L;

	/**
	 * The dependency that defines the view.
	 */
	protected TGD relationToViewDependency;
	
	/**
	 * The inverse of the dependency that defines the view.
	 */
	protected LinearGuarded viewToRelationDependency;


	public View(String name, Attribute[] attributes) {
		super(name, attributes);
	}

	public View(String name, Attribute[] attributes, AccessMethod[] methods) {
		super(name, attributes, methods);
	}

	/**
	 * Instantiates a new view.
	 *
	 * @param dependency
	 *            The dependency that defines the view
	 * @param accessMethods
	 *            The binding patterns with which a view can be accessed. By
	 *            default, a view has free access
	 */
	public View(LinearGuarded dependency, AccessMethod[] accessMethods) {
		super(dependency.getBodyAtom(0).getPredicate().getName(), Utility.getAttributes(dependency.getGuard()), accessMethods);
		this.viewToRelationDependency = dependency;
		this.relationToViewDependency = TGD.create(this.viewToRelationDependency.getBody().getAtoms(), this.viewToRelationDependency.getHead().getAtoms());
	}

	/**
	 * TOCOMMENT ??? Gets the inverse of the dependency that defines the view
	 *
	 * @return LinearGuarded
	 */
	public LinearGuarded getViewToRelationDependency() {
		return this.viewToRelationDependency;
	}

	/**
	 * Gets the definition.
	 *
	 * @return the TGD defining the view
	 */
	public TGD getRelationToViewDependency() {
		return this.relationToViewDependency;
	}

	/**
	 * Sets the dependency.
	 *
	 * @param viewToRelationDependency
	 *            LinearGuarded
	 */
	public void setViewToRelationDependency(LinearGuarded viewToRelationDependency) {
		this.viewToRelationDependency = viewToRelationDependency;
		this.relationToViewDependency = TGD.create(this.viewToRelationDependency.getHead().getAtoms(), this.viewToRelationDependency.getBody().getAtoms());
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null) {
			return false;
		}
		return this.getClass().isInstance(o) && this.name.equals(((View) o).name);
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.name);
	}

}
