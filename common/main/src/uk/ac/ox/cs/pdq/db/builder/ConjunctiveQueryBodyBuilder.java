package uk.ac.ox.cs.pdq.db.builder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import uk.ac.ox.cs.pdq.db.Attribute;
import uk.ac.ox.cs.pdq.db.Relation;
import uk.ac.ox.cs.pdq.db.Schema;
import uk.ac.ox.cs.pdq.db.TypedConstant;
import uk.ac.ox.cs.pdq.fol.Conjunction;
import uk.ac.ox.cs.pdq.fol.ConjunctiveQuery;
import uk.ac.ox.cs.pdq.fol.Constant;
import uk.ac.ox.cs.pdq.fol.Atom;
import uk.ac.ox.cs.pdq.fol.Formula;
import uk.ac.ox.cs.pdq.fol.Predicate;
import uk.ac.ox.cs.pdq.fol.Term;
import uk.ac.ox.cs.pdq.fol.Variable;
import uk.ac.ox.cs.pdq.util.Types;

/**
 * TOCOMMENT this class seems to be only used in DEMO -- not sure what it does instead of tagging every method I write this here.
 * The Class ConjunctiveQueryBodyBuilder.
 *
 * @author Gonzalo Diaz
 */
public class ConjunctiveQueryBodyBuilder {
	
	/** The log. */
	private static Logger log = Logger.getLogger(ConjunctiveQueryBodyBuilder.class);

	/** 
	 * TOCOMMENT not sure where this is used
	 * The alias to predicate formulas. */
	private Map<String, Atom> aliasToAtoms = new HashMap<>();

	/** The schema. */
	private Schema              schema;
	
	/** 
	 * TOCOMMENT not sure where this is used
	 * The alias to relations. */
	private Map<String, String> aliasToRelations;
	
	/** The result predicate. */
	private Atom    resultPredicate;
	
	/** The query's name. */
	private String              qName;
	
	/** 
	 * TOCOMMENT Does this builder build only one query?
	 * The conjunctive query built. */
	private ConjunctiveQuery    conjQuery; 
	
	/** The returns all vars. */
	private boolean             returnsAllVars;
	

	/**
	 * Constructor for ConjunctiveQueryBodyBuilder.
	 * @param schema Schema
	 * @param aliasToRelations Map<String,String>
	 */
	public ConjunctiveQueryBodyBuilder(Schema schema, Map<String, String> aliasToRelations) {
		this.schema = schema;
		this.aliasToRelations = aliasToRelations;
		
		this.qName = "Q";
		
		this.returnsAllVars = false;
		
		this.buildInitialPredicates();
	}

	/**
	 * Builds an initial set of Atoms with fresh variables in every position.
	 */
	private void buildInitialPredicates() {
		log.debug("buildInitialPredicates. aliasToRelations = " + this.aliasToRelations);
		
		int counter = 0;
		for( Map.Entry<String, String> entry : this.aliasToRelations.entrySet() ) {
			String aliasName = entry.getKey();
			String relationName = entry.getValue();

			Relation relation = this.schema.getRelation(relationName);
			List<Attribute> attributes = relation.getAttributes();

			Term[] terms = new Term[ attributes.size() ];

			for( int i = 0; i < terms.length; i++ ) {
				terms[i] = new Variable(  "x_" + attributes.get(i).getName()  + "_" + counter );
			}

			this.aliasToAtoms.put(aliasName, new Atom( relation, terms ));

			counter++;
		}
	}

	/**
	 * TOCOMMENT I am not sure what the following methods do
	 * Adds the constraint.
	 *
	 * @param left ConstraintTerm
	 * @param right ConstraintTerm
	 * @throws Exception the exception
	 */
	public void addConstraint(ConstraintTerm left, ConstraintTerm right) throws Exception {

		if( left.isConstant() && right.isConstant() ) {
			ConstantConstraintTerm leftConst = (ConstantConstraintTerm) left;
			ConstantConstraintTerm rightConst = (ConstantConstraintTerm) right;

			if( !leftConst.getConstant().equals(rightConst.getConstant()) ) {
				throw new Exception("conflicting constants");
			}
		} else if( left.isConstant() && right.isAliasAttr() ) {

			this._addConstraint( (ConstantConstraintTerm) left, (AliasAttrConstraintTerm) right );

		} else if( left.isAliasAttr() && right.isConstant() ) {

			this._addConstraint((ConstantConstraintTerm) right, (AliasAttrConstraintTerm) left);

		} else if( left.isAliasAttr() && right.isAliasAttr() ) {

			this._addConstraint( (AliasAttrConstraintTerm) left, (AliasAttrConstraintTerm) right);

		} else {
			throw new Exception("internal error");
		}

	}

