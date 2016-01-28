package uk.ac.ox.cs.pdq.cost.statistics;


import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.log4j.Logger;

import uk.ac.ox.cs.pdq.db.AccessMethod;
import uk.ac.ox.cs.pdq.db.Attribute;
import uk.ac.ox.cs.pdq.db.Relation;
import uk.ac.ox.cs.pdq.db.Schema;
import uk.ac.ox.cs.pdq.db.TypedConstant;
import uk.ac.ox.cs.pdq.fol.ConjunctiveQuery;
import uk.ac.ox.cs.pdq.fol.Query;
import uk.ac.ox.cs.pdq.io.xml.QueryReader;
import uk.ac.ox.cs.pdq.io.xml.SchemaReader;
import uk.ac.ox.cs.pdq.plan.CommandToTGDTranslator;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

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
 * @author Efthymia Tsamoura
 *
 */
public class SimpleCatalog implements Catalog{

	/** Logger. */
	private static Logger log = Logger.getLogger(SimpleCatalog.class);
	
	public static double DEFAULT_ATTRIBUTE_EQUALITY_SELECTIVITY = 0.1;

	private static int DEFAULT_CARDINALITY = 1000000;
	private static double DEFAULT_QUALITY = 0.0;
	private static int DEFAULT_COLUMN_CARDINALITY = 1000;
	private static double DEFAULT_COST = 1.0;
	private static String CATALOG_FILE_NAME = "catalog/catalog.properties";

	private static String READ_CARDINALITY = "^(RE:(\\w+)(\\s+)CA:(\\d+))";
	private static String READ_COLUMN_CARDINALITY = "^(RE:(\\w+)(\\s+)AT:(\\w+)(\\s+)CC:(\\d+))";
	private static String READ_COLUMN_SELECTIVITY = "^(RE:(\\w+)(\\s+)AT:(\\w+)(\\s+)SE:(\\d+(\\.\\d+)?))";
	private static String READ_ERSPI = "^(RE:(\\w+)(\\s+)BI:(\\w+)(\\s+)ERSPI:(\\d+(\\.\\d+)?))";
	private static String READ_COST = "^(RE:(\\w+)(\\s+)BI:(\\w+)(\\s+)RT:(\\d+(\\.\\d+)?))";
	private static String READ_SQLSERVERHISTOGRAM = "^(RE:(\\w+)(\\s+)AT:(\\w+)(\\s+)SQLH:((/[a-zA-Z0-9._-]+)+/?))";

	/** Cardinalities of the schema relations*/
	private final Map<Relation,Integer> cardinalities;
	/** Cardinalities of the relations' attributes*/
	private final Map<Pair<Relation,Attribute>,Integer> columnCardinalities;
	/** The estimated result size per invocation of each access method*/
	private final Map<Pair<Relation,AccessMethod>,Integer> erpsi;
	/** The response time of each access method*/
	private final Map<Pair<Relation,AccessMethod>,Double> costs;
	/** The selectivity of each attribute*/
	private final Map<Pair<Relation,Attribute>,Double> columnSelectivity;
	/** The frequency histogram of each attribute*/
	private final Map<Pair<Relation,Attribute>, SimpleFrequencyMap> frequencyMaps;
	/** The SQL Server histograms of each attribute*/
	private final Map<Pair<Relation,Attribute>, SQLServerHistogram> SQLServerHistograms;
	/** 
	 * The queries correspond to cardinality expressions. 
	 * This structure maps cardinality queries to its size. 
	 * This implementation keeps only cardinality expressions coming from single relations-single attribute.*/
	private final Map<Query<?>,Integer> queries; 
	/** The schema of the input database */
	private final Schema schema;

	/**
	 * Creates a catalog by loading metadata located at "catalog/catalog.properties"
	 * @param schema
	 */
	public SimpleCatalog(Schema schema) {
		this(schema, SimpleCatalog.CATALOG_FILE_NAME);
	}

