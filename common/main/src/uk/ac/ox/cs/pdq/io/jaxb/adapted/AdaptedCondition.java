package uk.ac.ox.cs.pdq.io.jaxb.adapted;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

import uk.ac.ox.cs.pdq.algebra.AttributeEqualityCondition;
import uk.ac.ox.cs.pdq.algebra.Condition;
import uk.ac.ox.cs.pdq.algebra.ConjunctiveCondition;
import uk.ac.ox.cs.pdq.algebra.ConstantEqualityCondition;
import uk.ac.ox.cs.pdq.algebra.SimpleCondition;
import uk.ac.ox.cs.pdq.db.TypedConstant;
import uk.ac.ox.cs.pdq.fol.Constant;

/**
 * @author Gabor
 *
 */
public class AdaptedCondition {
	 
	protected Integer position;				//AttributeEqualityCondition
	protected Integer other;				//AttributeEqualityCondition
	private Integer position2;			//ConstantEqualityCondition
	private Constant constant;		//ConstantEqualityCondition

	private Condition[] predicates;		//ConjunctiveCondition
	private String type;

	public AdaptedCondition() {
	}

	public AdaptedCondition(Condition value) {
		if (value instanceof AttributeEqualityCondition) {
			position = ((AttributeEqualityCondition)value).getPosition();
			other = ((AttributeEqualityCondition)value).getOther();
		} else if (value instanceof ConstantEqualityCondition) {
			position = ((ConstantEqualityCondition)value).getPosition();
			setConstant(((ConstantEqualityCondition)value).getConstant());
		} else if (value instanceof ConjunctiveCondition) {
			setPredicates(((ConjunctiveCondition)value).getSimpleConditions());
		}
		type = value.getClass().getSimpleName();
	}

	@XmlAttribute(name = "position")
	public String getPosition() {
		if (position == null)
			return null;
		return String.valueOf(position);
	}
	@XmlAttribute(name = "other")
	public String getOther() {
		if (other == null)
			return null;
		return String.valueOf(other);
	}
	@XmlAttribute(name = "position")
	public String getPosition2() {
		if (position2 == null)
			return null;
		return String.valueOf(position2);
	}
	@XmlElement(name = "constant")
	public Constant getConstant() {
		if (constant == null)
			return null;
		return constant;
	}
	@XmlElement(name = "predicates")
	public Condition[] getPredicates() {
		if (predicates == null)
			return null;
		return predicates;
	}
	
	@XmlAttribute(name = "type")
	public String getType() {
		return type;
	}

	public Condition toCondition() {
		if (type == null)
			return null;
		if ("AttributeEqualityCondition".equals(type)) {
			return AttributeEqualityCondition.create(position, other);
		} else if ("ConstantEqualityCondition".equals(type)) {
			return ConstantEqualityCondition.create(position2, (TypedConstant)constant);
		} else if ("ConjunctiveCondition".equals(type)) {
			if (predicates!=null) {
				SimpleCondition[] con = new SimpleCondition[predicates.length];
				for (int i=0; i<predicates.length; i++) {
					con[i] = (SimpleCondition) predicates[i];
				}
				return ConjunctiveCondition.create(con);
			} else {
				throw new IllegalArgumentException("Predicates cannot be null!");
			}
		}
		throw new IllegalArgumentException("type not set or unknown: " + type);
	}

	public void setPosition(String p) {
		if (p == null) {
			this.position = null;
		} else {
			this.position = Integer.parseInt(p);
		}
	}
	public void setPosition2(String p) {
		if (p == null) {
			this.position2 = null;
		} else {
			this.position2 = Integer.parseInt(p);
		}
	}
	public void setOther(String o) {
		if (o == null) {
			this.other = null;
		} else {
			this.other = Integer.parseInt(o);
		}
	}
	
	public void setPredicates(Condition[] predicates) {
		this.predicates = predicates;
	}

	public void setConstant(Constant constant) {
		this.constant = constant;
	}

	public void setType(String type) {
		this.type = type;
	}

}
