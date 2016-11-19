package uk.ac.ox.cs.pdq.reasoning.utility;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.apache.commons.collections4.CollectionUtils;

import uk.ac.ox.cs.pdq.fol.Atom;
import uk.ac.ox.cs.pdq.fol.Term;
import uk.ac.ox.cs.pdq.reasoning.chase.ChaseException;

import com.beust.jcommander.internal.Sets;
import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;

// TODO: Auto-generated Javadoc
/**
 * Keeps the multiple classes of equal constants created during EGD chasing.
 *
 * @author Efthymia Tsamoura
 */
public class EqualConstantsClasses {

	/**  The classes of equal constants*. */
	private Set<EqualConstantsClass> classes = new HashSet<>();

	/**
	 * Instantiates a new equal constants classes.
	 */
	public EqualConstantsClasses() {}

	/**
	 * Instantiates a new equal constants classes.
	 *
	 * @param classes the classes
	 */
	private EqualConstantsClasses(Set<EqualConstantsClass> classes) {
		Preconditions.checkNotNull(classes);
		this.classes.addAll(classes);
		for(EqualConstantsClass clas:classes) {
			this.classes.add(clas.clone());
		}
	}

	/**
	 * Adds the.
	 *
	 * @param equality the equality
	 * @return 		true if the input equality does not cause chase failure
	 */
	public boolean add(Atom equality) { 
		Preconditions.checkArgument(equality.isEquality());
		List<Term> terms = equality.getTerms();
		EqualConstantsClass c0 = this.getClass(terms.get(0));
		EqualConstantsClass c1 = this.getClass(terms.get(1));

		if(c0 != null && c1 != null && c0.equals(c1)) {
			return true;
		}
		if(c0 != null && c1 != null) {
			if(c0.add(terms.get(1), c1)) {
				if(!c0.equals(c1)) {
					Set<EqualConstantsClass> classes = new HashSet<>();
					Iterator<EqualConstantsClass> iterator = this.classes.iterator();
					while (iterator.hasNext()) {
						EqualConstantsClass cls = iterator.next();
						if (!cls.equals(c1)) {
							classes.add(cls);
						}
					}
					this.classes = classes;
				}
			}
			else {
				return false;
			}
		}
		if(c0 != null) {
			return c0.add(terms.get(1), c1);
		}
		if(c1 != null) {
			return c1.add(terms.get(0), c0);
		}
		if(c0 == null && c1 == null) {
			try {
				this.classes.add(new EqualConstantsClass(equality));
			} catch (ChaseException e) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Size.
	 *
	 * @return the int
	 */
	public int size() {
		return this.classes.size();
	}

	/**
	 * Gets the class.
	 *
	 * @param term the term
	 * @return the class
	 */
	public EqualConstantsClass getClass(Term term) {
		for(EqualConstantsClass c:this.classes) {
			if(c.contains(term)) {
				return c;
			}
		}
		return null;
	}

	/**
	 * Equals.
	 *
	 * @param o Object
	 * @return boolean
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
				&& this.classes.equals(((EqualConstantsClasses) o).classes);
	}

	/**
	 * Hash code.
	 *
	 * @return int
	 */
	@Override
	public int hashCode() {
		return Objects.hash(this.classes);
	}

	/**
	 * To string.
	 *
	 * @return String
	 */
	@Override
	public String toString() {
		return Joiner.on("\n").join(this.classes);
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	@Override
	public EqualConstantsClasses clone() {
		Set<EqualConstantsClass> classes = new HashSet<>();
		for(EqualConstantsClass clas:classes) {
			this.classes.add(clas.clone());
		}
		return new EqualConstantsClasses(classes);
	}

	/**
	 * Merge.
	 *
	 * @param classes the classes
	 * @return 		true if the input collection of classes of equal constants has been successfully merged with this collection.
	 * 		Two classes of equal constants fail to merge if they contain different schema constants.
	 */
	public boolean merge(EqualConstantsClasses classes) {
		Collection<EqualConstantsClass> classesToAppend = Sets.newHashSet();
		for(EqualConstantsClass cls:this.classes) {
			for(EqualConstantsClass target:classes.classes) {
				if(CollectionUtils.containsAny(cls.getConstants(), target.getConstants())) {
					boolean successfull = cls.merge(target);
					if(!successfull) {
						return false;
					}
				}
				else {
					classesToAppend.add(target);
				}
			}
		}
		this.classes.addAll(classesToAppend);
		return true;
	}
}