// This file is part of PDQ (https://github.com/ProofDrivenQuerying/pdq) which is released under the MIT license.
// See accompanying LICENSE for copyright notice and full details.

package uk.ac.ox.cs.pdq.runtime.exec.spliterator;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Spliterator;
import java.util.function.Consumer;

import com.google.common.base.Preconditions;

import uk.ac.ox.cs.pdq.algebra.AccessTerm;
import uk.ac.ox.cs.pdq.algebra.Plan;
import uk.ac.ox.cs.pdq.datasources.ExecutableAccessMethod;
import uk.ac.ox.cs.pdq.db.AccessMethodDescriptor;
import uk.ac.ox.cs.pdq.db.Attribute;
import uk.ac.ox.cs.pdq.db.Relation;
import uk.ac.ox.cs.pdq.db.tuple.Tuple;
import uk.ac.ox.cs.pdq.db.tuple.TupleType;
import uk.ac.ox.cs.pdq.fol.TypedConstant;
import uk.ac.ox.cs.pdq.runtime.exec.PlanDecorator;

/**
 * An executable access plan. 
 * 
 * Note that the child field in an {@code Access} instance is not used, but 
 * instead has an underlying {@code AccessMethod}. 
 * 
 * @author Tim Hobson
 *
 */
public class Access extends UnaryExecutablePlan {

	// Dynamic input.
	protected Iterator<Tuple> inputTuples;

	public Access(Plan plan, PlanDecorator decorator) {
		super(plan,decorator);
		// Check compatibility with the given Plan instance.
	//	Preconditions.checkArgument(plan instanceof AccessTerm);
	}

	private AccessTerm getDecoratedAccess() {
		if (super.getDecoratedPlan() instanceof AccessTerm) {
			return (AccessTerm) this.getDecoratedPlan();
		} 
		return (AccessTerm) this.getDecoratedPlan().getChild(0);
	}
	
	@Override
	public UnaryPlanSpliterator spliterator() {
		AccessTerm accessTerm = (AccessTerm) this.getDecoratedAccess();

		Spliterator<Tuple> underlying = null;
		ExecutableAccessMethod aam = (ExecutableAccessMethod) accessTerm.getAccessMethod(); 
		// Case 1: the underlying access method has no input attributes.
		if (aam.inputAttributes().length == 0)
			underlying = aam.access().spliterator();

		// Case 2: the access method has input attributes but the AccessTerm does not 
		// (i.e. if all of the access method inputs are supplied by input constants).
		if (aam.inputAttributes().length != 0 && 
				accessTerm.getInputAttributes().length == 0)
			underlying = aam.access(this.constantInput()).spliterator();

		// Case 3: the AccessTerm has inputs.
		if (accessTerm.getInputAttributes().length != 0) {
			Preconditions.checkState(this.inputTuples != null && this.inputTuples.hasNext(), 
					"Missing dynamic input accessing relation: " + ((AccessTerm) this.getDecoratedAccess()).getRelation().getName());
			underlying = aam.access(this.combineInputs()).spliterator();
		}
		
		return new AccessSpliterator(underlying);
	}

	@Override
	public void setInputTuples(Iterator<Tuple> inputTuples) {
		this.inputTuples = inputTuples;
	}

	Relation getRelation() {
		return ((AccessTerm) this.getDecoratedAccess()).getRelation();
	}

	@Override
	public void close() {
		this.inputTuples = null;
		// TODO: the AccessTerm's AccessMethod does the actual I/O. Should that be closed here?  
	}
	
	/*
	 * Converts the inputConstants map into an iterator over a single tuple, 
	 * for use in the case where there is no dynamic input.
	 */
	private Iterator<Tuple> constantInput() {
		AccessTerm accessTerm = (AccessTerm) this.getDecoratedAccess();
		Preconditions.checkState(accessTerm.getInputAttributes().length == 0);
		Preconditions.checkState(accessTerm.getInputConstants().size() != 0);

		Attribute[] inputAttributes = ((ExecutableAccessMethod)accessTerm.getAccessMethod()).inputAttributes();
		List<Tuple> constantInput = new ArrayList<Tuple>();
		TupleType tt = TupleType.createFromTyped(inputAttributes);
		Object[] values = new Object[tt.size()];

		// Since the AccessTerm has no input attributes, the input constants must provide
		// all inputs to the underlying AccessMethod.
		for (int i = 0; i != inputAttributes.length; i++)
			values[i] = accessTerm.getInputConstantsAsAttributes().get(inputAttributes[i]).getValue();
		constantInput.add(tt.createTuple(values));
		return constantInput.iterator();
	}

	/*
	 * Takes the inputTuples iterator and combines with the input constants.
	 */
	private Iterator<Tuple> combineInputs() {

		AccessTerm accessTerm = (AccessTerm) this.getDecoratedAccess();
		if (accessTerm.getInputConstants().size() == 0)
			return this.inputTuples;
		return new CombinedInputsIterator();
	}

	private class CombinedInputsIterator implements Iterator<Tuple> {
		AccessMethodDescriptor am = ((AccessTerm) getDecoratedAccess()).getAccessMethod();
		Attribute[] allInputAttributes = ((ExecutableAccessMethod)am).inputAttributes(true);
		Map<Attribute, TypedConstant> inputConstants = ((AccessTerm) getDecoratedAccess()).getInputConstantsAsAttributes();
		TupleType tt = TupleType.createFromTyped(allInputAttributes);

		@Override
		public boolean hasNext() {
			return inputTuples.hasNext();
		}

		@Override
		public Tuple next() {
			// Combine the input constants with the dynamic input tuple.
			Tuple dynamicInput = inputTuples.next();
			Object[] values = new Object[tt.size()];
			int dynamicCount = 0;
			for (int i = 0; i != this.allInputAttributes.length; i++) {
				values[i] = inputConstants.containsKey(allInputAttributes[i]) ? 
						inputConstants.get(allInputAttributes[i]).getValue() : 
							dynamicInput.getValue(dynamicCount++);
			}
			return tt.createTuple(values);
		}
	}

	private class AccessSpliterator extends UnaryPlanSpliterator {

		public AccessSpliterator(Spliterator<Tuple> childSpliterator) {
			super(childSpliterator);
		}

		@Override
		public boolean tryAdvance(Consumer<? super Tuple> action) {
			return childSpliterator.tryAdvance(action);
		}
	}

}
