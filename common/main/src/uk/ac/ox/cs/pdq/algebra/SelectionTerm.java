package uk.ac.ox.cs.pdq.algebra;

import java.util.List;
import java.util.Map;

import org.junit.Assert;

import uk.ac.ox.cs.pdq.db.Attribute;
import uk.ac.ox.cs.pdq.db.TypedConstant;
import uk.ac.ox.cs.pdq.fol.Formula;
import uk.ac.ox.cs.pdq.fol.Term;

/**
 * 
 * @author Efthymia Tsamoura
 *
 */
public class SelectionTerm extends RelationalTerm {

	private static final long serialVersionUID = 3979377421532058418L;

	protected final RelationalTerm child;

	/** The predicate associated with this selection. */
	protected final Condition selectionCondition;

	/** Cached string representation. */
	private String toString = null;

	private SelectionTerm(Condition selectionCondition, RelationalTerm child) {
		super(child.getInputAttributes(), child.getOutputAttributes());
		Assert.assertNotNull(selectionCondition);
		Assert.assertNotNull(child);
		Assert.assertTrue(AlgebraUtilities.assertSelectionCondition(selectionCondition, child.getOutputAttributes()));
		this.selectionCondition = selectionCondition;
		this.child = child;
	}

	public Condition getSelectionCondition() {
		return this.selectionCondition;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		if (this.toString == null) {
			StringBuilder result = new StringBuilder();
			result.append("Select");
			result.append('{');
			result.append('[').append(this.selectionCondition).append(']');
			result.append(this.child.toString());
			result.append('}');
			this.toString = result.toString();
		}
		return this.toString;
	}

	@Override
	public RelationalTerm[] getChildren() {
		RelationalTerm[] children = new RelationalTerm[1];
		children[0] = this.child;
		return children;
	}

	public static SelectionTerm create(Condition predicate, RelationalTerm child) {
		return Cache.selectionTerm.retrieve(new SelectionTerm(predicate, child));
	}

	@Override
	public RelationalTerm getChild(int childIndex) {
		Assert.assertTrue(childIndex == 0);
		return this.child;
	}

	@Override
	public Integer getNumberOfChildren() {
		return 1;
	}

	/**
	 * 3) Inductive case for constant selection:
	 * 
	 * input T is a selection term sigma_{posi=c} T_0 where posi is an attribute and
	 * c is a constant. Again we let a be the attribute corresponding to posi.
	 * 
	 * let (phi_0, M_0)=T_0.toLogic
	 * 
	 * return (phi'_0, M'_0) where
	 * 
	 * phi'_0 is formed from phi_0 by substituting the variable M'_0(a) with c
	 * 
	 * M'_0 agrees with M_0 except M'_0(a) is set to c
	 */
	@Override
	public RelationalTermAsLogic toLogic() {
		RelationalTermAsLogic T1logic = getChildren()[0].toLogic();
		if (!this.getConditions().isEmpty()) {
			// this case deals with different joins and selectionTerm case.
			RelationalTermAsLogic TNewlogic = T1logic; 
			Formula phiNew = TNewlogic.getPhi();
			Map<Attribute, Term> mapNew = TNewlogic.getMapping();
			List<SimpleCondition> conditions = this.getConditions();
			// Apply conditions
			for (SimpleCondition s:conditions) {
				if (s instanceof ConstantEqualityCondition) {
					TypedConstant constant = ((ConstantEqualityCondition)s).getConstant();
					int position = ((ConstantEqualityCondition)s).getPosition();
					Attribute a = this.getOutputAttribute(position);
					if (T1logic.getMapping().get(a)!=null)
						phiNew = AlgebraUtilities.replaceTerm(phiNew,T1logic.getMapping().get(a),constant);
					mapNew.put(a, constant);
				}
			}
			return new RelationalTermAsLogic(phiNew, mapNew);
		} else {
			return T1logic;
		}
		
	}

}
