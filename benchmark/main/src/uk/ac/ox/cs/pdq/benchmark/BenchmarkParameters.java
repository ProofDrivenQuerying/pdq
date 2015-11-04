package uk.ac.ox.cs.pdq.benchmark;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.log4j.Logger;

import uk.ac.ox.cs.pdq.cost.CostParameters.CostTypes;

/**
 * Specialises ConfigurationHelper for query/view generation.
 * 
 * @author Julien LEBLAY
 */
public class BenchmarkParameters extends uk.ac.ox.cs.pdq.Parameters {

	/** Logger. */
	private static Logger log = Logger.getLogger(BenchmarkParameters.class);

	/** Properties file name */
	public static final String DEFAULT_CONFIG_FILE_NAME = "pdq-benchmark.properties";

	/** Properties file path */
	public static final String DEFAULT_CONFIG_FILE_PATH = "./" + DEFAULT_CONFIG_FILE_NAME;

	@Parameter(description =
		"If true, prints the result of the evaluation of the input query to STDOUT")
	protected Boolean printQueryResult;

	@Parameter(description = 
		"If true, prints the result of the evaluation of the output plan to STDOUT")
	protected Boolean printPlanResult;

	@Parameter(description =
		"If true, prints the accessible version of the input schema to STDOUT")
	protected Boolean printAccessibleSchema;

	@Parameter(description=
		"If true, only run the search "
		+ "(e.g. does not attempt to evluation the resulting plan")
	protected Boolean searchOnly;

	@Parameter(description="Maximum arity of generated relations")
	private Integer arity;

	@Parameter(description="Number of generated relations")
	private Integer numberOfRelations;

	@Parameter(description="Maximum number of access method in generated relations")
	private Integer maxAccessMethods;

	@Parameter(description="Ratio of generated relations that should be accessible")
	private Double accessibility;

	@Parameter(description="Ratio of distinguished variables in generated queries")
	private Double freePosition;

	@Parameter(description="Connectivity ratio in generated queries")
	private Double connectivity;

	@Parameter(description="Projectivity ratio in generated queries")
	private Double projectivity;

	@Parameter(description="Maximun distance (number of rule firing) from the query to a free access relation")
	private Integer maxDistanceToFree;
	
	@Parameter(description="Type of generate to use")
	private GeneratorTypes generatorType;

	@Parameter(description="JoinTest ratio in generated queries")
	private Double join;

	@Parameter(description="Type of queries to generate")
	private QueryTypes queryType;

	@Parameter(description="Number of conjuncts in the generated queries")
	private Integer queryConjuncts;

	@Parameter(description="Number of conjuncts in the generated constraints")
	private Integer constraintConjuncts;

	@Parameter(description="Number of views to generate")
	private Integer numderOfViews;

	@Parameter(description="Number of constraints to generate")
	private Integer numberOfConstraints;

	@Parameter(description="Number of access methods to generate")
	private Integer numberOfAccessMethods;

	@Parameter(description="Maximum cardinality of the generated relations")
	private Integer relationSize;

	@Parameter(description="Maximun (simple) cost of access to generated relations")
	private Double maxCost;

	@Parameter(description="Mean (simple) cost of access to generated relations")
	private Double meanCost;

	@Parameter(description="Ratio of attribute positions to use as inputs in generated access methods")
	private Double inputPosition;

	@Parameter(description="Ratio of generated relations with a free access")
	private Double freeAccess;

	@Parameter(description="If true, generated queries are allowed to reference relations multiple times")
	private Boolean repeatedRelations;

	@Parameter(description="Path to the input schema file")
	private String inputSchema;

	@Parameter(description="Ratio of distinguished variables in generated queries",
			defaultValue = "0.0")
	private Double freeVariable = 0.0;

	@Parameter(description="Maximun (simple) cost of access to generated relations")
	private String validationDB;

	@Parameter(description="Type of cost estimation to use.")
	protected CostTypes costType;
	
	/**
	 * @return the version of the planner code, as given by Maven
	 */
	public static String getVersion() {
	    String path = "/benchmark.version";
	    try (InputStream stream = BenchmarkParameters.class.getResourceAsStream(path)) {
		    if (stream == null) {
				return "UNKNOWN";
			}
		    Properties props = new Properties();
	        props.load(stream);
	        stream.close();
	        return (String) props.get("version");
	    } catch (IOException e) {
	    	log.debug(e);
			return "UNKNOWN";
	    }
	}