	/**
	 * _add constraint.
	 *
	 * @param leftConst ConstantConstraintTerm
	 * @param rightAliasAttr AliasAttrConstraintTerm
	 * @throws Exception the exception
	 */
	private void _addConstraint(ConstantConstraintTerm leftConst, AliasAttrConstraintTerm rightAliasAttr) throws Exception {

		// Prepare right variable:
		String rightAlias = rightAliasAttr.getAlias();
		String rightAttr  = rightAliasAttr.getAttr();
		Atom rightPredForm = this.aliasToAtoms.get(rightAlias);

		// Prepare left constant term:
		TypedConstant<?> leftConstant = new TypedConstant<>(
				Types.cast(((Relation) rightPredForm.getPredicate()).getAttribute(rightAttr).getType(),
						leftConst.getConstant()));


		// Get term in said position:
		int rightAttrIndex = this.schema.getRelation( this.aliasToRelations.get(rightAlias) ).getAttributeIndex(rightAttr);
		Term rightTerm = rightPredForm.getTerm(rightAttrIndex);

		if( rightTerm.isVariable() ) {
			// right has a variable
			this.replaceTerm(rightTerm, leftConstant);
		} else {
			// right has a constant
			Constant rightConstant = (Constant) rightTerm;
			if( !rightConstant.equals(leftConstant)) {
				throw new Exception("conflicting constants");
			}
		}
	}

	/**
	 * _add constraint.
	 *
	 * @param leftAliasAttr AliasAttrConstraintTerm
	 * @param rightAliasAttr AliasAttrConstraintTerm
	 * @throws Exception the exception
	 */
	private void _addConstraint(AliasAttrConstraintTerm leftAliasAttr, AliasAttrConstraintTerm rightAliasAttr) throws Exception {

		// Prepare left variable:
		String leftAlias = leftAliasAttr.getAlias();
		String leftAttr  = leftAliasAttr.getAttr();
		Atom leftPredForm = this.aliasToAtoms.get(leftAlias);

		// Get term in said position:
		int leftAttrIndex = this.schema.getRelation( this.aliasToRelations.get(leftAlias) ).getAttributeIndex(leftAttr);
		Term leftTerm = leftPredForm.getTerm(leftAttrIndex);

		// Prepare right variable:
		String rightAlias = rightAliasAttr.getAlias();
		String rightAttr  = rightAliasAttr.getAttr();
		Atom rightPredForm = this.aliasToAtoms.get(rightAlias);

		// Get term in said position:
		int rightAttrIndex = -1;
		
		Term rightTerm = null;
		try {
			rightAttrIndex = this.schema.getRelation( this.aliasToRelations.get(rightAlias) ).getAttributeIndex(rightAttr);
			rightTerm = rightPredForm.getTerm(rightAttrIndex);
		} catch( NullPointerException e ) {
			log.error("null pointer. rightAttr=" + rightAttr
					+ ", rightAlias=" + rightAlias
					+ ", rightAttrIndex=" + rightAttrIndex
					+ ", relationName=" + this.aliasToRelations.get(rightAlias)
					+ ", rightPredForm=" + rightPredForm
			, e);
			throw e;
		}

		if( !leftTerm.isVariable() && !rightTerm.isVariable() ) {
			if( !leftTerm.equals(rightTerm) ) {
				throw new Exception("conflicting constants");
			}

		} else if( !leftTerm.isVariable() && rightTerm.isVariable() ) {

			this.replaceTerm( rightTerm, leftTerm );

		} else if( leftTerm.isVariable() && !rightTerm.isVariable() ) {

			this.replaceTerm( leftTerm, rightTerm );

		} else if( leftTerm.isVariable() && rightTerm.isVariable() ) {

			// make a new term
			Variable newTerm = Variable.getFreshVariable();

			// replace everyone:
			this.replaceTerm( leftTerm,  newTerm);
			this.replaceTerm( rightTerm, newTerm);

		}

	}

	/**
	 * Replace a term for a constant in all atoms.
	 *
	 * @param oldTerm Term
	 * @param newTerm Term
	 */
	private void replaceTerm(Term oldTerm, Term newTerm) {

		for( Map.Entry<String, Atom> entry : this.aliasToAtoms.entrySet() ) {
			String alias = entry.getKey();
			Atom atom = entry.getValue();

			List<Term> terms = atom.getTerms();

			Term[] newTerms = new Term[terms.size()];

			for(int i = 0; i < newTerms.length; i++) {
				if( terms.get(i).equals( oldTerm ) ) {
					newTerms[i] = newTerm;
				} else {
					newTerms[i] = terms.get(i);
				}
			}

			Atom newAtom = new Atom(atom.getPredicate(), newTerms);

			this.aliasToAtoms.put(alias, newAtom);
		}

	}
	
