package uk.ac.ox.cs.pdq.benchmark;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.log4j.Logger;

import uk.ac.ox.cs.pdq.cost.CostParameters.CostTypes;

// TODO: Auto-generated Javadoc
/**
 * Specialises ConfigurationHelper for query/view generation.
 * 
 * @author Julien LEBLAY
 */
public class BenchmarkParameters extends uk.ac.ox.cs.pdq.Parameters {

	/** Logger. */
	private static Logger log = Logger.getLogger(BenchmarkParameters.class);

	/**  Properties file name. */
	public static final String DEFAULT_CONFIG_FILE_NAME = "pdq-benchmark.properties";

	/**  Properties file path. */
	public static final String DEFAULT_CONFIG_FILE_PATH = "./" + DEFAULT_CONFIG_FILE_NAME;

	/** The print query result. */
	@Parameter(description =
		"If true, prints the result of the evaluation of the input query to STDOUT")
	protected Boolean printQueryResult;

	/** The print plan result. */
	@Parameter(description = 
		"If true, prints the result of the evaluation of the output plan to STDOUT")
	protected Boolean printPlanResult;

	/** The print accessible schema. */
	@Parameter(description =
		"If true, prints the accessible version of the input schema to STDOUT")
	protected Boolean printAccessibleSchema;

	/** The search only. */
	@Parameter(description=
		"If true, only run the search "
		+ "(e.g. does not attempt to evluation the resulting plan")
	protected Boolean searchOnly;

	/** The arity. */
	@Parameter(description="Maximum arity of generated relations")
	private Integer arity;

	/** The number of relations. */
	@Parameter(description="Number of generated relations")
	private Integer numberOfRelations;

	/** The max access methods. */
	@Parameter(description="Maximum number of access method in generated relations")
	private Integer maxAccessMethods;

	/** The accessibility. */
	@Parameter(description="Ratio of generated relations that should be accessible")
	private Double accessibility;

	/** The free position. */
	@Parameter(description="Ratio of distinguished variables in generated queries")
	private Double freePosition;

	/** The connectivity. */
	@Parameter(description="Connectivity ratio in generated queries")
	private Double connectivity;

	/** The projectivity. */
	@Parameter(description="Projectivity ratio in generated queries")
	private Double projectivity;

	/** The max distance to free. */
	@Parameter(description="Maximun distance (number of rule firing) from the query to a free access relation")
	private Integer maxDistanceToFree;
	
	/** The generator type. */
	@Parameter(description="Type of generate to use")
	private GeneratorTypes generatorType;

	/** The join. */
	@Parameter(description="JoinTest ratio in generated queries")
	private Double join;

	/** The query type. */
	@Parameter(description="Type of queries to generate")
	private QueryTypes queryType;

	/** The query conjuncts. */
	@Parameter(description="Number of conjuncts in the generated queries")
	private Integer queryConjuncts;

	/** The constraint conjuncts. */
	@Parameter(description="Number of conjuncts in the generated constraints")
	private Integer constraintConjuncts;

	/** The numder of views. */
	@Parameter(description="Number of views to generate")
	private Integer numderOfViews;

	/** The number of constraints. */
	@Parameter(description="Number of constraints to generate")
	private Integer numberOfConstraints;

	/** The number of access methods. */
	@Parameter(description="Number of access methods to generate")
	private Integer numberOfAccessMethods;

	/** The relation size. */
	@Parameter(description="Maximum cardinality of the generated relations")
	private Integer relationSize;

	/** The max cost. */
	@Parameter(description="Maximun (simple) cost of access to generated relations")
	private Double maxCost;

	/** The mean cost. */
	@Parameter(description="Mean (simple) cost of access to generated relations")
	private Double meanCost;

	/** The input position. */
	@Parameter(description="Ratio of attribute positions to use as inputs in generated access methods")
	private Double inputPosition;

	/** The free access. */
	@Parameter(description="Ratio of generated relations with a free access")
	private Double freeAccess;

	/** The repeated relations. */
	@Parameter(description="If true, generated queries are allowed to reference relations multiple times")
	private Boolean repeatedRelations;

	/** The input schema. */
	@Parameter(description="Path to the input schema file")
	private String inputSchema;

	/** The free variable. */
	@Parameter(description="Ratio of distinguished variables in generated queries",
			defaultValue = "0.0")
	private Double freeVariable = 0.0;

	/** The validation db. */
	@Parameter(description="Maximun (simple) cost of access to generated relations")
	private String validationDB;

	/** The cost type. */
	@Parameter(description="Type of cost estimation to use.")
	protected CostTypes costType;
	
	/**
	 * Gets the version.
	 *
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

	/**
	 * Instantiates a new benchmark parameters.
	 */
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

