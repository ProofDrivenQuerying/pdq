package uk.ac.ox.cs.pdq.services.logicblox.rewrite;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.log4j.Logger;

import uk.ac.ox.cs.pdq.db.AccessMethod;
import uk.ac.ox.cs.pdq.db.Attribute;
import uk.ac.ox.cs.pdq.db.EntityRelation;
import uk.ac.ox.cs.pdq.db.LinearGuarded;
import uk.ac.ox.cs.pdq.db.Relation;
import uk.ac.ox.cs.pdq.db.Schema;
import uk.ac.ox.cs.pdq.db.TypedConstant;
import uk.ac.ox.cs.pdq.db.View;
import uk.ac.ox.cs.pdq.db.builder.DependencyBuilder;
import uk.ac.ox.cs.pdq.db.builder.SchemaBuilder;
import uk.ac.ox.cs.pdq.fol.Conjunction;
import uk.ac.ox.cs.pdq.fol.ConjunctiveQuery;
import uk.ac.ox.cs.pdq.fol.Disjunction;
import uk.ac.ox.cs.pdq.fol.Negation;
import uk.ac.ox.cs.pdq.fol.Atom;
import uk.ac.ox.cs.pdq.fol.Predicate;
import uk.ac.ox.cs.pdq.fol.Variable;
import uk.ac.ox.cs.pdq.rewrite.RewriterException;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.logicblox.common.protocol.CommonProto;
//import com.logicblox.common.protocol.CommonProto.Atom;
import com.logicblox.common.protocol.CommonProto.Clause;
import com.logicblox.common.protocol.CommonProto.ClauseBody;
import com.logicblox.common.protocol.CommonProto.Constraint;
import com.logicblox.common.protocol.CommonProto.Formula;
import com.logicblox.common.protocol.CommonProto.HeadAtom;
import com.logicblox.common.protocol.CommonProto.P2PMapping;
import com.logicblox.common.protocol.CommonProto.PredicateDeclaration;
import com.logicblox.common.protocol.CommonProto.PredicateDeclaration.Kind;
import com.logicblox.common.protocol.CommonProto.Rule;
import com.logicblox.common.protocol.CommonProto.Term;
import com.logicblox.common.protocol.CommonProto.VariableDeclaration;
import com.logicblox.compiler.ProtoBuf.CompilationUnit;


// TODO: Auto-generated Javadoc
/**
 * Builds a partial schema from a compile result.
 * 
 * @author Julien Leblay
 */
public class ProtoBufferUnwrapper {

	/** Logger. */
	static Logger log = Logger.getLogger(ProtoBufferUnwrapper.class);
	
	/** The schema under construction. */
	private final SchemaBuilder builder;

	/** Variables recorded so far. */
	private final Map<String , Pair<Variable, Object>> variables = new LinkedHashMap<>();

	/**  Known entity relations. */
	private final Map<String , EntityRelation> entityTypes = new LinkedHashMap<>();
	
	/**
	 * Constructor for ProtoBufferUnwrapper.
	 * @param schema Schema
	 */
	public ProtoBufferUnwrapper(Schema schema) {
		this.builder = Schema.builder(schema);
	}
	
	/**
	 * Constructor for ProtoBufferUnwrapper.
	 * @param schema Schema
	 */
	public ProtoBufferUnwrapper(SchemaBuilder schema) {
		this.builder = schema;
	}

	/**
	 * Unwrap schema.
	 *
	 * @param cu CompilationUnit
	 * @return Schema
	 */
	public Schema unwrapSchema(CompilationUnit cu) {
		this.unwrapPredicateDeclarations(cu.getPredicateList());
		for (Clause clause: cu.getClauseList()) {
			try {
				switch (clause.getKind()) {
				case CONSTRAINT:
					uk.ac.ox.cs.pdq.db.Constraint c = this.unwrapConstraint(clause.getConstraint());
					if (c != null) {
						this.builder.addDependency(c);
					}
					break;
				case RULE:
					Collection<uk.ac.ox.cs.pdq.fol.Rule> col = this.unwrapRule(clause.getRule(), false, true);
					if (col != null && !col.isEmpty()) {
						for (uk.ac.ox.cs.pdq.fol.Rule r: col) {
							if (r instanceof View) {
								this.builder.addRelation((Relation) r);
							} else {
								this.builder.addDependency((uk.ac.ox.cs.pdq.db.Constraint) r);
							} 
						}
					}
					break;
				case P2P:
					this.unwrapP2P(clause.getP2P());
					break;
				default:
					throw new ParserException("Unsupported clause kind " + clause.getKind());
				}
			} catch (UnsupportedOperationException | ParserException e) {
				log.warn("Ignoring clause due to unsupported construct: " + e.getMessage());
				log.debug("Ignored clause: " + clause);
			}
			this.variables.clear();
		}
		return this.builder.build();
	}
	
