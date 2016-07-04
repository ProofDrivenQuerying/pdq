package uk.ac.ox.cs.pdq.io.xml;

import static uk.ac.ox.cs.pdq.db.AccessMethod.Types.FREE;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.log4j.Logger;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import uk.ac.ox.cs.pdq.algebra.Access;
import uk.ac.ox.cs.pdq.algebra.Count;
import uk.ac.ox.cs.pdq.algebra.CrossProduct;
import uk.ac.ox.cs.pdq.algebra.DependentAccess;
import uk.ac.ox.cs.pdq.algebra.DependentJoin;
import uk.ac.ox.cs.pdq.algebra.Distinct;
import uk.ac.ox.cs.pdq.algebra.IsEmpty;
import uk.ac.ox.cs.pdq.algebra.Join;
import uk.ac.ox.cs.pdq.algebra.Projection;
import uk.ac.ox.cs.pdq.algebra.RelationalOperator;
import uk.ac.ox.cs.pdq.algebra.Scan;
import uk.ac.ox.cs.pdq.algebra.Selection;
import uk.ac.ox.cs.pdq.algebra.StaticInput;
import uk.ac.ox.cs.pdq.algebra.Union;
import uk.ac.ox.cs.pdq.algebra.predicates.AttributeEqualityPredicate;
import uk.ac.ox.cs.pdq.algebra.predicates.ConjunctivePredicate;
import uk.ac.ox.cs.pdq.algebra.predicates.ConstantEqualityPredicate;
import uk.ac.ox.cs.pdq.algebra.predicates.Predicate;
import uk.ac.ox.cs.pdq.db.AccessMethod;
import uk.ac.ox.cs.pdq.db.Attribute;
import uk.ac.ox.cs.pdq.db.Relation;
import uk.ac.ox.cs.pdq.db.Schema;
import uk.ac.ox.cs.pdq.db.TypedConstant;
import uk.ac.ox.cs.pdq.fol.Term;
import uk.ac.ox.cs.pdq.fol.Variable;
import uk.ac.ox.cs.pdq.util.Typed;
import uk.ac.ox.cs.pdq.util.Utility;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * Reads physical operators from XML.
 * 
 * @author Julien Leblay
 */
public class OperatorReader extends AbstractXMLReader<RelationalOperator> {

	/** Logger. */
	private static Logger log = Logger.getLogger(OperatorReader.class);

	/**
	 * The Enum Types.
	 */
	public static enum Types {
		
		/** The select. */
		SELECT, 
 /** The project. */
 PROJECT, 
 /** The rename. */
 RENAME, 
 /** The static input. */
 STATIC_INPUT, 
 /** The cross product. */
 CROSS_PRODUCT,
		
		/** The dependent access. */
		DEPENDENT_ACCESS, 
 /** The access. */
 ACCESS, 
 /** The dependent join. */
 DEPENDENT_JOIN, 
 /** The join. */
 JOIN, 
 /** The alias. */
 ALIAS,
		
		/** The distinct. */
		DISTINCT, 
 /** The count. */
 COUNT, 
 /** The is empty. */
 IS_EMPTY, 
 /** The union. */
 UNION
	}
	
	/** The operator being built. */
	private RelationalOperator operator = null;

	/** The schema from which the relation come. */
	protected Schema schema = null;
	
	/** The type. */
	protected Types type;
	
	/** The variant. */
	protected Join.Variants variant;
	
	/** The predicate. */
	protected Predicate predicate = null;

	/** The conjunction. */
	protected List<Predicate> conjunction = Lists.newLinkedList();
	
	/** The sideways. */
	protected List<Integer> sideways = Lists.newLinkedList();
	
	/** The outputs. */
	protected List<Typed> outputs = Lists.newLinkedList();
	
	/** The projection. */
	protected List<Term> projection = Lists.newLinkedList();
	
	/** The static inputs. */
	protected Map<Integer, TypedConstant<?>> staticInputs = Maps.newLinkedHashMap();
	
