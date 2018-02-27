package uk.ac.ox.cs.pdq.algebra;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.junit.Assert;

import uk.ac.ox.cs.pdq.db.Attribute;
import uk.ac.ox.cs.pdq.db.Relation;
import uk.ac.ox.cs.pdq.db.TypedConstant;
import uk.ac.ox.cs.pdq.fol.Atom;
import uk.ac.ox.cs.pdq.fol.Conjunction;
import uk.ac.ox.cs.pdq.fol.Formula;
import uk.ac.ox.cs.pdq.fol.Term;
import uk.ac.ox.cs.pdq.fol.Variable;
import uk.ac.ox.cs.pdq.io.jaxb.adapters.RelationalTermAdapter;
import uk.ac.ox.cs.pdq.util.GlobalCounterProvider;

/**
 * 
 * @author Efthymia Tsamoura
 *
 */
@XmlJavaTypeAdapter(RelationalTermAdapter.class)
public abstract class RelationalTerm implements Serializable {

	private static final long serialVersionUID = 1734503933593174613L;

	protected final Attribute[] inputAttributes;

	protected final Attribute[] outputAttributes;

	/**
	 * List of access children. used often enough to deserve a local cache holding
	 * this list.
	 */
	private Set<AccessTerm> accessesCached = null;

	protected RelationalTerm(Attribute[] inputAttributes, Attribute[] outputAttributes) {
		Assert.assertTrue(outputAttributes != null);
		this.inputAttributes = inputAttributes;
		this.outputAttributes = outputAttributes;
	}

	public Attribute[] getOutputAttributes() {
		return this.outputAttributes.clone();
	}

	public Attribute[] getInputAttributes() {
		return this.inputAttributes.clone();
	}

	public Attribute getOutputAttribute(int index) {
		return this.outputAttributes[index];
	}

	public Attribute getInputAttribute(int index) {
		return this.inputAttributes[index];
	}

	public Integer getNumberOfOutputAttributes() {
		return this.outputAttributes.length;
	}

	public Integer getNumberOfInputAttributes() {
		return this.inputAttributes.length;
	}

	public abstract RelationalTerm[] getChildren();

	public abstract RelationalTerm getChild(int childIndex);

	public abstract Integer getNumberOfChildren();

	public boolean isClosed() {
		return this.inputAttributes.length == 0;
	}

	/**
	 * Gets the accesses, and caches them, since it is a slow operation to generate
	 * the list, but needed frequently.
	 * 
	 * @param operator
	 *            the operator
	 * @return the access operators that are children of the input operator
	 */
	public Set<AccessTerm> getAccesses() {
		if (accessesCached != null)
			return accessesCached;

		Set<AccessTerm> result = new LinkedHashSet<>();
		if (this instanceof AccessTerm) {
			result.add(((AccessTerm) this));
			accessesCached = result;
			return result;
		} else if (this instanceof JoinTerm) {
			for (RelationalTerm child : ((JoinTerm) this).getChildren())
				result.addAll(child.getAccesses());
			accessesCached = result;
			return result;
		} else if (this instanceof DependentJoinTerm) {
			for (RelationalTerm child : ((DependentJoinTerm) this).getChildren())
				result.addAll(child.getAccesses());
			accessesCached = result;
			return result;
		} else if (this instanceof SelectionTerm) {
			result.addAll(((SelectionTerm) this).getChildren()[0].getAccesses());
		} else if (this instanceof ProjectionTerm) {
			result.addAll(((ProjectionTerm) this).getChildren()[0].getAccesses());
		} else if (this instanceof RenameTerm) {
			result.addAll(((RenameTerm) this).getChildren()[0].getAccesses());
		}
		accessesCached = result;
		return result;
	}

