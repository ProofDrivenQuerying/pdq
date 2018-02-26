package uk.ac.ox.cs.pdq.algebra;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.tuple.Pair;

import uk.ac.ox.cs.pdq.db.Attribute;
import uk.ac.ox.cs.pdq.fol.Atom;
import uk.ac.ox.cs.pdq.fol.Conjunction;
import uk.ac.ox.cs.pdq.fol.Constant;
import uk.ac.ox.cs.pdq.fol.Formula;
import uk.ac.ox.cs.pdq.fol.Term;
import uk.ac.ox.cs.pdq.fol.Variable;
import uk.ac.ox.cs.pdq.util.GlobalCounterProvider;

/**
 * Converts a plan given as a RelationalTerm object to Logic objects.   
 * @author gabor
 *
 */
public class RelationalTermToLogicConverter {

	private RelationalTerm plan;
	private Formula formula;
	private Variable[] freeVariables;
	private Map<Attribute, Variable> mapping;

	/** Converts the plan to a Logic objects such as Atom or Conjunction. Also returns the final attribute - variable mapping, and the list of free variables.
	 * @param plan
	 * @return
	 */
	public RelationalTermToLogicConverter(RelationalTerm plan) {
		this.plan = plan;
		toLogic();
		
	}
	
	/**
	 * Converts to logic
	 */
	private void toLogic() {
		RelationalTermDescriptor td = new RelationalTermDescriptor(plan);
		Collection<Atom> queryAtoms = td.getQueryAtoms();
		this.freeVariables = td.calculateFreeVariables();
		if (queryAtoms.size() == 1)
			this.formula = queryAtoms.iterator().next();
		else
			this.formula = (Conjunction) Conjunction.of(queryAtoms.toArray(new Atom[queryAtoms.size()]));
		mapping = td.getFinalAttributeToVariableMapping();
	}
	
	/** The result conjunction or atom that represents the plan.
	 * @return
	 */
	public Formula getFormula() {
		return this.formula;
	}
	
	/** Free variables of the converted plan
	 * @return
	 */
	public Variable[] getFreeVariables() {
		return freeVariables;
	}
	
	/**Attribute to variable mapping of the converted plan.
	 * @return
	 */
	public Map<Attribute,Variable> getMapping() {
		return this.mapping;
	}
	
	/**
	 * Describes a RelationalTerm to be able to easily convert into a
	 * ConjunctiveQuery.
	 * 
	 * @author Gabor
	 *
	 */
	public class RelationalTermDescriptor {
		private RelationalTerm term;
		private List<RelationalTermDescriptor> children;
		private Atom queryAtom;
		private Map<Attribute, List<Pair<RelationalTermDescriptor, Integer>>> attributeMap = new HashMap<>();
		private RelationalTermDescriptor root = null;

		/**
		 * Loops over the given relational term creating a descriptor object for each
		 * term, linking them, so eventually creates the same tree. When a relational
		 * term is an AccessTerm it creates a queryAtom representing it, initialised
		 * with unique variables. For RenameTerms it caches the renaming attributes into
		 * an attributeMap.
		 * 
		 * @param rt
		 */
		public RelationalTermDescriptor(RelationalTerm rt) {
			this(rt, null);
			processQueryAtoms();
		}

		public Map<Attribute, Variable> getFinalAttributeToVariableMapping() {
			Map<Attribute, Variable> results = new HashMap<Attribute, Variable>();
			if (this.term instanceof AccessTerm) {
				for (int index = 0; index < this.term.getOutputAttributes().length; index++) {
					if (this.queryAtom.getTerm(index).isVariable())
						results.put(this.term.getOutputAttributes()[index], (Variable)this.queryAtom.getTerm(index));
				}
			}
			for (RelationalTermDescriptor child: children) {
				results.putAll(child.getFinalAttributeToVariableMapping());
			}
			return results;
		}

		/**
		 * This constructor is called by the root to create descriptors for the
		 * children.
		 * 
		 * @param rt
		 * @param root
		 */
		private RelationalTermDescriptor(RelationalTerm rt, RelationalTermDescriptor root) {
			this.term = rt;
			if (root == null)
				root = this;
			this.root = root;
			this.children = new ArrayList<RelationalTermDescriptor>();
			for (RelationalTerm child : rt.getChildren()) {
				children.add(new RelationalTermDescriptor(child, root));
			}

			// map output attributes
			if (this.term instanceof RenameTerm || this.term instanceof AccessTerm) {
				int index = 0;
				RelationalTermDescriptor toAdd = this;
				if (this.term instanceof RenameTerm)
					toAdd = children.get(0);
				for (Attribute output : this.term.getOutputAttributes()) {
					if (root.attributeMap.containsKey(output)) {
						Pair<RelationalTermDescriptor, Integer> pair = Pair.of(toAdd, index++);
						if (!root.attributeMap.get(output).contains(pair))
							root.attributeMap.get(output).add(pair);
					} else {
						List<Pair<RelationalTermDescriptor, Integer>> l = new ArrayList<>();
						l.add(Pair.of(toAdd, index++));
						root.attributeMap.put(output, l);
					}
				}
			}

			// create Atoms based on accesses
			if (this.term instanceof AccessTerm) {
				List<Term> outputVariables = new ArrayList<>();
				AccessTerm access = (AccessTerm) term;
				for (int i = 0; i < term.getNumberOfOutputAttributes(); i++) {
					if (access.getInputConstants().containsKey(i)) {
						outputVariables.add(access.getInputConstants().get(i));
					} else {
						outputVariables.add(Variable.create("v" + GlobalCounterProvider.getNext("VariableName")));
					}
				}
				this.queryAtom = Atom.create(access.getRelation(), outputVariables.toArray(new Term[outputVariables.size()]));
			}
		}

