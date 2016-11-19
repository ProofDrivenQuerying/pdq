package uk.ac.ox.cs.pdq.runtime.exec;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import uk.ac.ox.cs.pdq.algebra.DependentAccess;
import uk.ac.ox.cs.pdq.algebra.DependentJoin;
import uk.ac.ox.cs.pdq.algebra.NaryOperator;
import uk.ac.ox.cs.pdq.algebra.RelationalOperator;
import uk.ac.ox.cs.pdq.algebra.UnaryOperator;
import uk.ac.ox.cs.pdq.algebra.predicates.AttributeEqualityPredicate;
import uk.ac.ox.cs.pdq.algebra.predicates.ConjunctivePredicate;
import uk.ac.ox.cs.pdq.db.AccessMethod;
import uk.ac.ox.cs.pdq.db.AccessMethod.Types;
import uk.ac.ox.cs.pdq.db.Relation;
import uk.ac.ox.cs.pdq.db.wrappers.RelationAccessWrapper;
import uk.ac.ox.cs.pdq.fol.Term;
import uk.ac.ox.cs.pdq.runtime.exec.iterator.BindJoin;
import uk.ac.ox.cs.pdq.runtime.exec.iterator.BottomUpAccess;
import uk.ac.ox.cs.pdq.runtime.exec.iterator.CrossProduct;
import uk.ac.ox.cs.pdq.runtime.exec.iterator.Distinct;
import uk.ac.ox.cs.pdq.runtime.exec.iterator.IsEmpty;
import uk.ac.ox.cs.pdq.runtime.exec.iterator.MemoryScan;
import uk.ac.ox.cs.pdq.runtime.exec.iterator.NestedLoopJoin;
import uk.ac.ox.cs.pdq.runtime.exec.iterator.Projection;
import uk.ac.ox.cs.pdq.runtime.exec.iterator.Scan;
import uk.ac.ox.cs.pdq.runtime.exec.iterator.Selection;
import uk.ac.ox.cs.pdq.runtime.exec.iterator.SymmetricMemoryHashJoin;
import uk.ac.ox.cs.pdq.runtime.exec.iterator.TopDownAccess;
import uk.ac.ox.cs.pdq.runtime.exec.iterator.TupleIterator;
import uk.ac.ox.cs.pdq.util.Typed;
import uk.ac.ox.cs.pdq.util.Utility;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

// TODO: Auto-generated Javadoc
/**
 * Translate logical plans to physical relation plans.
 * 
 * @author Julien Leblay
 */