	/** The children. */
	protected List<RelationalOperator> children = Lists.newLinkedList();
	
	/** The relation name. */
	protected String relationName;
	
	/** The access method name. */
	protected String accessMethodName;
	
	/** Boolean tracking wither the reader is current reading conjunctions. */
	private boolean inConjunction = false;
	
	/** Boolean tracking wither the reader is current reading outputs. */
	private boolean inOutputs = false;
	
	/** Boolean tracking wither the reader is current reading options. */
	private boolean inOptions = false;

	/** The aliases. */
	private final Map<String, RelationalOperator> aliases;
	
	/** The alias. */
	private String alias;
	
	/**  The child operator reader. */
	private OperatorReader childReader;
	
	/**  The parent operator reader. */
	private OperatorReader parentReader;

	/**
	 * Default constructor.
	 *
	 * @param schema the schema
	 */
	public OperatorReader(Schema schema) {
		this(schema, Maps.<String, RelationalOperator>newHashMap(), null);
	}

	/**
	 * Default constructor.
	 *
	 * @param schema the schema
	 * @param aliases the aliases
	 */
	public OperatorReader(Schema schema, Map<String, RelationalOperator> aliases) {
		this(schema, aliases, null);
	}

	/**
	 * Default constructor.
	 *
	 * @param schema the schema
	 * @param parent the parent
	 */
	public OperatorReader(Schema schema, OperatorReader parent) {
		this(schema, Maps.<String, RelationalOperator>newHashMap(), parent);
	}

	/**
	 * Default constructor.
	 *
	 * @param schema the schema
	 * @param aliases the aliases
	 * @param parent the parent
	 */
	OperatorReader(Schema schema, Map<String, RelationalOperator> aliases, OperatorReader parent) {
		this.schema = schema;
		this.aliases = aliases;
		this.parentReader = parent;
	}
	
	/**
	 * Adds A child.
	 *
	 * @param i the i
	 */
	public void addChild(RelationalOperator i) {
		this.children.add(i);
		this.childReader = new OperatorReader(this.schema, this.aliases, this);
	}
	
	/**
	 * {@inheritDoc}
	 * @see uk.ac.ox.cs.pdq.io.Reader#read(java.io.InputStream)
	 */
	@Override
	public RelationalOperator read(InputStream in) {
		try {
			SAXParserFactory factory = SAXParserFactory.newInstance();
			SAXParser parser = factory.newSAXParser();
			parser.parse(in, this);
			return this.getOperator();
		} catch (ParserConfigurationException | SAXException | IOException e) {
			log.error(e);
		}
		return null;
	}

	/**
	 * Gets the operator.
	 *
	 * @return the operator under construction
	 */
	public RelationalOperator getOperator() {
		return this.operator;
	}

