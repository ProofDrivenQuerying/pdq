package uk.ac.ox.cs.pdq.datasources.legacy.services;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.ws.rs.core.MediaType;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.log4j.Logger;
import org.junit.Assert;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import com.google.common.base.CaseFormat;
import com.google.common.base.Preconditions;

import uk.ac.ox.cs.pdq.builder.Builder;
import uk.ac.ox.cs.pdq.builder.BuilderException;
import uk.ac.ox.cs.pdq.io.xml.AbstractXMLReader;
import uk.ac.ox.cs.pdq.datasources.legacy.services.policies.PolicyFactory;
import uk.ac.ox.cs.pdq.datasources.legacy.services.policies.UsagePolicy;
import uk.ac.ox.cs.pdq.datasources.legacy.services.rest.InputMethod;
import uk.ac.ox.cs.pdq.datasources.legacy.services.rest.OutputMethod;
import uk.ac.ox.cs.pdq.datasources.legacy.services.rest.PathOutputMethod;
import uk.ac.ox.cs.pdq.util.QNames;
import uk.ac.ox.cs.pdq.datasources.schemabuilder.SchemaDiscoverer;
import uk.ac.ox.cs.pdq.db.AccessMethodDescriptor;
import uk.ac.ox.cs.pdq.db.Attribute;
import uk.ac.ox.cs.pdq.db.PrimaryKey;
import uk.ac.ox.cs.pdq.db.Relation;
import uk.ac.ox.cs.pdq.db.Schema;
import uk.ac.ox.cs.pdq.io.ReaderException;

/**
 * Reads a service repository initialConfig from XML.
 * 
 * @author Julien Leblay
 * @author Efthymia Tsamoura
 * 
 */
public class ServiceReader extends AbstractXMLReader<ServiceRepository> implements SchemaDiscoverer {

	private static Logger log = Logger.getLogger(ServiceReader.class);

	protected Properties properties = new Properties();
	
	protected Schema discovered = null;

	/**  The service repository being built. */
	protected ServiceRepository services = null;
	
	protected ServiceBuilder builder = null;
	
	protected boolean inUsagePolicies = false;
	
	protected boolean inInputMethods = false;

	/**
	 * Default constructor. 
	 */
	public ServiceReader() {}
	
