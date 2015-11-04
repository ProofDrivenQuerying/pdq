package uk.ac.ox.cs.pdq.services.logicblox.rewrite;

import static com.logicblox.common.protocol.CommonProto.Formula.Kind.ATOM;
import static com.logicblox.common.protocol.CommonProto.Formula.Kind.CONJUNCTION;
import static com.logicblox.common.protocol.CommonProto.Formula.Kind.DISJUNCTION;
import static com.logicblox.common.protocol.CommonProto.Formula.Kind.NEGATION;
import static com.logicblox.common.protocol.CommonProto.PrimitiveType.Kind.BOOL;
import static com.logicblox.common.protocol.CommonProto.PrimitiveType.Kind.DATETIME;
import static com.logicblox.common.protocol.CommonProto.PrimitiveType.Kind.DECIMAL;
import static com.logicblox.common.protocol.CommonProto.PrimitiveType.Kind.FLOAT;
import static com.logicblox.common.protocol.CommonProto.PrimitiveType.Kind.INT;
import static com.logicblox.common.protocol.CommonProto.PrimitiveType.Kind.STRING;
import static com.logicblox.common.protocol.CommonProto.PrimitiveType.Kind.UINT;
import static com.logicblox.common.protocol.CommonProto.Term.Kind.CONSTANT;
import static com.logicblox.common.protocol.CommonProto.Term.Kind.VARIABLE;
import static com.logicblox.common.protocol.CommonProto.Type.Kind.PRIMITIVE;
import static com.logicblox.common.protocol.CommonProto.Type.Kind.UNARY;

import java.lang.reflect.Type;
import java.sql.Date;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import uk.ac.ox.cs.pdq.db.Constraint;
import uk.ac.ox.cs.pdq.db.Relation;
import uk.ac.ox.cs.pdq.db.Schema;
import uk.ac.ox.cs.pdq.db.TypedConstant;
import uk.ac.ox.cs.pdq.fol.Conjunction;
import uk.ac.ox.cs.pdq.fol.Disjunction;
import uk.ac.ox.cs.pdq.fol.Negation;
import uk.ac.ox.cs.pdq.fol.Predicate;
import uk.ac.ox.cs.pdq.fol.Query;
import uk.ac.ox.cs.pdq.fol.Signature;
import uk.ac.ox.cs.pdq.fol.Term;
import uk.ac.ox.cs.pdq.rewrite.Rewriter;
import uk.ac.ox.cs.pdq.rewrite.RewriterException;
import uk.ac.ox.cs.pdq.util.Named;
import uk.ac.ox.cs.pdq.util.Typed;

import com.logicblox.common.protocol.CommonProto.Atom;
import com.logicblox.common.protocol.CommonProto.BoolConstant;
import com.logicblox.common.protocol.CommonProto.ClauseBody;
import com.logicblox.common.protocol.CommonProto.ClauseHead;
import com.logicblox.common.protocol.CommonProto.Constant;
import com.logicblox.common.protocol.CommonProto.Constant.Kind;
import com.logicblox.common.protocol.CommonProto.DateTimeConstant;
import com.logicblox.common.protocol.CommonProto.DecimalConstant;
import com.logicblox.common.protocol.CommonProto.FloatConstant;
import com.logicblox.common.protocol.CommonProto.Formula;
import com.logicblox.common.protocol.CommonProto.HeadAtom;
import com.logicblox.common.protocol.CommonProto.IntConstant;
import com.logicblox.common.protocol.CommonProto.PrimitiveType;
import com.logicblox.common.protocol.CommonProto.Rule;
import com.logicblox.common.protocol.CommonProto.StringConstant;
import com.logicblox.common.protocol.CommonProto.UnaryPredicateType;
import com.logicblox.common.protocol.CommonProto.UnsignedIntConstant;
import com.logicblox.common.protocol.CommonProto.VariableDeclaration;

/**
 * Converts query to Logicblox-ready protocol buffer messages.
 */
