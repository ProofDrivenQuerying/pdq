package uk.ac.ox.cs.pdq.cost.statistics;


import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.log4j.Logger;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;

import uk.ac.ox.cs.pdq.cost.sqlserverhistogram.SQLServerHistogram;
import uk.ac.ox.cs.pdq.cost.sqlserverhistogram.SQLServerHistogramLoader;
import uk.ac.ox.cs.pdq.db.AccessMethodDescriptor;
import uk.ac.ox.cs.pdq.db.Attribute;
import uk.ac.ox.cs.pdq.db.Relation;
import uk.ac.ox.cs.pdq.db.Schema;
import uk.ac.ox.cs.pdq.fol.TypedConstant;

/**
 * Holds unconditional statistics.
 * The statistics that are maintained are:
 * -the relation cardinalities,
 * -cardinalities of single attributes
 * -the size of output per invocation of an access method
 * -the cost of an access method
 * -selectivities of single attribute filtering predicates
 * -frequency maps of single attributes
 * -and SQL Server 2014 single attribute histograms.
 * All the statistics are loaded by default from a catalog.properties file
 *
 * The catalog.properties file format is:
 * Each line represent one piece of information, and parsed separately from the rest.
 * The format of a line can be one of the following (The [] indicate editable parts of the line, the file should not contain it.):
 * Cost:
 * RE:[TABLE_NAME]		BI:[ACCESS_METHOD_NAME]		RT:[COST_OF_ACCESS_METHOD] #COMMENT
 * Erspi:
 * RE:[TABLE_NAME]		BI:[ACCESS_METHOD_NAME]		ERSPI:[NUMBER OF OUTPUT TUPLES PER INPUT]
 * Sql server histogram (this has no known usages/examples or tests):
 * RE:[TABLE_NAME]		AT:[ATTRIBUTE_NAME]		SQLH:[???]
 * Column selectivity:
 * RE:[TABLE_NAME]		AT:[ATTRIBUTE_NAME]		SE:[SELECTIVITY]
 * Column cardinality:
 * RE:[TABLE_NAME]		AT:[ATTRIBUTE_NAME]		CC:[DECIMAL_CARDINALITY]
 * relation cardinality:
 * RE:[TABLE_NAME]		CA:[DECIMAL_CARDINALITY]
 * Frequency map (No examples or any usage):
 * RE:[TABLE_NAME] 		AT:[ATTRIBUTE_NAME]		HH:[???] VA:[???] FR:[???]
 * Example content of the file for a relation that contains 150 records and has 2 access methods, and a selectivity example
 * RE: cars CA: 150
 * RE: cars BI: read_all RT: 12.5
 * RE: cars BI: read_one RT: 0.5
 * RE:AssayLimited  AT:assay_type SE:0.4 #Selectivity if assay_type='F'

 * @author Efthymia Tsamoura
 * @author Gabor
 *
 */
public class SimpleCatalog implements Catalog{

	/** Logger. */
	private static Logger log = Logger.getLogger(SimpleCatalog.class);

	public static double DEFAULT_ATTRIBUTE_EQUALITY_SELECTIVITY = 0.1;

	private static final int DEFAULT_CARDINALITY = 1000000;
	private static final double DEFAULT_QUALITY = 0.0;
	private static final int DEFAULT_COLUMN_CARDINALITY = 1000;
	private static final double DEFAULT_COST = 1.0;
	private static final String CATALOG_FILE_NAME = "catalog/catalog.properties";

	private static final String READ_CARDINALITY = "^(RE:(\\w+)(\\s+)CA:(\\d+))";
	private static final String READ_COLUMN_CARDINALITY = "^(RE:(\\w+)(\\s+)AT:(\\w+)(\\s+)CC:(\\d+))";
	private static final String READ_COLUMN_SELECTIVITY = "^(RE:(\\w+)(\\s+)AT:(\\w+)(\\s+)SE:(\\d+(\\.\\d+)?))";
	private static final String READ_ERSPI = "^(RE:(\\w+)(\\s+)BI:(\\w+)(\\s+)ERSPI:(\\d+(\\.\\d+)?))";
	private static final String READ_COST = "^(RE:(\\w+)(\\s+)BI:(\\w+)(\\s+)RT:(\\d+(\\.\\d+)?))";
	private static final String READ_SQLSERVERHISTOGRAM = "^(RE:(\\w+)(\\s+)AT:(\\w+)(\\s+)SQLH:((/[a-zA-Z0-9._-]+)+/?))";

