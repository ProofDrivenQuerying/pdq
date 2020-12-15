// This file is part of PDQ (https://github.com/ProofDrivenQuerying/pdq) which is released under the MIT license.
// See accompanying LICENSE for copyright notice and full details.

package uk.ac.ox.cs.pdq.algebra;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import uk.ac.ox.cs.pdq.db.Attribute;
import uk.ac.ox.cs.pdq.fol.Formula;
import uk.ac.ox.cs.pdq.fol.LogicalSymbols;
import uk.ac.ox.cs.pdq.fol.QuantifiedFormula;
import uk.ac.ox.cs.pdq.fol.Term;
import uk.ac.ox.cs.pdq.fol.Variable;

/**
 * 
 * @author Efthymia Tsamoura
 * @author Stefano
 *
 */
public class ProjectionTerm extends RelationalTerm {
	protected static final long serialVersionUID = -1073141016751509636L;

	protected final RelationalTerm child;

	protected final Attribute[] projections;

	protected String toString = null;

	private ProjectionTerm(Attribute[] projections, RelationalTerm child) {
		super(child.getInputAttributes(), projections);
		assert (projections != null);
		assert (child != null);
		for (int outputAttributeIndex = 0; outputAttributeIndex < projections.length; ++outputAttributeIndex)
			assert (
					Arrays.asList(child.getOutputAttributes()).indexOf(projections[outputAttributeIndex]) >= 0);
		this.projections = projections.clone();
		this.child = child;
	}

	@Override
	public String toString() {
		if (this.toString == null) {
			StringBuilder result = new StringBuilder();
			result.append("Project");
			result.append('{');
			result.append('[');
			for (int index = 0; index < this.projections.length; ++index) {
				result.append(this.projections[index]);
				if (index < this.projections.length - 1)
					result.append(",");
			}
			result.append(']');
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

	public Attribute[] getProjections() {
		return this.projections.clone();
	}

	public static ProjectionTerm create(Attribute[] projections, RelationalTerm child) {
		return Cache.projectionTerm.retrieve(new ProjectionTerm(projections, child));
	}

	@Override
	public RelationalTerm getChild(int childIndex) {
		assert (childIndex == 0);
		return this.child;
	}

	@Override
	public Integer getNumberOfChildren() {
		return 1;
	}

	/**
	 * 8) projection term pi_A T_1
	 * 
	 * where A=a1...an
	 * 
	 * Let (phi_1, M_1)=T_1.toLogic
	 * 
	 * Let x_1=M_1(a1) .... xn= Mn(an) //variables corresponding to attributes that
	 * should remain
	 * 
	 * Let x_{m1} ... x_{mk} be all free variables of phi except x1.... xn
	 * //variables that should be projected out
	 * 
	 * Let M'_1=M_1 restricted to a1... an
	 * 
	 * Return (exists xj1.... xjk phi_1, M'_1)
	 */
	@Override
	public RelationalTermAsLogic toLogic() {
		ProjectionTerm pt = (ProjectionTerm) this;
		Attribute[] pi = pt.getProjections();
		RelationalTerm T1 = getChildren()[0];
		RelationalTermAsLogic t1Logic = T1.toLogic();
		Map<Attribute, Term> mapNew = new HashMap<>();
		for (Attribute p : pi) {
			mapNew.put(p, t1Logic.getMapping().get(p));
		}
		Formula phi = t1Logic.getFormula();
		List<Term> existentialQuantifiers = new ArrayList<>();
		for (Attribute allAttribute : t1Logic.getMapping().keySet()) {
			if (!mapNew.containsKey(allAttribute)) {
				if (t1Logic.getMapping().get(allAttribute).isVariable())
					existentialQuantifiers.add(t1Logic.getMapping().get(allAttribute));
			}
		}
		Formula phiNew = addExistentialQuantifiers(phi, existentialQuantifiers);
		
		return new RelationalTermAsLogic(phiNew, mapNew);
	}
	
	private Formula addExistentialQuantifiers(Formula phi, List<Term> freeVariables) {
		QuantifiedFormula qf = QuantifiedFormula.of(LogicalSymbols.EXISTENTIAL, freeVariables.toArray(new Variable[freeVariables.size()]), phi); 
		return qf;
	}
	
}