	/**
	 * Gets the prints the query result.
	 *
	 * @return the prints the query result
	 */
	public Boolean getPrintQueryResult() {
		return this.printQueryResult;
	}

	/**
	 * Sets the prints the query result.
	 *
	 * @param print the new prints the query result
	 */
	public void setPrintQueryResult(Boolean print) {
		this.printQueryResult = print;
	}

	/**
	 * Gets the prints the plan result.
	 *
	 * @return the prints the plan result
	 */
	public Boolean getPrintPlanResult() {
		return this.printPlanResult;
	}

	/**
	 * Sets the prints the plan result.
	 *
	 * @param print the new prints the plan result
	 */
	public void setPrintPlanResult(Boolean print) {
		this.printPlanResult = print;
	}

	/**
	 * Gets the prints the accessible schema.
	 *
	 * @return the prints the accessible schema
	 */
	public Boolean getPrintAccessibleSchema() {
		return this.printAccessibleSchema;
	}

	/**
	 * Sets the prints the accessible schema.
	 *
	 * @param print the new prints the accessible schema
	 */
	public void setPrintAccessibleSchema(Boolean print) {
		this.printAccessibleSchema = print;
	}

	/**
	 * Gets the search only.
	 *
	 * @return the search only
	 */
	public Boolean getSearchOnly() {
		return this.searchOnly;
	}

	/**
	 * Sets the search only.
	 *
	 * @param searchOnly the new search only
	 */
	public void setSearchOnly(Boolean searchOnly) {
		this.searchOnly = searchOnly;
	}

	/**
	 * Gets the arity.
	 *
	 * @return the arity
	 */
	public Integer getArity() {
		return this.arity;
	}

	/**
	 * Gets the number of access methods.
	 *
	 * @return the number of access methods
	 */
	public Integer getNumberOfAccessMethods() {
		return this.numberOfAccessMethods;
	}

	/**
	 * Gets the number of constraints.
	 *
	 * @return the number of constraints
	 */
	public Integer getNumberOfConstraints() {
		return this.numberOfConstraints;
	}

	/**
	 * Gets the dependency conjuncts.
	 *
	 * @return the dependency conjuncts
	 */
	public Integer getDependencyConjuncts() {
		return this.constraintConjuncts;
	}


	/**
	 * Gets the free access.
	 *
	 * @return the free access
	 */
	public Double getFreeAccess() {
		return this.freeAccess;
	}

	/**
	 * Gets the free variable.
	 *
	 * @return the free variable
	 */
	public Double getFreeVariable() {
		return this.freeVariable;
	}

	/**
	 * Gets the input position.
	 *
	 * @return the input position
	 */
	public Double getInputPosition() {
		return this.inputPosition;
	}

	/**
	 * Gets the input schema.
	 *
	 * @return the input schema
	 */
	public String getInputSchema() {
		return this.inputSchema;
	}

	/**
	 * Gets the max cost.
	 *
	 * @return the max cost
	 */
	public Double getMaxCost() {
		return this.maxCost;
	}

	/**
	 * Gets the mean cost.
	 *
	 * @return the mean cost
	 */
	public Double getMeanCost() {
		return this.meanCost;
	}

	/**
	 * Gets the query conjuncts.
	 *
	 * @return the query conjuncts
	 */
	public Integer getQueryConjuncts() {
		return this.queryConjuncts;
	}

	/**
	 * Gets the number of relations.
	 *
	 * @return the number of relations
	 */
	public Integer getNumberOfRelations() {
		return this.numberOfRelations;
	}

	/**
	 * Gets the relation size.
	 *
	 * @return the relation size
	 */
	public Integer getRelationSize() {
		return this.relationSize;
	}

	/**
	 * Gets the number of views.
	 *
	 * @return the number of views
	 */
	public Integer getNumberOfViews() {
		return this.numderOfViews;
	}

	/**
	 * Sets the arity.
	 *
	 * @param arity the new arity
	 */
	public void setArity(Number arity) {
		this.arity = arity.intValue();
	}

	/**
	 * Sets the number of access methods.
	 *
	 * @param bindings the new number of access methods
	 */
	public void setNumberOfAccessMethods(Number bindings) {
		this.numberOfAccessMethods = bindings.intValue();
	}

	/**
	 * Sets the number of constraints.
	 *
	 * @param dependencies the new number of constraints
	 */
	public void setNumberOfConstraints(Number dependencies) {
		this.numberOfConstraints = dependencies.intValue();
	}

	/**
	 * Sets the constraint conjuncts.
	 *
	 * @param dependencyConjuncts the new constraint conjuncts
	 */
	public void setConstraintConjuncts(Number dependencyConjuncts) {
		this.constraintConjuncts = dependencyConjuncts.intValue();
	}