	/** Cardinalities of the schema relations*/
	private final Map<Relation,Integer> cardinalities;
	/** Cardinalities of the relations' attributes*/
	private final Map<Pair<Relation,Attribute>,Integer> columnCardinalities;
	/** The estimated result size per invocation of each access method*/
	private final Map<Pair<Relation,AccessMethodDescriptor>,Integer> numberOfOutputTuplesPerInput;
	/** The response time of each access method*/
	private final Map<Pair<Relation,AccessMethodDescriptor>,Double> costs;
	/** The selectivity of each attribute*/
	private final Map<Pair<Relation,Attribute>,Double> columnSelectivity;
	/** The frequency histogram of each attribute*/
	private final Map<Pair<Relation,Attribute>, SimpleFrequencyMap> frequencyMaps;
	/** The SQL Server histograms of each attribute*/
	private final Map<Pair<Relation,Attribute>, SQLServerHistogram> SQLServerHistograms;

	/** The schema of the input database */
	private final Schema schema;

	/**
	 * Creates a catalog by loading metadata located at "catalog/catalog.properties"
	 *
	 * @param schema the schema
	 */
	public SimpleCatalog(Schema schema) {
		this(schema, SimpleCatalog.CATALOG_FILE_NAME);
	}

	/**
	 * Creates a catalog by loading schema metadata located at the input file.
	 *
	 * @param schema the schema
	 * @param fileName the file name
	 */
	public SimpleCatalog(Schema schema, String fileName) {
		Preconditions.checkNotNull(schema);
		this.schema = schema;
		this.numberOfOutputTuplesPerInput = new HashMap<>();
		this.costs = new HashMap<>();
		this.columnSelectivity = new HashMap<>();
		this.cardinalities = new HashMap<>();
		this.columnCardinalities = new HashMap<>();
		this.frequencyMaps = new HashMap<>();
		this.SQLServerHistograms = new HashMap<>();
		this.read(schema, fileName);
	}

	/**
	 * Read.
	 *
	 * @param schema the schema
	 * @param fileName 		The file that stores the statistics
	 */
	private void read(Schema schema, String fileName) {
		String line = null;
		try {
			FileReader fileReader = new FileReader(fileName);
			BufferedReader bufferedReader = new BufferedReader(fileReader);
			while((line = bufferedReader.readLine()) != null) {
				this.parse(schema, line);
			}
			bufferedReader.close();
		}
		catch(FileNotFoundException ex) {
			System.out.println("Warning, no catalog.properties found in " + fileName);
		}
		catch(IOException ex) {
			ex.printStackTrace(System.out);
		}
	}