	/**
	 * Unwrap queries.
	 *
	 * @param cu CompilationUnit
	 * @return Collection<ConjunctiveQuery>
	 */
	public Collection<ConjunctiveQuery> unwrapQueries(CompilationUnit cu) {
 		this.unwrapPredicateDeclarations(cu.getPredicateList());
		Collection<ConjunctiveQuery> result = new LinkedList<>();
		for (Clause clause: cu.getClauseList()) {
			switch (clause.getKind()) {
			case RULE:
				for (uk.ac.ox.cs.pdq.fol.Rule rule: this.unwrapRule(clause.getRule(), true, false)) {
					result.add((ConjunctiveQuery) rule);
				}
				break;
			default:
				log.info("Ignoring clause " + clause);
				break;
			}
			this.variables.clear();
		}
		return result;
	}
	
	/**
	 * Unwrap predicate declarations.
	 *
	 * @param predList List<PredicateDeclaration>
	 */
	private void unwrapPredicateDeclarations(List<PredicateDeclaration> predList) {
		for (PredicateDeclaration predDecl: predList) {
			switch (predDecl.getDerivationType()) {
			case DERIVED:
			case DERIVED_AND_STORED:
			case NOT_DERIVED:
			case EXTENSIONAL:
				this.unwrapPredicateDeclaration(predDecl);
				break;
			case INTEGRITY_CONSTRAINT:
			default:
				throw new UnsupportedOperationException();
			}
		}
	}

	/**
	 * Unwrap constraint.
	 *
	 * @param constraint Constraint
	 * @return the uk.ac.ox.cs.pdq.db. constraint
	 */
	public uk.ac.ox.cs.pdq.db.Constraint unwrapConstraint(Constraint constraint) {
		this.variables.clear();
		if (constraint.hasBody()) {
			this.variables.putAll(this.unwrapVariableDeclarations(constraint.getBody().getVarList()));
			uk.ac.ox.cs.pdq.fol.Formula f = this.unwrapFormula(constraint.getBody().getFormula());
			try {
				log.debug("Constraint " + f);
				 return f.rewrite(new PushEqualityRewriter<>())
						.rewrite(new SentenceToRule<>());
			} catch (RewriterException e) {
				log.warn("Contraint could not be rewritten for PDQ. Ignoring constraint: " + e.getMessage());
				log.debug(constraint);
				return null;
			}
		} else {
			throw new ParserException("Expected body in constraint " + constraint);
		}
	}

	/**
	 * Rule to constraint.
	 *
	 * @param rule Rule
	 * @return Collection<ConjunctiveQuery>
	 */
	public uk.ac.ox.cs.pdq.db.Constraint ruleToConstraint(Rule rule) {
		this.variables.clear();
		uk.ac.ox.cs.pdq.fol.Formula body = null;
		this.variables.putAll(this.unwrapVariableDeclarations(rule.getVarList()));
		if (rule.hasBody()) {
			ClauseBody bodyClause = rule.getBody();
			this.variables.putAll(this.unwrapVariableDeclarations(bodyClause.getVarList()));
			body = this.unwrapFormula(bodyClause.getFormula());

			try {
				log.debug("Constraint " + body);
				 return body.rewrite(new PushEqualityRewriter<>())
						.rewrite(new SentenceToRule<>());
			} catch (RewriterException e) {
				log.warn("Contraint could not be rewritten for PDQ. Ignoring constraint: " + e.getMessage());
				log.debug(body);
				return null;
			}

		} else {
			throw new ParserException("Expected body in constraint. ");
		}
	}

	/**
	 * Rule to view.
	 *
	 * @param rule Rule
	 * @return View
	 */
	public View ruleToView(Rule rule) {
		Iterator<uk.ac.ox.cs.pdq.fol.Rule> it = this.unwrapRule(rule, false, false).iterator();
		if (!it.hasNext()) {
			return null;
		}
		return (View) it.next();
	}