	/**
	 * Creates a catalog by loading schema metadata located at the input file
	 * @param schema
	 * @param fileName
	 */
	public SimpleCatalog(Schema schema, String fileName) {
		Preconditions.checkNotNull(schema);
		this.schema = schema;
		this.erpsi = new HashMap<>();
		this.costs = new HashMap<>();
		this.columnSelectivity = new HashMap<>();
		this.cardinalities = new HashMap<>();
		this.columnCardinalities = new HashMap<>();
		this.frequencyMaps = new HashMap<>();
		this.SQLServerHistograms = new HashMap<>();
		this.read(schema, fileName);
		this.queries = SimpleCatalog.getQueries(this.columnCardinalities);
		this.queries.putAll(SimpleCatalog.getQueriesFromHistograms(this.frequencyMaps));
	}

	/**
	 * 
	 * @param columnCardinalities
	 * @return
	 * 		query expressions for the input set of column cardinalities
	 */
	private static Map<Query<?>,Integer> getQueries(Map<Pair<Relation,Attribute>,Integer> columnCardinalities) {
		Map<Query<?>,Integer> ret = Maps.newHashMap();
		for(Entry<Pair<Relation, Attribute>, Integer> entry:columnCardinalities.entrySet()) {
			Relation relation = entry.getKey().getLeft();
			Attribute attribute = entry.getKey().getRight();
			Query<?> query = new CommandToTGDTranslator().toQuery(relation, attribute);
			ret.put(query, entry.getValue());
		}
		return ret;
	}

	/**
	 * 
	 * @param columnCardinalities
	 * @return
	 * 		query expressions for the input set of histograms
	 */
	private static Map<Query<?>,Integer> getQueriesFromHistograms(Map<Pair<Relation,Attribute>, SimpleFrequencyMap> histograms) {
		Map<Query<?>,Integer> ret = Maps.newHashMap();
		for(Entry<Pair<Relation, Attribute>, SimpleFrequencyMap> entry:histograms.entrySet()) {
			Relation relation = entry.getKey().getLeft();
			Attribute attribute = entry.getKey().getRight();
			SimpleFrequencyMap histogram = entry.getValue();
			for(Entry<String, Integer> frequency:histogram.getFrequencies().entrySet()) {
				Map<Attribute, TypedConstant> constantsMap = new HashMap<>();
				constantsMap.put(attribute, new TypedConstant(frequency.getKey()));
				List<Attribute> free = Lists.newArrayList(relation.getAttributes());
				free.remove(attribute);
				Query<?> query = new CommandToTGDTranslator().toQuery(relation, constantsMap, free);
				ret.put(query, frequency.getValue());
			}
		}
		return ret;
	}

