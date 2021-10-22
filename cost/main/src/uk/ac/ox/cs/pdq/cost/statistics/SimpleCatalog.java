// This file is part of PDQ (https://github.com/ProofDrivenQuerying/pdq) which is released under the MIT license.
// See accompanying LICENSE for copyright notice and full details.

package uk.ac.ox.cs.pdq.cost.statistics;


import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.log4j.Logger;
import uk.ac.ox.cs.pdq.db.AccessMethodDescriptor;
import uk.ac.ox.cs.pdq.db.Attribute;
import uk.ac.ox.cs.pdq.db.Relation;
import uk.ac.ox.cs.pdq.db.Schema;
import uk.ac.ox.cs.pdq.fol.TypedConstant;

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

/**
 * Holds unconditional statistics.
 * The statistics that are maintained are:
 * -the relation cardinalities,
 * -cardinalities of single attributes
 * -the size of output per invocation of an access method
 * -the cost of an access method
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
 * Example content of the file for a relation that contains 150 records and has 2 access methods, and a selectivity example
 * RE: cars CA: 150
 * RE: cars BI: read_all RT: 12.5
 * RE: cars BI: read_one RT: 0.5
 * RE:AssayLimited  AT:assay_type SE:0.4 #Selectivity if assay_type='F'

 * @author Efthymia Tsamoura
 * @author Gabor
 * @Contributor Brandon Moore
 */
public class SimpleCatalog implements Catalog{

	/** Logger. */
	private static final Logger log = Logger.getLogger(SimpleCatalog.class);

	private static final int DEFAULT_CARDINALITY = 1000000;
	private static final double DEFAULT_QUALITY = 0.0;
	private static final int DEFAULT_COLUMN_CARDINALITY = 1000;
	private static final double DEFAULT_COST = 1.0;
	private static final String CATALOG_FILE_NAME = "catalog.properties";

	private static final String READ_CARDINALITY = "^(RE:(\\w+)(\\s+)CA:(\\d+))";
	private static final String READ_COLUMN_CARDINALITY = "^(RE:(\\w+)(\\s+)AT:(\\w+)(\\s+)CC:(\\d+))";
	private static final String READ_ERSPI = "^(RE:(\\w+)(\\s+)BI:(\\w+)(\\s+)ERSPI:(\\d+(\\.\\d+)?))";
	private static final String READ_COST = "^(RE:(\\w+)(\\s+)BI:(\\w+)(\\s+)RT:(\\d+(\\.\\d+)?))";

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

	/** Cardinalities of the schema relations, with lookup by name*/
	private final Map<String,Integer> cardinalitiesLookupByName;
	/** Cardinalities of the relations' attributes, with lookup by name*/
	private final Map<Pair<String,String>,Integer> columnCardinalitiesLookupByName;
	/** The estimated result size per invocation of each access method, with lookup by name*/
	private final Map<Pair<String,String>,Integer> numberOfOutputTuplesPerInputLookupByName;
	/** The response time of each access method, with lookup by name*/
	private final Map<Pair<String,String>,Double> costsLookupByName;
	/** The selectivity of each attribute, with lookup by name*/
	private final Map<Pair<String,String>,Double> columnSelectivityLookupByName;

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

		this.cardinalitiesLookupByName = new HashMap<>();
		this.columnCardinalitiesLookupByName = new HashMap<>();
		this.numberOfOutputTuplesPerInputLookupByName = new HashMap<>();
		this.costsLookupByName = new HashMap<>();
		this.columnSelectivityLookupByName = new HashMap<>();