	/**
	 *
	 * @param in InputStream
	 * @return ServiceRepository
	 * @see uk.ac.ox.cs.pdq.io.Reader#read(InputStream)
	 */
	@Override
	public ServiceRepository read(InputStream in) {
		try {
			SAXParserFactory factory = SAXParserFactory.newInstance();
			SAXParser parser = factory.newSAXParser();
			parser.parse(in, this);
			return this.services;
		} catch (ParserConfigurationException | SAXException | IOException e) {
			log.error(e);
		}
		return null;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.xml.sax.helpers.DefaultHandler#startElement(java.lang.String, java.lang.String, java.lang.String, org.xml.sax.Attributes)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void startElement(String uri, String localName, String qName, Attributes atts) {
		
		switch(QNames.parse(qName)) {
		case SERVICES:
			this.services = new ServiceRepository();
			break;

		case SERVICE:
			this.builder = new ServiceBuilder();
			this.builder.setName(this.getValue(atts, QNames.NAME));
			this.builder.setProtocol(this.getValue(atts, QNames.PROTOCOL));
			this.builder.setUrl(this.getValue(atts, QNames.URL));
			this.builder.setResultsDelimiter(this.getValue(atts, QNames.RESULT_DELIMITER));
			String mediaType = this.getValue(atts, QNames.MEDIA_TYPE);
			if (mediaType != null) {
				this.builder.setMediaType(MediaType.valueOf(mediaType));
			}
			break;
			
		case USAGE_POLICIES:
			this.inUsagePolicies = true;
			break;

		case POLICY:
			{
				String name = this.getValue(atts, QNames.NAME);
				
				if (name != null && !this.inUsagePolicies) {
					UsagePolicy p = this.services.getUsagePolicy(name);
					if (p == null) {
						throw new ReaderException("No such usage policy '" + name + "'");
					}
					this.builder.addUsagePolicy(p);

				} else {

					Properties prop = new Properties();
					for (int i = 0, l = atts.getLength(); i < l; i++) {
						prop.put(atts.getLocalName(i), atts.getValue(i));
					}
					Class<UsagePolicy> cl = null;
					try {
						cl = (Class<UsagePolicy>) Class.forName(this.getValue(atts, QNames.TYPE));
					} catch (ClassNotFoundException e) {
						String className = this.getValue(atts, QNames.TYPE);
						if (className!=null && className.startsWith("uk.ac.ox.cs.pdq.services.policies")) {
							className = className.replace("uk.ac.ox.cs.pdq.services.policies", "uk.ac.ox.cs.pdq.datasources.services.policies");
						}
						try {
							cl = (Class<UsagePolicy>) Class.forName(className);
						} catch (ClassNotFoundException e1) {
							throw new ReaderException("Class '" 
									+ this.getValue(atts, QNames.TYPE) + "' not found.");
						}
					}
					if (cl!=null) {
						if (this.inUsagePolicies) {
							this.services.registerUsagePolicy(name, cl, prop);
						} else {
							this.builder.addUsagePolicy(PolicyFactory.getInstance(this.services, cl, prop));
						}
					}
				}
			}
			break;
			
		case INPUT_METHODS:
			this.inInputMethods = true;
			break;

		case INPUT_METHOD:
			Preconditions.checkState(this.inInputMethods);
			InputMethod method = new InputMethod(
					this.getValue(atts, QNames.NAME),
					this.getValue(atts, QNames.TEMPLATE),
					InputMethod.Types.valueOf(CaseFormat.LOWER_HYPHEN.to(
							CaseFormat.UPPER_UNDERSCORE, this.getValue(atts, QNames.TYPE))),
					this.getValue(atts, QNames.BATCH_DELIMITER),
					this.getIntValue(atts, QNames.BATCH_SIZE),
					this.getValue(atts, QNames.VALUE));
			this.services.registerInputMethod(method.getName(), method);
			break;
			
		case STATIC_INPUT:
			{
				Attribute attribute = Attribute.create(String.class, this.getValue(atts, QNames.NAME));
				String im = this.getValue(atts, QNames.INPUT_METHOD);
				if (im == null) {
					this.builder.addStaticInput(attribute, this.getValue(atts, QNames.VALUE));
					break;
				}
				Pair<InputMethod, String[]> inputs = this.services.parseInputMethod(im);
				if (inputs == null) {
					throw new ReaderException(
							"Service " + this.builder.getName() +
							" refers to unknow input method " + im);
				}
				this.builder.addStaticInput(
						attribute, this.getValue(atts, QNames.VALUE),
						inputs.getLeft(), inputs.getRight());
			}
			break;

		case ATTRIBUTE:
			try {
				String name = this.getValue(atts, QNames.NAME);
				String type = this.getValue(atts, QNames.TYPE);
				if (type == null) {
					throw new ReaderException("Syntax error. Type of attribute '" + name + "' is missing.");
				}
				Attribute attribute = Attribute.create(Class.forName(type), name);
				String path = this.getValue(atts, QNames.PATH);
				OutputMethod om = null;
				if (path != null) {
					om = new PathOutputMethod(path);
				}
				String im = this.getValue(atts, QNames.INPUT_METHOD);
				if (im == null) {
					this.builder.addAttribute(attribute, om);
					break;
				}
				Pair<InputMethod, String[]> inputs = this.services.parseInputMethod(im);
				if (inputs == null) {
					throw new ReaderException(
							"Service " + this.builder.getName() +
							" refers to unknow input method " + im);
				}
				this.builder.addAttribute(attribute, om, inputs.getLeft(), inputs.getRight());
			} catch (ClassNotFoundException e) {
				throw new ReaderException("Class '" 
						+ this.getValue(atts, QNames.TYPE)
						+ "' not found for attribute '"
						+ this.getValue(atts, QNames.NAME));
			}
			break;

		case ACCESS_METHOD:
			String positions = this.getValue(atts, QNames.INPUTS);
			List<Integer> inputs = new ArrayList<>();
			if (positions != null && !positions.trim().isEmpty()) 
				inputs.addAll(toIntList(positions));
			AccessMethodDescriptor b = null;
			String n = this.getValue(atts, QNames.NAME);
			if (n != null && !n.trim().isEmpty()) 
				b = AccessMethodDescriptor.create(n, inputs.toArray(new Integer[inputs.size()]));
			else 
				b = AccessMethodDescriptor.create(inputs.toArray(new Integer[inputs.size()]));
			String cost = this.getValue(atts, QNames.COST);
			if (cost == null || cost.trim().isEmpty()) {
				throw new ReaderException("Cost is a mandatory attribute in " + QNames.ACCESS_METHOD);
			}
			this.builder.addAccessMethod(b);
			break;	
		case KEY:
			List<Attribute> skey = new ArrayList<>();
			String attributes = this.getValue(atts, QNames.ATTRIBUTES);
			for(String key:attributes.split(",")) {
				Attribute k = null;
				for(Attribute attr:this.builder.getAttributes()) {
					if(attr.getName().equals(key)) {
						 k = attr;
						 break;
					}
				}
				Assert.assertNotNull("Undentified input key", k);
				skey.add(k);
			}
			this.builder.addPrimaryKey(PrimaryKey.create(skey.toArray(new Attribute[skey.size()])));
			break;
		default:
			throw new ReaderException("Illegal element " + qName);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.xml.sax.helpers.DefaultHandler#endElement(java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public void endElement(String uri, String localName, String qName) {
		switch(QNames.parse(qName)) {
		case SERVICE:
			Service service = this.builder.build();
			this.services.registerService(service.getName(), service);
			break;

		case USAGE_POLICIES:
			this.inUsagePolicies = false;
			break;

		case INPUT_METHODS:
			this.inInputMethods = false;
			break;

		default:
			return;
		}
	}

	/**
	 *
	 * @param p Properties
	 * @see uk.ac.ox.cs.pdq.datasources.schemabuilder.SchemaDiscoverer#setProperties(Properties)
	 */
	@Override
	public void setProperties(Properties p) {
		this.properties.putAll(p);
	}

	/**
	 *
	 * @return Properties
	 * @see uk.ac.ox.cs.pdq.datasources.schemabuilder.SchemaDiscoverer#getProperties()
	 */
	@Override
	public Properties getProperties() {
		return this.properties;
	}

	/*
	 * (non-Javadoc)
	 * @see uk.ac.ox.cs.pdq.builder.discovery.SchemaDiscoverer#discover()
	 */
	@Override
	public Schema discover() throws BuilderException {
		if (this.discovered == null) {
			String path = this.properties.getProperty("file");
			File f = new File(path);
			log.info("Reading service initialConfig '" + f.getAbsolutePath() + "'...");
			try (FileInputStream fis = new FileInputStream(f.getAbsolutePath())) {
				ServiceRepository repo = this.read(fis);
				Relation[] relations = new Relation[repo.getServices().size()];
				int i = 0;
				for (Service s: repo.getServices()) {
					relations[i] = (Relation) s;
					i++;
				}
				this.discovered = new Schema(relations);
			} catch (IOException e) {
				throw new ReaderException(e.getMessage(), e);
			}
		}
		return this.discovered;
	}
}