	/**
	 * {@inheritDoc}
	 * @see org.xml.sax.helpers.DefaultHandler#startElement(java.lang.String, java.lang.String, java.lang.String, org.xml.sax.Attributes)
	 */
	@Override
	public void startElement(String uri, String localName, String qName, Attributes atts) {
		if (isProcessingChild()) {
			this.childReader.startElement(uri, localName, qName, atts);
		} else switch(QNames.parse(qName)) {
		case OPERATOR:
			this.type = Types.valueOf(this.getValue(atts, QNames.TYPE));
			this.alias = this.getValue(atts, QNames.NAME);
			Preconditions.checkState(this.type == Types.ALIAS ? this.alias != null : true);
			if (this.type == Types.JOIN) {
				this.variant = Join.Variants.valueOf(this.getValue(atts, QNames.VARIANT));
			}
			if (this.type == Types.DEPENDENT_JOIN) {
				String inputsString = this.getValue(atts, QNames.PARAM);
				if (!Strings.isNullOrEmpty(inputsString)) {
					this.sideways = new ArrayList<>();
					for (String position: inputsString.split(",")) {
						this.sideways.add(Integer.parseInt(position));
					}
				}
			}
			this.relationName = this.getValue(atts, QNames.RELATION);
			this.accessMethodName = this.getValue(atts, QNames.ACCESS_METHOD);
			break;

		case CONJUNCTION:
			this.inConjunction = true;
			this.conjunction = Lists.newArrayList();
			break;

		case PREDICATE:
			this.inConjunction = true;
			int left = Integer.valueOf(this.getValue(atts, QNames.LEFT));
			if (this.getValue(atts, QNames.RIGHT) == null) {
				this.predicate = new ConstantEqualityPredicate(
						left,
						new TypedConstant<>(
								uk.ac.ox.cs.pdq.util.Types.cast(
									this.outputs.get(left).getType(),
									this.getValue(atts, QNames.VALUE))));
			} else {
				this.predicate = new AttributeEqualityPredicate(
						left,
						Integer.valueOf(this.getValue(atts, QNames.RIGHT)));
			}
			break;

		case OUTPUTS:
			this.inOutputs = true;
			this.outputs = Lists.newLinkedList();
			break;

		case PROJECT:
			this.inOptions = true;
			this.projection = Lists.newLinkedList();
			break;

		case STATIC_INPUT:
			this.inOptions = true;
			this.staticInputs = Maps.newLinkedHashMap();
			break;
	
		case CHILD:
		case CHILDREN:
			this.childReader = new OperatorReader(this.schema, this.aliases, this);
			break;

		case ATTRIBUTE:
			if (this.inOutputs) {
				try {
					this.outputs.add(new Attribute(
							Class.forName(this.getValue(atts, QNames.TYPE)),
							this.getValue(atts, QNames.NAME)));
				} catch (ClassNotFoundException e) {
					throw new IllegalStateException();
				}
			} else if (this.inOptions) {
				this.projection.add(new Variable(this.getValue(atts, QNames.NAME)));
			} else {
				throw new IllegalStateException("Attempting to read term while not in output list.");
			}
			break;

		case CONSTANT:
			if (this.inOutputs) {
				try {
					this.outputs.add(
							new TypedConstant<>(uk.ac.ox.cs.pdq.util.Types.cast(
							Class.forName(this.getValue(atts, QNames.TYPE)),
							this.getValue(atts, QNames.VALUE))));
				} catch (ClassNotFoundException e) {
					throw new IllegalStateException();
				}
			} else if (this.inOptions) {
				try {
					this.projection.add(
							new TypedConstant<>(uk.ac.ox.cs.pdq.util.Types.cast(
									Class.forName(this.getValue(atts, QNames.TYPE)),
									this.getValue(atts, QNames.VALUE))));
				} catch (ClassNotFoundException e) {
					throw new IllegalStateException();
				}
			} else {
				throw new IllegalStateException("Attempting to read term while not in output list.");
			}
			break;

		case INPUT:
			if (this.inOptions) {
				try {
					this.staticInputs.put(
							Integer.valueOf(this.getValue(atts, QNames.ATTRIBUTE)),
							new TypedConstant<>(uk.ac.ox.cs.pdq.util.Types.cast(
							Class.forName(this.getValue(atts, QNames.TYPE)),
							this.getValue(atts, QNames.VALUE))));
				} catch (ClassNotFoundException e) {
					throw new IllegalStateException();
				}
			} else {
				throw new IllegalStateException("Attempting to read term while not in option list.");
			}
			break;

		default:
			throw new IllegalStateException("In children of " + this.getClass().getSimpleName());
		}				
	}

	/**
	 * Checks if is processing child.
	 *
	 * @return true, if is processing child
	 */
	private boolean isProcessingChild() {
		return this.childReader != null;
	}
	