	/**
	 * Rule to query.
	 *
	 * @param rule Rule
	 * @return Collection<ConjunctiveQuery>
	 */
	public ConjunctiveQuery ruleToQuery(Rule rule) {
		Iterator<uk.ac.ox.cs.pdq.fol.Rule> it = this.unwrapRule(rule, true, false).iterator();
		if (!it.hasNext()) {
			return null;
		}
		return (ConjunctiveQuery) it.next();
	}

	/**
	 * Unwrap rule.
	 *
	 * @param rule Rule
	 * @param forget boolean if true, the return rule is not recorded into the
	 * schema. This is typically useful when rule are actually queries.
	 * @param strict the strict
	 * @return Collection<Rule>
	 */
	public Collection<uk.ac.ox.cs.pdq.fol.Rule> unwrapRule(Rule rule, boolean forget, boolean strict) {
		this.variables.clear();
		uk.ac.ox.cs.pdq.fol.Formula body = null;
		this.variables.putAll(this.unwrapVariableDeclarations(rule.getVarList()));
		if (rule.hasBody()) {
			ClauseBody bodyClause = rule.getBody();
			this.variables.putAll(this.unwrapVariableDeclarations(bodyClause.getVarList()));
			body = this.unwrapFormula(bodyClause.getFormula());
			if (body instanceof Atom) {
				body = Conjunction.of((Atom) body);
			} else if (!(body instanceof Conjunction)) {
				throw new ParserException("Expected atom or positive conjunction as rule body: " + body);
			} else {
				for (uk.ac.ox.cs.pdq.fol.Formula sub: body.getChildren()) {
					if (!(sub instanceof Atom)) {
						throw new ParserException("Expected atom or positive conjunction as rule body: " + body);
					}
				}
			}
		} else {
			throw new ParserException("Expected body in rule ");
		}
		Collection<uk.ac.ox.cs.pdq.fol.Rule> result = new LinkedList<>();
		if (rule.hasHead()) {
			this.variables.putAll(this.unwrapVariableDeclarations(rule.getHead().getVarList()));
			for (HeadAtom atom: rule.getHead().getHeadAtomList()) {
				Atom headAtom = this.unwrapHeadAtom(atom, strict);
				if (body.getAtoms().size() > 0) {
					if (!forget) {
						try {
							LinearGuarded lg =
									new LinearGuarded(headAtom, (Conjunction) body)
											.rewrite(new PushEqualityRewriter<LinearGuarded>());
							this.builder.addDependency(lg);
							this.builder.addDependency(lg.invert());
							result.add(new View(lg, Lists.<AccessMethod>newArrayList()));
							log.debug("View:" + lg);
						} catch (RewriterException e) {
							log.warn("Contraint could not be rewritten for PDQ. Ignoring constraint: " + e.getMessage());
							throw new ParserException("Unable to push equalities on " + body);
						}
					} else {
						ConjunctiveQuery query = new ConjunctiveQuery(headAtom, (Conjunction) body);
						log.debug("Query:" + query);
						result.add(query);
					}
				}
			}
		} else {
			throw new ParserException("Expected head in rule " + rule);
		}
		return result;
	}

	/**
	 * Unwrap p2 p.
	 *
	 * @param p2p P2PMapping
	 */
	private void unwrapP2P(P2PMapping p2p) {
		log.warn("P2P not yet supported.");
	}
	
