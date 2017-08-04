package uk.ac.ox.cs.pdq.algebra;

import uk.ac.ox.cs.pdq.InterningManager;
import uk.ac.ox.cs.pdq.db.TypedConstant;

/**
 * Creates and maintains a cache of each object type in this package. The
 * purpose of this class is it make sure objects are never duplicated.
 * 
 * @author Gabor
 *
 */
public class Cache {
	protected static InterningManager<AccessTerm> accessTerm = null;
	protected static InterningManager<AttributeEqualityCondition> attributeEqualityCondition = null;
	protected static InterningManager<CartesianProductTerm> cartesianProductTerm = null;
	protected static InterningManager<ConjunctiveCondition> conjunctiveCondition = null;
	protected static InterningManager<ConstantEqualityCondition> constantEqualityCondition = null;
	protected static InterningManager<DependentJoinTerm> dependentJoinTerm = null;
	protected static InterningManager<JoinTerm> joinTerm = null;
	protected static InterningManager<ProjectionTerm> projectionTerm = null;
	protected static InterningManager<RenameTerm> renameTerm = null;
	protected static InterningManager<SelectionTerm> selectionTerm = null;

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
		accessTerm = new InterningManager<AccessTerm>() {
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

		attributeEqualityCondition = new InterningManager<AttributeEqualityCondition>() {
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

		cartesianProductTerm = new InterningManager<CartesianProductTerm>() {
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

		conjunctiveCondition = new InterningManager<ConjunctiveCondition>() {
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
		constantEqualityCondition = new InterningManager<ConstantEqualityCondition>() {
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
		dependentJoinTerm = new InterningManager<DependentJoinTerm>() {
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

		joinTerm = new InterningManager<JoinTerm>() {
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

		projectionTerm = new InterningManager<ProjectionTerm>() {
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

		renameTerm = new InterningManager<RenameTerm>() {
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

		selectionTerm = new InterningManager<SelectionTerm>() {
			protected boolean equal(SelectionTerm object1, SelectionTerm object2) {
				if (!object1.child.equals(object2.child) || !object1.predicate.equals(object2.predicate))
					return false;
				return true;
			}

			protected int getHashCode(SelectionTerm object) {
				int hashCode = object.child.hashCode() + object.predicate.hashCode() * 7;
				return hashCode;
			}
		};

	}
}