	/**
	 * Sets the free access.
	 *
	 * @param freeAccess the new free access
	 */
	public void setFreeAccess(Number freeAccess) {
		this.freeAccess = freeAccess.doubleValue();
	}

	/**
	 * Sets the free variable.
	 *
	 * @param freeVarRatio the new free variable
	 */
	public void setFreeVariable(Number freeVarRatio) {
		this.freeVariable = freeVarRatio.doubleValue();
	}

	/**
	 * Sets the input position.
	 *
	 * @param inputPosition the new input position
	 */
	public void setInputPosition(Number inputPosition) {
		this.inputPosition = inputPosition.doubleValue();
	}

	/**
	 * Sets the input schema.
	 *
	 * @param inputSchema the new input schema
	 */
	public void setInputSchema(String inputSchema) {
		if (inputSchema != null && inputSchema.trim().isEmpty()) {
			this.inputSchema = null;
			return;
		}
		this.inputSchema = inputSchema;
	}

	/**
	 * Sets the max cost.
	 *
	 * @param maxCost the new max cost
	 */
	public void setMaxCost(Number maxCost) {
		this.maxCost = maxCost.doubleValue();
	}

	/**
	 * Sets the mean cost.
	 *
	 * @param meanCost the new mean cost
	 */
	public void setMeanCost(Number meanCost) {
		this.meanCost = meanCost.doubleValue();
	}

	/**
	 * Sets the query conjuncts.
	 *
	 * @param queryConjuncts the new query conjuncts
	 */
	public void setQueryConjuncts(Number queryConjuncts) {
		this.queryConjuncts = queryConjuncts.intValue();
	}

	/**
	 * Sets the number of relations.
	 *
	 * @param relations the new number of relations
	 */
	public void setNumberOfRelations(Number relations) {
		this.numberOfRelations = relations.intValue();
	}

	/**
	 * Sets the relation size.
	 *
	 * @param relationSize the new relation size
	 */
	public void setRelationSize(Number relationSize) {
		this.relationSize = relationSize.intValue();
	}

	/**
	 * Sets the number of views.
	 *
	 * @param views the new number of views
	 */
	public void setNumberOfViews(Number views) {
		this.numderOfViews = views.intValue();
	}

	/**
	 * Gets the repeated relations.
	 *
	 * @return the repeated relations
	 */
	public Boolean getRepeatedRelations() {
		return this.repeatedRelations;
	}

	/**
	 * Sets the repeated relations.
	 *
	 * @param allowRepeatedRelations the new repeated relations
	 */
	public void setRepeatedRelations(Boolean allowRepeatedRelations) {
		this.repeatedRelations = allowRepeatedRelations;
	}

	/**
	 * Gets the query type.
	 *
	 * @return the query type
	 */
	public QueryTypes getQueryType() {
		return this.queryType;
	}

	/**
	 * Sets the query type.
	 *
	 * @param type the new query type
	 */
	public void setQueryType(QueryTypes type) {
		this.queryType = type;
	}

	/**
	 * Sets the query type.
	 *
	 * @param type the new query type
	 */
	public void setQueryType(String type) {
		this.queryType = QueryTypes.valueOf(type);
	}

	/**
	 * Gets the max bindings.
	 *
	 * @return the max bindings
	 */
	public Integer getMaxBindings() {
		return this.maxAccessMethods;
	}
	
	/**
	 * Sets the max access methods.
	 *
	 * @param maxAccessMethods the new max access methods
	 */
	public void setMaxAccessMethods(Integer maxAccessMethods) {
		this.maxAccessMethods = maxAccessMethods;
	}

	/**
	 * Sets the max access methods.
	 *
	 * @param maxAccessMethods the new max access methods
	 */
	public void setMaxAccessMethods(Number maxAccessMethods) {
		this.maxAccessMethods = maxAccessMethods.intValue();
	}

	/**
	 * Gets the accessibility.
	 *
	 * @return the accessibility
	 */
	public Double getAccessibility() {
		return this.accessibility;
	}

	/**
	 * Sets the accessibility.
	 *
	 * @param accessibility the new accessibility
	 */
	public void setAccessibility(Double accessibility) {
		this.accessibility = accessibility;
	}

	/**
	 * Sets the accessibility.
	 *
	 * @param accessibility the new accessibility
	 */
	public void setAccessibility(Number accessibility) {
		this.accessibility = accessibility.doubleValue();
	}

	/**
	 * Gets the free position.
	 *
	 * @return the free position
	 */
	public Double getFreePosition() {
		return this.freePosition;
	}

	/**
	 * Sets the free position.
	 *
	 * @param freePosition the new free position
	 */
	public void setFreePosition(Double freePosition) {
		this.freePosition = freePosition;
	}

