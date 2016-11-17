package uk.ac.ox.cs.pdq.io.xml;

import java.io.PrintStream;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import uk.ac.ox.cs.pdq.algebra.Access;
import uk.ac.ox.cs.pdq.algebra.Count;
import uk.ac.ox.cs.pdq.algebra.CrossProduct;
import uk.ac.ox.cs.pdq.algebra.DependentAccess;
import uk.ac.ox.cs.pdq.algebra.DependentJoin;
import uk.ac.ox.cs.pdq.algebra.Distinct;
import uk.ac.ox.cs.pdq.algebra.IsEmpty;
import uk.ac.ox.cs.pdq.algebra.Join;
import uk.ac.ox.cs.pdq.algebra.NaryOperator;
import uk.ac.ox.cs.pdq.algebra.Projection;
import uk.ac.ox.cs.pdq.algebra.RelationalOperator;
import uk.ac.ox.cs.pdq.algebra.Scan;
import uk.ac.ox.cs.pdq.algebra.Selection;
import uk.ac.ox.cs.pdq.algebra.StaticInput;
import uk.ac.ox.cs.pdq.algebra.SubPlanAlias;
import uk.ac.ox.cs.pdq.algebra.UnaryOperator;
import uk.ac.ox.cs.pdq.algebra.Union;
import uk.ac.ox.cs.pdq.algebra.predicates.AttributeEqualityPredicate;
import uk.ac.ox.cs.pdq.algebra.predicates.ConjunctivePredicate;
import uk.ac.ox.cs.pdq.algebra.predicates.ConstantEqualityPredicate;
import uk.ac.ox.cs.pdq.algebra.predicates.Predicate;
import uk.ac.ox.cs.pdq.db.Attribute;
import uk.ac.ox.cs.pdq.db.TypedConstant;
import uk.ac.ox.cs.pdq.fol.Term;
import uk.ac.ox.cs.pdq.io.xml.OperatorReader.Types;
import uk.ac.ox.cs.pdq.plan.AccessOperator;
import uk.ac.ox.cs.pdq.util.Tuple;
import uk.ac.ox.cs.pdq.util.TupleType;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;

/**
 * Writes plans to XML.
 * 
 * @author Julien Leblay
 */
public class OperatorWriter extends AbstractXMLWriter<RelationalOperator> {

	/**
	 * Writes the given plan to the given output.
	 *
	 * @param out the out
	 * @param operator the operator
	 */
	public void writeOperator(PrintStream out, RelationalOperator operator) {
		this.writeOperator(out, operator, Maps.<RelationalOperator, String>newHashMap());
	}

	/**
	 * Writes the given plan to the given output.
	 *
	 * @param out the out
	 * @param operator the operator
	 * @param aliases the aliases
	 */
	public void writeOperator(PrintStream out, RelationalOperator operator, Map<RelationalOperator, String> aliases) {
		Preconditions.checkArgument(out != null);
		Preconditions.checkArgument(operator != null);
		Preconditions.checkArgument(aliases != null);
		Map<QNames, String> att = new LinkedHashMap<>();
		
		Types type = typeOf(operator);
		att.put(QNames.TYPE, type.toString());
		if (type == Types.ALIAS) {
			RelationalOperator aliased =((SubPlanAlias) operator).getPlan().getOperator();
			String alias = aliases.get(aliased);
			Preconditions.checkState(alias != null);
			att.put(QNames.NAME, alias);
		}
		switch (type) {
		case JOIN:
			att.put(QNames.VARIANT, ((Join) operator).getVariant().toString());
			break;
		case DEPENDENT_JOIN:
			att.put(QNames.PARAM, Joiner.on(",").join(((DependentJoin) operator).getSidewaysInput()));
			break;
		case ACCESS:
			att.put(QNames.RELATION, ((AccessOperator) operator).getRelation().getName());
			att.put(QNames.ACCESS_METHOD, ((AccessOperator) operator).getAccessMethod().getName());
			break;
		}
		if (type == Types.ALIAS) {
			openclose(out, QNames.OPERATOR, att);
			return;
		}
		open(out, QNames.OPERATOR, att);
		this.writeOutputs(out, operator);
		this.writeOptions(out, operator, type);
		this.writeChildren(out, operator, aliases);
		close(out, QNames.OPERATOR);
	}
	
