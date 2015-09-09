package uk.ac.ox.cs.pdq.test.cost.estimators;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Collection;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.Test;

import uk.ac.ox.cs.pdq.algebra.predicates.ConjunctivePredicate;
import uk.ac.ox.cs.pdq.algebra.predicates.ConstantEqualityPredicate;
import uk.ac.ox.cs.pdq.cost.statistics.Catalog;
import uk.ac.ox.cs.pdq.cost.statistics.SimpleCatalog;
import uk.ac.ox.cs.pdq.cost.statistics.estimators.ConstraintCardinalityEstimator;
import uk.ac.ox.cs.pdq.db.Attribute;
import uk.ac.ox.cs.pdq.db.Constraint;
import uk.ac.ox.cs.pdq.db.EGD;
import uk.ac.ox.cs.pdq.db.Relation;
import uk.ac.ox.cs.pdq.db.Schema;
import uk.ac.ox.cs.pdq.db.TGD;
import uk.ac.ox.cs.pdq.db.TypedConstant;
import uk.ac.ox.cs.pdq.fol.ConjunctiveQuery;
import uk.ac.ox.cs.pdq.fol.Query;
import uk.ac.ox.cs.pdq.fol.Term;
import uk.ac.ox.cs.pdq.fol.Variable;
import uk.ac.ox.cs.pdq.io.xml.QueryReader;
import uk.ac.ox.cs.pdq.io.xml.SchemaReader;
import uk.ac.ox.cs.pdq.plan.Access;
import uk.ac.ox.cs.pdq.plan.Command;
import uk.ac.ox.cs.pdq.plan.CommandToTGDTranslator;
import uk.ac.ox.cs.pdq.plan.Join;
import uk.ac.ox.cs.pdq.plan.NormalisedPlan;
import uk.ac.ox.cs.pdq.plan.Project;
import uk.ac.ox.cs.pdq.plan.Select;
import uk.ac.ox.cs.pdq.reasoning.HomomorphismException;
import uk.ac.ox.cs.pdq.reasoning.ReasoningParameters.HomomorphismDetectorTypes;
import uk.ac.ox.cs.pdq.reasoning.homomorphism.DBHomomorphismManager;
import uk.ac.ox.cs.pdq.reasoning.homomorphism.HomomorphismDetector;
import uk.ac.ox.cs.pdq.reasoning.homomorphism.HomomorphismManagerFactory;

import com.beust.jcommander.internal.Sets;
import com.google.common.collect.Lists;

public class TotalAccessCostEstimatorTest4 {

	
	String driver = null;
	String url = "jdbc:mysql://localhost/";
	String database = "pdq_chase";
	String username = "root";
	String password ="root";