	/**
	 * Sets the free position.
	 *
	 * @param freePosition the new free position
	 */
	public void setFreePosition(Number freePosition) {
		this.freePosition = freePosition.doubleValue();
	}

	/**
	 * Gets the connectivity.
	 *
	 * @return the connectivity
	 */
	public Double getConnectivity() {
		return this.connectivity;
	}
	
	/**
	 * Sets the connectivity.
	 *
	 * @param connectivity the new connectivity
	 */
	public void setConnectivity(Double connectivity) {
		this.connectivity = connectivity;
	}

	/**
	 * Sets the connectivity.
	 *
	 * @param connectivity the new connectivity
	 */
	public void setConnectivity(Number connectivity) {
		this.connectivity = connectivity.doubleValue();
	}

	/**
	 * Gets the projectivity.
	 *
	 * @return the projectivity
	 */
	public Double getProjectivity() {
		return this.projectivity;
	}
	
	/**
	 * Sets the projectivity.
	 *
	 * @param projectivity the new projectivity
	 */
	public void setProjectivity(Double projectivity) {
		this.projectivity = projectivity;
	}

	/**
	 * Sets the projectivity.
	 *
	 * @param projectivity the new projectivity
	 */
	public void setProjectivity(Number projectivity) {
		this.projectivity = projectivity.doubleValue();
	}

	/**
	 * Gets the max distance to free.
	 *
	 * @return the max distance to free
	 */
	public Integer getMaxDistanceToFree() {
		return this.maxDistanceToFree;
	}
	
	/**
	 * Sets the max distance to free.
	 *
	 * @param maxDistanceToFree the new max distance to free
	 */
	public void setMaxDistanceToFree(Integer maxDistanceToFree) {
		this.maxDistanceToFree = maxDistanceToFree;
	}

	/**
	 * Sets the max distance to free.
	 *
	 * @param maxDistanceToFree the new max distance to free
	 */
	public void setMaxDistanceToFree(Number maxDistanceToFree) {
		this.maxDistanceToFree = maxDistanceToFree.intValue();
	}

	/**
	 * Gets the join.
	 *
	 * @return the join
	 */
	public Double getJoin() {
		return this.join;
	}
	
	/**
	 * Sets the join.
	 *
	 * @param join the new join
	 */
	public void setJoin(Double join) {
		this.join = join;
	}

	/**
	 * Sets the join.
	 *
	 * @param join the new join
	 */
	public void setJoin(Number join) {
		this.join = join.doubleValue();
	}
	
	/**
	 * Gets the generator type.
	 *
	 * @return the generator type
	 */
	public GeneratorTypes getGeneratorType() {
		return this.generatorType;
	}
	
	/**
	 * Sets the generator type.
	 *
	 * @param generatorType the new generator type
	 */
	public void setGeneratorType(GeneratorTypes generatorType) {
		this.generatorType = generatorType;
	}
	
	/**
	 * Sets the generator type.
	 *
	 * @param type the new generator type
	 */
	public void setGeneratorType(String type) {
		this.generatorType = GeneratorTypes.valueOf(type);
	}

	/**
	 * Gets the cost type.
	 *
	 * @return the cost type
	 */
	public CostTypes getCostType() {
		return this.costType;
	}

	/**
	 * Sets the cost type.
	 *
	 * @param costType the new cost type
	 */
	public void setCostType(String costType) {
		try {
			this.costType = CostTypes.valueOf(costType);
		} catch (IllegalArgumentException e) {
			log.warn("Setting cost type to " + null);
			this.costType = null;
		}
	}

	/**
	 * Sets the cost type.
	 *
	 * @param costType the new cost type
	 */
	public void setCostType(CostTypes costType) {
		this.costType = costType;
	}

	/**
	 * The Enum QueryTypes.
	 */
	public static enum QueryTypes {
		
		/** The from fks. */
		@EnumParameterValue(description = 
				"Generates queries whose join satisfy the key/foreign key "
				+ "relationships observed in the schema")
		FROM_FKS,

		/** The guarded. */
		@EnumParameterValue(description ="Generates guarded queries")
		GUARDED,

		/** The chainguarded. */
		@EnumParameterValue(description ="Generates chained guarded queries")
		CHAINGUARDED,

		/** The acyclic. */
		@EnumParameterValue(description ="Generates acyclic queries")
		ACYCLIC
	}

	/**
	 * The Enum GeneratorTypes.
	 */
	public static enum GeneratorTypes {
		
		/** The first. */
		@EnumParameterValue(description ="TODO: description coming soon.")
		FIRST,

		/** The second. */
		@EnumParameterValue(description ="TODO: description coming soon.")
		SECOND,

		/** The third. */
		@EnumParameterValue(description ="TODO: description coming soon.")
		THIRD
	}
}