		this.read(schema, fileName);
	}

	private void read(Schema schema){

	}

	/**
	 * Read.
	 *
	 * @param schema the schema
	 * @param fileName 		The file that stores the statistics
	 */
	private void read(Schema schema, String fileName) {
		String catalogFile;
		//Find the catalog.properties in the subfolder of the schema names in the ./.pdq/catalog Directory
		//if no Filename location specified the default SimpleCatalog.CATALOG_FILE_NAME is used
		// and is filtered into working directory
		if(fileName.equals(SimpleCatalog.CATALOG_FILE_NAME)){
			catalogFile = String.format("./.pdq/catalog/%s/%s",schema.getName(),fileName);
		}else{
			catalogFile = fileName;
		}
		String line;
		try {
			FileReader fileReader = new FileReader(catalogFile);
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
				final int value = Integer.parseInt(cardinality);
				this.cardinalities.put(r, value);
				this.cardinalitiesLookupByName.put(r.getName(), value);
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
					final int value = Integer.parseInt(cardinality);
					this.columnCardinalities.put( Pair.of(r,attribute), value);
					this.columnCardinalitiesLookupByName.put( Pair.of(r.getName(),attribute.getName()), value);
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
					final int value = Integer.parseInt(erspi);
					this.numberOfOutputTuplesPerInput.put( Pair.of(r,b), value);
					this.numberOfOutputTuplesPerInputLookupByName.put( Pair.of(r.getName(),b.getName()), value);
					log.info("RELATION: " + relation + " BINDING: " + binding + " ERPSI: " + erspi);
				}
				else {
					throw new java.lang.IllegalArgumentException(String.format("Requesting AccessMethod [%s] but not found in relation [%s]", binding, relation));
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
					final double value = Double.parseDouble(cost);
					this.costs.put(Pair.of(r, b), value);
					this.costsLookupByName.put(Pair.of(r.getName(), b.getName()), value);
					log.info("RELATION: " + relation + " BINDING: " + binding + " COST: " + cost);
				}
				else {
					throw new java.lang.IllegalArgumentException(String.format("Requesting AccessMethod [%s] but not found in relation [%s]", binding, relation));
				}
			}
			else {
				throw new java.lang.IllegalArgumentException(String.format(String.format("Requesting relation [%s] in schema [%s] but not found", relation, schema)));
			}
			return;
		}

	}

	/**
	 * Populate the maps with lookup by name from the maps with lookup by object.
	 *
	 * The former are, for instance
	 *     Map<Pair<Relation,AccessMethodDescriptor>,Double>
	 * while the latter are, for instance
	 *     Map<Pair<String,String>,Double>
	 *
	 * The more permissive lookups allow a base Relation to match a derived relation such as a View.
	 */
	private void updateMapsThatLookUpByName() {

		for (Map.Entry<Relation, Integer> entry : this.cardinalities.entrySet()) {
			Relation key = entry.getKey();
			Integer value = entry.getValue();
			this.cardinalitiesLookupByName.put(key.getName(), value);
		}

		for (Map.Entry<Pair<Relation, Attribute>, Integer> entry : this.columnCardinalities.entrySet()) {
			Pair<Relation, Attribute> key = entry.getKey();
			Integer value = entry.getValue();
			this.columnCardinalitiesLookupByName.put(Pair.of(key.getLeft().getName(), key.getRight().getName()), value);
		}

		for (Map.Entry<Pair<Relation, AccessMethodDescriptor>, Integer> entry : this.numberOfOutputTuplesPerInput.entrySet()) {
			Pair<Relation,AccessMethodDescriptor> key = entry.getKey();
			Integer value = entry.getValue();
			this.numberOfOutputTuplesPerInputLookupByName.put(Pair.of(key.getLeft().getName(), key.getRight().getName()), value);
		}

		for (Map.Entry<Pair<Relation,AccessMethodDescriptor>,Double> entry : this.costs.entrySet()) {
			Pair<Relation,AccessMethodDescriptor> key = entry.getKey();
			Double value = entry.getValue();
			this.costsLookupByName.put(Pair.of(key.getLeft().getName(), key.getRight().getName()), value);
		}

		for (Map.Entry<Pair<Relation, Attribute>, Double> entry : this.columnSelectivity.entrySet()) {
			Pair<Relation, Attribute> key = entry.getKey();
			Double value = entry.getValue();
			this.columnSelectivityLookupByName.put(Pair.of(key.getLeft().getName(), key.getRight().getName()), value);
		}

	}

	/**
	 * Query this.cardinalities given the relation.
	 *
	 * If the lookup fails, try looking up by name in this.cardinalitiesLookupByName.
	 */
	private Integer getCardinalityFromMap(Relation r) {
		Integer value = this.cardinalities.get(r);
		if (value == null) {
			value = this.cardinalitiesLookupByName.get(r.getName());
		}
		return value;
	}

	/**
	 * Query this.columnCardinalities given the relation and attribute.
	 *
	 * If the lookup fails, try looking up by name in this.columnCardinalitiesLookupByName.
	 */
	private Integer getColumnCardinalityFromMap(Relation relation, Attribute attribute) {
		Integer value = this.columnCardinalities.get(Pair.of(relation, attribute));
		if (value == null) {
			value = this.columnCardinalitiesLookupByName.get(Pair.of(relation.getName(), attribute.getName()));
		}
		return value;
	}

	/**
	 * Query this.numberOfOutputTuplesPerInput given the relation and access method.
	 *
	 * If the lookup fails, try looking up by name in this.numberOfOutputTuplesPerInputLookupByName.
	 */
	private Integer getNumberOfOutputTuplesPerInputFromMap(Relation r, AccessMethodDescriptor amd) {
		Integer value = this.numberOfOutputTuplesPerInput.get(Pair.of(r, amd));
		if (value == null) {
			value = this.numberOfOutputTuplesPerInputLookupByName.get(Pair.of(r.getName(), amd.getName()));
		}
		return value;
	}

	/**
	 * Query this.costs given the relation and access method.
	 *
	 * If the lookup fails, try looking up by name in this.costsLookupByName.
	 */
	private Double getCostFromMap(Relation r, AccessMethodDescriptor amd) {
		Double value = this.costs.get(Pair.of(r, amd));
		if (value == null) {
			value = this.costsLookupByName.get(Pair.of(r.getName(), amd.getName()));
		}
		return value;
	}

	/**
	 * Query this.columnSelectivity given the relation and attribute.
	 *
	 * If the lookup fails, try looking up by name in this.columnSelectivityLookupByName.
	 */
	private Double getColumnSelectivityFromMap(Relation relation, Attribute attribute) {
		Double value = this.columnSelectivity.get(Pair.of(relation, attribute));
		if (value == null) {
			value = this.columnSelectivityLookupByName.get(Pair.of(relation.getName(), attribute.getName()));
		}
		return value;
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
	 */
	public SimpleCatalog(
			Schema schema,
			Map<Relation,Integer> cardinalities,
			Map<Pair<Relation,AccessMethodDescriptor>,Integer> erpsi,
			Map<Pair<Relation,AccessMethodDescriptor>,Double> responseTimes,
			Map<Pair<Relation,Attribute>,Double> columnSelectivity,
			Map<Pair<Relation,Attribute>,Integer> columnCardinalities
			) {
		Preconditions.checkNotNull(schema);
		Preconditions.checkNotNull(cardinalities);
		Preconditions.checkNotNull(erpsi);
		Preconditions.checkNotNull(responseTimes);
		Preconditions.checkNotNull(columnSelectivity);
		Preconditions.checkNotNull(columnCardinalities);
		this.schema = schema;
		this.cardinalities = Maps.newHashMap(cardinalities);
		this.numberOfOutputTuplesPerInput = Maps.newHashMap(erpsi);
		this.costs = Maps.newHashMap(responseTimes);
		this.columnSelectivity = Maps.newHashMap(columnSelectivity);
		this.columnCardinalities = Maps.newHashMap(columnCardinalities);

		// Initialise the maps with lookup by name, then construct them from the maps above
		this.cardinalitiesLookupByName = new HashMap<>();
		this.columnCardinalitiesLookupByName = new HashMap<>();
		this.numberOfOutputTuplesPerInputLookupByName = new HashMap<>();
		this.costsLookupByName = new HashMap<>();
		this.columnSelectivityLookupByName = new HashMap<>();
		this.updateMapsThatLookUpByName();
	}

	/** (non-Javadoc)
	 * @see uk.ac.ox.cs.pdq.cost.statistics.Catalog#getSelectivity(Relation, Attribute, TypedConstant)
	 */
	public Double getSelectivity(Relation relation, Attribute attribute, TypedConstant constant) {
		Preconditions.checkNotNull(relation);
		Preconditions.checkNotNull(attribute);
		Preconditions.checkNotNull(constant);

		return this.getSelectivity(relation, attribute);
	}

	/** (non-Javadoc)
	 * @see uk.ac.ox.cs.pdq.cost.statistics.Catalog#getSize(Relation, Attribute, TypedConstant)
	 */
	@Override
	public int getSize(Relation relation, Attribute attribute, TypedConstant constant) {
		Preconditions.checkNotNull(relation);
		Preconditions.checkNotNull(attribute);
		Preconditions.checkNotNull(constant);

		String search = constant.toString();
		if(constant.getType() instanceof Class && BigDecimal.class.isAssignableFrom((Class<?>) constant.getType())) {
			BigInteger integer = new BigDecimal(constant.toString()).toBigInteger();
			search = integer.toString();
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
		final Double selectivities = getColumnSelectivityFromMap(relation, attribute);
		if(selectivities != null) {
			log.info("RELATION: " + relation.getName() + " ATTRIBUTE: " + attribute + " SELECTIVITY: " + selectivities);
			return selectivities;
		} else {
			Integer columnCardinality = this.getColumnCardinalityFromMap(relation, attribute);
			if (columnCardinality != null) {
				final double selectivity = 1.0 / (double) columnCardinality;
				log.info("RELATION: " + relation.getName() + " ATTRIBUTE: " + attribute + " SELECTIVITY: " + selectivity);
				return selectivity;
			}
			final double selectivity = 1.0 / (double) DEFAULT_COLUMN_CARDINALITY;
			log.warn("RELATION: " + relation.getName() + " ATTRIBUTE: " + attribute
					+ " is using the DEFAULT_COLUMN_CARDINALITY: " + DEFAULT_COLUMN_CARDINALITY);
			log.info("RELATION: " + relation.getName() + " ATTRIBUTE: " + attribute + " SELECTIVITY: " + selectivity);
			return selectivity;
		}
	}

	@Override
	public int getTotalNumberOfOutputTuplesPerInputTuple(Relation relation, AccessMethodDescriptor method) {
		log.info("[DEBUG] case_001a");
		Preconditions.checkNotNull(relation);
		Preconditions.checkNotNull(method);
		final Integer erspi = getNumberOfOutputTuplesPerInputFromMap(relation, method);
		if(erspi == null) {
			double columnProduct = 1.0;
			for(Integer input:method.getInputs()) {
				Attribute attribute = relation.getAttribute(input);
				final Integer columnCardinality = getColumnCardinalityFromMap(relation, attribute);
				if(columnCardinality != null) {
					columnProduct *= columnCardinality;
				}
				else {
					log.warn("RELATION: " + relation.getName() + " AccessMethod: " + method + " is using the DEFAULT_CARDINALITY: "
							+ DEFAULT_CARDINALITY + " and DEFAULT_COLUMN_CARDINALITY: " + DEFAULT_COLUMN_CARDINALITY);

					final int defaultValue = DEFAULT_CARDINALITY / DEFAULT_COLUMN_CARDINALITY;
					log.warn("RELATION: " + relation.getName() + " AccessMethod: " + method + " ERPSI: " + defaultValue);
					return defaultValue;
				}
			}
			if (getCardinalityFromMap(relation) != null) {
				final int value = getCardinalityFromMap(relation);
				log.info("RELATION: " + relation.getName() + " AccessMethod: " + method + " ERPSI: " + value / columnProduct);
				return (int) (value / columnProduct);
			}
			else {
				log.warn("RELATION: " + relation.getName() + " AccessMethod: " + method + " is using the DEFAULT_CARDINALITY: "
						+ DEFAULT_CARDINALITY + " and DEFAULT_COLUMN_CARDINALITY: " + DEFAULT_COLUMN_CARDINALITY);

				final int defaultValue = DEFAULT_CARDINALITY / DEFAULT_COLUMN_CARDINALITY;

				log.info("RELATION: " + relation.getName() + " AccessMethod: " + method + " ERPSI: " + defaultValue);
				return defaultValue;
			}
		}
		return erspi;
	}


	@Override
	public int getTotalNumberOfOutputTuplesPerInputTuple(Relation relation, AccessMethodDescriptor method, Map<Integer, TypedConstant> inputs) {
		Preconditions.checkNotNull(relation);
		Preconditions.checkNotNull(inputs);
		int erpsi = this.getTotalNumberOfOutputTuplesPerInputTuple(relation, method);
		log.info("RELATION: " + relation.getName() + " ACCESS: " + method + " INPUTS: " + inputs + " ERPSI: " + erpsi);
		return erpsi;
	}

	/** (non-Javadoc)
	 * @see uk.ac.ox.cs.pdq.cost.statistics.Catalog#getCardinality(uk.ac.ox.cs.pdq.db.Relation)
	 */
	@Override
	public int getCardinality(Relation relation) {

		final Integer cardinality = getCardinalityFromMap(relation);

		if(cardinality == null) {
			log.warn("RELATION: " + relation.getName() + " is using the DEFAULT_CARDINALITY: " + DEFAULT_CARDINALITY);
			return DEFAULT_CARDINALITY;
		} else {
			return cardinality;
		}
	}

	/** (non-Javadoc)
	 * @see uk.ac.ox.cs.pdq.cost.statistics.Catalog#getCardinality(uk.ac.ox.cs.pdq.db.Relation, uk.ac.ox.cs.pdq.db.Attribute)
	 */
	@Override
	public int getCardinality(Relation relation, Attribute attribute) {

		final Integer cardinality = getColumnCardinalityFromMap(relation, attribute);

		if(cardinality == null) {
			log.warn("RELATION: " + relation.getName() + " ATTRIBUTE: " + attribute
					+ " is using the DEFAULT_COLUMN_CARDINALITY: " + DEFAULT_COLUMN_CARDINALITY);
			return DEFAULT_COLUMN_CARDINALITY;
		} else {
			return cardinality;
		}
	}

	/** (non-Javadoc)
	 * @see uk.ac.ox.cs.pdq.cost.statistics.Catalog#getCost(Relation, AccessMethodDescriptor)
	 */
	@Override
	public double getCost(Relation relation, AccessMethodDescriptor method) {
		Preconditions.checkNotNull(relation);
		Preconditions.checkNotNull(method);
		final Double cost = this.getCostFromMap(relation, method);

		if(cost == null) {
			log.warn("RELATION: " + relation.getName() + " ACCESS METHOD: " + method + " is using the DEFAULT_COST: "
					+ DEFAULT_COST);
			return DEFAULT_COST;
		} else {
			return cost;
		}
	}

	/** (non-Javadoc)
	 * @see uk.ac.ox.cs.pdq.cost.statistics.Catalog#getCost(Relation, AccessMethodDescriptor)
	 */
	@Override
	public double getCost(Relation relation, AccessMethodDescriptor method, Map<Integer, TypedConstant> inputs) {
		Preconditions.checkNotNull(relation);
		Preconditions.checkNotNull(method);
		double erpsi = -1;
		Double cost = this.getCostFromMap(relation, method);
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
		return new SimpleCatalog(this.schema, this.cardinalities, this.numberOfOutputTuplesPerInput, this.costs,
				this.columnSelectivity, this.columnCardinalities);
	}

	/** This method can be used by schema discovery functions to add table cardinalities.
	 * @param r
	 * @param value
	 */
	public void addRelationCardinality(Relation r, Integer value) {
		this.cardinalities.put(r, value);
		this.cardinalitiesLookupByName.put(r.getName(), value);
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


		return sb.toString();
	}

	/* (non-Javadoc)
	 * @see uk.ac.ox.cs.pdq.cost.statistics.Catalog#getQuality(uk.ac.ox.cs.pdq.db.Relation)
	 */
	@Override
	public double getQuality(Relation relation) {
		return DEFAULT_QUALITY;
	}

	/** (non-Javadoc)
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
						Joiner.on("\n").join(this.costs.entrySet()) );
	}

}