	@Test
	public void callTestPlan4() throws FileNotFoundException, IOException {
		String PATH = "C:/Users/tsamoura/workspace2/dev4.benchmark/local/bio/queries/schema2/DAG/CONSTRAINT_CARDINALITY";
		String schemaPath = "/case_018a/schema.xml";
		String queryPath = "/case_018a/query.xml";
		String catalogPath = "C:/Users/tsamoura/workspace2/dev4.benchmark/catalog5/catalog.properties";

		try(FileInputStream sis = new FileInputStream(PATH + schemaPath);
				FileInputStream qis = new FileInputStream(PATH + queryPath)) {

			//Load query and schema
			Schema schema = new SchemaReader().read(sis);
			ConjunctiveQuery query = new QueryReader(schema).read(qis);

			if (schema == null || query == null) {
				throw new IllegalStateException("Schema and query must be provided.");
			}
			schema.updateConstants(query.getSchemaConstants());
			
			//Define keys 
			Relation activityFree = schema.getRelation("ActivityFree");
			Attribute activity_id = activityFree.getAttribute(1);
			activityFree.setKey(Lists.newArrayList(activity_id));
			Relation activityLimited = schema.getRelation("ActivityLimited");
			activity_id = activityLimited.getAttribute(1);
			activityLimited.setKey(Lists.newArrayList(activity_id));
			
			Relation AssayFree = schema.getRelation("AssayFree");
			Attribute assay_chembl_id = AssayFree.getAttribute(2);
			AssayFree.setKey(Lists.newArrayList(assay_chembl_id));
			Relation AssayLimited = schema.getRelation("AssayLimited");
			assay_chembl_id = AssayLimited.getAttribute(2);
			AssayLimited.setKey(Lists.newArrayList(assay_chembl_id));
			
//			Relation CellLineFree = schema.getRelation("CellLineFree");
//			Attribute cell_chembl_id = CellLineFree.getAttribute(0);
//			CellLineFree.setKey(Lists.newArrayList(cell_chembl_id));
//			Relation CellLineLimited = schema.getRelation("CellLineLimited");
//			cell_chembl_id = CellLineLimited.getAttribute(0);
//			CellLineLimited.setKey(Lists.newArrayList(cell_chembl_id));
			
			Relation DocumentFree = schema.getRelation("DocumentFree");
			Attribute document_chembl_id = DocumentFree.getAttribute(2);
			DocumentFree.setKey(Lists.newArrayList(document_chembl_id));
			Relation DocumentLimited = schema.getRelation("DocumentLimited");
			document_chembl_id = DocumentLimited.getAttribute(2);
			DocumentLimited.setKey(Lists.newArrayList(document_chembl_id));
			
			Relation MoleculeFree = schema.getRelation("MoleculeFree");
			Attribute molecule_chembl_id = MoleculeFree.getAttribute(14);
			MoleculeFree.setKey(Lists.newArrayList(molecule_chembl_id));
			Relation MoleculeLimited = schema.getRelation("MoleculeLimited");
			molecule_chembl_id = MoleculeLimited.getAttribute(14);
			MoleculeLimited.setKey(Lists.newArrayList(molecule_chembl_id));
			
			Relation TargetFree = schema.getRelation("TargetFree");
			Attribute target_chembl_id = TargetFree.getAttribute(3);
			TargetFree.setKey(Lists.newArrayList(target_chembl_id));
			Relation TargetLimited = schema.getRelation("TargetLimited");
			target_chembl_id = TargetLimited.getAttribute(3);
			TargetLimited.setKey(Lists.newArrayList(target_chembl_id));
			
			Relation TargetComponentFree = schema.getRelation("TargetComponentFree");
			Attribute component_id = TargetComponentFree.getAttribute(1);
			TargetComponentFree.setKey(Lists.newArrayList(component_id));
			Relation TargetComponentLimited = schema.getRelation("TargetComponentLimited");
			component_id = TargetComponentLimited.getAttribute(1);
			TargetComponentLimited.setKey(Lists.newArrayList(component_id));
			
			
			Relation PublicationFull = schema.getRelation("PublicationFull");
			Attribute id = PublicationFull.getAttribute(0);
			PublicationFull.setKey(Lists.newArrayList(id));
//			Relation PublicationShort = schema.getRelation("PublicationShort");
//			id = PublicationShort.getAttribute(0);
//			PublicationShort.setKey(Lists.newArrayList(id));
			
			Relation Citation = schema.getRelation("Citation");
			id = Citation.getAttribute(0);
			Citation.setKey(Lists.newArrayList(id));
			
			Relation Reference = schema.getRelation("Reference");
			id = Reference.getAttribute(0);
			Reference.setKey(Lists.newArrayList(id));
			
			Relation PathwayBySpecies = schema.getRelation("PathwayBySpecies");
			Attribute pathwayId = PathwayBySpecies.getAttribute(0);
			PathwayBySpecies.setKey(Lists.newArrayList(pathwayId));
			
			Relation PathwayById = schema.getRelation("PathwayById");
			pathwayId = PathwayById.getAttribute(0);
			PathwayById.setKey(Lists.newArrayList(pathwayId));
			
			Relation OrganismById = schema.getRelation("OrganismById");
			Attribute organismId = OrganismById.getAttribute(0);
			OrganismById.setKey(Lists.newArrayList(organismId));
			
			Relation OrganismFree = schema.getRelation("OrganismFree");
			organismId = OrganismFree.getAttribute(0);
			OrganismFree.setKey(Lists.newArrayList(organismId));
			
			Relation ProteinLimited = schema.getRelation("ProteinLimited");
			id = ProteinLimited.getAttribute(1);
			ProteinLimited.setKey(Lists.newArrayList(id));
			
			Relation ProteinFree = schema.getRelation("ProteinFree");
			id = ProteinFree.getAttribute(0);
			ProteinFree.setKey(Lists.newArrayList(id));

			//Load the catalog
			Catalog catalog = new SimpleCatalog(schema, catalogPath);
			
			this.testPlan4(schema, query, catalog);
		}

	}
	