	/**
	 * {@inheritDoc}
	 * @see org.xml.sax.helpers.DefaultHandler#endElement(java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public void endElement(String uri, String localName, String qName) {
		if (isProcessingChild()) {
			if (!this.childReader.isProcessingChild() 
				&& (QNames.parse(qName) == QNames.CHILDREN
					|| QNames.parse(qName) == QNames.CHILD)){
				this.childReader = null;
				return;
			}
			this.childReader.endElement(uri, localName, qName);
		} else switch(QNames.parse(qName)) {
		case OPERATOR:
			
			switch (this.type) {
			case DISTINCT:
				this.operator = new Distinct(this.children.get(0));
				break;
			case COUNT:
				this.operator = new Count(this.children.get(0));
				break;
			case IS_EMPTY:
				this.operator = new IsEmpty(this.children.get(0));
				break;
			case SELECT:
				this.operator = new Selection(this.predicate, this.children.get(0));
				break;
			case PROJECT:
				Map<Integer, Term> renaming = new LinkedHashMap<>();
				int i = 0;
				for (Term t: this.projection) {
					if (t.isVariable() || t.isSkolem()) {
						Term r = Utility.typedToTerm(this.outputs.get(i));
						if (!String.valueOf(r).equals(String.valueOf(t))) {
							renaming.put(this.children.get(0).getColumns().indexOf(t), r);
						}
					}
					i++;
				}
				this.operator = new Projection(this.children.get(0), renaming, this.projection);
				break;
			case STATIC_INPUT:
				this.operator = new StaticInput(Utility.toTypedConstants(this.outputs));
				break;
			case CROSS_PRODUCT:
				this.operator = new CrossProduct(this.children);
				break;
			case UNION:
				this.operator = new Union(this.children);
				break;
			case DEPENDENT_JOIN:
				if (this.sideways == null || this.sideways.isEmpty()) {
					log.warn("Sideways information missing. Inferring from LHS.");
					this.sideways = new ArrayList<>();
					for (Term t: this.children.get(1).getInputTerms()) {
						int index = this.children.get(0).getColumns().indexOf(t);
						if (index < 0) {
							throw new IllegalStateException("No sideways inputs provided");
						}
						this.sideways.add(index);
					}
				}
				this.operator = new DependentJoin(this.children.get(0), this.children.get(1), this.sideways);
				break;
			case JOIN:
				this.operator = new Join(this.predicate, this.children);
				((Join) this.operator).setVariant(this.variant);
				break;
			case ACCESS:
				Relation r = this.schema.getRelation(this.relationName);
				Preconditions.checkState(r != null, "Plan refers to unknow relation " + this.relationName);
				AccessMethod b = null;
				if (this.accessMethodName == null) {
					for (AccessMethod bm: r.getAccessMethods()) {
						if (bm.getType() == FREE) {
							b = bm;
							break;
						}
					}
				} else {
					b = r.getAccessMethod(this.accessMethodName);
				}
				if (b == null) {
					throw new IllegalStateException("No access method defined for relation " + this.accessMethodName);
				}
				if (b.getType() == FREE) {
					this.operator = new Scan(r);
				} else if (this.children.isEmpty()) {
					this.operator = new DependentAccess(r, b, this.staticInputs);
				} else {
					this.operator = new Access(r, b, this.children.get(0));
				}
				break;
			case ALIAS:
				this.operator = this.aliases.get(this.alias);
				Preconditions.checkState(this.operator != null);
				break;
			default:
				throw new IllegalStateException("Unsupported operator type " + this.type);
			}
			if (this.type != Types.ALIAS && this.alias != null) {
				this.aliases.put(this.alias, this.operator);
			}
			this.alias = null;
			if (this.parentReader != null) {
				this.parentReader.addChild(this.operator);
			}
			break;
		case CONJUNCTION:
			this.inConjunction = false;
			this.predicate = new ConjunctivePredicate<>(this.conjunction);
			this.conjunction = Lists.newArrayList();
			break;
		case PREDICATE:
			if (this.inConjunction) {
				this.conjunction.add(this.predicate);
			}
			break;
		case OUTPUTS:
			this.inOutputs = false;
			break;
		case PROJECT:
			this.inOptions = false;
			break;
		case STATIC_INPUT:
			this.inOptions = false;
			break;
		}				
	}
}
