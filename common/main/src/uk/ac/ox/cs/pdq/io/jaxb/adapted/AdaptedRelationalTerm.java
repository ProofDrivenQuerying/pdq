package uk.ac.ox.cs.pdq.io.jaxb.adapted;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.apache.log4j.Logger;

import uk.ac.ox.cs.pdq.algebra.AccessTerm;
import uk.ac.ox.cs.pdq.algebra.CartesianProductTerm;
import uk.ac.ox.cs.pdq.algebra.Condition;
import uk.ac.ox.cs.pdq.algebra.DependentJoinTerm;
import uk.ac.ox.cs.pdq.algebra.JoinTerm;
import uk.ac.ox.cs.pdq.algebra.ProjectionTerm;
import uk.ac.ox.cs.pdq.algebra.RelationalTerm;
import uk.ac.ox.cs.pdq.algebra.RenameTerm;
import uk.ac.ox.cs.pdq.algebra.SelectionTerm;
import uk.ac.ox.cs.pdq.db.AccessMethodDescriptor;
import uk.ac.ox.cs.pdq.db.Attribute;
import uk.ac.ox.cs.pdq.db.Relation;
import uk.ac.ox.cs.pdq.fol.TypedConstant;

/**
 * @author Gabor
 *
 */
@XmlRootElement(name = "RelationalTerm")
@XmlType(propOrder = { "outputAttributes", "inputAttributes","accessMethod", "inputConstants","predicate","projections","relation","renamings","children" })
public class AdaptedRelationalTerm implements Serializable {

	private static final long serialVersionUID = 1734503933593174613L;

	public static enum RELATIONAL_TERM_TYPES {
		AccessTerm, CartesianProductTerm, DependentJoinTerm, JoinTerm, ProjectionTerm, RenameTerm, SelectionTerm
	};

	private RELATIONAL_TERM_TYPES rtType;

	protected Attribute[] inputAttributes;

	protected Attribute[] outputAttributes;

	protected Relation relation;

	protected RelationalTerm[] children = new RelationalTerm[2];

	protected Attribute[] renamings;

	private Condition predicate;

	/** Input positions for the right hand child **/
	protected Integer[] sidewaysInput;

	protected Attribute[] projections=new Attribute[0];
	/** The access method to use. */
	protected AccessMethodDescriptor accessMethod;

	/** The constants used to call the underlying access method. */
	protected Map<Integer, AdaptedConstant> inputConstants;

	public AdaptedRelationalTerm() {
		
	}
	
	public AdaptedRelationalTerm(RelationalTerm v) {
		rtType = getType(v);
		if (v == null || rtType == null) {
			throw new IllegalArgumentException();
		}
		switch (rtType) {
		case AccessTerm:
			relation = ((AccessTerm) v).getRelation();
			accessMethod = ((AccessTerm) v).getAccessMethod();
			Map<Integer, TypedConstant> iConstants = ((AccessTerm) v).getInputConstants();
			inputConstants = new HashMap<>();
			for (Integer key:iConstants.keySet()) {
				inputConstants.put(key, new AdaptedConstant(iConstants.get(key)));
			}
			break;
		case CartesianProductTerm:
			children = ((CartesianProductTerm) v).getChildren();
			break;
		case DependentJoinTerm:
			children = ((DependentJoinTerm) v).getChildren();
			break;
		case JoinTerm:
			children = ((JoinTerm) v).getChildren();
			break;
		case ProjectionTerm:
			projections = ((ProjectionTerm) v).getProjections();
			children = ((ProjectionTerm) v).getChildren();
			break;
		case RenameTerm:
			renamings = ((RenameTerm) v).getRenamings();
			children = ((RenameTerm) v).getChildren();
			break;
		case SelectionTerm:
			predicate = ((SelectionTerm) v).getSelectionCondition();
			children = ((SelectionTerm) v).getChildren();
			break;
		default:
			break;
		}
	}

