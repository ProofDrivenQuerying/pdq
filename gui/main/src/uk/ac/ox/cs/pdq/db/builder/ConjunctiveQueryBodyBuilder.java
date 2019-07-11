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
import uk.ac.ox.cs.pdq.fol.TypedConstant;
import uk.ac.ox.cs.pdq.fol.Atom;
import uk.ac.ox.cs.pdq.fol.ConjunctiveQuery;
import uk.ac.ox.cs.pdq.fol.Constant;
import uk.ac.ox.cs.pdq.fol.Predicate;
import uk.ac.ox.cs.pdq.fol.Term;
import uk.ac.ox.cs.pdq.fol.Variable;
import uk.ac.ox.cs.pdq.util.Types;

// TODO: Auto-generated Javadoc
/**
 * The Class ConjunctiveQueryBodyBuilder.
 *
 * @author Gonzalo Diaz
 */
public class ConjunctiveQueryBodyBuilder {
	
	/** The log. */
	private static Logger log = Logger.getLogger(ConjunctiveQueryBodyBuilder.class);

	/** The alias to predicate formulas. */
	private Map<String, Atom> aliasToPredicateFormulas = new HashMap<>();

	/** The schema. */
	private Schema              schema;
	
	/** The alias to relations. */
	private Map<String, String> aliasToRelations;
	
	/** The result predicate. */
	private Atom    resultPredicate;
	
	/** The q name. */
	private String              qName;
	
	/** The conj query. */
	private ConjunctiveQuery    conjQuery; 
	
	/** The returns all vars. */
	private boolean             returnsAllVars;
	
	/** The all terms. */
	private Term[] 				terms;

	/**
	 * Constructor for ConjunctiveQueryBodyBuilder.
	 * @param schema Schema
	 * @param aliasToRelations Map<String,String>
	 * @throws Exception 
	 */
	public ConjunctiveQueryBodyBuilder(Schema schema, Map<String, String> aliasToRelations, Term[] terms) throws Exception {
		this.schema = schema;
		
		this.aliasToRelations = aliasToRelations;
		
		this.qName = "Q";
		
		this.returnsAllVars = false;
		
		this.terms = terms;
		
		this.buildInitialPredicates();
	}

	/**
	 * Builds an initial set of PredicateFormulas with fresh variables in every position.
	 * @throws Exception 
	 */
	private void buildInitialPredicates() throws Exception {
		log.debug("buildInitialPredicates. aliasToRelations = " + this.aliasToRelations);
		
		// Build an Atom from predicate and terms
		int counter = 0;
		for( Map.Entry<String, String> entry : this.aliasToRelations.entrySet() ) {
			try
			{
				String aliasName = entry.getKey();
				String relationName = entry.getValue();

				Relation relation = this.schema.getRelation(relationName);
				if(relation == null)
				{
					throw new Exception("No such relation: " + relationName);
				}
				Attribute[] attributes = relation.getAttributes();

				Predicate predicate = Predicate.create(relationName, attributes.length);

				Term[] terms = new Term[ attributes.length ];

				for( int i = 0; i < terms.length; i++ ) {
					terms[i] = new Variable(attributes[i].getName());
				}

				this.aliasToPredicateFormulas.put(aliasName, Atom.create( predicate, terms ));

				counter++;
			}
			catch(NullPointerException e)
			{
				throw new Exception("No such relation");
			}
		}
	}

