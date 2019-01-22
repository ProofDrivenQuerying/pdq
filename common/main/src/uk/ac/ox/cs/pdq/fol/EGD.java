package uk.ac.ox.cs.pdq.fol;

import java.util.Arrays;
import java.util.Map;

import org.junit.Assert;

import uk.ac.ox.cs.pdq.db.Attribute;
import uk.ac.ox.cs.pdq.db.Relation;
import uk.ac.ox.cs.pdq.util.Utility;

/**
 *
 * @author Efthymia Tsamoura
 * @author Gabor
 */
public class EGD extends Dependency {
	private static final long serialVersionUID = 7220579236533295677L;
	/**  The dependency's universally quantified variables. */
	protected Variable[] universal;
	
	/**
	 * Flag to indicate if this EGD was created from a functional dependency.
	 */
	private boolean isFromFunctionalDependency;
	
	protected EGD(Atom[] body, Atom[] head, boolean isFromFunctionalDependency) {
		super(body, head);
		this.isFromFunctionalDependency = isFromFunctionalDependency;
		Assert.assertTrue(isConjunctionOfNonEqualities(body));
		Assert.assertTrue(!isConjunctionOfNonEqualities(head));
		if (isFromFunctionalDependency) {
			Assert.assertEquals(2,body.length);
			Assert.assertEquals(body[0].getPredicate(),body[1].getPredicate());
		}
	}
	
	public boolean isFromFunctionalDependency() {
		return isFromFunctionalDependency;
	}

	private static boolean isConjunctionOfNonEqualities(Atom[] atoms) {
		for(Atom atom:atoms) {
			if(atom.isEquality())
				return false;
		}
		return true;
	}

	/**
	 * Gets the universally quantified variables.
	 *
	 * @return List<Variable>
	 */
	public Variable[] getUniversal() {
		if(this.universal == null) 
			this.universal = this.variables;
		return this.universal.clone();
	}

	@Override
	public String toString() {
		String f = "";
		
		if(this.getUniversal().length > 0)
			f = Arrays.asList(this.getUniversal()).toString();
		
		return f + this.body + LogicalSymbols.IMPLIES + this.head;
	}
	
	public boolean isLinear() {
		return this.body.getAtoms().length == 1;
	}
	
    public static EGD create(Atom[] head, Atom[] body) {
        return Cache.egd.retrieve(new EGD(head, body,false));
    }
    public static EGD create(Atom[] head, Atom[] body, boolean isFromFunctionalDependency) {
        return Cache.egd.retrieve(new EGD(head, body,isFromFunctionalDependency));
    }

	/**
	 * Constructs an EGD for the given relation and key attibutes.
	 *
	 * @param relation the relation
	 * @param keys the key attirbutes
	 * @return the EGD representing the primary key
	 */
	public static EGD create(Relation relation, Attribute[] keys) {
		return create(Predicate.create(relation.getName(), relation.getArity()), relation.getAttributes(), keys);
	}

	/**
	 * Let R be a relation of arity n and x_k be its key.
	 * The EGD that captures the EGD dependency is given by
	 * R(x_1,...,x_k,...x_n) ^ R(x_1',...,x_k,...x_n') --> \Wedge_{i \neq k} x_i=x_i'
	 *
	 * @param predicate the signature
	 * @param attributes the attributes
	 * @param keys the keys
	 * @return 		a collection of EGDs for the input relation and keys
	 */
	public static EGD create(Predicate predicate, Attribute[] attributes, Attribute[] keys) {
		Term[] leftTerms = Utility.typedToTerms(attributes);
		Term[] copiedTerms = leftTerms.clone();
		//Keeps the terms that should be equal
		Map<Term,Term> tobeEqual = com.google.common.collect.Maps.newHashMap();
		int i = 0;
		for(Attribute typed:attributes) {
			if(!Arrays.asList(keys).contains(typed)) {
				// the ? is a naming convention, could be anything.
				Term term = Variable.create(String.valueOf("?" + typed));
				copiedTerms[i] = term;
				tobeEqual.put(leftTerms[i], term);
			}
			i++;
		}
		Predicate equality = Predicate.create("EQUALITY", 2, true);
		//Create the constant equality predicates
		int index = 0;
		Atom[] equalities = new Atom[tobeEqual.entrySet().size()];
		for(java.util.Map.Entry<Term, Term> pair:tobeEqual.entrySet()) 
			equalities[index++] = Atom.create(equality, pair.getKey(), pair.getValue());
		Atom body[] = new Atom[]{Atom.create(Predicate.create(predicate.getName(), leftTerms.length), leftTerms), 
						Atom.create(Predicate.create(predicate.getName(), copiedTerms.length), copiedTerms)};
		return create(body, equalities, true);
	}
	
}