	/**
	 * Unwrap predicate declaration.
	 *
	 * @param predDecl PredicateDeclaration
	 * @return Relation
	 */
	public Relation unwrapPredicateDeclaration(PredicateDeclaration predDecl) {
		String name = predDecl.getName();
		List<Attribute> attributes = new ArrayList<>(predDecl.getArgumentTypeCount());
		Map<Attribute, Relation> entityTypedAtts = new LinkedHashMap<>();
		switch (predDecl.getKind()) {
		case ENTITY:
		case SUBENTITY:
			Relation r = this.builder.getRelation(name);
			assert r instanceof EntityRelation;
			EntityRelation e = (EntityRelation) r;
			if (e == null) {
				e = this.builder.addEntityRelation(name);
				this.entityTypes.put(name, e);
			}
			Attribute typeAtt = new Attribute(e, "_");
			attributes.add(typeAtt);
			if (predDecl.getKind() == Kind.SUBENTITY) {
				Relation parent = this.builder.getRelation(predDecl.getSuper());
				entityTypedAtts.put(typeAtt, parent);
			}
			break;
		default:
			int i = 0;
			for (CommonProto.Type type : predDecl.getArgumentTypeList()) {
				Object cl = this.unwrapType(type);
				if (type.hasPrimitive()) {
					attributes.add(new Attribute((Class) cl, "_" + (i++)));
				} else if (type.hasUnary()) {
					EntityRelation relType = this.entityTypes.get(type.getUnary().getName());
					Preconditions.checkNotNull(relType, "No such entity type: " + type.getUnary().getName());
					Attribute att = relType.getAttribute(0);
					entityTypedAtts.put(att, relType);
					attributes.add(att);
				} 
			}
			break;
		}
		Relation relation = this.builder.addOrReplaceRelation(name, attributes, name.contains(":eq_"));
		if (!entityTypedAtts.isEmpty()) {
			DependencyBuilder db = new DependencyBuilder();
			Atom atom = relation.createAtoms();
			db.addLeftAtom(atom);
			for (int i = 0, l = atom.getTermsCount(); i < l; i++) {
				Relation r = entityTypedAtts.get(relation.getAttribute(i));
				if (r != null) {
//					attributes.set(i, new Attribute(r, attributes.get(i).getName()));
					relation = this.builder.addOrReplaceRelation(name, attributes, name.contains(":eq_"));
					db.addRightAtom(new Atom(r, atom.getTerm(i)));
				}
			}
			uk.ac.ox.cs.pdq.db.Constraint ic = db.build();
			log.debug("Type constraint:" + ic);
			this.builder.addDependency(ic);
		}
		log.debug("Relation:" + name + " -> " + attributes);
		return relation;
	}
	
	/**
	 * Unwrap formula.
	 *
	 * @param formula Formula
	 * @return uk.ac.ox.cs.pdq.formula.Formula
	 */
	private uk.ac.ox.cs.pdq.fol.Formula unwrapFormula(Formula formula) {
		switch (formula.getKind()) {
		case ATOM:
			return this.unwrapAtom(formula.getAtom(), true);
		case CONJUNCTION:
			uk.ac.ox.cs.pdq.fol.Formula[] conjuncts = new uk.ac.ox.cs.pdq.fol.Formula[formula.getConjunction().getFormulaCount()];
			int i = 0;
			for (Formula subForm: formula.getConjunction().getFormulaList()) {
				conjuncts[i++] = this.unwrapFormula(subForm);
			}
			return Conjunction.of(conjuncts);
		case DISJUNCTION:
			uk.ac.ox.cs.pdq.fol.Formula[] disjuncts = new uk.ac.ox.cs.pdq.fol.Formula[formula.getDisjunction().getFormulaCount()];
			int j = 0;
			for (Formula subForm: formula.getDisjunction().getFormulaList()) {
				disjuncts[j++] = this.unwrapFormula(subForm);
			}
			return Disjunction.of(disjuncts);
		case NEGATION:
			this.variables.putAll(this.unwrapVariableDeclarations(formula.getNegation().getVarList()));
			return Negation.of(this.unwrapFormula(formula.getNegation().getFormula()));
		default:
			throw new UnsupportedOperationException("Unsupported construct " + formula.getKind());
		}
	}

	/**
	 * Unwrap atom.
	 *
	 * @param atom Atom
	 * @param strict the strict
	 * @return PredicateFormula
	 */
	private Atom unwrapAtom(com.logicblox.common.protocol.CommonProto.Atom atom, boolean strict) {
		Atom.Builder atomBuilder = Atom.builder();
		for (Term term: atom.getKeyArgumentList()) {
			atomBuilder.addTerm(this.unwrapTerm(term));
		}
		for (Term term: atom.getValueArgumentList()) {
			atomBuilder.addTerm(this.unwrapTerm(term));
		}
		Predicate relation = this.getRelation(atom);
		if (relation == null) {
			if (strict) {
				throw new ParserException("Referring to unknow predicate " + atom.getPredicateName());
			}
			relation = new Predicate(atom.getPredicateName(), atomBuilder.getTermCount());
		}
		atomBuilder.setSignature(relation);
		return atomBuilder.build();
	}
	
	/**
	 * Gets the relation.
	 *
	 * @param atom Atom
	 * @return Relation
	 */
	private Relation getRelation(com.logicblox.common.protocol.CommonProto.Atom atom) {
		Relation result = this.builder.getRelation(atom.getPredicateName());
		if (result != null) {
			return result;
		}
		return null;
	}

