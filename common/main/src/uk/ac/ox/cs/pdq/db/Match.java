package uk.ac.ox.cs.pdq.db;

import java.util.Map;

import com.google.common.base.Preconditions;

import uk.ac.ox.cs.pdq.InterningManager;
import uk.ac.ox.cs.pdq.fol.ConjunctiveQuery;
import uk.ac.ox.cs.pdq.fol.Constant;
import uk.ac.ox.cs.pdq.fol.Dependency;
import uk.ac.ox.cs.pdq.fol.Formula;
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
	
    protected static final InterningManager<Match> s_interningManager = new InterningManager<Match>() {
        protected boolean equal(Match object1, Match object2) {
            if (!object1.formula.equals(object2.formula) || 
            		object1.mapping.size() != object2.mapping.size())
                return false;
            for(java.util.Map.Entry<Variable, Constant> entry:object1.mapping.entrySet()) {
            	if(!object2.mapping.containsKey(entry.getKey()) || object2.mapping.get(entry.getKey()).equals(entry.getValue())) 
            		return false;
            }
            return true;
        }

        protected int getHashCode(Match object) {
            int hashCode = object.formula.hashCode();
            for(java.util.Map.Entry<Variable, Constant> entry:object.mapping.entrySet()) 
                hashCode = hashCode * 8 + entry.getKey().hashCode() * 9 + entry.getValue().hashCode() * 10;
            return hashCode;
        }
    };
    
    protected Object readResolve() {
        return s_interningManager.intern(this);
    }

    public static Match create(Formula formula, Map<Variable, Constant> mapping) {
        return s_interningManager.intern(new Match(formula, mapping));
    }
}
