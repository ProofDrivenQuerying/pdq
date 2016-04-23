package cy.ac.cut.cs.workloadgen;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

import uk.ac.ox.cs.pdq.cost.statistics.Histogram;
import uk.ac.ox.cs.pdq.cost.statistics.SQLServerHistogram;
import uk.ac.ox.cs.pdq.cost.statistics.SQLServerHistogramWriter;
import uk.ac.ox.cs.pdq.io.xml.DependencyWriter;
import uk.ac.ox.cs.pdq.io.xml.QueryWriter;

import com.beust.jcommander.DynamicParameter;
import com.beust.jcommander.IParameterValidator;
import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import com.beust.jcommander.internal.Lists;
import com.google.common.collect.Maps;

import cy.ac.cut.cs.workloadgen.database.DatabaseManager;
import cy.ac.cut.cs.workloadgen.database.SQLServerManager;
import cy.ac.cut.cs.workloadgen.io.xml.ViewWriter;
import cy.ac.cut.cs.workloadgen.policies.querygen.QueryGenPolicy;
import cy.ac.cut.cs.workloadgen.policies.viewgen.ViewGenPolicy;
import cy.ac.cut.cs.workloadgen.query.Query;
import cy.ac.cut.cs.workloadgen.query.View;
import cy.ac.cut.cs.workloadgen.schema.Attribute;
import cy.ac.cut.cs.workloadgen.schema.Schema;
import cy.ac.cut.cs.workloadgen.translator.QueryTranslator;
import cy.ac.cut.cs.workloadgen.xml.PoliciesXmlParser;
import cy.ac.cut.cs.workloadgen.xml.PoliciesXmlParser.PoliciesXmlParsingException;
import cy.ac.cut.cs.workloadgen.xml.SchemaXmlParser;
import cy.ac.cut.cs.workloadgen.xml.SchemaXmlParser.SchemaXmlParsingException;
import cy.ac.cut.cs.workloadgen.xml.StatsXmlParser;
import cy.ac.cut.cs.workloadgen.xml.StatsXmlParser.StatsXmlParsingException;
/**
 * Bootstrapping class for starting the reasoner. 
 * 
 * @author Efthymia Tsamoura
 */
public class Workloadgen {

	/** Logger. */
	private static Logger log = Logger.getLogger(Workloadgen.class); 

	private static final String PROGRAM_NAME = "pdq-workloadgen-<version>.jar";

	@Parameter(names = { "-h", "--help" }, help = true, description = "Displays this help message.")
	private boolean help;
	public boolean isHelp() {
		return this.help;
	}

	@Parameter(names = { "-s", "--schema" }, required = true,
			validateWith=FileValidator.class,
			description ="Path to the input schema definition file.")
	private String schemaPath;
	public String getSchemaPath() {
		return this.schemaPath;
	}
	
	@Parameter(names = { "-t", "--statistics" }, required = true,
			validateWith=FileValidator.class,
			description ="Path to the input schema definition file.")
	private String statsPath;
	public String getStatisticsPath() {
		return this.statsPath;
	}

	@Parameter(names = { "-q", "--query-policies" }, required = true,
			validateWith=FileValidator.class,
			description ="Path to the query generation policies file.")
	private String queryPoliciesPath;
	public String getQueryPoliciesPath() {
		return this.queryPoliciesPath;
	}

	@Parameter(names = { "-v", "--view-policies" }, required = true,
			validateWith=FileValidator.class,
			description ="Path to the view generation policies file.")
	private String viewPoliciesPath;
	public String getViewPoliciesPath() {
		return this.viewPoliciesPath;
	}

	@Parameter(names = { "-o", "--output-queries" }, required = false,
			//validateWith=FileValidator.class,
			description ="Path to store generated queries.")
	private String outputPath = "queries/";
	public String getOutputPath() {
		return this.outputPath;
	}

	@DynamicParameter(names = "-D", description = "Dynamic parameters. Override values defined in the configuration files.")
	protected Map<String, String> dynamicParams = new LinkedHashMap<>();