	/**
	 * Unwrap head atom.
	 *
	 * @param atom HeadAtom
	 * @param strict the strict
	 * @return PredicateFormula
	 */
	private Atom unwrapHeadAtom(HeadAtom atom, boolean strict) {
		if (!atom.hasAtom()) {
			throw new UnsupportedOperationException("Unsupported construct: " + atom);
		}
		return this.unwrapAtom(atom.getAtom(), strict);
	}

	/**
	 * Unwrap term.
	 *
	 * @param term Term
	 * @return uk.ac.ox.cs.pdq.formula.Term
	 */
	private uk.ac.ox.cs.pdq.fol.Term unwrapTerm(Term term) {
		if (term.hasVariable()) {
			Pair<Variable, Object> vPair = this.variables.get(term.getVariable().getName());
			if (vPair == null) {
				log.warn("Referring to undeclared variable " + term.getVariable().getName());
				return Variable.getFreshVariable();
			}
			return vPair.getLeft();
		}
		if (!term.hasConstant()) {
			throw new ParserException("Term appears to be neither a variable nor a constant" + term);
		}
		switch(term.getConstant().getKind()) {
		case BOOL:     return new TypedConstant<>(term.getConstant().getBoolConstant().getValue());
		case INT:      return new TypedConstant<>(term.getConstant().getIntConstant().getValue());
		case UINT:     return new TypedConstant<>(term.getConstant().getUintConstant().getValue());
		case STRING:   return new TypedConstant<>(term.getConstant().getStringConstant().getValue());
		case FLOAT:    return new TypedConstant<>(term.getConstant().getFloatConstant().getValue());
		case DECIMAL:  return new TypedConstant<>(term.getConstant().getDecimalConstant().getValue());
		case DATETIME: return new TypedConstant<>(term.getConstant().getDateTimeConstant().getValue());
		default:       throw new ParserException("Unsupported constant type " + term);
		}
	}
	

	/**
	 * Unwrap variable declarations.
	 *
	 * @param declarations List<VariableDeclaration>
	 * @return Map<String,Pair<Variable,Object>>
	 */
	private Map<String, Pair<Variable, Object>> unwrapVariableDeclarations(List<VariableDeclaration> declarations) {
		Map<String, Pair<Variable, Object>> result = new LinkedHashMap<>();
		for (VariableDeclaration varDec : declarations) {
			String name = varDec.getName();
			result.put(name, Pair.of(new Variable(name), this.unwrapType(varDec.getType())));
		}
		return result;
	}

	/**
	 * Unwrap type.
	 *
	 * @param type com.logicblox.common.protocol.CommonProto.Type
	 * @return Object
	 */
	private Object unwrapType(com.logicblox.common.protocol.CommonProto.Type type) {
		if (type.hasPrimitive()) {
			switch(type.getPrimitive().getKind()) {
			case BOOL:     return Boolean.class;
			case INT:      return Integer.class;
			case UINT:     return Long.class;
			case STRING:   return String.class;
			case FLOAT:    return String.class;
			case DECIMAL:  return String.class;
			case DATETIME: return Long.class;
			default:       return Object.class;
			}
		}
		if (type.hasUnary()) {
			return type.getUnary().getName();
		}
		// TODO: variables of arbitrary types are not supported.
		// Until this happens, such variable are said to be of type Long the default
		// type of refmods.
		return Long.class;
	}
	
	/**
	 * An exception that occur while parsing a compile result.
	 * @author Julien Leblay
	 */
	public static class ParserException extends RuntimeException {

		/**  generated. */
		private static final long serialVersionUID = 1453097478156826396L;

		/**
		 * Constructor for ParserException.
		 * @param message String
		 * @param cause Throwable
		 * @param enableSuppression boolean
		 * @param writableStackTrace boolean
		 */
		public ParserException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
			super(message, cause, enableSuppression, writableStackTrace);
		}

		/**
		 * Constructor for ParserException.
		 * @param message String
		 * @param cause Throwable
		 */
		public ParserException(String message, Throwable cause) {
			super(message, cause);
		}

		/**
		 * Constructor for ParserException.
		 * @param message String
		 */
		public ParserException(String message) {
			super(message);
		}

		/**
		 * Constructor for ParserException.
		 * @param cause Throwable
		 */
		public ParserException(Throwable cause) {
			super(cause);
		}
	}
}
