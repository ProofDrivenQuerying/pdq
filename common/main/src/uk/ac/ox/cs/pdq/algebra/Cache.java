// This file is part of PDQ (https://github.com/michaelbenedikt/pdq) which is released under the MIT license.
// See accompanying LICENSE for copyright notice and full details.

package uk.ac.ox.cs.pdq.algebra;

import uk.ac.ox.cs.pdq.ClassManager;
import uk.ac.ox.cs.pdq.fol.TypedConstant;

/**
 * Creates and maintains a cache of each object type in this package. The
 * purpose of this class is to make sure objects are never duplicated.
 * 
 * @author Gabor
 *
 */
public class Cache {
	protected static ClassManager<AccessTerm> accessTerm = null;
	protected static ClassManager<AttributeEqualityCondition> attributeEqualityCondition = null;
	protected static ClassManager<CartesianProductTerm> cartesianProductTerm = null;
	protected static ClassManager<ConjunctiveCondition> conjunctiveCondition = null;
	protected static ClassManager<ConstantEqualityCondition> constantEqualityCondition = null;
	protected static ClassManager<DependentJoinTerm> dependentJoinTerm = null;
	protected static ClassManager<JoinTerm> joinTerm = null;
	protected static ClassManager<ProjectionTerm> projectionTerm = null;
	protected static ClassManager<RenameTerm> renameTerm = null;
	protected static ClassManager<SelectionTerm> selectionTerm = null;

	static {
		startCaches();
	}

	/**
	 * Needed in case we want to work with multiple schemas. Most commonly in case
	 * of unit testing.
	 */
	public static void reStartCaches() {
		accessTerm = null;
		attributeEqualityCondition = null;
		cartesianProductTerm = null;
		conjunctiveCondition = null;
		constantEqualityCondition = null;
		dependentJoinTerm = null;
		joinTerm = null;
		projectionTerm = null;
		renameTerm = null;
		selectionTerm = null;

		startCaches();
	}

