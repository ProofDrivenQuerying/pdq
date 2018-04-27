package uk.ac.ox.cs.pdq.algebra;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;

import com.google.common.base.Preconditions;

import uk.ac.ox.cs.pdq.db.Attribute;
import uk.ac.ox.cs.pdq.fol.Term;

/**
 * 
 * @author Efthymia Tsamoura
 *
 */
public class RenameTerm extends RelationalTerm {
	protected static final long serialVersionUID = -5225689808613760428L;

	protected final RelationalTerm child;

	/**
         * A rename does a bulk renaming describing the new name of each 
         * attribute of a relation. */
	 /** It is described by an attribute array giving the output attributes of this rename term Mapping
	 * between the child's output attribute array is done by index, so the number of
	 * outputs have to be the same and the order also have to be the same. 
	 /** The rename term
	 * can only change the names of these attributes.
	 */
	protected final Attribute[] renamings;

	/** Cached string representation. */
	private String toString = null;

	private RenameTerm(Attribute[] renamings, RelationalTerm child) {
		super(AlgebraUtilities.computeRenamedInputAttributes(renamings, child), renamings);
		Assert.assertNotNull(renamings);
		Assert.assertNotNull(child);
		Assert.assertTrue(renamings.length == child.getOutputAttributes().length);
		this.renamings = renamings.clone();
		this.child = child;
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
			result.append("Rename");
			result.append('{');
			result.append('[');
			for (int index = 0; index < this.renamings.length; ++index) {
				result.append(this.renamings[index]);
				if (index < this.renamings.length - 1)
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

	public Attribute[] getRenamings() {
		return this.renamings.clone();
	}

	public static RenameTerm create(Attribute[] renamings, RelationalTerm child) {
		return Cache.renameTerm.retrieve(new RenameTerm(renamings, child));
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
	 * 4) Inductive case for a renaming term rename(c to d, T_0) (I do not
	 * understand the syntax for renamings in algebra -- left a comment about this;
	 * I assume a syntax as above)
	 * 
	 * let (phi_0, M_0)=T_0.toLogic
	 * 
	 * return phi_0, M'_0 where M'_0 is the same as M_0 except its domain has d
	 * instead of c, where M'_0(d)=M_0(c)
	 */
	@Override
	public RelationalTermAsLogic toLogic() {
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
		return new RelationalTermAsLogic(t0Logic.getFormula(),mapNew);
	}
	
	protected static Attribute[] getInputAttributes(Attribute[] renamings, Plan child) {
		Attribute[] newInputAttributes = new Attribute[child.getInputAttributes().length];
		Attribute[] oldOutputAttributes = child.getOutputAttributes();
		for (int i = 0; i < child.getInputAttributes().length; ++i) {
			int indexInputAttribute = Arrays.asList(oldOutputAttributes).indexOf(child.getInputAttributes()[i]);
			Preconditions.checkArgument(indexInputAttribute >= 0, "Input attribute not found");
			newInputAttributes[i] = renamings[indexInputAttribute];
		}
		return newInputAttributes;
	}
	
}