	public BenchmarkParameters() {
		this(new File(DEFAULT_CONFIG_FILE_PATH));
	}

	/**
	 * Constructor for BenchmarkParameters.
	 * @param config path to the configuration file to read
	 */
	public BenchmarkParameters(File config) {
		this(config, false, false);
	}

	/**
	 * Constructor for BenchmarkParameters.
	 * @param config path to the configuration file to read
	 * @param verbose if true, param loading problem will be reported
	 */
	public BenchmarkParameters(File config, boolean verbose) {
		this(config, false, verbose);
	}

	/**
	 * Constructor for BenchmarkParameters.
	 * @param config path to the configuration file to read
	 * @param verbose if true, param loading problem will be reported
	 * @param strict if true, param loading problem will throw an exception
	 */
	public BenchmarkParameters(File config, boolean verbose, boolean strict) {
		this(config, false, verbose, strict);
	}

	/**
	 * Constructor for BenchmarkParameters.
	 * @param config path to the configuration file to read
	 * @param delay true if the loading of the given config file should be delayed
	 * @param verbose if true, param loading problem will be reported
	 * @param strict if true, param loading problem will throw an exception
	 */
	public BenchmarkParameters(File config, boolean delay, boolean verbose, boolean strict) {
		super(config, true, verbose, strict);
		if (!delay) {
			this.load(config, verbose, strict);
		}
	}

	public Boolean getPrintQueryResult() {
		return this.printQueryResult;
	}

	public void setPrintQueryResult(Boolean print) {
		this.printQueryResult = print;
	}

	public Boolean getPrintPlanResult() {
		return this.printPlanResult;
	}

	public void setPrintPlanResult(Boolean print) {
		this.printPlanResult = print;
	}

	public Boolean getPrintAccessibleSchema() {
		return this.printAccessibleSchema;
	}

	public void setPrintAccessibleSchema(Boolean print) {
		this.printAccessibleSchema = print;
	}

	public Boolean getSearchOnly() {
		return this.searchOnly;
	}

	public void setSearchOnly(Boolean searchOnly) {
		this.searchOnly = searchOnly;
	}

	public Integer getArity() {
		return this.arity;
	}

	public Integer getNumberOfAccessMethods() {
		return this.numberOfAccessMethods;
	}

	public Integer getNumberOfConstraints() {
		return this.numberOfConstraints;
	}

	public Integer getDependencyConjuncts() {
		return this.constraintConjuncts;
	}


	public Double getFreeAccess() {
		return this.freeAccess;
	}

	public Double getFreeVariable() {
		return this.freeVariable;
	}

	public Double getInputPosition() {
		return this.inputPosition;
	}

	public String getInputSchema() {
		return this.inputSchema;
	}

	public Double getMaxCost() {
		return this.maxCost;
	}

	public Double getMeanCost() {
		return this.meanCost;
	}

	public Integer getQueryConjuncts() {
		return this.queryConjuncts;
	}

	public Integer getNumberOfRelations() {
		return this.numberOfRelations;
	}

	public Integer getRelationSize() {
		return this.relationSize;
	}

	public Integer getNumberOfViews() {
		return this.numderOfViews;
	}

	public void setArity(Number arity) {
		this.arity = arity.intValue();
	}

	public void setNumberOfAccessMethods(Number bindings) {
		this.numberOfAccessMethods = bindings.intValue();
	}

	public void setNumberOfConstraints(Number dependencies) {
		this.numberOfConstraints = dependencies.intValue();
	}

	public void setConstraintConjuncts(Number dependencyConjuncts) {
		this.constraintConjuncts = dependencyConjuncts.intValue();
	}


	public void setFreeAccess(Number freeAccess) {
		this.freeAccess = freeAccess.doubleValue();
	}

	public void setFreeVariable(Number freeVarRatio) {
		this.freeVariable = freeVarRatio.doubleValue();
	}

	public void setInputPosition(Number inputPosition) {
		this.inputPosition = inputPosition.doubleValue();
	}

	public void setInputSchema(String inputSchema) {
		if (inputSchema != null && inputSchema.trim().isEmpty()) {
			this.inputSchema = null;
			return;
		}
		this.inputSchema = inputSchema;
	}

	public void setMaxCost(Number maxCost) {
		this.maxCost = maxCost.doubleValue();
	}