	/**
	 * Initialize the Bootstrap by reading command line parameters, and running
	 * the planner on them.
	 * @param args String[]
	 */
	private Workloadgen(String... args) {
		JCommander jc = new JCommander(this);
		jc.setProgramName(PROGRAM_NAME);
		try {
			jc.parse(args);
		} catch (ParameterException e) {
			System.err.println(e.getMessage());
			jc.usage();
			return;
		}
		if (this.isHelp()) {
			jc.usage();
			return;
		}
		run();
	}

	/**
	 * Runs the planner from the input parameters, schema and query.
	 */
	public void run() {				

		File schemaFile = new File(this.getSchemaPath());
		File statsFile = new File(this.getStatisticsPath());
		File qpoliciesFile = new File(this.getQueryPoliciesPath());
		File vpoliciesFile = new File(this.getViewPoliciesPath());

		String schemaName = "dbo";
		String username ="tpch_user";
		String password ="tpch";
		String database = "TPCH2";
		String driver = "com.microsoft.sqlserver.jdbc.SQLServerDriver";
		String url ="jdbc:sqlserver://TSAMOURALAPTOP";

		//Set up the database connection
		DatabaseManager manager = null;
		try {
			manager = new SQLServerManager(driver, url, database, username, password);
		} catch (SQLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		// Import the schema
		Schema schema;
		try {
			schema = SchemaXmlParser.ImportSchema(schemaFile);
		} catch (SchemaXmlParsingException e) {
			System.err.println("Failed to parse the schema XML file");
			e.printStackTrace();
			return;
		}

		// Import the stats
		try {
			StatsXmlParser.ImportAttrStats(statsFile, schema);
		} catch (StatsXmlParsingException e) {
			System.err.println("Failed to parse the stats XML file");
			e.printStackTrace();
			return;
		}

		// Import the query generation policy
		QueryGenPolicy queryGen;
		try {
			queryGen = PoliciesXmlParser.ImportQueryGenPolicy(qpoliciesFile);
		} catch (PoliciesXmlParsingException e) {
			System.err.println("Failed to parse the policies XML file");
			e.printStackTrace();
			return;
		}

		// Import the view generation policy
		ViewGenPolicy viewGen;
		try {
			viewGen = PoliciesXmlParser.ImportViewGenPolicy(vpoliciesFile);
		} catch (PoliciesXmlParsingException e) {
			System.err.println("Failed to parse the policies XML file");
			e.printStackTrace();
			return;
		}

		// Generate and output the queries
		List<Query> queries = queryGen.generateQueries(schema);
		for (Query query : queries) {
			log.info(query.toString());
			try {
				//Prepare the output top level folder
				File theDir = new File(this.outputPath + "case_" + String.format("%03d", query.getId()));

				// if the directory does not exist, create it
				if (!theDir.exists()) {
					boolean result = false;

					try{
						theDir.mkdir();
						result = true;
					} 
					catch(SecurityException se){
						//handle it
					}        
					if(result) {    
						log.info("DIR created");  
					}
				}


				//Get the query size
				Integer size = manager.getSize(query);
				log.info("|"+ query.getName() + "|=" + size);
				if(size > 0) {
					//Prepare the query output files
					PrintStream outxml = new PrintStream(this.outputPath + "case_" + String.format("%03d", query.getId()) + "/query.xml");
					PrintStream outtxt = new PrintStream(this.outputPath + "case_" + String.format("%03d", query.getId()) + "/query.txt");
					//Write the query size 
					outtxt.println("--Actual size: " + size);
					//Write the generated query in a txt file
					outtxt.println(query.toString());
					outtxt.close();
					//Write the generated query in CNF
					uk.ac.ox.cs.pdq.fol.ConjunctiveQuery cq = QueryTranslator.translate(query);
					new QueryWriter().write(outxml, cq);
					outxml.close();
					
					
					log.info("Views generated for query " + query.toString());
					//Prepare the view output files
					PrintStream outxml2 = new PrintStream(this.outputPath + "case_" + String.format("%03d", query.getId()) + "/views.xml");
					PrintStream outtxt2 = new PrintStream(this.outputPath + "case_" + String.format("%03d", query.getId()) + "/views.txt");
					PrintStream outproperties = new PrintStream(this.outputPath + "case_" + String.format("%03d", query.getId()) + "/catalog.properties");

					//Generate views for the given query
					List<View> qviews = viewGen.generateViews(schema, query);

					outxml2.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
					for (View view : qviews) {
						log.info(view.toString());
						
						//Create this view in the database to create statistics
						manager.createView(schemaName, view);
						
						int viewSize = manager.getSize(view);
						//Write the query size 
						log.info("|" + view.getName() + "|=" + viewSize);
						outtxt2.println("--Size: " + viewSize);
						//Write the generated view in a txt file
						outtxt2.println(view.toString());
						//Write the generated view in CNF
						uk.ac.ox.cs.pdq.db.LinearGuarded cq2 = QueryTranslator.translate(view);
						new DependencyWriter().writeDependency(outxml2, cq2);
						new DependencyWriter().writeDependency(outxml2, cq2.invert());
						
						//Write the generated view definition
						new ViewWriter().writeRelation(outxml2, view);

						//Write the single-value view statistics in catalog.properties
						outproperties.println("RE:"+view.getName() + "\t\t\t\t" + "CA:"+ viewSize);
						//Find the single-value view statistics
						for(Attribute attribute:view.getSelectClause()) {
							View v = new View(view.getId(), Lists.newArrayList(attribute), view.getFromClause(), view.getWhereClause(), view.getForeignKeys(), view.getJoinableKeys());
							int cardinality = manager.getSize(v);
							outproperties.println("RE:"+view.getName() + "\t\t\t\t" + "AT:"+ attribute.getName() + "\t\t\t\t" + "CC:"+cardinality);
							log.info("|" + attribute.getName() + "|" + "=" + cardinality);
						}
						//Get all view statistics 
						
						//Create statistics for each attribute of the view
						Map<Attribute, String> statistics = Maps.newHashMap();
						for(Attribute attribute:view.getSelectClause()) {
							String statistic = manager.createStatistics(schemaName, view, Lists.newArrayList(attribute));
							statistics.put(attribute, statistic);
						}

						//Write the view histograms 
						for(Entry<Attribute, String> entry:statistics.entrySet()) {
							Attribute attribute = entry.getKey();
							String statistic = entry.getValue();
							Histogram histogram = manager.getHistogram(schemaName, view, attribute, statistic);
							String histPath = this.outputPath + "case_" + String.format("%03d", query.getId()) + "/" + "HIST_" + view.getName() + "_" + attribute.getName();
							PrintStream histFile = new PrintStream(histPath);
							if(histogram instanceof SQLServerHistogram) {
								SQLServerHistogramWriter.write((SQLServerHistogram)histogram, histFile);
								outproperties.println("RE:"+view.getName() + "\t\t\t\t" + "AT:"+ attribute.getName() + "\t\t\t\t" + "SQLH:" + histPath);
								log.info("Histogram on " + attribute.getName() + ": " + histPath);
							}
						}
						outproperties.println("//###########################################################");
						//Drop the view from the database
						manager.dropView(view);
					}
					outxml2.close();
					outtxt2.close();
					outproperties.close();
				}
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	/**
	 * Filters out files that do not exist or are directories.
	 * @author Julien LEBLAY
	 */
	public static class FileValidator implements IParameterValidator {
		@Override
		public void validate(String name, String value) throws ParameterException {
			try {
				File f = new File(value);
				if (!f.exists() || f.isDirectory()) {
					throw new ParameterException(name + " must be a valid configuration file.");
				}
			} catch (Exception e) {
				throw new ParameterException(name + " must be a valid configuration file.");
			}
		}
	}

	/**
	 * Instantiates the bootstrap
	 * @param args String[]
	 */
	public static void main(String... args) {
		new Workloadgen(args);
	}
}
