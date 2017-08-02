package uk.ac.ox.cs.pdq.io.jaxb.adapted;

import java.io.Serializable;
import java.util.Map;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

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
import uk.ac.ox.cs.pdq.db.AccessMethod;
import uk.ac.ox.cs.pdq.db.Attribute;
import uk.ac.ox.cs.pdq.db.Relation;
import uk.ac.ox.cs.pdq.db.TypedConstant;

/**
 * @author Gabor
 *
 */
public class AdaptedRelationalTerm implements Serializable {
	
	private static final long serialVersionUID = 1734503933593174613L;
	
	public static enum RELATIONAL_TERM_TYPES { AccessTerm, CartesianProductTerm,DependentJoinTerm,JoinTerm,ProjectionTerm,RenameTerm,SelectionTerm};
	
	private RELATIONAL_TERM_TYPES rtType;
	
	protected Attribute[] inputAttributes;
	
	protected Attribute[] outputAttributes;
	
	protected Relation relation;
	
	protected RelationalTerm[] children = new RelationalTerm[2];
	
	protected Attribute[] renamings;

	/** The join conditions. */
	protected Condition joinConditions;
	private Condition predicate;
	
	/** Input positions for the right hand child**/
	protected Integer[] sidewaysInput;
	
	protected Attribute[] projections;
	/** The access method to use. */
	protected AccessMethod accessMethod;

	/**  The constants used to call the underlying access method. */
	protected Map<Integer, TypedConstant> inputConstants;
	
	public AdaptedRelationalTerm(RelationalTerm v) {
		rtType = getType(v);
		if (v==null || rtType==null) {
			throw new IllegalArgumentException();
		}
		switch (rtType) {
		case AccessTerm:
			relation = ((AccessTerm)v).getRelation();
			accessMethod = ((AccessTerm)v).getAccessMethod();
			break;
		case CartesianProductTerm:
			children = ((CartesianProductTerm)v).getChildren();
			break;
		case DependentJoinTerm:
			children = ((DependentJoinTerm)v).getChildren();
			break;
		case JoinTerm:
			children = ((JoinTerm)v).getChildren();
			break;
		case ProjectionTerm:
			projections= ((ProjectionTerm)v).getProjections();
			children = ((ProjectionTerm)v).getChildren();
			break;
		case RenameTerm:
			renamings = ((RenameTerm)v).getRenamings();
			children = ((RenameTerm)v).getChildren();
			break;
		case SelectionTerm:
			predicate = ((SelectionTerm)v).getPredicate();
			children = ((SelectionTerm)v).getChildren();
			break;
		default:
			break;
		}
	}
	protected RELATIONAL_TERM_TYPES getType(RelationalTerm t) {
		if (t==null)
			return null;
		
		if (t instanceof AccessTerm)
			return RELATIONAL_TERM_TYPES.AccessTerm;
		
		if (t instanceof CartesianProductTerm)
			return RELATIONAL_TERM_TYPES.CartesianProductTerm;
		
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

		throw new IllegalArgumentException("RelationalTerm "+ t + " has unknown type.");
	}

	@XmlElement
	public Attribute[] getOutputAttributes() {
		return this.outputAttributes;
	}
	
	@XmlElement
	public Attribute[] getInputAttributes() {
		return this.inputAttributes;
	}
	
	@XmlElement
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

	public Condition getPredicate() {
		return predicate;
	}

	public Condition getJoinConditions() {
		return joinConditions;
	}

	public void setJoinConditions(Condition joinConditions) {
		this.joinConditions = joinConditions;
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
	public AccessMethod getAccessMethod() {
		return accessMethod;
	}

	public void setAccessMethod(AccessMethod accessMethod) {
		this.accessMethod = accessMethod;
	}

	@XmlElement
	public Map<Integer, TypedConstant> getInputConstants() {
		return inputConstants;
	}

	public void setInputConstants(Map<Integer, TypedConstant> inputConstants) {
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

	@XmlAttribute
	public String getXmlRtType() {
		return rtType.name();
	}

	public void setXmlRtType(String rtType) {
		try {
			if (rtType==null)
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
	
	public RELATIONAL_TERM_TYPES getRtType() {
		return rtType;
	}

	public void setRtType(RELATIONAL_TERM_TYPES rtType) {
		this.rtType = rtType;
	}

	public void setPredicate(Condition predicate) {
		this.predicate = predicate;
	}
	public RelationalTerm toRelationalTerm(AdaptedRelationalTerm v) {
		switch (v.getRtType()) {
		case AccessTerm:
			return AccessTerm.create(v.relation, v.accessMethod);
		case CartesianProductTerm:
			return CartesianProductTerm.create(v.children[0], v.children[1]);
		case DependentJoinTerm:
			return DependentJoinTerm.create(v.children[0], v.children[1]);
		case JoinTerm:
			return JoinTerm.create(v.children[0], v.children[1]);
		case ProjectionTerm:
			return ProjectionTerm.create(v.projections, v.children[0]);
		case RenameTerm:
			return RenameTerm.create(v.renamings, v.children[0]);
		case SelectionTerm:
			return SelectionTerm.create(v.getPredicate(), v.children[0]);
		default:
			throw new IllegalArgumentException("Unknown relational term type: " + v.getRtType());
		}
	}
	
}