	/**
	 * Parses the statistics file.
	 *
	 * @param schema the schema
	 * @param line the line
	 */
	private void parse(Schema schema, String line) {
		if (line== null || line.trim().isEmpty() || line.trim().startsWith("#") || line.trim().startsWith("//")) {
			// empty line or comment line.
			return;
		}
		Pattern p = Pattern.compile(READ_CARDINALITY);
		Matcher m = p.matcher(line);
		if (m.find()) {
			String relation = m.group(2);
			String cardinality = m.group(4);
			if(schema.contains(relation)) {
				Relation r = schema.getRelation(relation);
				this.cardinalities.put(r, Integer.parseInt(cardinality));
				log.info("RELATION: " + relation + " CARDINALITY: " + cardinality);
			}
			else {
				throw new java.lang.IllegalArgumentException();
			}
			return;
		}

		p = Pattern.compile(READ_COLUMN_CARDINALITY);
		m = p.matcher(line);
		if (m.find()) {
			String relation = m.group(2);
			String column = m.group(4);
			String cardinality = m.group(6);
			if(schema.contains(relation)) {
				Relation r = schema.getRelation(relation);
				if(r.getAttribute(column) != null) {
					Attribute attribute = r.getAttribute(column);
					this.columnCardinalities.put( Pair.of(r,attribute), Integer.parseInt(cardinality));
					log.info("RELATION: " + relation + " ATTRIBUTE: " + attribute + " CARDINALITY: " + cardinality);
				}
				else {
					throw new java.lang.IllegalArgumentException();
				}
			}
			else {
				throw new java.lang.IllegalArgumentException();
			}
			return;
		}

		p = Pattern.compile(READ_COLUMN_SELECTIVITY);
		m = p.matcher(line);
		if (m.find()) {
			String relation = m.group(2);
			String column = m.group(4);
			String selectivity = m.group(6);
			if(schema.contains(relation)) {
				Relation r = schema.getRelation(relation);
				if(r.getAttribute(column) != null) {
					Attribute attribute = r.getAttribute(column);
					this.columnSelectivity.put( Pair.of(r,attribute), Double.parseDouble(selectivity));
					log.info("RELATION: " + relation + " ATTRIBUTE: " + attribute + " SELECTIVITY: " + selectivity);
				}
				else {
					throw new java.lang.IllegalArgumentException();
				}
			}
			else {
				throw new java.lang.IllegalArgumentException();
			}
			return;
		}

		p = Pattern.compile(READ_ERSPI);
		m = p.matcher(line);
		if (m.find()) {
			log.info(line);
			String relation = m.group(2);
			String binding = m.group(4);
			String erspi = m.group(6);
			if(schema.contains(relation)) {
				Relation r = schema.getRelation(relation);
				AccessMethodDescriptor b = r.getAccessMethod(binding);
				if(b != null) {
					this.numberOfOutputTuplesPerInput.put( Pair.of(r,b), Integer.parseInt(erspi));
					log.info("RELATION: " + relation + " BINDING: " + binding + " ERPSI: " + erspi);
				}
				else {
					throw new java.lang.IllegalArgumentException();
				}
			}
			else {
				throw new java.lang.IllegalArgumentException();
			}
			return;
		}

		p = Pattern.compile(READ_COST);
		m = p.matcher(line);
		if (m.find()) {
			String relation = m.group(2);
			String binding = m.group(4);
			String cost = m.group(6);
			if(schema.contains(relation)) {
				Relation r = schema.getRelation(relation);
				AccessMethodDescriptor b = r.getAccessMethod(binding);
				if(b != null) {
					this.costs.put( Pair.of(r,b), Double.parseDouble(cost));
					log.info("RELATION: " + relation + " BINDING: " + binding + " COST: " + cost);
				}
				else {
					throw new java.lang.IllegalArgumentException();
				}
			}
			else {
				throw new java.lang.IllegalArgumentException();
			}
			return;
		}

		SimpleFrequencyMap h = SimpleFrequencyMap.build(schema, line);
		if(h != null) {
			this.frequencyMaps.put(Pair.of(h.getRelation(), h.getAttibute()), h);
			return;
		}

		p = Pattern.compile(READ_SQLSERVERHISTOGRAM);
		m = p.matcher(line);
		if (m.find()) {
			String relation = m.group(2);
			String column = m.group(4);
			String histogramFile = m.group(6);
			if(schema.contains(relation)) {
				Relation r = schema.getRelation(relation);
				if(r.getAttribute(column) != null) {
					Attribute attribute = r.getAttribute(column);
					SQLServerHistogram histogram = SQLServerHistogramLoader.load(attribute.getType(), histogramFile);
					this.SQLServerHistograms.put(Pair.of(r, attribute), histogram);
					log.info("RELATION: " + relation + " ATTRIBUTE: " + attribute + " Histogram file: " + histogramFile);
				}
				else {
					throw new java.lang.IllegalArgumentException();
				}
			}
			else {
				throw new java.lang.IllegalArgumentException();
			}
			return;
		}

	}