	/**
	 * TOCOMMENT
	 * Return all vars of what?.
	 */
	public void returnAllVars() {
		this.returnsAllVars = true;
		
		// Find arity:
		Set<Variable> vars = new HashSet<>();
		for( Atom predFormula : this.aliasToAtoms.values() ) {
			vars.addAll( predFormula.getVariables() );
		}
		
		Predicate predicate = new Predicate(this.qName, vars.size());
		
		this.resultPredicate = new Atom(predicate, vars);
	}
	
	/**
	 * Adds the result column.
	 *
	 * @param aliasName String
	 * @param attrName String
	 */
	public void addResultColumn(String aliasName, String attrName) {
		if( this.returnsAllVars) {
			throw new RuntimeException("cannot add result column to this query!");
		}
		
		if( this.resultPredicate == null ) {
			
			Predicate predicate = new Predicate(this.qName, 1);
			Term term = this._findTerm(aliasName, attrName);
			this.resultPredicate = new Atom(predicate, term);
			
		} else {
			int newArity = this.resultPredicate.getPredicate().getArity() + 1;
			Predicate newSignature = new Predicate(this.qName, newArity);
			
			Term[] newTerms = new Term[newArity];
			
			for(int i = 0; i < newTerms.length-1; i++) {
				newTerms[i] = this.resultPredicate.getTerm(i);
			}
			newTerms[newTerms.length-1] = this._findTerm(aliasName, attrName);
			
			Atom newPredForm = new Atom(newSignature, newTerms);
			
			this.resultPredicate = newPredForm;
		}
		
	}
	
	/**
	 * _find term.
	 *
	 * @param aliasName String
	 * @param attrName String
	 * @return Term
	 */
	private Term _findTerm(String aliasName, String attrName) {
		Atom predFormula = this.aliasToAtoms.get(aliasName);

		// Get term in said position:
		int attrIndex = this.schema.getRelation( this.aliasToRelations.get(aliasName) ).getAttributeIndex(attrName);
		return predFormula.getTerm(attrIndex);
	}
		
	/**
	 * To conjunctive query.
	 *
	 * @return ConjunctiveQuery
	 */
	public ConjunctiveQuery toConjunctiveQuery() {
		List<Formula> preds = new ArrayList<>();
		preds.addAll(this.aliasToAtoms.values());
		this.conjQuery = new ConjunctiveQuery(this.resultPredicate.getVariables(), Conjunction.of(preds));
		return this.conjQuery;
	}

	/**
	 * To string.
	 *
	 * @return String
	 */
	@Override
	public String toString() {
		StringBuilder ans = new StringBuilder();
		
		ans.append("<StartQueryBuilder: ");
		for( Atom atom : this.aliasToAtoms.values() ) {
			ans.append(atom.toString()).append(", ");
		}
		ans.append(":EndQueryBuilder>");
		
		return ans.toString();
	}


	/**
	 * The Class ConstraintTerm.
	 */
	public static abstract class ConstraintTerm {
		
		/**
		 * Checks if is constant.
		 *
		 * @return boolean
		 */
		public abstract boolean isConstant();

		/**
		 * Checks if is alias attr.
		 *
		 * @return boolean
		 */
		public abstract boolean isAliasAttr();
	}

	/**
	 * The Class ConstantConstraintTerm.
	 */
	public static class ConstantConstraintTerm extends ConstraintTerm {
		
		/** The constant. */
		private final String constant;
		/**
		 * Constructor for ConstantConstraintTerm.
		 * @param constant String
		 */
		public ConstantConstraintTerm(String constant) {
			this.constant = constant;
		}
		
		/**
		 * Gets the constant.
		 *
		 * @return String
		 */
		public String getConstant() {
			return this.constant;
		}
		
		/**
		 * Checks if is constant.
		 *
		 * @return boolean
		 */
		@Override public boolean isConstant() { return true; }
		
		/**
		 * Checks if is alias attr.
		 *
		 * @return boolean
		 */
		@Override public boolean isAliasAttr() { return false; }
	}

	/**
	 * The Class AliasAttrConstraintTerm.
	 */
	public static class AliasAttrConstraintTerm extends ConstraintTerm {
		
		/** The alias. */
		private final String alias;
		
		/** The attr. */
		private final String attr;
		/**
		 * Constructor for AliasAttrConstraintTerm.
		 * @param aliasName String
		 * @param attrName String
		 */
		public AliasAttrConstraintTerm(String aliasName, String attrName) {
			this.alias = aliasName;
			this.attr = attrName;
		}
		
		/**
		 * Gets the alias.
		 *
		 * @return String
		 */
		public String getAlias() {
			return this.alias;
		}
		
		/**
		 * Gets the attr.
		 *
		 * @return String
		 */
		public String getAttr() {
			return this.attr;
		}
		
		/**
		 * Checks if is constant.
		 *
		 * @return boolean
		 */
		@Override public boolean isConstant() { return false; }
		/**
		 * Method isAliasAttr.
		 * @return boolean
		 */
		@Override public boolean isAliasAttr() { return true; }
	}
}
