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
import uk.ac.ox.cs.pdq.fol.Predicate;
import uk.ac.ox.cs.pdq.fol.Signature;
import uk.ac.ox.cs.pdq.fol.Term;
import uk.ac.ox.cs.pdq.fol.Variable;
import uk.ac.ox.cs.pdq.util.Types;

/**
 * @author Gonzalo Diaz
 */
public class ConjunctiveQueryBodyBuilder {
	private static Logger log = Logger.getLogger(ConjunctiveQueryBodyBuilder.class);

	private Map<String, Predicate> aliasToPredicateFormulas = new HashMap<>();

	private Schema              schema;
	private Map<String, String> aliasToRelations;
	private Predicate    resultPredicate;
	private String              qName;
	private ConjunctiveQuery    conjQuery; 
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
	 * Builds an initial set of PredicateFormulas with fresh variables in every position.
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

			this.aliasToPredicateFormulas.put(aliasName, new Predicate( relation, terms ));

			counter++;
		}
	}

	/**
	 * @param left ConstraintTerm
	 * @param right ConstraintTerm
	 * @throws Exception
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
	 * @param leftConst ConstantConstraintTerm
	 * @param rightAliasAttr AliasAttrConstraintTerm
	 * @throws Exception
	 */
	private void _addConstraint(ConstantConstraintTerm leftConst, AliasAttrConstraintTerm rightAliasAttr) throws Exception {

		// Prepare right variable:
		String rightAlias = rightAliasAttr.getAlias();
		String rightAttr  = rightAliasAttr.getAttr();
		Predicate rightPredForm = this.aliasToPredicateFormulas.get(rightAlias);

		// Prepare left constant term:
		TypedConstant<?> leftConstant = new TypedConstant<>(
				Types.cast(((Relation) rightPredForm.getSignature()).getAttribute(rightAttr).getType(),
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
	 * @param leftAliasAttr AliasAttrConstraintTerm
	 * @param rightAliasAttr AliasAttrConstraintTerm
	 * @throws Exception
	 */
	private void _addConstraint(AliasAttrConstraintTerm leftAliasAttr, AliasAttrConstraintTerm rightAliasAttr) throws Exception {

		// Prepare left variable:
		String leftAlias = leftAliasAttr.getAlias();
		String leftAttr  = leftAliasAttr.getAttr();
		Predicate leftPredForm = this.aliasToPredicateFormulas.get(leftAlias);

		// Get term in said position:
		int leftAttrIndex = this.schema.getRelation( this.aliasToRelations.get(leftAlias) ).getAttributeIndex(leftAttr);
		Term leftTerm = leftPredForm.getTerm(leftAttrIndex);

		// Prepare right variable:
		String rightAlias = rightAliasAttr.getAlias();
		String rightAttr  = rightAliasAttr.getAttr();
		Predicate rightPredForm = this.aliasToPredicateFormulas.get(rightAlias);

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
	 * Replace a term for a constant in all predicates
	 * 
	 * @param oldTerm Term
	 * @param newTerm Term
	 */
	private void replaceTerm(Term oldTerm, Term newTerm) {

		for( Map.Entry<String, Predicate> entry : this.aliasToPredicateFormulas.entrySet() ) {
			String alias = entry.getKey();
			Predicate predicateFormula = entry.getValue();

			List<Term> terms = predicateFormula.getTerms();

			Term[] newTerms = new Term[terms.size()];

			for(int i = 0; i < newTerms.length; i++) {
				if( terms.get(i).equals( oldTerm ) ) {
					newTerms[i] = newTerm;
				} else {
					newTerms[i] = terms.get(i);
				}
			}

			Predicate newPredicateFormula = new Predicate(predicateFormula.getSignature(), newTerms);

			this.aliasToPredicateFormulas.put(alias, newPredicateFormula);
		}

	}
	
	public void returnAllVars() {
		this.returnsAllVars = true;
		
		// Find arity:
		Set<Variable> vars = new HashSet<>();
		for( Predicate predFormula : this.aliasToPredicateFormulas.values() ) {
			vars.addAll( predFormula.getVariables() );
		}
		
		Signature signature = new Signature(this.qName, vars.size());
		
		this.resultPredicate = new Predicate(signature, vars);
	}
	
	/**
	 * @param aliasName String
	 * @param attrName String
	 */
	public void addResultColumn(String aliasName, String attrName) {
		if( this.returnsAllVars) {
			throw new RuntimeException("cannot add result column to this query!");
		}
		
		if( this.resultPredicate == null ) {
			
			Signature signature = new Signature(this.qName, 1);
			Term term = this._findTerm(aliasName, attrName);
			this.resultPredicate = new Predicate(signature, term);
			
		} else {
			int newArity = this.resultPredicate.getSignature().getArity() + 1;
			Signature newSignature = new Signature(this.qName, newArity);
			
			Term[] newTerms = new Term[newArity];
			
			for(int i = 0; i < newTerms.length-1; i++) {
				newTerms[i] = this.resultPredicate.getTerm(i);
			}
			newTerms[newTerms.length-1] = this._findTerm(aliasName, attrName);
			
			Predicate newPredForm = new Predicate(newSignature, newTerms);
			
			this.resultPredicate = newPredForm;
		}
		
	}
	
	/**
	 * @param aliasName String
	 * @param attrName String
	 * @return Term
	 */
	private Term _findTerm(String aliasName, String attrName) {
		Predicate predFormula = this.aliasToPredicateFormulas.get(aliasName);

		// Get term in said position:
		int attrIndex = this.schema.getRelation( this.aliasToRelations.get(aliasName) ).getAttributeIndex(attrName);
		return predFormula.getTerm(attrIndex);
	}
		
	/**
	 * @return ConjunctiveQuery
	 */
	public ConjunctiveQuery toConjunctiveQuery() {
		List<Predicate> preds = new ArrayList<>();
		preds.addAll(this.aliasToPredicateFormulas.values());
		this.conjQuery = new ConjunctiveQuery(this.resultPredicate, Conjunction.of(preds));
		return this.conjQuery;
	}

	/**
	 * @return String
	 */
	@Override
	public String toString() {
		StringBuilder ans = new StringBuilder();
		
		ans.append("<StartQueryBuilder: ");
		for( Predicate predicateFormula : this.aliasToPredicateFormulas.values() ) {
			ans.append(predicateFormula.toString()).append(", ");
		}
		ans.append(":EndQueryBuilder>");
		
		return ans.toString();
	}


	/**
	 */
	public static abstract class ConstraintTerm {
		/**
		 * @return boolean
		 */
		public abstract boolean isConstant();

		/**
		 * @return boolean
		 */
		public abstract boolean isAliasAttr();
	}

	/**
	 */
	public static class ConstantConstraintTerm extends ConstraintTerm {
		private final String constant;
		/**
		 * Constructor for ConstantConstraintTerm.
		 * @param constant String
		 */
		public ConstantConstraintTerm(String constant) {
			this.constant = constant;
		}
		/**
		 * @return String
		 */
		public String getConstant() {
			return this.constant;
		}
		/**
		 * @return boolean
		 */
		@Override public boolean isConstant() { return true; }
		/**
		 * @return boolean
		 */
		@Override public boolean isAliasAttr() { return false; }
	}

	/**
	 */
	public static class AliasAttrConstraintTerm extends ConstraintTerm {
		private final String alias;
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
		 * @return String
		 */
		public String getAlias() {
			return this.alias;
		}
		/**
		 * @return String
		 */
		public String getAttr() {
			return this.attr;
		}
		/**
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
