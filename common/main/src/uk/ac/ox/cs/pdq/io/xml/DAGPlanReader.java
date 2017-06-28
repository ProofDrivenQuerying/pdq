package uk.ac.ox.cs.pdq.io.xml;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.log4j.Logger;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import uk.ac.ox.cs.pdq.algebra.RelationalOperator;
import uk.ac.ox.cs.pdq.cost.DoubleCost;
import uk.ac.ox.cs.pdq.db.Schema;
import uk.ac.ox.cs.pdq.io.ReaderException;
import uk.ac.ox.cs.pdq.plan.DAGPlan;

/**
 * Reads plans from XML.
 * 
 * @author Julien Leblay
 */
public class DAGPlanReader extends AbstractXMLReader<DAGPlan> {

	/** Logger. */
	private static Logger log = Logger.getLogger(DAGPlanReader.class);
	
	/**  The plan being built. */
	private DAGPlan plan = null;
	
	/**  The plan cost. */
	private Double cost;
	
	/**  The current operator being built. */
	private RelationalOperator operator = null;
	
	/** The schema. */
	private Schema schema = null;

	/**  The operator reader. */
	private final OperatorReader operatorReader;
	
	/**
	 * Default constructor.
	 *
	 * @param schema Schema
	 */
	public DAGPlanReader(Schema schema) {
		this.schema = schema;
		this.operatorReader = new OperatorReader(this.schema);
	}

	/**
	 * Reads plans from XML.
	 *
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

	/**
	 * {@inheritDoc}
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
		case PLAN:
			this.plan = new DAGPlan(this.operatorReader.getOperator());
			this.plan.setCost(new DoubleCost(this.cost));
			break;

		default:
			this.operatorReader.endElement(uri, localName, qName);
			return;
		}
	}
}