	//Creates the optimal plan for case_018a	
	//RE:DocumentFree BI:chembl_document_free
	//RE:AssayLimited BI:chembl_assay_limited_2
	//RE:TargetLimited BI:chembl_target_limited
	private void testPlan4(Schema schema, Query<?> query, Catalog catalog) {
		//Define all schema and chase constants
		Term organism = new Variable("organism");
		Term pref_name = new Variable("pref_name");
		Term species_group_flag = new Variable("species_group_flag");
		Term target_chembl_id = new Variable("target_chembl_id");
		Term target_component_accession = new Variable("target_component_accession");
		Term target_component_id = new Variable("target_component_id");
		Term target_component_type = new Variable("target_component_type");
		Term target_type = new Variable("target_type");
		
		Term assay_category = new Variable("assay_category");
		Term assay_cell_type = new Variable("assay_cell_type");
		Term assay_chembl_id = new Variable("assay_chembl_id");
		Term assay_strain = new Variable("assay_strain");
		Term assay_subcellular_fraction = new Variable("assay_subcellular_fraction");
		Term assay_tax_id = new Variable("assay_tax_id");
		Term assay_test_type = new Variable("assay_test_type");
		Term assay_tissue = new Variable("assay_tissue");
		Term assay_type = new Variable("assay_type");
		Term assay_type_description = new Variable("assay_type_description");
		Term bao_format = new Variable("bao_format");
		Term cell_chembl_id = new Variable("cell_chembl_id");
		Term confidence_description = new Variable("confidence_description");
		Term confidence_score = new Variable("confidence_score");
		Term description = new Variable("description");
		Term document_chembl_id = new Variable("document_chembl_id");
		Term relationship_description = new Variable("relationship_description");
		Term relationship_type = new Variable("relationship_type");
		Term src_assay_id = new Variable("src_assay_id");
		Term src_id = new Variable("src_id");
		
		Term authors = new Variable("authors");
		Term doc_type = new Variable("doc_type");
		Term doi = new Variable("doi");
		Term first_page = new Variable("first_page");
		Term issue = new Variable("issue");
		Term journal = new Variable("journal");
		Term last_page = new Variable("last_page");
		Term pubmed_id = new Variable("pubmed_id");
		Term title = new Variable("title");
		Term volume = new Variable("volume");
		Term year = new Variable("year");

		Command access0 = new Access(schema.getRelation("DocumentFree"), schema.getRelation("DocumentFree").getAccessMethod("chembl_document_free"), 
				Lists.newArrayList(authors, doc_type, document_chembl_id, doi, first_page, issue, journal, last_page,
						pubmed_id, title, volume, year), null, null);
		ConstantEqualityPredicate p00 = new ConstantEqualityPredicate(11, new TypedConstant<String>("2007"));
		Command selection0 = new Select(new ConjunctivePredicate(Lists.newArrayList(p00)), access0.getOutput());
		Attribute attr = (Attribute) access0.getOutput().getHeader().get(2);
		Command projection0 = new Project(Lists.newArrayList(attr), selection0.getOutput());
		
		Command access1 = new Access(schema.getRelation("AssayLimited"), schema.getRelation("AssayLimited").getAccessMethod("chembl_assay_limited_2"), 
				Lists.newArrayList(assay_category, assay_cell_type, assay_chembl_id, organism, assay_strain, assay_subcellular_fraction, 
						assay_tax_id, assay_test_type, assay_tissue, assay_type, assay_type_description, bao_format,
						cell_chembl_id, confidence_description, confidence_score, description, document_chembl_id,
						relationship_description, relationship_type, src_assay_id, src_id, target_chembl_id), projection0.getOutput(), null);
		ConstantEqualityPredicate p10 = new ConstantEqualityPredicate(8, new TypedConstant<String>("Liver"));
		Command selection1 = new Select(new ConjunctivePredicate(Lists.newArrayList(p10)), access1.getOutput());
		Command join1 = new Join(selection1.getOutput(), selection0.getOutput());
		Attribute attr1 = (Attribute) join1.getOutput().getHeader().get(21);
		Command projection1 = new Project(Lists.newArrayList(attr1), join1.getOutput());
		
		Command access2 = new Access(schema.getRelation("TargetLimited"), schema.getRelation("TargetLimited").getAccessMethod("chembl_target_limited"), 
				Lists.newArrayList(organism,pref_name,species_group_flag,target_chembl_id,target_component_accession,target_component_id,
						target_component_type,target_type), projection1.getOutput(), null);
		Command join2 = new Join(access2.getOutput(), join1.getOutput());
		
		
		NormalisedPlan plan = new NormalisedPlan(Lists.newArrayList(access0, selection0, projection0, access1, selection1, join1, projection1, 
				access2, join2));
		
		
		HomomorphismDetector detector = null;
		try {
			detector = new HomomorphismManagerFactory().getInstance(
					schema, 
					query, 
					HomomorphismDetectorTypes.DATABASE,
					this.driver,
					this.url,
					this.database,
					this.username,			
					this.password);
			((DBHomomorphismManager) detector).consolidateBaseTables(plan.getTables());
		} catch (HomomorphismException | SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		ConstraintCardinalityEstimator estimator = new ConstraintCardinalityEstimator(schema, (DBHomomorphismManager) detector);
		
		Collection<EGD> keyDependencies = Lists.newArrayList();
		for(Relation relation:schema.getRelations()) {
			if(!relation.getKey().isEmpty()) {
				keyDependencies.add(EGD.getEGDs(relation, relation.getKey()));
			}
		}
		
		//Get the tgds from the input commands;
		Collection<TGD> forwardTgds = new CommandToTGDTranslator().toTGD(plan);
		Collection<TGD> backwardTgds = Sets.newLinkedHashSet();
		for(TGD tgd:forwardTgds) {
			backwardTgds.add(tgd.invert());
		}
		Collection<Constraint> constraints = CollectionUtils.union(CollectionUtils.union(schema.getDependencies(), forwardTgds), backwardTgds);
		
/*
		//Test keys and inclusion dependencies between the tables
		boolean r1 = estimator.existsInclustionDependency(selection1.getOutput(), selection0.getOutput(), constraints);
		boolean r2 = estimator.existsInclustionDependency(selection0.getOutput(), selection1.getOutput(), constraints);
		
		boolean r5 = estimator.existsInclustionDependency(access2.getOutput(), join1.getOutput(), constraints);
		boolean r6 = estimator.existsInclustionDependency(join1.getOutput(), access2.getOutput(), constraints);
		
		Pair<Integer, Boolean> estim1 = estimator.constraintDriven(projection0.getOutput(), plan, catalog);
//		Integer estim3 = estimator.commandDriven(projection0.getOutput(), plan, catalog);
		Integer estim5 = estimator.simple(projection0.getOutput(), plan, catalog);
		
		Pair<Integer, Boolean> estim2 = estimator.constraintDriven(projection1.getOutput(), plan, catalog);
//		Integer estim4 = estimator.commandDriven(projection1.getOutput(), plan, catalog);
		Integer estim6 = estimator.simple(projection1.getOutput(), plan, catalog);
*/
		
		Attribute k1 = (Attribute) selection0.getOutput().getHeader().get(2);
		boolean r3 = estimator.isKey(selection1.getOutput(), Lists.newArrayList(k1), CollectionUtils.union(keyDependencies, constraints));
		boolean r4 = estimator.isKey(selection0.getOutput(), Lists.newArrayList(k1), CollectionUtils.union(keyDependencies, constraints));
		
		Attribute k2 = (Attribute) access2.getOutput().getHeader().get(3);
		boolean r7 = estimator.isKey(access2.getOutput(), Lists.newArrayList(k2), CollectionUtils.union(keyDependencies, constraints));
		boolean r8 = estimator.isKey(join1.getOutput(), Lists.newArrayList(k2), CollectionUtils.union(keyDependencies, constraints));
		
		System.out.println();
	}

}