public class PlanTranslator {
	
	
	/**
	 * Translate a logical plan to a bottom-up physical plan.
	 *
	 * @param logOp the logical operator
	 * @return a physical that corresponds exactly to the given logical plan.
	 * In particular, not further optimization is applied to the resulting plan.
	 * It uses hash join as default for equijoins and NestedLoopJoin for 
	 * joins with arbitrary predicates.
	 */
	public static TupleIterator translate(RelationalOperator logOp) {
		Preconditions.checkArgument(logOp != null);
		TupleIterator[] children = null;

		if (logOp instanceof uk.ac.ox.cs.pdq.algebra.StaticInput) {
			List<Typed> columns = Utility.termsToTyped(logOp.getColumns(), logOp.getType());
			return new MemoryScan(
					columns, ((uk.ac.ox.cs.pdq.algebra.StaticInput) logOp).getTuples());

		} else if (logOp instanceof uk.ac.ox.cs.pdq.algebra.DependentAccess) {
			Relation r = ((DependentAccess) logOp).getRelation(); 
			AccessMethod b = ((DependentAccess) logOp).getAccessMethod();
			if (b.getType() == Types.FREE) {
				return new Scan((RelationAccessWrapper) r);
			}
			return new TopDownAccess((RelationAccessWrapper) r, b, ((DependentAccess) logOp).getStaticInputs());

		} else if (logOp instanceof uk.ac.ox.cs.pdq.algebra.Scan) {
			Relation r = ((uk.ac.ox.cs.pdq.algebra.Scan) logOp).getRelation(); 
			return new Scan((RelationAccessWrapper) r);

		} else if (logOp instanceof uk.ac.ox.cs.pdq.algebra.UnaryOperator) {
			children = new TupleIterator[1];
			children[0] = translate(((UnaryOperator) logOp).getChild());

			if (logOp instanceof uk.ac.ox.cs.pdq.algebra.Projection) {
				List<Typed> columns = Utility.termsToTyped(((uk.ac.ox.cs.pdq.algebra.Projection) logOp).getProjected(), logOp.getType());
				Map<Integer, Term> logicalRenaming = ((uk.ac.ox.cs.pdq.algebra.Projection) logOp).getRenaming();
				if (logicalRenaming != null) {
					Map<Integer, Typed> renaming = new LinkedHashMap<>();
					for (Integer i: logicalRenaming.keySet()) {
						renaming.put(i, Utility.termToTyped(logicalRenaming.get(i), ((UnaryOperator) logOp).getChild().getType().getType(i)));
					}
					return new Projection(columns, renaming, children[0]);
				}
				return new Projection(columns, children[0]);

			} else if (logOp instanceof uk.ac.ox.cs.pdq.algebra.Access) {
				Relation r = ((uk.ac.ox.cs.pdq.algebra.Access) logOp).getRelation(); 
				AccessMethod b = ((uk.ac.ox.cs.pdq.algebra.Access) logOp).getAccessMethod();
				Preconditions.checkState(r instanceof RelationAccessWrapper, "Relation '" + r + "' cannot be used to produce an access physical operator.");
				return new BottomUpAccess((RelationAccessWrapper) r, b, translate(((uk.ac.ox.cs.pdq.algebra.Access) logOp).getChild()));

			} else if (logOp instanceof uk.ac.ox.cs.pdq.algebra.Selection) {
				return new Selection(
						((uk.ac.ox.cs.pdq.algebra.Selection) logOp).getPredicate(),
						children[0]);

			} else if (logOp instanceof uk.ac.ox.cs.pdq.algebra.Distinct) {
				return new Distinct(children[0]);

			} else if (logOp instanceof uk.ac.ox.cs.pdq.algebra.IsEmpty) {
				return new IsEmpty(children[0]);

			}			
		} else if (logOp instanceof uk.ac.ox.cs.pdq.algebra.NaryOperator) {
			List<RelationalOperator> c = ((NaryOperator) logOp).getChildren();
			children = new TupleIterator[c.size()];
			for (int i = 0, l = children.length; i < l; i++) {
				children[i] = translate(c.get(i));
			}

			if (logOp instanceof uk.ac.ox.cs.pdq.algebra.DependentJoin 
					&& ((uk.ac.ox.cs.pdq.algebra.DependentJoin) logOp).hasSidewaysInputs()) {
				return new BindJoin(((DependentJoin) logOp).getPredicate(), ((DependentJoin) logOp).getSidewaysInput(),
						children[0], children[1]);

			} else if (logOp instanceof uk.ac.ox.cs.pdq.algebra.Join) {
				uk.ac.ox.cs.pdq.algebra.Join join = (uk.ac.ox.cs.pdq.algebra.Join) logOp;
				List<Typed> inputsColumns = Utility.termsToTyped(logOp.getInputTerms(), logOp.getInputType());
				switch(join.getVariant()) {
				case NESTED_LOOP:
					return new NestedLoopJoin(
							join.getPredicate(), inputsColumns, Lists.newArrayList(children));
				default:
					return new SymmetricMemoryHashJoin(
							(ConjunctivePredicate<AttributeEqualityPredicate>) join.getPredicate(),
							inputsColumns,
							children[0], children[1]);
				}

			} else if (logOp instanceof uk.ac.ox.cs.pdq.algebra.CrossProduct) {
				List<Typed> inputsColumns = Utility.termsToTyped(logOp.getInputTerms(), logOp.getInputType());
				return new CrossProduct(inputsColumns, Lists.newArrayList(children));

			}
			
		} else if (logOp instanceof uk.ac.ox.cs.pdq.plan.SubPlanAlias) {
			return translate((RelationalOperator) ((uk.ac.ox.cs.pdq.plan.SubPlanAlias) logOp).getPlan().getOperator());

		}  
		throw new IllegalArgumentException("Unsupported logical operator " + logOp);
	}
}
