package uk.ac.ox.cs.pdq.plan;

import java.util.Collection;
import java.util.List;

import uk.ac.ox.cs.pdq.algebra.DependentAccess;
import uk.ac.ox.cs.pdq.algebra.Projection;
import uk.ac.ox.cs.pdq.algebra.RelationalOperator;
import uk.ac.ox.cs.pdq.algebra.Selection;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

/**
 * Translates DAG and Linear plans to Normalised plans
 * @author Efthymia Tsamoura
 *
 */
public class ToNormalisedPlanTranslator {
	
	public NormalisedPlan translate(RelationalOperator operator) {
		if(operator instanceof uk.ac.ox.cs.pdq.algebra.Join) {
			//Get the left hand child plan
			NormalisedPlan left = this.translate(((uk.ac.ox.cs.pdq.algebra.Join) operator).getChildren().get(0));
			//Do the right hand child access
			RelationalOperator rightOp = ((uk.ac.ox.cs.pdq.algebra.Join) operator).getChildren().get(1);
			Collection<AccessOperator> rightAccesses = RelationalOperator.getAccesses(rightOp);
			Preconditions.checkArgument(rightAccesses.size()==1);
			AccessOperator rightAccess = rightAccesses.iterator().next();
			//Create an access command
			Command access = new AccessCommand(rightAccess, null);
			//Create selection commands for the right hand child if exist
			Collection<Selection> rightSelections = RelationalOperator.getSelections(rightOp);
			Preconditions.checkArgument(rightSelections.size()==1 || rightSelections.size() == 0);
			Command selection = access;
			if(rightSelections.size()==1) {
				Selection rightSelection = rightSelections.iterator().next();
				selection = new SelectCommand(rightSelection.getPredicate(), access.getOutput());
			}
			//Create the join command
			Command join = new JoinCommand(left.getLast().getOutput(), selection.getOutput(), ((uk.ac.ox.cs.pdq.algebra.Join) operator).getPredicate());
			return new NormalisedPlan(left, join);
		}
		else if(operator instanceof DependentAccess) {
			return new NormalisedPlan(new AccessCommand((AccessOperator) operator, null));
		}
		else if(operator instanceof Selection) {
			NormalisedPlan child = this.translate(((Selection) operator).getChild());
			Command selection = new SelectCommand(((Selection) operator).getPredicate(), child.getFirst().getOutput());
			List<Command> commands = Lists.newArrayList();
			commands.addAll(child.getCommands());
			commands.add(selection);
			return new NormalisedPlan(commands);
		}
		else if(operator instanceof Projection) {
			return this.translate(((Projection) operator).getChild());
		}
		throw new java.lang.IllegalStateException("Unknown operator type");
	}

}