	/**
	 * Recursively converts this RelationalTerm into logic objects.
	 * 
	 * @return
	 */
	public RelationalTermAsLogic toLogic() {
		/*
		 * 1) RelationalTerm T is an access term for relation R with attributes a1 ...
		 * an. Let p1 ... pk be the positions that include constants c1.. ck in them.
		 * 
		 * tologic() produces:
		 * 
		 * phi=R(tau1...tau_n) where tau_i=c_i for each p_i and tau_i= a variable x_i
		 * for other position
		 * 
		 * mapping M takes ai to tau_i (we need to access the schema of R to figure out
		 * the positions of each attribute). </pre>
		 */
		if (this instanceof AccessTerm) {
			AccessTerm at = (AccessTerm) this;
			Relation R = at.getRelation();
			Term[] tau = new Term[R.getArity()];
			Map<Attribute, Term> mapping = new HashMap<>();
			for (int index = 0; index < R.getArity(); index++) {
				if (at.getInputConstants().containsKey(index)) {
					tau[index] = at.getInputConstants().get(index);
				} else {
					tau[index] = Variable.create("x_" + index + "_"+GlobalCounterProvider.getNext("VariableName")); 
				}
				mapping.put(at.getOutputAttribute(index), tau[index]);
			}
			Formula phi = Atom.create(R, tau);
			return new RelationalTermAsLogic(phi, mapping);
		}
		/*
		 * 2) Selection with attribute equality condition. T= selection term
		 * sigma_{posi=posj} T_0 where both posi and posj are positions. We assume we
		 * have a way of getting attributes a for posi and atrribute b for posj
		 * 
		 * let (phi_0, M_0)=T_0.toLogic
		 * 
		 * return (phi'_0, M'_0) where phi'_0 is formed from phi_0 by substituting the
		 * variable M_0(b) with M_0(a) and M'_0 agrees with M_0 except M'_0(b) is set to
		 * M_0(a)
		 */
		if (hasAttribueEqualityCondition()) {
			
			RelationalTermAsLogic Tlogic = getChildren()[0].toLogic();
			if (this.getChildren().length > 1) {
				RelationalTermAsLogic T2logic = getChildren()[1].toLogic();
				Tlogic = merge(Tlogic,T2logic);
			}
			Formula phiNew = Tlogic.getPhi();
			Map<Attribute, Term> mapNew = Tlogic.getMapping();
			List<SimpleCondition> conditions = this.getConditions();
			for (SimpleCondition s:conditions) {
				if (s instanceof AttributeEqualityCondition) {
					int position = ((AttributeEqualityCondition)s).getPosition();
					int other = ((AttributeEqualityCondition)s).getOther();
					Attribute a = this.getOutputAttribute(position);
					Attribute b = this.getOutputAttribute(other);
					phiNew = this.replaceTerm(phiNew, mapNew.get(b), mapNew.get(a));
					mapNew.put(b, mapNew.get(a));
				}
			}
			return new RelationalTermAsLogic(phiNew, mapNew);
		}
		/*
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
		if (hasConstantSelection()) {
			RelationalTerm T0 = getChildren()[0];
			RelationalTermAsLogic t0Logic = T0.toLogic();
			Formula phiNew = t0Logic.getPhi();			
			Map<Attribute, Term> mapNew = new HashMap<>();
			mapNew.putAll(t0Logic.getMapping());
			List<SimpleCondition> conditions = this.getConditions();
			for (SimpleCondition s:conditions) {
				if (s instanceof ConstantEqualityCondition) {
					TypedConstant constant = ((ConstantEqualityCondition)s).getConstant();
					int position = ((ConstantEqualityCondition)s).getPosition();
					Attribute a = T0.getOutputAttribute(position);
					phiNew = replaceTerm(phiNew,t0Logic.getMapping().get(a),constant);
					mapNew.put(a, constant);
				}
			}
			return new RelationalTermAsLogic(phiNew,mapNew);
		}

		/*
		 * 4) Inductive case for a renaming term rename(c to d, T_0) (I do not
		 * understand the syntax for renamings in algebra -- left a comment about this;
		 * I assume a syntax as above)
		 * 
		 * let (phi_0, M_0)=T_0.toLogic
		 * 
		 * return phi_0, M'_0 where M'_0 is the same as M_0 except its domain has d
		 * instead of c, where M'_0(d)=M_0(c)
		 */
		if (this instanceof RenameTerm) {
			RelationalTerm T0 = getChildren()[0];
			RelationalTermAsLogic t0Logic = T0.toLogic();
			Map<Attribute, Term> mapNew = new HashMap<>();
			mapNew.putAll(t0Logic.getMapping());
			Attribute[] renamings = ((RenameTerm)this).getRenamings(); // new Attribute names. Not necessarily different from the old one.
			for (int index = 0; index < renamings.length; index ++) {
				if (!renamings[index].equals(T0.getOutputAttributes()[index])) {
					// we found renaming from T0.getOutputAttributes()[index] to renamings[index]
					Term value = mapNew.get(T0.getOutputAttributes()[index]);
					mapNew.remove(T0.getOutputAttributes()[index]);
					mapNew.put(renamings[index],value);
				}
			}
			return new RelationalTermAsLogic(t0Logic.getPhi(),mapNew);
		}

		/*
		 * 5) Inductive case for a cartesian product term T_1 times T_2 where the
		 * attributes of T_1 and T_2 are disjoint.
		 * 
		 * let (phi_1, M_1)=T_1.toLogic let (phi_2, M_2)=T_2.toLogic
		 * 
		 * revise phi_1 and M_1 to avoid any variable overlap with phi_2.
		 * 
		 * return phi_3, M_3 where
		 * 
		 * phi_3= phi_1 \wedge phi_2
		 * 
		 * M_3 has domain that is the union of the domains of M_1 and M_2, and M_3(a)=
		 * M_1(a) on the domain of M_1 while M_3(a)= M_2(a) on the domain of M_2
		 */
		if (this instanceof CartesianProductTerm) {
			RelationalTerm T1 = getChildren()[0];
			RelationalTerm T2 = getChildren()[1];
			RelationalTermAsLogic t1Logic = T1.toLogic();
			RelationalTermAsLogic t2Logic = T2.toLogic();
			return merge(t1Logic,t2Logic);
		}

		/*
		 * 6) Inductive case for natural join of T1 and T2 with common attributes
		 * d1...dk.
		 * 
		 * let (phi_1, M_1)=T_1.toLogic let (phi_2, M_2)=T_2.toLogic
		 * 
		 * revise phi_1 so that variables are disjoint form phi_2 variables, and revise
		 * M_1 accordingly.
		 * 
		 * let x1... xk be M_1(d1)... M_1(dk) let y1....yk be M_2(d1) ... M_2(dk)
		 * 
		 * let sigma be the substitution taking xi to yi
		 * 
		 * let phi'_1 be applying sigma to phi_1
		 * 
		 * We return phi'_1 \wedge phi_2 as the formula
		 * 
		 * The mapping M_3 has domain that is the union of the domains of M_1 and M_2,
		 * and M_3(a)= sigma(M_1(a)) on the domain of M_1 while M_3(a)= rho( M_2(a)) on
		 * the domain of M_2
		 */
		/*
		 * 7) dependent join is the same as join for toLogic()
		 */
		if (this instanceof JoinTerm || this instanceof DependentJoinTerm) {
			// this is already covered in the attribute equality case.
		}
		// TOCOMMENT projection is not described.
		
		//default is to simply join the child toLogic results:
		if (this.getChildren().length == 1)
			return this.getChildren()[0].toLogic();
		else {
			RelationalTermAsLogic results = null;
			for (RelationalTerm child : this.getChildren()) {
				results = merge(results, child.toLogic());
			}
			return results;
		}
	}