	/**
	 * Instantiates a new simple catalog.
	 *
	 * @param schema the schema
	 * @param cardinalities the cardinalities
	 * @param erpsi the erpsi
	 * @param responseTimes the response times
	 * @param columnSelectivity the column selectivity
	 * @param columnCardinalities the column cardinalities
	 * @param queries the queries
	 * @param frequencyMaps the frequency maps
	 * @param SQLServerHistograms the SQL server histograms
	 */
	public SimpleCatalog(Schema schema, Map<Relation,Integer> cardinalities, Map<Pair<Relation,AccessMethodDescriptor>,Integer> erpsi, Map<Pair<Relation,AccessMethodDescriptor>,Double> responseTimes,
			Map<Pair<Relation,Attribute>,Double> columnSelectivity, Map<Pair<Relation,Attribute>,Integer> columnCardinalities,
			Map<Pair<Relation,Attribute>, SimpleFrequencyMap> frequencyMaps,
			Map<Pair<Relation,Attribute>, SQLServerHistogram> SQLServerHistograms
			) {
		Preconditions.checkNotNull(schema);
		Preconditions.checkNotNull(cardinalities);
		Preconditions.checkNotNull(erpsi);
		Preconditions.checkNotNull(responseTimes);
		Preconditions.checkNotNull(columnSelectivity);
		Preconditions.checkNotNull(columnCardinalities);
		Preconditions.checkNotNull(frequencyMaps);
		Preconditions.checkNotNull(SQLServerHistograms);
		this.schema = schema;
		this.cardinalities = Maps.newHashMap(cardinalities);
		this.numberOfOutputTuplesPerInput = Maps.newHashMap(erpsi);
		this.costs = Maps.newHashMap(responseTimes);
		this.columnSelectivity = Maps.newHashMap(columnSelectivity);
		this.columnCardinalities = Maps.newHashMap(columnCardinalities);
		this.frequencyMaps = Maps.newHashMap(frequencyMaps);
		this.SQLServerHistograms = Maps.newHashMap(SQLServerHistograms);
	}

	/* (non-Javadoc)
	 * @see uk.ac.ox.cs.pdq.cost.statistics.Catalog#getSelectivity(uk.ac.ox.cs.pdq.db.Relation, uk.ac.ox.cs.pdq.db.Attribute, uk.ac.ox.cs.pdq.db.TypedConstant)
	 */
	public Double getSelectivity(Relation relation, Attribute attribute, TypedConstant constant) {
		Preconditions.checkNotNull(relation);
		Preconditions.checkNotNull(attribute);
		Preconditions.checkNotNull(constant);

		SimpleFrequencyMap histogram = this.frequencyMaps.get(Pair.of(relation, attribute));
		String search = constant.toString();
		if(constant.getType() instanceof Class && BigDecimal.class.isAssignableFrom((Class<?>) constant.getType())) {
			BigInteger integer = new BigDecimal(constant.toString()).toBigInteger();
			search = integer.toString();
		}
		if(histogram != null && histogram.getFrequency(search) != null) {
			int erpsi = histogram.getFrequency(search);
			log.info("RELATION: " + relation.getName() + " ATTRIBUTE: " + attribute + " CONSTANTS: " + constant);
			return (double)erpsi/this.getCardinality(relation);
		}
		return this.getSelectivity(relation, attribute);
	}

	/* (non-Javadoc)
	 * @see uk.ac.ox.cs.pdq.cost.statistics.Catalog#getSize(uk.ac.ox.cs.pdq.db.Relation, uk.ac.ox.cs.pdq.db.Attribute, uk.ac.ox.cs.pdq.db.TypedConstant)
	 */
	@Override
	public int getSize(Relation relation, Attribute attribute, TypedConstant constant) {
		Preconditions.checkNotNull(relation);
		Preconditions.checkNotNull(attribute);
		Preconditions.checkNotNull(constant);

		SimpleFrequencyMap histogram = this.frequencyMaps.get(Pair.of(relation, attribute));
		String search = constant.toString();
		if(constant.getType() instanceof Class && BigDecimal.class.isAssignableFrom((Class<?>) constant.getType())) {
			BigInteger integer = new BigDecimal(constant.toString()).toBigInteger();
			search = integer.toString();
		}
		if(histogram != null && histogram.getFrequency(search) != null) {
			int erpsi = histogram.getFrequency(search);
			log.info("RELATION: " + relation.getName() + " ATTRIBUTE: " + attribute + " CONSTANTS: " + constant);
			return erpsi;
		}
		return SimpleCatalog.DEFAULT_COLUMN_CARDINALITY;
	}