	/**
	 * Adds the constraint.
	 *
	 * @param left ConstraintTerm
	 * @param right ConstraintTerm
	 * @throws Exception the exception
	 */
	public void addConstraint(ConstraintTerm left, ConstraintTerm right) throws Exception {

		// Check left and right constant / aliasAttr
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
		Atom rightPredForm;
		try
		{
			rightPredForm = this.aliasToPredicateFormulas.get(rightAlias);
			if(rightPredForm == null) throw new Exception();
		}
		catch(Exception e)
		{
			throw new Exception("Missing relation: " + rightAlias);
		}

		// Prepare left constant term:
		Relation relation = this.schema.getRelation(rightPredForm.getPredicate().getName());

		TypedConstant leftConstant;
		Attribute attribute;
		try
		{
			attribute = relation.getAttribute(rightAttr);
			if(attribute == null) throw new Exception();
		}
		catch(Exception e)
		{
			throw new Exception("Missing attribute: " + rightAttr);
		}

		try
		{
			leftConstant = TypedConstant.create(
				Types.cast(attribute.getType(),
						leftConst.getConstant()));
		}
		catch(Exception e)
		{
			throw new Exception("Attribute: " + rightAttr + ": Type Mismatch");			
		}
		
		// Get term in said position:
		int rightAttrIndex = this.schema.getRelation( this.aliasToRelations.get(rightAlias) ).getAttributePosition(rightAttr);
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
		Atom leftPredForm = this.aliasToPredicateFormulas.get(leftAlias);

		// Get term in said position:
		int leftAttrIndex = -1;
		Term leftTerm = null;
		
		Relation r;
		try {
			r = this.schema.getRelation(this.aliasToRelations.get(leftAlias));
			r.toString();
		} catch(Exception e)
		{	
			log.error("null pointer. leftAttr=" + leftAttr
					+ ", leftAlias=" + leftAlias
					+ ", leftAttrIndex=" + leftAttrIndex
					+ ", relationName=" + this.aliasToRelations.get(leftAlias)
					+ ", leftPredForm=" + leftPredForm
			, e);
			throw new Exception("Missing relation: " + leftAlias);
		}
		try
		{
			leftAttrIndex = r.getAttributePosition(leftAttr);
			leftTerm = leftPredForm.getTerm(leftAttrIndex);
		}
		catch(Exception e)
		{
			throw new Exception("Missing attribute: " + leftAttr);
		}

		// Prepare right variable:
		String rightAlias = rightAliasAttr.getAlias();
		String rightAttr  = rightAliasAttr.getAttr();
		Atom rightPredForm = this.aliasToPredicateFormulas.get(rightAlias);

		// Get term in said position:
		int rightAttrIndex = -1;
		
		Term rightTerm = null;
		try {
			r = this.schema.getRelation( this.aliasToRelations.get(rightAlias) );
			r.toString();
		} catch( Exception e ) {
			log.error("null pointer. rightAttr=" + rightAttr
					+ ", rightAlias=" + rightAlias
					+ ", rightAttrIndex=" + rightAttrIndex
					+ ", relationName=" + this.aliasToRelations.get(rightAlias)
					+ ", rightPredForm=" + rightPredForm
			, e);
			throw new Exception("Missing relation: " + rightAlias);
		}
		try
		{
			rightAttrIndex = r.getAttributePosition(rightAttr);
			rightTerm = rightPredForm.getTerm(rightAttrIndex);
		}
		catch(Exception e)
		{
			throw new Exception("Missing attribute: " + rightAttr);
		}
		
		// Check left and right isVariable()
		if( !leftTerm.isVariable() && !rightTerm.isVariable() ) {
			if( !leftTerm.equals(rightTerm) ) {
				throw new Exception("Conflicting constants");
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
	 * Replace a term for a constant in all predicates.
	 *
	 * @param oldTerm Term
	 * @param newTerm Term
	 */
	private void replaceTerm(Term oldTerm, Term newTerm) {

		// Iterate over aliasToPredicateFormulas
		for( Map.Entry<String, Atom> entry : this.aliasToPredicateFormulas.entrySet() ) {
			String alias = entry.getKey();
			Atom predicateFormula = entry.getValue();

			Term[] terms = predicateFormula.getTerms();

			Term[] newTerms = new Term[terms.length];

			for(int i = 0; i < newTerms.length; i++) {
				if( terms[i].equals( oldTerm ) ) {
					newTerms[i] = newTerm;
				} else {
					newTerms[i] = terms[i];
				}
			}
			
			// Create shiny new Atom
			Atom newPredicateFormula = Atom.create(predicateFormula.getPredicate(), newTerms);

			// Replace in the alias map
			this.aliasToPredicateFormulas.put(alias, newPredicateFormula);
		}

	}
	
	/**
	 * Return all vars.
	 */
	public void returnAllVars() {
		this.returnsAllVars = true;
		
		// Find arity:
		Set<Variable> vars = new HashSet<>();
		for( Atom predFormula : this.aliasToPredicateFormulas.values() ) {
			for(Variable var : predFormula.getVariables())
			{
				vars.add(var);
			}
		}
		
		// Create brand new Predicate
		Predicate predicate = Predicate.create(this.qName, vars.size());
		
		Term[] terms = new Term[vars.size()];
		int i = 0;
		for(Variable var : vars) terms[i++] = var;
		
		// Create brand new Atom
		this.resultPredicate = Atom.create(predicate, terms);
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
			
			// Create Atom from Predicate and Term
			Predicate predicate = Predicate.create(this.qName, 1);
			Term term = this._findTerm(aliasName, attrName);
			if(term != null)
			{
				this.resultPredicate = Atom.create(predicate, term);
			}
			
		} else {
			
			// Create Atom from Predicate and Terms
			int newArity = this.resultPredicate.getPredicate().getArity() + 1;
			Predicate newSignature = Predicate.create(this.qName, newArity);
			
			Term[] newTerms = new Term[newArity];
			
			for(int i = 0; i < newTerms.length-1; i++) {
				newTerms[i] = this.resultPredicate.getTerm(i);
			}
			newTerms[newTerms.length-1] = this._findTerm(aliasName, attrName);
			
			Atom newPredForm = Atom.create(newSignature, newTerms);
			
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
		Atom predFormula = this.aliasToPredicateFormulas.get(aliasName);
		if(predFormula != null)
		{
			// Get term in said position:
			int attrIndex = this.schema.getRelation( this.aliasToRelations.get(aliasName) ).getAttributePosition(attrName);
			return predFormula.getTerm(attrIndex);
		}
		return null;
	}
		
	/**
	 * To conjunctive query.
	 *
	 * @return ConjunctiveQuery
	 */
	public ConjunctiveQuery toConjunctiveQuery() {
		List<Atom> preds = new ArrayList<>();
		preds.addAll(this.aliasToPredicateFormulas.values());
		Atom[] atoms;
		atoms = new Atom[preds.size()];
		int i = 0;
		for(Atom atom : preds) atoms[i++] = atom;
		if(atoms.length > 0)
		{
			this.conjQuery = ConjunctiveQuery.create((this.resultPredicate != null) ? this.resultPredicate.getFreeVariables() : new Variable[0], atoms);
		}
		else
		{
			this.conjQuery = null;
		}
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
		for( Atom predicateFormula : this.aliasToPredicateFormulas.values() ) {
			ans.append(predicateFormula.toString()).append(", ");
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
