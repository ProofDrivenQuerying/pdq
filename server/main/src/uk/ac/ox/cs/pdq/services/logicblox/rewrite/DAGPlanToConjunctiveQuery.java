package uk.ac.ox.cs.pdq.services.logicblox.rewrite;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import uk.ac.ox.cs.pdq.algebra.NaryOperator;
import uk.ac.ox.cs.pdq.algebra.Projection;
import uk.ac.ox.cs.pdq.algebra.RelationalOperator;
import uk.ac.ox.cs.pdq.algebra.Selection;
import uk.ac.ox.cs.pdq.algebra.UnaryOperator;
import uk.ac.ox.cs.pdq.algebra.predicates.AttributeEqualityPredicate;
import uk.ac.ox.cs.pdq.algebra.predicates.ConjunctivePredicate;
import uk.ac.ox.cs.pdq.algebra.predicates.ConstantEqualityPredicate;
import uk.ac.ox.cs.pdq.db.TypedConstant;
import uk.ac.ox.cs.pdq.db.builder.QueryBuilder;
import uk.ac.ox.cs.pdq.fol.ConjunctiveQuery;
import uk.ac.ox.cs.pdq.fol.Atom;
import uk.ac.ox.cs.pdq.fol.Predicate;
import uk.ac.ox.cs.pdq.fol.Term;
import uk.ac.ox.cs.pdq.plan.AccessOperator;
import uk.ac.ox.cs.pdq.plan.DAGPlan;
import uk.ac.ox.cs.pdq.rewrite.Rewriter;

/**
 * Rewrites DAGPlans to conjunctive queries.
 * @author Julien LEBLAY
 */
public class DAGPlanToConjunctiveQuery implements Rewriter<DAGPlan, ConjunctiveQuery> {
	
	/**  The query's head predicate. */
	private final Atom head;
	
	/**
	 * Constructor for DAGPlanToConjunctiveQuery.
	 * @param head PredicateFormula
	 */
	public DAGPlanToConjunctiveQuery(Atom head) {
		this.head = head;
	}
	
	/**
	 * Constructor for anonymous queries.
	 */
	public DAGPlanToConjunctiveQuery() {
		this(Atom.builder().setSignature(new Predicate("_", 0)).build());
	}
	
	/**
	 * Performs the actual rewriting.
	 * @param input DAGPlan
	 * @return a conjunctive obtain by converting the input plan.
	 */
	@Override
	public ConjunctiveQuery rewrite(DAGPlan input) {
		QueryBuilder builder = new QueryBuilder();
		builder.setName(this.head.getName().replace("$", "dollars"));
		
		Map<Term, Term> mapping = new LinkedHashMap<>();
		List<? extends Term> headTerms = input.getOutput();
		if (this.head.getPredicate().getArity() > 0) {
			assert this.head.getPredicate().getArity() == input.getOutput().size();
			headTerms = this.head.getTerms();
			for (int i = 0, l = headTerms.size(); i < l; i++) {
				Term t = input.getOutput().get(i);
				if (!(t instanceof TypedConstant)) {
					mapping.put(t, headTerms.get(i));
				}
			}
		}
		this.propagateNamesAndConstants(builder, mapping, input.getOperator());
		for (Term t: headTerms) {
			builder.addHeadTerm(t);
		}
		return builder.build();
	}

	/**
	 * Propagates attribute renaming down to the plan's leafs.
	 * @param builder QueryBuilder
	 * @param mapping Map<Term,Term>
	 * @param logOp LogicalOperator
	 */
	private void propagateNamesAndConstants(QueryBuilder builder,
			Map<Term, Term> mapping, RelationalOperator logOp) {
		if (logOp instanceof AccessOperator) {
			Atom.Builder atomBuilder = Atom.builder()
					.setSignature(((AccessOperator) logOp).getRelation());
			for (Term t : logOp.getColumns()) {
				Term substitue = t;
				while (mapping.containsKey(substitue)) {
					substitue = mapping.get(substitue);
				}
				atomBuilder.addTerm(substitue);
			}
			builder.addBodyAtom(atomBuilder.build());
		} else if (logOp instanceof UnaryOperator) {
			if (logOp instanceof Projection) {
				Projection proj = (Projection) logOp;
				Map<Integer, Term> renaming = ((Projection) logOp).getRenaming();
				if (renaming != null) {
					for (int i: renaming.keySet()) {
						mapping.put(proj.getChild().getColumn(i), renaming.get(i));
					}
				}
			}
			if (logOp instanceof Selection) {
				Selection sel = (Selection) logOp;
				this.propagateConstants(mapping, sel.getPredicate(), sel.getChild());
			}
			this.propagateNamesAndConstants(builder, mapping, ((UnaryOperator) logOp).getChild());
		} else if (logOp instanceof NaryOperator) {
			for (RelationalOperator child: ((NaryOperator) logOp).getChildren()) {
				this.propagateNamesAndConstants(builder, mapping, child);
			}
		}
		
	}

	/**
	 * Propagates renaming into plan predicates.
	 *
	 * @param mapping Map<Term,Term>
	 * @param pred LogicalOperator
	 * @param child the child
	 */
	private void propagateConstants(Map<Term, Term> mapping, uk.ac.ox.cs.pdq.algebra.predicates.Predicate pred, RelationalOperator child) {
		if (pred instanceof ConjunctivePredicate) {
			ConjunctivePredicate<uk.ac.ox.cs.pdq.algebra.predicates.Predicate> conj = (ConjunctivePredicate<uk.ac.ox.cs.pdq.algebra.predicates.Predicate>) pred;
			for (uk.ac.ox.cs.pdq.algebra.predicates.Predicate p : conj) {
				this.propagateConstants(mapping, p, child);
			}
		} else if (pred instanceof AttributeEqualityPredicate) {
			AttributeEqualityPredicate ap = (AttributeEqualityPredicate) pred;
			mapping.put(child.getColumn(ap.getPosition()),
					child.getColumn(ap.getOther()));
		} else if (pred instanceof ConstantEqualityPredicate) {
			ConstantEqualityPredicate ap = (ConstantEqualityPredicate) pred;
			mapping.put(child.getColumn(ap.getPosition()), ap.getValue());
		}
	}
}