	/**
	 * 
	 * @param schema
	 * @param fileName
	 * 		The file that stores the statistics 
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
			ex.printStackTrace(System.out);
		}
		catch(IOException ex) {
			ex.printStackTrace(System.out);
		}
	}

	/**
	 * Parses the statistics file
	 * @param schema
	 * @param line
	 */
	private void parse(Schema schema, String line) {
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
				if(r.getAccessMethod(binding) != null) {
					AccessMethod b = r.getAccessMethod(binding);
					this.erpsi.put( Pair.of(r,b), Integer.parseInt(erspi));  
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
				if(r.getAccessMethod(binding) != null) {
					AccessMethod b = r.getAccessMethod(binding);
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
	 * 
	 * @param cardinalities
	 * @param erpsi
	 * @param responseTimes
	 * @param columnSelectivity
	 * @param columnCardinalities
	 */
	private SimpleCatalog(Schema schema, Map<Relation,Integer> cardinalities, Map<Pair<Relation,AccessMethod>,Integer> erpsi, Map<Pair<Relation,AccessMethod>,Double> responseTimes,
			Map<Pair<Relation,Attribute>,Double> columnSelectivity, Map<Pair<Relation,Attribute>,Integer> columnCardinalities, 
			Map<Query<?>,Integer> queries,
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
		this.erpsi = Maps.newHashMap(erpsi);
		this.costs = Maps.newHashMap(responseTimes);
		this.columnSelectivity = Maps.newHashMap(columnSelectivity);
		this.columnCardinalities = Maps.newHashMap(columnCardinalities);
		this.frequencyMaps = Maps.newHashMap(frequencyMaps);
		this.SQLServerHistograms = Maps.newHashMap(SQLServerHistograms);
		this.queries = Maps.newHashMap(queries);
	}

	public Double getSelectivity(Relation relation, Attribute attribute, TypedConstant<?> constant) {
		Preconditions.checkNotNull(relation);
		Preconditions.checkNotNull(attribute);
		Preconditions.checkNotNull(constant);

		SimpleFrequencyMap histogram = this.frequencyMaps.get(Pair.of(relation, attribute));
		String search = constant.toString();
		if(constant.getType() instanceof Class && BigDecimal.class.isAssignableFrom((Class) constant.getType())) {
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
	public int getSize(Relation relation, Attribute attribute, TypedConstant<?> constant) {
		Preconditions.checkNotNull(relation);
		Preconditions.checkNotNull(attribute);
		Preconditions.checkNotNull(constant);

		SimpleFrequencyMap histogram = this.frequencyMaps.get(Pair.of(relation, attribute));
		String search = constant.toString();
		if(constant.getType() instanceof Class && BigDecimal.class.isAssignableFrom((Class) constant.getType())) {
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

	@Override
	public int getERPSI(Relation relation, AccessMethod method) {
		Preconditions.checkNotNull(relation);
		Preconditions.checkNotNull(method);
		Integer erspi = this.erpsi.get(Pair.of(relation, method));
		if(erspi == null) {
			double columnProduct = 1.0;
			for(Integer input:method.getZeroBasedInputs()) {
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


	@Override
	public int getERPSI(Relation relation, AccessMethod method, Map<Integer, TypedConstant<?>> inputs) {
		Preconditions.checkNotNull(relation);
		Preconditions.checkNotNull(inputs);
		if(inputs.size() == 1 && method.getZeroBasedInputs().size() == 1) {
			Attribute attribute = relation.getAttribute(method.getZeroBasedInputs().get(0));
			SimpleFrequencyMap histogram = this.frequencyMaps.get(Pair.of(relation, attribute));
			if(histogram != null && histogram.getFrequency(inputs.get(0).toString()) != null) {
				int erpsi = histogram.getFrequency(inputs.get(0).toString());
				log.info("RELATION: " + relation.getName() + " ACCESS: " + method + " INPUTS: " + inputs + " ERPSI: " + erpsi);
				return erpsi;
			}
		}
		int erpsi = this.getERPSI(relation, method);
		log.info("RELATION: " + relation.getName() + " ACCESS: " + method + " INPUTS: " + inputs + " ERPSI: " + erpsi);
		return erpsi;
	}

	@Override
	public int getCardinality(Relation relation) {
		if(this.cardinalities.get(relation) != null) {
			return this.cardinalities.get(relation);
		}
		return DEFAULT_CARDINALITY;
	}

	@Override
	public int getCardinality(Relation relation, Attribute attribute) {
		if(this.columnCardinalities.get(Pair.of(relation, attribute)) != null) {
			log.info("RELATION: " + relation.getName() + " ATTRIBUTE: " + attribute + " CARDINALITY: " + this.columnCardinalities.get(Pair.of(relation, attribute)));
			return this.columnCardinalities.get(Pair.of(relation, attribute));
		}
		log.info("RELATION: " + relation.getName() + " ATTRIBUTE: " + attribute + " CARDINALITY: " + DEFAULT_COLUMN_CARDINALITY);
		return DEFAULT_COLUMN_CARDINALITY;
	}

	@Override
	public double getCost(Relation relation, AccessMethod method) {
		Preconditions.checkNotNull(relation);		
		Preconditions.checkNotNull(method);		
		Double cost = this.costs.get(Pair.of(relation, method));
		return cost == null ? DEFAULT_COST : cost;
	}

	@Override
	public double getCost(Relation relation, AccessMethod method, Map<Integer, TypedConstant<?>> inputs) {
		Preconditions.checkNotNull(relation);		
		Preconditions.checkNotNull(method);		
		double erpsi = -1;
		if(inputs.size() == 1) {
			Attribute attribute = relation.getAttribute(method.getZeroBasedInputs().get(0));
			SimpleFrequencyMap histogram = this.frequencyMaps.get(Pair.of(relation, attribute));
			if(histogram != null && histogram.getFrequency(inputs.get(0).toString()) != null) {
				erpsi = histogram.getFrequency(inputs.get(0).toString());
				log.info("RELATION: " + relation.getName() + " ACCESS: " + method + " INPUTS: " + inputs + " ERPSI: " + erpsi);
			}
		}
		Double cost = this.costs.get(Pair.of(relation, method));
		if(cost == null) {
			log.info("RELATION: " + relation.getName() + " ACCESS METHOD: " + method + " COST: " + DEFAULT_COST);
			return DEFAULT_COST;
		}
		log.info("RELATION: " + relation.getName() + " ACCESS METHOD: " + method + " COST: " + cost);
		return erpsi > 0 ? erpsi * cost : cost;
	}

	@Override
	public Collection<Query<?>> getStatisticsExpressions() {
		return this.queries.keySet();
	}

	@Override
	public SimpleCatalog clone() {
		return new SimpleCatalog(this.schema, this.cardinalities, this.erpsi, this.costs, this.columnSelectivity, 
				this.columnCardinalities, this.queries, this.frequencyMaps, this.SQLServerHistograms);
	}

	@Override
	public double getQuality(Relation relation) {
		return DEFAULT_QUALITY;
	}
	
	public SQLServerHistogram getSQLServerHistogram(Relation relation, Attribute attribute) {
		Preconditions.checkNotNull(relation);
		Preconditions.checkNotNull(attribute);
		return this.SQLServerHistograms.get(Pair.of(relation, attribute));
	}
	
	@Override
	public Histogram getHistogram(Relation relation, Attribute attribute) {
		Preconditions.checkNotNull(relation);
		Preconditions.checkNotNull(attribute);
		return this.SQLServerHistograms.get(Pair.of(relation, attribute));
	}

	@Override
	public String toString() {
		return new String(
				"\n============RELATION CARDINALITIES==========\n" + 
						Joiner.on("\n").join(this.cardinalities.entrySet()) + 

						"\n============COLUMN CARDINALITIES============\n" + 
						Joiner.on("\n").join(this.columnCardinalities.entrySet()) +

						"\n==================ERSPI=====================\n" + 
						Joiner.on("\n").join(this.erpsi.entrySet()) +

						"\n==================COSTS=====================\n" + 
						Joiner.on("\n").join(this.costs.entrySet()) +

						"\n============COLUMN SELECTIVITIES============\n" + 
						Joiner.on("\n").join(this.columnSelectivity.entrySet()) +

						"\n==============COLUMN HISTOGRAMS=============\n" + 
						Joiner.on("\n").join(this.frequencyMaps.entrySet()) );
	}

	public static void main(String... args) {
		String PATH = "SCHEMA AND QUERY PATH";
		String schemafile = "SCHEMA FILE";
		String queryfile = "QUERY FILE";
		String catalogfile = "CATALOG FILE";
		try(FileInputStream sis = new FileInputStream(PATH + schemafile);
				FileInputStream qis = new FileInputStream(PATH + queryfile)) {

			Schema schema = new SchemaReader().read(sis);
			ConjunctiveQuery query = new QueryReader(schema).read(qis);

			if (schema == null || query == null) {
				throw new IllegalStateException("Schema and query must be provided.");
			}
			schema.updateConstants(query.getSchemaConstants());
			SimpleCatalog catalog = new SimpleCatalog(schema, catalogfile);
			log.trace(catalog.toString());
		} catch (FileNotFoundException e) {
			log.error("Cannot find input files");
		} catch (Exception e) {
			log.error("EXCEPTION: " + e.getClass().getSimpleName() + " " + e.getMessage());
		} catch (Error e) {
			log.error("ERROR: " + e.getClass().getSimpleName() + " " + e.getMessage());
			System.exit(-1);
		}
	}
}