	/**
	 * Gets the type of the given operator.
	 *
	 * @param operator the operator
	 * @return the types
	 */
	static Types typeOf(RelationalOperator operator) {
		if (operator instanceof Selection) {
			return Types.SELECT;
		}
		if (operator instanceof Projection) {
			return Types.PROJECT;
		}
		if (operator instanceof DependentJoin) {
			return Types.DEPENDENT_JOIN;
		}
		if (operator instanceof Join) {
			if (!(operator instanceof DependentJoin) && !((Join) operator).hasPredicate()) {
				return Types.CROSS_PRODUCT;
			}
			return Types.JOIN;
		}
		if (operator instanceof CrossProduct) {
			return Types.CROSS_PRODUCT;
		}
		if (operator instanceof DependentAccess || operator instanceof Access || operator instanceof Scan) {
			return Types.ACCESS;
		}
		if (operator instanceof StaticInput) {
			return Types.STATIC_INPUT;
		}
		if (operator instanceof Distinct) {
			return Types.DISTINCT;
		}
		if (operator instanceof Count) {
			return Types.COUNT;
		}
		if (operator instanceof IsEmpty) {
			return Types.IS_EMPTY;
		}
		if (operator instanceof Union) {
			return Types.UNION;
		}
		if (operator instanceof SubPlanAlias) {
			return Types.ALIAS;
		}
		throw new IllegalArgumentException("Unsupported operator type " + operator);
	}

	/**
	 * Writes the given plan to the given output.
	 *
	 * @param out the out
	 * @param operator the operator
	 * @param aliases the aliases
	 */
	public void writeChildren(PrintStream out, RelationalOperator operator, Map<RelationalOperator, String> aliases) {
		if (operator instanceof UnaryOperator) {
			open(out, QNames.CHILD);
			this.writeOperator(out, ((UnaryOperator) operator).getChild(), aliases);
			close(out, QNames.CHILD);
			return;
		}
		if (operator instanceof NaryOperator) {
			List<RelationalOperator> children = ((NaryOperator) operator).getChildren();
			open(out, QNames.CHILDREN);
			for (RelationalOperator child: children) {
				this.writeOperator(out, child, aliases);
			}
			close(out, QNames.CHILDREN);
			return;
		}
	}

	/**
	 * Writes the given plan to the given output.
	 *
	 * @param out the out
	 * @param plan the plan
	 */
	public void writeOutputs(PrintStream out, RelationalOperator plan) {
		open(out, QNames.OUTPUTS);
		if (plan instanceof StaticInput) {
			Collection<Tuple> tuples = ((StaticInput) plan).getTuples();
			Preconditions.checkState(tuples.size() == 1, "Single tuple static input only supported for now.");
			Tuple first = tuples.iterator().next();
			TupleType ttype = first.getType();
			for (int j = 0, l = ttype.size(); j < l; j++) {
				this.writeConstant(out, new TypedConstant<>(uk.ac.ox.cs.pdq.util.Types.cast(ttype.getType(j), first.getValue(j))));
			}
		} else for (int i = 0, l = plan.getColumns().size(); i < l; i++) {
			Term t = plan.getColumn(i);
			if (t instanceof TypedConstant) {
				TypedConstant<?> c = ((TypedConstant) t);
				this.writeConstant(out, new TypedConstant<>(uk.ac.ox.cs.pdq.util.Types.cast(c.getType(), c.getValue())));
			} else {
				this.writeAttribute(out, new Attribute(plan.getType().getType(i), String.valueOf(t)));
			}
		}
		close(out, QNames.OUTPUTS);
	}
	
	/**
	 * Writes the given plan to the given output.
	 *
	 * @param out the out
	 * @param operator the operator
	 * @param type the type
	 */
	public void writeOptions(PrintStream out, RelationalOperator operator, Types type) {
		switch (type) {
		case PROJECT:
			Projection proj = ((Projection) operator);
			List<Term> projected = proj.getProjected();
			int i = 0;
			open(out, QNames.PROJECT);
			for (Term t: projected) {
				if (t.isVariable() || t.isUntypedConstant()) {
					this.writeAttribute(out, new Attribute(proj.getType().getType(i), t.toString()));
				} else {
					this.writeConstant(out, (TypedConstant) t);
				}
				i++;
			}
			close(out, QNames.PROJECT);
			break;
		case SELECT:
			Predicate sp = ((Selection) operator).getPredicate();
			if (sp != null) {
				this.writePredicate(out, sp);
			}
			break;
		case JOIN:
			if (((Join) operator).hasPredicate()) {
				this.writePredicate(out, ((Join) operator).getPredicate());
			}
			break;
		case ACCESS:
			if (operator instanceof DependentAccess) {
				Map<Integer, TypedConstant<?>> inputs = ((DependentAccess) operator).getStaticInputs();
				if (inputs != null && !inputs.isEmpty()) {
					open(out, QNames.STATIC_INPUT);
					for (Integer j: inputs.keySet()) {
						this.writeStaticInput(out, j, inputs.get(j));
					}
					close(out, QNames.STATIC_INPUT);
				}
			}
			break;
		}
	}