	/**
	 * Gets the selectivity.
	 *
	 * @param relation the relation
	 * @param attribute the attribute
	 * @return the selectivity
	 */
	public double getSelectivity(Relation relation, Attribute attribute) {
		Preconditions.checkNotNull(attribute);
		Preconditions.checkNotNull(relation);
		Double selectivities = this.columnSelectivity.get(Pair.of(relation, attribute));
		if(selectivities != null) {
			log.info("RELATION: " + relation.getName() + " ATTRIBUTE: " + attribute + " SELECTIVITY: " + selectivities);
			return selectivities;
		}
		else {
			Integer columnCardinality = this.columnCardinalities.get(Pair.of(relation, attribute));
			if(columnCardinality != null) {
				log.info("RELATION: " + relation.getName() + " ATTRIBUTE: " + attribute + " SELECTIVITY: " + 1.0/columnCardinality);
				return 1.0/columnCardinality;
			}
			log.info("RELATION: " + relation.getName() + " ATTRIBUTE: " + attribute + " SELECTIVITY: " + 1.0/SimpleCatalog.DEFAULT_COLUMN_CARDINALITY);
			return 1.0/SimpleCatalog.DEFAULT_COLUMN_CARDINALITY;
		}
	}

	/**
	 * Gets the selectivity.
	 *
	 * @param left the left
	 * @param right the right
	 * @param leftAttribute the left attribute
	 * @param rightAttribute the right attribute
	 * @return the selectivity
	 */
	public double getSelectivity(Relation left, Relation right, Attribute leftAttribute, Attribute rightAttribute) {
		Preconditions.checkNotNull(left);
		Preconditions.checkNotNull(right);
		Preconditions.checkNotNull(leftAttribute);
		Preconditions.checkNotNull(rightAttribute);
		Integer leftcardinalities = this.columnCardinalities.get(Pair.of(left, leftAttribute));
		Integer rightcardinalities = this.columnCardinalities.get(Pair.of(right, rightAttribute));
		if(leftcardinalities != null && rightcardinalities != null) {
			log.info("LEFT RELATION: " + left.getName() + " LEFT ATTRIBUTE " + leftAttribute + " RIGHT RELATION: " + right.getName() + " RIGHT ATTRIBUTE " + rightAttribute + " SELECTIVITY: " + 1.0 / Math.max(leftcardinalities, rightcardinalities));
			return 1.0 / Math.max(leftcardinalities, rightcardinalities);
		}
		else {
			log.info("LEFT RELATION: " + left.getName() + " LEFT ATTRIBUTE " + leftAttribute + " RIGHT RELATION: " + right.getName() + " RIGHT ATTRIBUTE " + rightAttribute + " SELECTIVITY: " + 1.0 / SimpleCatalog.DEFAULT_COLUMN_CARDINALITY);
			return 1.0 / SimpleCatalog.DEFAULT_COLUMN_CARDINALITY;
		}
	}