	/**  Replaces a term (Variable or constant) with a new term in a formula.
	 * @param phiNew
	 * @param term
	 * @param constant
	 * @return
	 */
	private Formula replaceTerm(Formula phi, Term old, Term newTerm) {
		if (phi instanceof Atom) {
			Term[] terms = phi.getTerms();
			for (int i = 0; i < terms.length; i++) {
				if (terms[i].equals(old)) {
					terms[i] = newTerm;
				}
			}
			return Atom.create(((Atom) phi).getPredicate(), terms);
		} else {
			Atom[] atoms = ((Conjunction)phi).getAtoms();
			Atom[] newAtoms = new Atom[atoms.length]; 
			for (int i = 0; i < atoms.length; i++) {
				newAtoms[i] = (Atom)replaceTerm(atoms[i],old,newTerm);
			}
			return Conjunction.of(newAtoms);
		}
	}

	/**
	 * If this term can have conditions (like joins or selects does) then it will
	 * return them as a list. Otherwise will return and empty list.
	 * 
	 * @return
	 */
	private List<SimpleCondition> getConditions() {
		Condition c = null;
		if (this instanceof SelectionTerm) {
			c = ((SelectionTerm) this).getSelectionCondition();
		}
		if (this instanceof JoinTerm) {
			c = ((JoinTerm) this).getJoinConditions();
		}
		if (this instanceof DependentJoinTerm) {
			c = ((DependentJoinTerm) this).getJoinConditions();
		}
		if (c == null)
			return new ArrayList<>();
		if (c instanceof ConjunctiveCondition) {
			return Arrays.asList(((ConjunctiveCondition) c).getSimpleConditions());
		} else {
			return Arrays.asList(new SimpleCondition[] { (SimpleCondition) c });
		}
	}

	/**
	 * @return true if this term can have conditions and it contains at least one
	 *         attribute equality condition.
	 */
	private boolean hasAttribueEqualityCondition() {
		for (SimpleCondition c : getConditions()) {
			if (c instanceof AttributeEqualityCondition)
				return true;
		}
		return false;
	}

	/**
	 * @return true if it has at least one constant equality condition.
	 */
	private boolean hasConstantSelection() {
		for (SimpleCondition c : getConditions()) {
			if (c instanceof ConstantEqualityCondition)
				return true;
		}
		return false;
	}

	/**
	 * Merges two RelationalTermAsLogic object into one that contains the
	 * conjunction formula of the two source formulas.
	 * 
	 * @param left
	 * @param right
	 * @return
	 */
	private RelationalTermAsLogic merge(RelationalTermAsLogic left, RelationalTermAsLogic right) {
		if (left == null && right == null)
			return null;
		if (left == null)
			return right;
		Formula phi_1 = left.getPhi();
		Formula phi_2 = right.getPhi();
		Formula phi = Conjunction.of(phi_1, phi_2);

		Map<Attribute, Term> map_1 = left.getMapping();
		Map<Attribute, Term> map_2 = right.getMapping();
		Map<Attribute, Term> map = new HashMap<>();
		map.putAll(map_1);
		map.putAll(map_2);

		return new RelationalTermAsLogic(phi, map);
	}
}