public class QueryToProtoBuffer implements Rewriter<Query<?>, Rule> {

	private final Map<Term, Object> varTypes = new LinkedHashMap<>();

	private final Schema schema;
	
	/**
	 * Constructor for QueryToProtoBuffer.
	 * @param schema Schema
	 */
	public QueryToProtoBuffer(Schema schema) {
		this.schema = schema;
	}
	
	/**
	 * @param input Query<?>
	 * @return ProtocolBuffer rule, representing the input query.
	 */
	@Override
	public Rule rewrite(Query<?> input) throws RewriterException {
		Rule.Builder builder = Rule.newBuilder();
		builder.setBody(this.rewriteBody(input));
		builder.setHead(this.rewriteHead(input));
		builder.addAllVar(this.rewriteVariableDeclarations(input));
		return builder.build();
	}
	
	/**
	 * @param input Query<?>
	 * @return Collection<VariableDeclaration> base on the input query
	 */
	private Collection<VariableDeclaration> rewriteVariableDeclarations(Query<?> input) throws RewriterException {
		Collection<VariableDeclaration> result = new LinkedList<>();
		for (Term term: input.getTerms()) {
			if (term instanceof Named) {
				VariableDeclaration.Builder builder = VariableDeclaration.newBuilder();
				builder.setName(((Named) term).getName());
				builder.setType(this.rewriteType(term));
				result.add(builder.build());
			}
		}
		return result;
	}

	/**
	 * @param input Query<?>
	 * @return ClauseHead
	 */
	private ClauseHead rewriteHead(Query<?> input) {
		ClauseHead.Builder builder = ClauseHead.newBuilder();
		builder.addHeadAtom(HeadAtom.newBuilder().setAtom(
				this.rewriteAtom(input.getHead())));
		return builder.build();
	}
	
	/**
	 * @param input Query<?>
	 * @return ClauseBody
	 */
	private ClauseBody rewriteBody(Query<?> input) {
		ClauseBody.Builder builder = ClauseBody.newBuilder()
				.setFormula(this.rewriteFormula(input.getBody()));
		return builder.build();
	}

	/**
	 * @param formula uk.ac.ox.cs.pdq.formula.Formula
	 * @return Formula
	 */
	private Formula rewriteFormula(uk.ac.ox.cs.pdq.fol.Formula formula) {
		Formula.Builder builder = Formula.newBuilder();
		if (formula instanceof Predicate) {
			return builder.setKind(ATOM)
				.setAtom(this.rewriteAtom((Predicate) formula))
				.build();
		}
		if (formula instanceof Conjunction) {
			return builder.setKind(CONJUNCTION)
				.setConjunction(this.rewriteConjunction((Conjunction) formula))
				.build();
		}
		if (formula instanceof Disjunction) {
			return builder.setKind(DISJUNCTION)
				.setDisjunction(this.rewriteDisjunction((Disjunction) formula))
				.build();
		}
		if (formula instanceof Negation) {
			return builder.setKind(NEGATION)
				.setNegation(this.rewriteNegation((Negation) formula))
				.build();
		}
		throw new IllegalStateException();
	}
	
	/**
	 * @param formula Conjunction<?>
	 * @return com.logicblox.common.protocol.CommonProto.Conjunction
	 */
	private com.logicblox.common.protocol.CommonProto.Conjunction rewriteConjunction(Conjunction<?> formula) {
		com.logicblox.common.protocol.CommonProto.Conjunction.Builder builder = 
				com.logicblox.common.protocol.CommonProto.Conjunction.newBuilder();
		for (uk.ac.ox.cs.pdq.fol.Formula subForm: formula.getChildren()) {
			builder.addFormula(this.rewriteFormula(subForm));
		}
		return builder.build();
	}
	