	public void setMeanCost(Number meanCost) {
		this.meanCost = meanCost.doubleValue();
	}

	public void setQueryConjuncts(Number queryConjuncts) {
		this.queryConjuncts = queryConjuncts.intValue();
	}

	public void setNumberOfRelations(Number relations) {
		this.numberOfRelations = relations.intValue();
	}

	public void setRelationSize(Number relationSize) {
		this.relationSize = relationSize.intValue();
	}

	public void setNumberOfViews(Number views) {
		this.numderOfViews = views.intValue();
	}

	public Boolean getRepeatedRelations() {
		return this.repeatedRelations;
	}

	public void setRepeatedRelations(Boolean allowRepeatedRelations) {
		this.repeatedRelations = allowRepeatedRelations;
	}

	public QueryTypes getQueryType() {
		return this.queryType;
	}

	public void setQueryType(QueryTypes type) {
		this.queryType = type;
	}

	public void setQueryType(String type) {
		this.queryType = QueryTypes.valueOf(type);
	}

	public Integer getMaxBindings() {
		return this.maxAccessMethods;
	}
	public void setMaxAccessMethods(Integer maxAccessMethods) {
		this.maxAccessMethods = maxAccessMethods;
	}

	public void setMaxAccessMethods(Number maxAccessMethods) {
		this.maxAccessMethods = maxAccessMethods.intValue();
	}

	public Double getAccessibility() {
		return this.accessibility;
	}

	public void setAccessibility(Double accessibility) {
		this.accessibility = accessibility;
	}

	public void setAccessibility(Number accessibility) {
		this.accessibility = accessibility.doubleValue();
	}

	public Double getFreePosition() {
		return this.freePosition;
	}

	public void setFreePosition(Double freePosition) {
		this.freePosition = freePosition;
	}

	public void setFreePosition(Number freePosition) {
		this.freePosition = freePosition.doubleValue();
	}

	public Double getConnectivity() {
		return this.connectivity;
	}
	public void setConnectivity(Double connectivity) {
		this.connectivity = connectivity;
	}

	public void setConnectivity(Number connectivity) {
		this.connectivity = connectivity.doubleValue();
	}

	public Double getProjectivity() {
		return this.projectivity;
	}
	public void setProjectivity(Double projectivity) {
		this.projectivity = projectivity;
	}

	public void setProjectivity(Number projectivity) {
		this.projectivity = projectivity.doubleValue();
	}

	public Integer getMaxDistanceToFree() {
		return this.maxDistanceToFree;
	}
	public void setMaxDistanceToFree(Integer maxDistanceToFree) {
		this.maxDistanceToFree = maxDistanceToFree;
	}

	public void setMaxDistanceToFree(Number maxDistanceToFree) {
		this.maxDistanceToFree = maxDistanceToFree.intValue();
	}

	public Double getJoin() {
		return this.join;
	}
	public void setJoin(Double join) {
		this.join = join;
	}

	public void setJoin(Number join) {
		this.join = join.doubleValue();
	}
	public GeneratorTypes getGeneratorType() {
		return this.generatorType;
	}
	public void setGeneratorType(GeneratorTypes generatorType) {
		this.generatorType = generatorType;
	}
	
	public void setGeneratorType(String type) {
		this.generatorType = GeneratorTypes.valueOf(type);
	}

	public CostTypes getCostType() {
		return this.costType;
	}

	public void setCostType(String costType) {
		try {
			this.costType = CostTypes.valueOf(costType);
		} catch (IllegalArgumentException e) {
			log.warn("Setting cost type to " + null);
			this.costType = null;
		}
	}

	public void setCostType(CostTypes costType) {
		this.costType = costType;
	}

	public static enum QueryTypes {
		@EnumParameterValue(description = 
				"Generates queries whose join satisfy the key/foreign key "
				+ "relationships observed in the schema")
		FROM_FKS,

		@EnumParameterValue(description ="Generates guarded queries")
		GUARDED,

		@EnumParameterValue(description ="Generates chained guarded queries")
		CHAINGUARDED,

		@EnumParameterValue(description ="Generates acyclic queries")
		ACYCLIC
	}

	public static enum GeneratorTypes {
		
		@EnumParameterValue(description ="TODO: description coming soon.")
		FIRST,

		@EnumParameterValue(description ="TODO: description coming soon.")
		SECOND,

		@EnumParameterValue(description ="TODO: description coming soon.")
		THIRD
	}
}
