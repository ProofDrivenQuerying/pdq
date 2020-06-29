// This file is part of PDQ (https://github.com/ProofDrivenQuerying/pdq) which is released under the MIT license.
// See accompanying LICENSE for copyright notice and full details.

package uk.ac.ox.cs.pdq.db;

import java.util.Map;

import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.google.common.base.Preconditions;

import uk.ac.ox.cs.pdq.fol.Atom;
import uk.ac.ox.cs.pdq.fol.ConjunctiveQuery;
import uk.ac.ox.cs.pdq.fol.Constant;
import uk.ac.ox.cs.pdq.fol.Dependency;
import uk.ac.ox.cs.pdq.fol.Formula;
import uk.ac.ox.cs.pdq.fol.Predicate;
import uk.ac.ox.cs.pdq.fol.Variable;

/**
 *
 * @author Efthymia Tsamoura
 * @author Julien Leblay
 *
 */
public class Match {

	/**  The formula or query that will be grounded using an homomorphism*. */
	protected final Formula formula;

	/** The mapping of query's variables to constants.*/
	protected final Map<Variable, Constant> mapping;

	protected Match(Formula formula, Map<Variable, Constant> mapping) {
		Preconditions.checkArgument(formula instanceof ConjunctiveQuery || formula instanceof Dependency);
		Preconditions.checkArgument(mapping != null);
		this.mapping = mapping;
		this.formula = formula;
	}

	public Map<Variable, Constant> getMapping() {
		return this.mapping;
	}

	public Formula getFormula() {
		return this.formula;
	}

	@Override
	public String toString() {
		return this.mapping.toString() + "\n" + this.formula.toString(); 
	}
	public Atom toAtom() {
		Predicate p = Predicate.create("QueryResult", mapping.size());
		Constant[] terms = mapping.values().toArray(new Constant[mapping.size()]);
		return Atom.create(p, terms);
	}
	
    public static Match create(Formula formula, Map<Variable, Constant> mapping) {
        return Cache.match.retrieve(new Match(formula, mapping));
    }
    
	@Override
	public int hashCode() {
		HashCodeBuilder builder = new HashCodeBuilder(17, 37);
		builder.append(formula);
		for (Variable v: mapping.keySet()) {
			builder.append(v);
			builder.append(mapping.get(v));
		}
		return builder.toHashCode();
	}

}