	/**
	 * @param formula Disjunction<?>
	 * @return com.logicblox.common.protocol.CommonProto.Disjunction
	 */
	private com.logicblox.common.protocol.CommonProto.Disjunction rewriteDisjunction(Disjunction<?> formula) {
		com.logicblox.common.protocol.CommonProto.Disjunction.Builder builder = 
				com.logicblox.common.protocol.CommonProto.Disjunction.newBuilder();
		for (uk.ac.ox.cs.pdq.fol.Formula subForm: formula.getChildren()) {
			builder.addFormula(this.rewriteFormula(subForm));
		}
		return builder.build();
	}
	
	/**
	 * @param formula Negation<?>
	 * @return com.logicblox.common.protocol.CommonProto.Negation
	 */
	private com.logicblox.common.protocol.CommonProto.Negation rewriteNegation(Negation<?> formula) {
		com.logicblox.common.protocol.CommonProto.Negation.Builder builder = 
				com.logicblox.common.protocol.CommonProto.Negation.newBuilder();
		builder.setFormula(this.rewriteFormula(formula.getChild()));
		return builder.build();
	}

	
	/**
	 * @param predicate PredicateFormula
	 * @return Atom
	 */
	private Atom rewriteAtom(Predicate predicate) {
		Atom.Builder builder = Atom.newBuilder();
		builder.setPredicateName(predicate.getName());
		Signature signature = predicate.getSignature();
		
		for (int i = 0, l = predicate.getTermsCount(); i < l; i++) {
			Term term = predicate.getTerm(i);
			if (!this.varTypes.containsKey(term) && signature instanceof Relation) {
				this.varTypes.put(term, this.resolveType((Relation) signature, i));
			}
			if (i == 0 || i < predicate.getTermsCount() - 1) {
				builder.addKeyArgument(this.rewriteTerm(term));
			} else {
				builder.addValueArgument(this.rewriteTerm(term));
			}
		}
		return builder.build();
	}
	
	/**
	 * @param signature Relation
	 * @param i int
	 * @return the type of the ith term
	 */
	private Object resolveType(Relation signature, int i) {
		if (signature.getArity() == 1 && i == 0
			&& (Long.class.equals(signature.getAttribute(i).getType()))) {
			return signature;
		}
		// Attempting to find a unary typing constraint
		for (Constraint dep: this.schema.getDependencies()) {
			List<Predicate> body = dep.getBody().getPredicates();
			List<Predicate> head = dep.getRight().getPredicates();
			if (body.size() == 1 && head.size() == 1) {
				Predicate b = body.get(0);
				Predicate h = head.get(0);
				if (b.getSignature().equals(signature)
					&& h.getSignature().getArity() == 1
					&& b.getTerm(i).equals(h.getTerm(0))) {
					return h.getSignature();
				}
			}
		}
		return signature.getAttribute(i).getType();
	}
	
	/**
	 * @param term Term
	 * @return com.logicblox.common.protocol.CommonProto.Term
	 */
	private com.logicblox.common.protocol.CommonProto.Term rewriteTerm(Term term) {
		com.logicblox.common.protocol.CommonProto.Term.Builder builder =
				com.logicblox.common.protocol.CommonProto.Term.newBuilder();
		if (term.isVariable() || term.isSkolem()) {
			builder.setKind(VARIABLE);
			return builder.setVariable(
					com.logicblox.common.protocol.CommonProto.Variable.newBuilder()
						.setName(((Named) term).getName()))
						.build();
		}
		if (!(term instanceof TypedConstant)) {
			throw new IllegalStateException();
		}
		builder.setKind(CONSTANT);
		builder.setConstant(this.rewriteConstant((TypedConstant) term));
		return builder.build();
	}

