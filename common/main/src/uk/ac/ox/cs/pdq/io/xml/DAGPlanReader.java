package uk.ac.ox.cs.pdq.io.xml;

import java.io.IOException;
import java.io.InputStream;
import java.util.Stack;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.log4j.Logger;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import uk.ac.ox.cs.pdq.algebra.RelationalOperator;
import uk.ac.ox.cs.pdq.db.Schema;
import uk.ac.ox.cs.pdq.io.ReaderException;
import uk.ac.ox.cs.pdq.plan.DAGPlan;
import uk.ac.ox.cs.pdq.plan.DoubleCost;

/**
 * Reads plans from XML.
 * 
 * @author Julien Leblay
 */
public class DAGPlanReader extends AbstractXMLReader<DAGPlan> {

	/** Logger. */
	private static Logger log = Logger.getLogger(DAGPlanReader.class);
	
	/** The plan being built */
	private DAGPlan plan = null;
	
	/** The plan cost*/
	private Double cost;
	
	/** The current operator being built */
	private RelationalOperator operator = null;

	/** The operator reader */
	private Stack<OperatorReader> operatorReaders = new Stack<>();
	
	private Schema schema = null;
	
	/**
	 * Default constructor
	 * @param schema Schema
	 */
	public DAGPlanReader(Schema schema) {
		this.schema = schema;
	}

	/**
	 * @param in InputStream
	 * @return DAGPlan
	 * @see uk.ac.ox.cs.pdq.io.Reader#read(InputStream)
	 */
	@Override
	public DAGPlan read(InputStream in) {
		try {
			SAXParserFactory factory = SAXParserFactory.newInstance();
			SAXParser parser = factory.newSAXParser();
			parser.parse(in, this);
			return this.plan;
		} catch (ParserConfigurationException | SAXException | IOException e) {
			throw new ReaderException(e.getMessage());
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.xml.sax.helpers.DefaultHandler#startElement(java.lang.String, java.lang.String, java.lang.String, org.xml.sax.Attributes)
	 */
	@Override
	public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
		switch(QNames.parse(qName)) {
		case PLAN:
			try {
				String costVal = this.getValue(atts, QNames.COST);
				if (costVal == null) {
					throw new ReaderException("No cost defined in plan file.");
				}
				this.cost = Double.valueOf(costVal);
			} catch (NumberFormatException e) {
				log.warn("Unable to parse cost " + this.getValue(atts, QNames.COST), e);
				this.cost = Double.POSITIVE_INFINITY;
			}
			break;

		case OPERATOR:
			OperatorReader opReader = new OperatorReader(this.schema);
			this.operatorReaders.add(opReader);
			opReader.startElement(uri, localName, qName, atts);
			break;

		default:
			this.operatorReaders.peek().startElement(uri, localName, qName, atts);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.xml.sax.helpers.DefaultHandler#endElement(java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException {
		switch(QNames.parse(qName)) {
		case PLAN:
			this.plan = new DAGPlan(this.operator);
			this.plan.setCost(new DoubleCost(this.cost));
			break;

		case OPERATOR:
			OperatorReader reader = this.operatorReaders.pop();
			reader.endElement(uri, localName, qName);
			if (!this.operatorReaders.isEmpty()) {
				this.operatorReaders.peek().addChild(reader.getOperator());
			}
			this.operator = reader.getOperator();
			break;

		default:
			this.operatorReaders.peek().endElement(uri, localName, qName);
			return;
		}
	}
}
