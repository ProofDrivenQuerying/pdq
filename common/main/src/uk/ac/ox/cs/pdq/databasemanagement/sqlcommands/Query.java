package uk.ac.ox.cs.pdq.databasemanagement.sqlcommands;

import java.util.ArrayList;
import java.util.List;

import uk.ac.ox.cs.pdq.db.Attribute;
import uk.ac.ox.cs.pdq.db.Relation;
import uk.ac.ox.cs.pdq.db.Schema;
import uk.ac.ox.cs.pdq.fol.Atom;
import uk.ac.ox.cs.pdq.fol.ConjunctiveQuery;
import uk.ac.ox.cs.pdq.fol.Formula;
import uk.ac.ox.cs.pdq.fol.Predicate;
import uk.ac.ox.cs.pdq.fol.Term;
import uk.ac.ox.cs.pdq.fol.Variable;

public class Query extends Command {
	private Term[] resultTerms;
	private Formula formula;

	/** creates a select reading all data from given relation 
	 * 
	 */
	public Query(Relation r) {
		super();
		List<Variable> variables = new ArrayList<>();
		for (Attribute a:r.getAttributes()) {
			variables.add(Variable.create(r.getName() + "_" + a.getName()));
		}
		resultTerms = variables.toArray(new Term[variables.size()]);
		Atom a = Atom.create(Predicate.create(r.getName(), r.getArity()), resultTerms);
		formula = ConjunctiveQuery.create(variables.toArray(new Variable[variables.size()]), a);
		convertFormulaToSql();
	}

	public Query(Schema schema, ConjunctiveQuery cq) {
		formula = cq;
		convertFormulaToSql();
	}

	private void convertFormulaToSql() {
		//TODO finish this.
		statements.add("select * from " + DATABASENAME + "."+ formula.toString());
	}

	public Term[] getResultTerms() {
		return resultTerms;
	}

	public void setResultTerms(Term[] resultTerms) {
		this.resultTerms = resultTerms;
	}

	public Formula getFormula() {
		return formula;
	}

	public void setFormula(Formula formula) {
		this.formula = formula;
	}

}