	/**
	 * @param constant TypedConstant<?>
	 * @return com.logicblox.common.protocol.CommonProto.Constant
	 */
	private com.logicblox.common.protocol.CommonProto.Constant rewriteConstant(TypedConstant<?> constant) {
		Constant.Builder builder = Constant.newBuilder();
		Type type = constant.getType();
		if (type.equals(String.class)) {
			return builder.setKind(Kind.STRING).setStringConstant(
					StringConstant.newBuilder().setValue(
							(String) constant.getValue()).build()).build();
		}
		if (type.equals(Boolean.class)) {
			return builder.setKind(Kind.BOOL).setBoolConstant(
					BoolConstant.newBuilder().setValue(
							(Boolean) constant.getValue()).build()).build();
		}
		if (type.equals(Integer.class)) {
			return builder.setKind(Kind.INT).setIntConstant(
					IntConstant.newBuilder().setValue(
							(Integer) constant.getValue()).build()).build();
		}
		if (type.equals(Long.class)) {
			return builder.setKind(Kind.UINT).setUintConstant(
					UnsignedIntConstant.newBuilder().setValue(
							(Long) constant.getValue()).build()).build();
		}
		if (type.equals(Float.class)) {
			return builder.setKind(Kind.FLOAT).setFloatConstant(
					FloatConstant.newBuilder().setValue(
							String.valueOf(constant.getValue())).build()).build();
		}
		if (type.equals(Double.class)) {
			return builder.setKind(Kind.DECIMAL).setDecimalConstant(
					DecimalConstant.newBuilder().setValue(
							String.valueOf(constant.getValue())).build()).build();
		}
		if (type.equals(Date.class)) {
			return builder.setKind(Kind.DATETIME).setDateTimeConstant(
					DateTimeConstant.newBuilder().setValue(
							(Long) constant.getValue()).build()).build();
		}
		return builder.build();

	}
	
	/**
	 * @param term Term
	 * @return com.logicblox.common.protocol.CommonProto.Type
	 */
	private com.logicblox.common.protocol.CommonProto.Type rewriteType(Term term) throws RewriterException {
		com.logicblox.common.protocol.CommonProto.Type.Builder builder =
				com.logicblox.common.protocol.CommonProto.Type.newBuilder();
		Object type = null;
		if ((term.isVariable() || term.isSkolem()) && this.varTypes.containsKey(term)) {
			type = this.varTypes.get(term);
		} else if (term instanceof Typed) {
			type = ((Typed) term).getType();
		} else {
			throw new RewriterException("Type of term '" + term + "' cannot be determined.");
		}
		if (type == null) {
			throw new RewriterException("Type of term '" + term + "' could not be determined.");
		}
		if (type instanceof Signature) {
			builder.setKind(UNARY);
			return builder.setUnary(UnaryPredicateType.newBuilder()
					.setName(((Signature) type).getName()).build()).build();
		}
		if (type instanceof Class<?>) {
			Class<?> cl = (Class<?>) type;
			builder.setKind(PRIMITIVE);
			if (cl.isAssignableFrom(String.class)) {
				return builder.setPrimitive(PrimitiveType.newBuilder()
						.setKind(STRING).build()).build();
			}
			if (cl.isAssignableFrom(Boolean.class)) {
				return builder.setPrimitive(PrimitiveType.newBuilder()
						.setKind(BOOL).build()).build();
			}
			if (cl.isAssignableFrom(Integer.class)) {
				return builder.setPrimitive(PrimitiveType.newBuilder()
						.setKind(INT).build()).build();
			}
			if (cl.isAssignableFrom(Long.class)) {
				return builder.setPrimitive(PrimitiveType.newBuilder()
						.setKind(UINT).build()).build();
			}
			if (cl.isAssignableFrom(Float.class)) {
				return builder.setPrimitive(PrimitiveType.newBuilder()
						.setKind(FLOAT).build()).build();
			}
			if (cl.isAssignableFrom(Double.class)) {
				return builder.setPrimitive(PrimitiveType.newBuilder()
						.setKind(DECIMAL).build()).build();
			}
			if (cl.isAssignableFrom(Date.class)) {
				return builder.setPrimitive(PrimitiveType.newBuilder()
						.setKind(DATETIME).build()).build();
			}
		}
		return builder.build();
	}
}