	/**
	 * Writes the given attribute to the given output.
	 *
	 * @param out the output stream being written to
	 * @param attribute the attribute
	 */
	public void writeAttribute(PrintStream out, Attribute attribute) {
		Map<QNames, String> att = new LinkedHashMap<>();
		att.put(QNames.NAME, attribute.getName());
		att.put(QNames.TYPE, uk.ac.ox.cs.pdq.util.Types.canonicalName(attribute.getType()));
		openclose(out, QNames.ATTRIBUTE, att);
	}

	/**
	 * Writes the given constant to the given output.
	 *
	 * @param out the output stream being written to
	 * @param constant the constant
	 */
	public void writeConstant(PrintStream out, TypedConstant<?> constant) {
		Map<QNames, String> att = new LinkedHashMap<>();
		att.put(QNames.VALUE, String.valueOf(constant.getValue()));
		att.put(QNames.TYPE, uk.ac.ox.cs.pdq.util.Types.canonicalName(constant.getType()));
		openclose(out, QNames.CONSTANT, att);
	}

	/**
	 * Writes the given relation to the given output.
	 * TOCOMMENT: CUT AND PAST HERE
	 *
	 * @param out the out
	 * @param i the i
	 * @param constant the constant
	 */
	public void writeStaticInput(PrintStream out, Integer i, TypedConstant<?> constant) {
		Map<QNames, String> att = new LinkedHashMap<>();
		att.put(QNames.ATTRIBUTE, String.valueOf(i));
		att.put(QNames.VALUE, String.valueOf(constant.getValue()));
		att.put(QNames.TYPE, uk.ac.ox.cs.pdq.util.Types.canonicalName(constant.getType()));
		openclose(out, QNames.INPUT, att);
	}

	/**
	 * Writes the given select command to the given output.
	 *
	 * @param out the out
	 * @param predicate the predicate
	 */
	private void writePredicate(PrintStream out, Predicate predicate) {
		if (predicate instanceof ConjunctivePredicate<?>) {
			open(out, QNames.CONJUNCTION);
			for (Predicate p: ((ConjunctivePredicate<?>) predicate)) {
				this.writePredicate(out, p);
			}
			close(out, QNames.CONJUNCTION);
		} else if (predicate instanceof ConstantEqualityPredicate) {
			Map<QNames, String> att = new LinkedHashMap<>();
			att.put(QNames.TYPE, "equality");
			att.put(QNames.LEFT, String.valueOf(((ConstantEqualityPredicate) predicate).getPosition()));
			att.put(QNames.VALUE, String.valueOf(((ConstantEqualityPredicate) predicate).getValue().getValue()));
			openclose(out, QNames.PREDICATE, att);

		} else if (predicate instanceof AttributeEqualityPredicate) {
			Map<QNames, String> att = new LinkedHashMap<>();
			att.put(QNames.TYPE, "equality");
			att.put(QNames.LEFT, String.valueOf(((AttributeEqualityPredicate) predicate).getPosition()));
			att.put(QNames.RIGHT, String.valueOf(((AttributeEqualityPredicate) predicate).getOther()));
			openclose(out, QNames.PREDICATE, att);
			
		} else {
			throw new UnsupportedOperationException(predicate + " is not a supported predicate type.");
		}
	}

	/**
	 * {@inheritDoc}
	 * @see uk.ac.ox.cs.pdq.io.Writer#write(java.io.PrintStream, java.lang.Object)
	 */
	@Override
	public void write(PrintStream out, RelationalOperator o) {
		out.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
		this.writeOperator(out, o);
	}
}