	protected RELATIONAL_TERM_TYPES getType(RelationalTerm t) {
		if (t == null)
			return null;

		if (t instanceof AccessTerm)
			return RELATIONAL_TERM_TYPES.AccessTerm;


		if (t instanceof DependentJoinTerm)
			return RELATIONAL_TERM_TYPES.DependentJoinTerm;

		if (t instanceof JoinTerm)
			return RELATIONAL_TERM_TYPES.JoinTerm;

		if (t instanceof ProjectionTerm)
			return RELATIONAL_TERM_TYPES.ProjectionTerm;

		if (t instanceof RenameTerm)
			return RELATIONAL_TERM_TYPES.RenameTerm;

		if (t instanceof SelectionTerm)
			return RELATIONAL_TERM_TYPES.SelectionTerm;

		if (t instanceof CartesianProductTerm)
			return RELATIONAL_TERM_TYPES.CartesianProductTerm;
		throw new IllegalArgumentException("RelationalTerm " + t + " has unknown type.");
	}

	@XmlElement
	public Attribute[] getOutputAttributes() {
		return this.outputAttributes;
	}

	@XmlElement
	public Attribute[] getInputAttributes() {
		return this.inputAttributes;
	}

	@XmlElement(name="RelationalTerm")
	public RelationalTerm[] getChildren() {
		return children;
	}

	@XmlElement
	public Relation getRelation() {
		return relation;
	}

	public void setRelation(Relation relation) {
		this.relation = relation;
	}

	@XmlElement
	public Attribute[] getRenamings() {
		return renamings;
	}

	public void setRenamings(Attribute[] renamings) {
		this.renamings = renamings;
	}

	@XmlElement
	public Condition getPredicate() {
		return predicate;
	}

	@XmlAttribute
	public Integer[] getSidewaysInput() {
		return sidewaysInput;
	}

	public void setSidewaysInput(Integer[] sidewaysInput) {
		this.sidewaysInput = sidewaysInput;
	}

	@XmlElement
	public Attribute[] getProjections() {
		return projections;
	}

	public void setProjections(Attribute[] projections) {
		this.projections = projections;
	}

	@XmlElement
	public AccessMethodDescriptor getAccessMethod() {
		return accessMethod;
	}

	public void setAccessMethod(AccessMethodDescriptor accessMethod) {
		this.accessMethod = accessMethod;
	}

	@XmlElement
	public Map<Integer, AdaptedConstant> getInputConstants() {
		return inputConstants;
	}
	
	public void setInputConstants(Map<Integer, AdaptedConstant> inputConstants) {
		this.inputConstants = inputConstants;
	}

	public void setInputAttributes(Attribute[] inputAttributes) {
		this.inputAttributes = inputAttributes;
	}

	public void setOutputAttributes(Attribute[] outputAttributes) {
		this.outputAttributes = outputAttributes;
	}

	public void setChildren(RelationalTerm[] children) {
		this.children = children;
	}

	@XmlAttribute(name="type")
	public String getXmlRtType() {
		return rtType.name();
	}

	public void setXmlRtType(String rtType) {
		try {
			if (rtType == null)
				return;
			for (RELATIONAL_TERM_TYPES r : RELATIONAL_TERM_TYPES.values()) {
				if (r.name().equals(rtType)) {
					this.rtType = r;
				}
			}
		} catch (Throwable t) {
			Logger.getLogger(this.getClass()).error(t.getMessage(), t);
			throw t;
		}
	}

	public void setPredicate(Condition predicate) {
		this.predicate = predicate;
	}

	public RelationalTerm toRelationalTerm() {
		switch (this.rtType) {
		case AccessTerm:
			if (inputConstants!=null) {
				Map<Integer, TypedConstant> iConstants = new HashMap<>();
				for (Integer key:inputConstants.keySet()) {
					iConstants.put(key, (TypedConstant)inputConstants.get(key).toConstant());
				}
				return AccessTerm.create(this.relation, this.accessMethod, iConstants);
			} else 
				return AccessTerm.create(this.relation, this.accessMethod);
		case CartesianProductTerm:
			return CartesianProductTerm.create(this.children[0], this.children[1]);
		case DependentJoinTerm:
			return DependentJoinTerm.create(this.children[0], this.children[1]);
		case JoinTerm:
			return JoinTerm.create(this.children[0], this.children[1]);
		case ProjectionTerm:
			return ProjectionTerm.create(this.projections, this.children[0]);
		case RenameTerm:
			return RenameTerm.create(this.renamings, this.children[0]);
		case SelectionTerm:
			return SelectionTerm.create(this.getPredicate(), this.children[0]);
		default:
			throw new IllegalArgumentException("Unknown relational term type: " + rtType);
		}
	}

}
