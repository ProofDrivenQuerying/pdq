package uk.ac.ox.cs.pdq.runtime.exec;

import java.util.Arrays;
import java.util.List;

import com.google.common.base.Preconditions;

import uk.ac.ox.cs.pdq.algebra.AccessTerm;
import uk.ac.ox.cs.pdq.algebra.DependentJoinTerm;
import uk.ac.ox.cs.pdq.algebra.JoinTerm;
import uk.ac.ox.cs.pdq.algebra.ProjectionTerm;
import uk.ac.ox.cs.pdq.algebra.RelationalTerm;
import uk.ac.ox.cs.pdq.algebra.RenameTerm;
import uk.ac.ox.cs.pdq.algebra.SelectionTerm;
import uk.ac.ox.cs.pdq.datasources.RelationAccessWrapper;
import uk.ac.ox.cs.pdq.db.AccessMethod;
import uk.ac.ox.cs.pdq.db.Attribute;
import uk.ac.ox.cs.pdq.db.Relation;
import uk.ac.ox.cs.pdq.runtime.exec.iterator.Access;
import uk.ac.ox.cs.pdq.runtime.exec.iterator.DependentJoin;
import uk.ac.ox.cs.pdq.runtime.exec.iterator.NestedLoopJoin;
import uk.ac.ox.cs.pdq.runtime.exec.iterator.Projection;
import uk.ac.ox.cs.pdq.runtime.exec.iterator.Scan;
import uk.ac.ox.cs.pdq.runtime.exec.iterator.Selection;
import uk.ac.ox.cs.pdq.runtime.exec.iterator.SymmetricMemoryHashJoin;
import uk.ac.ox.cs.pdq.runtime.exec.iterator.TupleIterator;

/**
 * Translate logical plans to physical relation plans.
 * TOCOMMENT: ALL COMMENTS APPEAR TO BE OUT OF DATE
 * 
 * @author Julien Leblay
 */
public class PlanTranslator {

	public static enum JoinType{
		NESTED_LOOP, SYMMETRIC_HASH
	};

	public static final JoinType joinType = JoinType.NESTED_LOOP;

	/**
	 * Translate a logical plan to a bottom-up physical plan.
	 * TOCOMMENT: DO NOT USE MEANINGLESS JARGON LIKE "BOTTOM-UP"
	 *
	 * @param logOp the logical operator
	 * @return a physical that corresponds exactly to the given logical plan.
	 * In particular, not further optimization is applied to the resulting plan.
	 * It uses hash join as default for equijoins and NestedLoopJoin for 
	 * joins with arbitrary predicates.
	 */
	public static TupleIterator translate(RelationalTerm logOp) {
		Preconditions.checkArgument(logOp != null);
		if (logOp instanceof AccessTerm) {
			Relation r = ((AccessTerm) logOp).getRelation(); 
			AccessMethod b = ((AccessTerm) logOp).getAccessMethod();
			if (r instanceof Relation) {
				r = new InMemoryTableWrapper(r);
			}
			if (b.getNumberOfInputs() == 0) {
				return new Scan((RelationAccessWrapper) r);
			}
			return new Access((RelationAccessWrapper) r, b, ((AccessTerm) logOp).getInputConstants());
		} 
		else if (logOp instanceof ProjectionTerm) {
			TupleIterator translatedChild = translate(logOp.getChild(0));
			Attribute[] translatedOutputAttributes = new Attribute[logOp.getOutputAttributes().length];
			List<Attribute> childsOutputs = Arrays.asList(logOp.getChild(0).getOutputAttributes());
			for (int i = 0; i < translatedOutputAttributes.length; i++) {
				Attribute currentAttribute = logOp.getOutputAttributes()[i];
				translatedOutputAttributes[i] = translatedChild.getOutputAttributes()[childsOutputs.indexOf(currentAttribute)]; 
			}
			
			return new Projection(translatedOutputAttributes, translate(logOp.getChild(0)));
		} 
		else if (logOp instanceof RenameTerm) {
			return translate(logOp.getChild(0));
		} 
		else if (logOp instanceof SelectionTerm) {
			return new Selection(((SelectionTerm) logOp).getSelectionCondition(), translate(logOp.getChild(0)));		
		} 
		else if (logOp instanceof DependentJoinTerm) {
//			try {
//				PlanPrinter.openPngPlan(logOp);
//			} catch (IOException | InterruptedException e) {
//				e.printStackTrace();
//			}
			return new DependentJoin(translate(logOp.getChild(0)), translate(logOp.getChild(1)));
		} 
		else if (logOp instanceof JoinTerm) {
			switch(joinType) {
			case NESTED_LOOP:
				return new NestedLoopJoin(translate(logOp.getChild(0)), translate(logOp.getChild(1)));
			case SYMMETRIC_HASH:
				return new SymmetricMemoryHashJoin(translate(logOp.getChild(0)), translate(logOp.getChild(1)));
			}

		}  
		throw new IllegalArgumentException("Unsupported logical operator " + logOp);
	}
}
