package uk.ac.ox.cs.pdq.plan;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

import uk.ac.ox.cs.pdq.algebra.predicates.AttributeEqualityPredicate;
import uk.ac.ox.cs.pdq.algebra.predicates.ConjunctivePredicate;
import uk.ac.ox.cs.pdq.algebra.predicates.ConstantEqualityPredicate;
import uk.ac.ox.cs.pdq.algebra.predicates.Predicate;
import uk.ac.ox.cs.pdq.db.Attribute;
import uk.ac.ox.cs.pdq.db.TGD;
import uk.ac.ox.cs.pdq.db.TypedConstant;
import uk.ac.ox.cs.pdq.util.Table;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

/**
 * 
 * @author Efthymia Tsamoura
 *
 */
public class Select implements Command{

	private final Table input;
	
	private final Table output;
	
	private final Predicate predicates;
	
	/** Caches the constraint that captures this access command **/
	private final TGD command;
	
	public Select(Predicate predicates, Table input) {
		Preconditions.checkNotNull(predicates);
		Preconditions.checkNotNull(input);
		this.input = input;
		this.predicates = predicates;
		
		/** 
		 * Attributes to be projected out
		 * we project out from the contraint's body all the attributes that should be equal to constants 
		 */
		List<Attribute> toEliminate = Lists.newArrayList();
		//Project out from the output table the attributes that should be equal to constants
		if(predicates instanceof ConjunctivePredicate) {
			Iterator<Predicate> iterator = ((ConjunctivePredicate) predicates).iterator();
			while(iterator.hasNext()) {
				Predicate p = iterator.next();
				if(p instanceof ConstantEqualityPredicate) {
					int position = ((ConstantEqualityPredicate) p).getPosition();
					toEliminate.add((Attribute) input.getHeader().get(position));
				}
			}
		}
		
		List<Attribute> outputs = Lists.newArrayList();
		outputs.addAll((Collection<? extends Attribute>) input.getHeader());
		outputs.removeAll(toEliminate);
		this.output = new Table(outputs);		
		this.command = new CommandToTGDTranslator().toTGD(this);
	}

	@Override
	public Table getOutput() {
		return this.output;
	}

	public Table getInput() {
		return input;
	}

	public Predicate getPredicates() {
		return predicates;
	}
	
	/**
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
		return Select.class.isInstance(o)
				&& this.predicates.equals(((Select) o).predicates)
				&& this.input.equals(((Select) o).input);
	}

	/**
	 * @return int
	 */
	@Override
	public int hashCode() {
		return Objects.hash(this.predicates, this.input);
	}

	/**
	 * @return String
	 */
	@Override
	public String toString() {
		return this.command.toString();
	}
	
}