		/**
		 * Recursively collects the query atoms.
		 * 
		 * @return
		 */
		public List<Atom> getQueryAtoms() {
			List<Atom> ret = new ArrayList<>();
			if (this.queryAtom != null)
				ret.add(this.queryAtom);
			for (RelationalTermDescriptor child : this.children) {
				ret.addAll(child.getQueryAtoms());
			}
			return ret;
		}

		public Atom getQueryAtom() {
			return queryAtom;
		}

		/**
		 * Goes over the tree processes all relational terms to be able to generate the
		 * query atoms.
		 */
		private void processQueryAtoms() {
			for (RelationalTermDescriptor child : this.children)
				child.processQueryAtoms();

			Condition c = null;
			if (this.term instanceof SelectionTerm) {
				c = ((SelectionTerm) this.term).getSelectionCondition();
			} else if (this.term instanceof DependentJoinTerm) {
				c = ((DependentJoinTerm) this.term).getJoinConditions();
			} else if (this.term instanceof JoinTerm) {
				c = ((JoinTerm) this.term).getJoinConditions();
			} else if (this.term instanceof CartesianProductTerm) {
				return;
			}
			if (c == null)
				return;
			List<SimpleCondition> selectionCondition = new ArrayList<>();
			if (c instanceof ConjunctiveCondition) {
				selectionCondition.addAll(Arrays.asList(((ConjunctiveCondition) c).getSimpleConditions()));
			} else {
				selectionCondition.add((SimpleCondition) c);
			}
			for (SimpleCondition sc : selectionCondition) {
				if (sc instanceof AttributeEqualityCondition) {
					applyAttributeEquality((AttributeEqualityCondition) sc);
				} else {
					applyConstantEquality((ConstantEqualityCondition) sc);
				}
			}
			return;
		}

		/**
		 * Finds the two variable that should be equal and chooses one of them to
		 * replace with the other. Makes sure that if one of the terms is a Constant it
		 * will replace the other term with it. (so constants never replaced).
		 * 
		 * @param sc
		 */
		private void applyAttributeEquality(AttributeEqualityCondition sc) {
			Attribute attrLeft = this.term.getOutputAttribute(sc.getPosition());
			Attribute attrRight = this.term.getOutputAttribute(sc.getOther());
			if (!attrLeft.equals(attrRight)) {
				throw new RuntimeException("This two should be equal");
			}
			Set<Term> toReplace = new HashSet<>();
			Set<Term> constants = new HashSet<>();
			for (Pair<RelationalTermDescriptor, Integer> matches : root.attributeMap.get(attrLeft)) {
				RelationalTermDescriptor descriptor = matches.getLeft();
				int index = matches.getRight();
				Term t = descriptor.queryAtom.getTerm(index);
				if (t instanceof Constant) {
					constants.add(t);
				}
				if (!toReplace.contains(t)) {
					toReplace.add(t);
				}
			}
			if (constants.size() > 1)
				throw new RuntimeException("Two constants cannot be replaced with each other!");
			Term toKeep = null;
			if (constants.size() == 1)
				toKeep = constants.iterator().next();
			else
				toKeep = toReplace.iterator().next();
			if (toReplace.size() < 2) {
				throw new RuntimeException("At least two matches needed to replace!");
			}
			for (Term t : toReplace) {
				if (!t.equals(toKeep))
					replaceAll(t, toKeep);
			}
		}

		/**
		 * Replaces a variable with a constant in the query atom.
		 * 
		 * @param sc
		 */
		private void applyConstantEquality(ConstantEqualityCondition sc) {
			Attribute attr = this.term.getOutputAttribute(sc.getPosition());
			Set<Term> toReplace = new HashSet<>();
			for (Pair<RelationalTermDescriptor, Integer> matches : root.attributeMap.get(attr)) {
				RelationalTermDescriptor descriptor = matches.getLeft();
				int index = matches.getRight();
				Term t = descriptor.queryAtom.getTerm(index);
				if (!toReplace.contains(t)) {
					toReplace.add(t);
					replaceAll(t, sc.getConstant());
				}

			}
		}

		/**
		 * Replaces all occurrences of a term in this Relationalterm and all of it's
		 * children with an other term.
		 * 
		 * @param toReplace
		 * @param toReplaceWith
		 */
		private void replaceAll(Term toReplace, Term toReplaceWith) {
			if (this.queryAtom != null) {
				List<Term> terms = new ArrayList<>();
				terms.addAll(Arrays.asList(this.queryAtom.getTerms()));
				if (terms.contains(toReplace)) {
					int index = terms.indexOf(toReplace);
					terms.remove(index);
					terms.add(index, toReplaceWith);

					this.queryAtom = Atom.create(this.queryAtom.getPredicate(), terms.toArray(new Term[terms.size()]));
				}
			}
			for (RelationalTermDescriptor child : children) {
				child.replaceAll(toReplace, toReplaceWith);
			}
		}

		/**
		 * Converts the output attributes of this term into a variable array.
		 * 
		 * @return
		 */
		public Variable[] calculateFreeVariables() {
			List<Variable> variables = new ArrayList<>();
			for (Attribute output : this.term.getOutputAttributes()) {
				Term outputTerm = attributeMap.get(output).get(0).getLeft().getQueryAtom().getTerm(attributeMap.get(output).get(0).getRight());
				if (outputTerm.isVariable()) {
					variables.add((Variable) outputTerm);
				} else {
					// throw new RuntimeException("Output shouldn't be constant");
				}
			}
			return variables.toArray(new Variable[variables.size()]);
		}
	}	
	
}
