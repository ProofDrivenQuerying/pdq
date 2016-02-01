package uk.ac.ox.cs.pdq.io.xml;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Stack;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import com.google.common.collect.Iterators;

import uk.ac.ox.cs.pdq.algebra.RelationalOperator;
import uk.ac.ox.cs.pdq.db.Schema;
import uk.ac.ox.cs.pdq.io.ReaderException;
import uk.ac.ox.cs.pdq.plan.AccessOperator;
import uk.ac.ox.cs.pdq.plan.DoubleCost;
import uk.ac.ox.cs.pdq.plan.LeftDeepPlan;

/**
 * Reads plans from XML.
 * 
 * @author Julien Leblay
 */
public class LeftDeepPlanReader extends AbstractXMLReader<LeftDeepPlan> {
	
	/** The plan's cost. */
	private Double cost = null;
	
	/** The pan being built */
	private LeftDeepPlan plan = null;
	
	/** The operator if the command being read */
	private RelationalOperator operator = null;
	
	/** The current access operator being read */
	private AccessOperator access = null;
	
	/** The map from logical operator to aliases */
	private Map<String, RelationalOperator> aliases = new LinkedHashMap<>();
	
	/** The last operator name read */
	private String name;

	/** The operator reader */
	private OperatorReader operatorReader;

	private Schema schema;
	
	/**
	 * Default constructor
	 * @param schema Schema
	 */
	public LeftDeepPlanReader(Schema schema) {
		this.schema = schema;
	}

	/**
	 * {@inheritDoc}
	 * @see uk.ac.ox.cs.pdq.io.Reader#read(java.io.InputStream)
	 */
	@Override
	public LeftDeepPlan read(InputStream in) {
		try {
			SAXParserFactory factory = SAXParserFactory.newInstance();
			SAXParser parser = factory.newSAXParser();
			parser.parse(in, this);
			return this.plan;
		} catch (ParserConfigurationException | SAXException | IOException e) {
			throw new ReaderException(e.getMessage());
		}
	}

	/**
	 * 
	 * {@inheritDoc}
	 * @see org.xml.sax.helpers.DefaultHandler#startElement(java.lang.String, java.lang.String, java.lang.String, org.xml.sax.Attributes)
	 */
	@Override
	public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
		switch(QNames.parse(qName)) {
		case EMPTY:
			break;
			
		case PLAN:
			String c = this.getValue(atts, QNames.COST);
			if (c != null) {
				this.cost = Double.valueOf(c);
			}
			break;

		case COMMAND:
			this.operatorReader = new OperatorReader(this.schema, this.aliases);
			this.name = this.getValue(atts, QNames.NAME);
			break;

		default:
			this.operatorReader.startElement(uri, localName, qName, atts);
		}
	}

	/**
	 * {@inheritDoc}
	 * @see org.xml.sax.helpers.DefaultHandler#endElement(java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException {
		switch(QNames.parse(qName)) {
		case EMPTY:
			this.plan = null;
			return;
			
		case PLAN:
			if (this.cost != null) {
				this.plan.setCost(new DoubleCost(this.cost));
			}
			break;

		case COMMAND:
			this.operator = this.operatorReader.getOperator();
			Collection<AccessOperator> accesses = RelationalOperator.getAccesses(this.operator);
			this.access = Iterators.getLast(accesses.iterator());
			this.plan = new LeftDeepPlan(this.operator, this.access, this.plan, null);
			//this.plan = new LeftDeepPlan(this.operator, this.plan, null);
			this.aliases.put(this.name, this.operator);
			this.name = null;
			break;

		default:
			this.operatorReader.endElement(uri, localName, qName);
			return;
		}
	}
}