	/* (non-Javadoc)
	 * @see uk.ac.ox.cs.pdq.cost.statistics.Catalog#getERPSI(uk.ac.ox.cs.pdq.db.Relation, uk.ac.ox.cs.pdq.db.AccessMethod)
	 */
	@Override
	public int getTotalNumberOfOutputTuplesPerInputTuple(Relation relation, AccessMethodDescriptor method) {
		Preconditions.checkNotNull(relation);
		Preconditions.checkNotNull(method);
		Integer erspi = this.numberOfOutputTuplesPerInput.get(Pair.of(relation, method));
		if(erspi == null) {
			double columnProduct = 1.0;
			for(Integer input:method.getInputs()) {
				Attribute attribute = relation.getAttribute(input);
				Integer columnCardinality = this.columnCardinalities.get(Pair.of(relation, attribute));
				if(columnCardinality != null) {
					columnProduct *= columnCardinality;
				}
				else {
					log.info("RELATION: " + relation.getName() + " AccessMethod: " + method + " ERPSI: " + SimpleCatalog.DEFAULT_CARDINALITY/SimpleCatalog.DEFAULT_COLUMN_CARDINALITY);
					return SimpleCatalog.DEFAULT_CARDINALITY/SimpleCatalog.DEFAULT_COLUMN_CARDINALITY;
				}
			}
			if(this.cardinalities.containsKey(relation)) {
				log.info("RELATION: " + relation.getName() + " AccessMethod: " + method + " ERPSI: " + this.cardinalities.get(relation)/columnProduct);
				return (int) ((int) this.cardinalities.get(relation)/columnProduct);
			}
			else {
				log.info("RELATION: " + relation.getName() + " AccessMethod: " + method + " ERPSI: " + SimpleCatalog.DEFAULT_CARDINALITY/SimpleCatalog.DEFAULT_COLUMN_CARDINALITY);
				return SimpleCatalog.DEFAULT_CARDINALITY/SimpleCatalog.DEFAULT_COLUMN_CARDINALITY;
			}
		}
		return erspi;
	}


	/* (non-Javadoc)
	 * @see uk.ac.ox.cs.pdq.cost.statistics.Catalog#getERPSI(uk.ac.ox.cs.pdq.db.Relation, uk.ac.ox.cs.pdq.db.AccessMethod, java.util.Map)
	 */
	@Override
	public int getTotalNumberOfOutputTuplesPerInputTuple(Relation relation, AccessMethodDescriptor method, Map<Integer, TypedConstant> inputs) {
		Preconditions.checkNotNull(relation);
		Preconditions.checkNotNull(inputs);
		if(inputs.size() == 1 && method.getInputs().length == 1) {
			Attribute attribute = relation.getAttribute(method.getInputs().length);
			SimpleFrequencyMap histogram = this.frequencyMaps.get(Pair.of(relation, attribute));
			if(histogram != null && histogram.getFrequency(inputs.get(0).toString()) != null) {
				int erpsi = histogram.getFrequency(inputs.get(0).toString());
				log.info("RELATION: " + relation.getName() + " ACCESS: " + method + " INPUTS: " + inputs + " ERPSI: " + erpsi);
				return erpsi;
			}
		}
		int erpsi = this.getTotalNumberOfOutputTuplesPerInputTuple(relation, method);
		log.info("RELATION: " + relation.getName() + " ACCESS: " + method + " INPUTS: " + inputs + " ERPSI: " + erpsi);
		return erpsi;
	}

	/* (non-Javadoc)
	 * @see uk.ac.ox.cs.pdq.cost.statistics.Catalog#getCardinality(uk.ac.ox.cs.pdq.db.Relation)
	 */
	@Override
	public int getCardinality(Relation relation) {
		if(this.cardinalities.get(relation) != null) {
			return this.cardinalities.get(relation);
		}
		return DEFAULT_CARDINALITY;
	}

	/* (non-Javadoc)
	 * @see uk.ac.ox.cs.pdq.cost.statistics.Catalog#getCardinality(uk.ac.ox.cs.pdq.db.Relation, uk.ac.ox.cs.pdq.db.Attribute)
	 */
	@Override
	public int getCardinality(Relation relation, Attribute attribute) {
		if(this.columnCardinalities.get(Pair.of(relation, attribute)) != null) {
			log.info("RELATION: " + relation.getName() + " ATTRIBUTE: " + attribute + " CARDINALITY: " + this.columnCardinalities.get(Pair.of(relation, attribute)));
			return this.columnCardinalities.get(Pair.of(relation, attribute));
		}
		log.info("RELATION: " + relation.getName() + " ATTRIBUTE: " + attribute + " CARDINALITY: " + DEFAULT_COLUMN_CARDINALITY);
		return DEFAULT_COLUMN_CARDINALITY;
	}