	private static void startCaches() {
		accessTerm = new ClassManager<AccessTerm>() {
			protected boolean equal(AccessTerm object1, AccessTerm object2) {
				if (!object1.relation.equals(object2.relation) || !object1.accessMethod.equals(object2.accessMethod)
						|| object1.inputConstants.size() != object2.inputConstants.size())
					return false;
				for (java.util.Map.Entry<Integer, TypedConstant> entry : object1.inputConstants.entrySet()) {
					if (!object2.inputConstants.containsKey(entry.getKey()) || object2.inputConstants.get(entry.getKey()).equals(entry.getValue()))
						return false;
				}
				return true;
			}

			protected int getHashCode(AccessTerm object) {
				int hashCode = object.relation.hashCode() + object.accessMethod.hashCode() * 7;
				for (java.util.Map.Entry<Integer, TypedConstant> entry : object.inputConstants.entrySet())
					hashCode = hashCode * 8 + entry.getKey().hashCode() * 9 + entry.getValue().hashCode() * 10;
				return hashCode;
			}
		};

		attributeEqualityCondition = new ClassManager<AttributeEqualityCondition>() {
			protected boolean equal(AttributeEqualityCondition object1, AttributeEqualityCondition object2) {
				if (object1.position != object2.position || object1.other != object2.other)
					return false;
				return true;
			}

			protected int getHashCode(AttributeEqualityCondition object) {
				int hashCode = object.position.hashCode() + object.other.hashCode() * 7;
				return hashCode;
			}
		};

		cartesianProductTerm = new ClassManager<CartesianProductTerm>() {
			protected boolean equal(CartesianProductTerm object1, CartesianProductTerm object2) {
				for (int index = 1; index >= 0; --index)
					if (!object1.children[index].equals(object2.children[index]))
						return false;
				return true;
			}

			protected int getHashCode(CartesianProductTerm object) {
				int hashCode = 0;
				for (int index = 1; index >= 0; --index)
					hashCode = hashCode * 7 + object.children[index].hashCode();
				return hashCode;
			}
		};

		conjunctiveCondition = new ClassManager<ConjunctiveCondition>() {
			protected boolean equal(ConjunctiveCondition object1, ConjunctiveCondition object2) {
				if (object1.predicates.length != object2.predicates.length)
					return false;
				for (int index = object1.predicates.length - 1; index >= 0; --index)
					if (!object1.predicates[index].equals(object2.predicates[index]))
						return false;
				return true;
			}

			protected int getHashCode(ConjunctiveCondition object) {
				int hashCode = 0;
				for (int index = object.predicates.length - 1; index >= 0; --index)
					hashCode = hashCode * 7 + object.predicates[index].hashCode();
				return hashCode;
			}
		};
		constantEqualityCondition = new ClassManager<ConstantEqualityCondition>() {
			protected boolean equal(ConstantEqualityCondition object1, ConstantEqualityCondition object2) {
				if (object1.position != object2.position || !object1.constant.equals(object1.constant))
					return false;
				return true;
			}

			protected int getHashCode(ConstantEqualityCondition object) {
				int hashCode = object.position.hashCode() + object.constant.hashCode() * 7;
				return hashCode;
			}
		};
		dependentJoinTerm = new ClassManager<DependentJoinTerm>() {
			protected boolean equal(DependentJoinTerm object1, DependentJoinTerm object2) {
				for (int index = 1; index >= 0; --index)
					if (!object1.children[index].equals(object2.children[index]))
						return false;
				return true;
			}

			protected int getHashCode(DependentJoinTerm object) {
				int hashCode = 0;
				for (int index = 1; index >= 0; --index)
					hashCode = hashCode * 7 + object.children[index].hashCode();
				return hashCode;
			}
		};

		joinTerm = new ClassManager<JoinTerm>() {
			protected boolean equal(JoinTerm object1, JoinTerm object2) {
				for (int index = 1; index >= 0; --index)
					if (!object1.children[index].equals(object2.children[index]))
						return false;
				return true;
			}

			protected int getHashCode(JoinTerm object) {
				int hashCode = 0;
				for (int index = 1; index >= 0; --index)
					hashCode = hashCode * 7 + object.children[index].hashCode();
				return hashCode;
			}
		};

		projectionTerm = new ClassManager<ProjectionTerm>() {
			protected boolean equal(ProjectionTerm object1, ProjectionTerm object2) {
				if (!object1.child.equals(object2.child) || object1.projections.length != object2.projections.length)
					return false;
				for (int index = object1.projections.length - 1; index >= 0; --index)
					if (!object1.projections[index].equals(object2.projections[index]))
						return false;
				return true;
			}

			protected int getHashCode(ProjectionTerm object) {
				int hashCode = object.child.hashCode();
				for (int index = object.projections.length - 1; index >= 0; --index)
					hashCode = hashCode * 7 + object.projections[index].hashCode();
				return hashCode;
			}
		};

		renameTerm = new ClassManager<RenameTerm>() {
			protected boolean equal(RenameTerm object1, RenameTerm object2) {
				if (!object1.child.equals(object2.child) || object1.renamings.length != object2.renamings.length)
					return false;
				for (int index = object1.renamings.length - 1; index >= 0; --index)
					if (!object1.renamings[index].equals(object2.renamings[index]))
						return false;
				return true;
			}

			protected int getHashCode(RenameTerm object) {
				int hashCode = object.child.hashCode();
				for (int index = object.renamings.length - 1; index >= 0; --index)
					hashCode = hashCode * 7 + object.renamings[index].hashCode();
				return hashCode;
			}
		};

		selectionTerm = new ClassManager<SelectionTerm>() {
			protected boolean equal(SelectionTerm object1, SelectionTerm object2) {
				if (!object1.child.equals(object2.child) || !object1.selectionCondition.equals(object2.selectionCondition))
					return false;
				return true;
			}

			protected int getHashCode(SelectionTerm object) {
				int hashCode = object.child.hashCode() + object.selectionCondition.hashCode() * 7;
				return hashCode;
			}
		};

	}
}