	/* (non-Javadoc)
	 * @see uk.ac.ox.cs.pdq.cost.statistics.Catalog#getCost(uk.ac.ox.cs.pdq.db.Relation, uk.ac.ox.cs.pdq.db.AccessMethod)
	 */
	@Override
	public double getCost(Relation relation, AccessMethodDescriptor method) {
		Preconditions.checkNotNull(relation);
		Preconditions.checkNotNull(method);
		Double cost = this.costs.get(Pair.of(relation, method));

		if(cost == null) {
			log.warn("RELATION: " + relation.getName() + " ACCESS METHOD: " + method + " is using the DEFAULT_COST: " + DEFAULT_COST);
			return DEFAULT_COST;
		} else {
			return cost;
		}
	}

	/* (non-Javadoc)
	 * @see uk.ac.ox.cs.pdq.cost.statistics.Catalog#getCost(uk.ac.ox.cs.pdq.db.Relation, uk.ac.ox.cs.pdq.db.AccessMethod, java.util.Map)
	 */
	@Override
	public double getCost(Relation relation, AccessMethodDescriptor method, Map<Integer, TypedConstant> inputs) {
		Preconditions.checkNotNull(relation);
		Preconditions.checkNotNull(method);
		double erpsi = -1;
		if(inputs.size() == 1) {
			Attribute attribute = relation.getAttribute(method.getInputs()[0]);
			SimpleFrequencyMap histogram = this.frequencyMaps.get(Pair.of(relation, attribute));
			if(histogram != null && histogram.getFrequency(inputs.get(0).toString()) != null) {
				erpsi = histogram.getFrequency(inputs.get(0).toString());
				log.info("RELATION: " + relation.getName() + " ACCESS: " + method + " INPUTS: " + inputs + " ERPSI: " + erpsi);
			}
		}
		Double cost = this.costs.get(Pair.of(relation, method));
		if(cost == null) {
			log.warn("RELATION: " + relation.getName() + " ACCESS METHOD: " + method + " is using the DEFAULT_COST: " + DEFAULT_COST);
			return DEFAULT_COST;
		}
		log.info("RELATION: " + relation.getName() + " ACCESS METHOD: " + method + " COST: " + cost);
		return erpsi > 0 ? erpsi * cost : cost;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	@Override
	public SimpleCatalog clone() {
		return new SimpleCatalog(this.schema, this.cardinalities, this.numberOfOutputTuplesPerInput, this.costs, this.columnSelectivity,
				this.columnCardinalities, this.frequencyMaps, this.SQLServerHistograms);
	}

	/** This method can be used by schema discovery functions to add table cardinalities.
	 * @param r
	 * @param value
	 */
	public void addRelationCardinality(Relation r, Integer value) {
		this.cardinalities.put(r, value);
	}

	/** Exports this class into a string that can be used to create a catalog.properties file.
	 * @return
	 */
	public String exportCatalog() {
		StringBuilder sb = new StringBuilder();
		sb.append("#Cost:\n");
		sb.append("#	RE:[TABLE_NAME]		BI:[ACCESS_METHOD_NAME]		RT:[COST_OF_ACCESS_METHOD] #COMMENT\n");
		sb.append("#Erspi:\n");
		sb.append("#	RE:[TABLE_NAME]		BI:[ACCESS_METHOD_NAME]		ERSPI:[COST_OF_ACCESS_METHOD]\n");
		sb.append("#Sql server histogram (this has no known usages/examples or tests):\n");
		sb.append("#	RE:[TABLE_NAME]		AT:[ATTRIBUTE_NAME]		SQLH:[???]\n");
		sb.append("#Column selectivity:\n");
		sb.append("#	RE:[TABLE_NAME]		AT:[ATTRIBUTE_NAME]		SE:[SELECTIVITY]\n");
		sb.append("#Column cardinality (Number Of Output Tuples Per Input):\n");
		sb.append("#	RE:[TABLE_NAME]		AT:[ATTRIBUTE_NAME]		CC:[DECIMAL_CARDINALITY]\n");
		sb.append("#Relation cardinality:\n");
		sb.append("#	RE:[TABLE_NAME]		CA:[DECIMAL_CARDINALITY]\n");

		sb.append("\n#RELATION CARDINALITIES:\n");
		for (Relation r: cardinalities.keySet()) {
			sb.append("RE:");
			sb.append(r.getName());
			sb.append("\tCA:");
			sb.append(cardinalities.get(r));
			sb.append("\n");
		}

		sb.append("\n#ACCESS METHOD COSTS:\n");
		for (Pair<Relation,AccessMethodDescriptor> p: costs.keySet()) {
			sb.append("RE:");
			sb.append(p.getLeft().getName());
			sb.append("\tBI:");
			sb.append(p.getRight().getName());
			sb.append("\tRT:");
			sb.append(costs.get(p));
			sb.append("\n");
		}
		sb.append("\n#COLUMN CARDINALITIES:\n");
		for (Pair<Relation,Attribute> p: columnCardinalities.keySet()) {
			sb.append("RE:");
			sb.append(p.getLeft().getName());
			sb.append("\tAT:");
			sb.append(p.getRight().getName());
			sb.append("\tCC:");
			sb.append(columnCardinalities.get(p));
			sb.append("\n");
		}

		sb.append("\n#ERSPI (Number Of Output Tuples Per Input):\n");
		for (Pair<Relation,AccessMethodDescriptor> p: numberOfOutputTuplesPerInput.keySet()) {
			sb.append("RE:");
			sb.append(p.getLeft().getName());
			sb.append("\tBI:");
			sb.append(p.getRight().getName());
			sb.append("\tERSPI:");
			sb.append(numberOfOutputTuplesPerInput.get(p));
			sb.append("\n");
		}

		sb.append("\n#COLUMN SELECTIVITY:\n");
		for (Pair<Relation,Attribute> p: columnSelectivity.keySet()) {
			sb.append("RE:");
			sb.append(p.getLeft().getName());
			sb.append("\tAT:");
			sb.append(p.getRight().getName());
			sb.append("\tSE:");
			sb.append(columnSelectivity.get(p));
			sb.append("\n");
		}


		return sb.toString();
	}

	/* (non-Javadoc)
	 * @see uk.ac.ox.cs.pdq.cost.statistics.Catalog#getQuality(uk.ac.ox.cs.pdq.db.Relation)
	 */
	@Override
	public double getQuality(Relation relation) {
		return DEFAULT_QUALITY;
	}

	/**
	 * Gets the SQL server histogram.
	 *
	 * @param relation the relation
	 * @param attribute the attribute
	 * @return the SQL server histogram
	 */
	public SQLServerHistogram getSQLServerHistogram(Relation relation, Attribute attribute) {
		Preconditions.checkNotNull(relation);
		Preconditions.checkNotNull(attribute);
		return this.SQLServerHistograms.get(Pair.of(relation, attribute));
	}

	/* (non-Javadoc)
	 * @see uk.ac.ox.cs.pdq.cost.statistics.Catalog#getHistogram(uk.ac.ox.cs.pdq.db.Relation, uk.ac.ox.cs.pdq.db.Attribute)
	 */
	@Override
	public Histogram getHistogram(Relation relation, Attribute attribute) {
		Preconditions.checkNotNull(relation);
		Preconditions.checkNotNull(attribute);
		return this.SQLServerHistograms.get(Pair.of(relation, attribute));
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return new String(
				"\n============RELATION CARDINALITIES==========\n" +
						Joiner.on("\n").join(this.cardinalities.entrySet()) +

						"\n============COLUMN CARDINALITIES============\n" +
						Joiner.on("\n").join(this.columnCardinalities.entrySet()) +

						"\n==================ERSPI=====================\n" +
						Joiner.on("\n").join(this.numberOfOutputTuplesPerInput.entrySet()) +

						"\n==================COSTS=====================\n" +
						Joiner.on("\n").join(this.costs.entrySet()) +

						"\n============COLUMN SELECTIVITIES============\n" +
						Joiner.on("\n").join(this.columnSelectivity.entrySet()) +

						"\n==============COLUMN HISTOGRAMS=============\n" +
						Joiner.on("\n").join(this.frequencyMaps.entrySet()) );
	}

}
