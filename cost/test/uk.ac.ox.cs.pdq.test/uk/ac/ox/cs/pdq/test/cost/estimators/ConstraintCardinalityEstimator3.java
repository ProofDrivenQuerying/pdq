package uk.ac.ox.cs.pdq.test.cost.estimators;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.Before;
import org.junit.Test;

import uk.ac.ox.cs.pdq.algebra.predicates.ConjunctivePredicate;
import uk.ac.ox.cs.pdq.algebra.predicates.ConstantEqualityPredicate;
import uk.ac.ox.cs.pdq.db.Attribute;
import uk.ac.ox.cs.pdq.db.TypedConstant;
import uk.ac.ox.cs.pdq.fol.Term;
import uk.ac.ox.cs.pdq.fol.Variable;
import uk.ac.ox.cs.pdq.plan.Access;
import uk.ac.ox.cs.pdq.plan.Command;
import uk.ac.ox.cs.pdq.plan.Join;
import uk.ac.ox.cs.pdq.plan.NormalisedPlan;
import uk.ac.ox.cs.pdq.plan.Project;
import uk.ac.ox.cs.pdq.plan.Select;

import com.google.common.collect.Lists;
/**
 * 
 * @author Efthymia Tsamoura
 *
 */
public class ConstraintCardinalityEstimator3 extends ConstraintCardinalityEstimator0{

	Command access0; 
	Command access2; 
	Command access1; 
	Command access3; 
	Command access4; 
	Command selection0;
	Command selection1;
	Command selection2;
	Command selection3;
	Command selection4;
	Command join1;
	Command join2;
	Command join3;
	Command join4;
	Command projection0;
	Command projection1;
	Command projection2;
	Command projection3;

	//Creates the this.plan for the configuration of case_018a
	//MERGE(JCOMPOSE(APPLYRULE(TargetFree(){TargetFree(c2272,c2300,c2301,c2289,c2302,c2303,c2304,c2305)}),
	//APPLYRULE(AssayLimited(4){AssayLimited(c2269,c2270,c2271,c2272,c2273,c2274,c2275,c2276,Liver,c2277,c2278,
	//c2279,c2280,c2281,c2282,c2283,c2284,c2285,c2286,c2287,c2288,c2289)})),
	//APPLYRULE(DocumentFree(){DocumentFree(c2290,c2291,c2284,c2292,c2293,c2294,c2295,c2296,c2297,c2298,c2299,2007)}))
	@Before
	protected void loadPlan() {
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

		this.access0 = new Access(schema.getRelation("TargetFree"), schema.getRelation("TargetFree").getAccessMethod("chembl_target_free"), 
				Lists.newArrayList(organism,pref_name,species_group_flag,target_chembl_id,target_component_accession,target_component_id,target_component_type,target_type), null, null);
		Attribute attr = (Attribute) access0.getOutput().getHeader().get(0);
		this.projection0 = new Project(Lists.newArrayList(attr), access0.getOutput());

		this.access1 = new Access(schema.getRelation("AssayLimited"), schema.getRelation("AssayLimited").getAccessMethod("chembl_assay_limited_1"), 
				Lists.newArrayList(assay_category, assay_cell_type, assay_chembl_id, organism, assay_strain, assay_subcellular_fraction, 
						assay_tax_id, assay_test_type, assay_tissue, assay_type, assay_type_description, bao_format,
						cell_chembl_id, confidence_description, confidence_score, description, document_chembl_id,
						relationship_description, relationship_type, src_assay_id, src_id, target_chembl_id), projection0.getOutput(), null);
		ConstantEqualityPredicate p10 = new ConstantEqualityPredicate(8, new TypedConstant<String>("Liver"));
		this.selection1 = new Select(new ConjunctivePredicate(Lists.newArrayList(p10)), access1.getOutput());
		this.join1 = new Join(selection1.getOutput(), access0.getOutput());

		this.access2 = new Access(schema.getRelation("DocumentFree"), schema.getRelation("DocumentFree").getAccessMethod("chembl_document_free"), 
				Lists.newArrayList(authors, doc_type, document_chembl_id, doi, first_page, issue, journal, last_page,
						pubmed_id, title, volume, year), null, null);
		ConstantEqualityPredicate p20 = new ConstantEqualityPredicate(11, new TypedConstant<String>("2007"));
		this.selection2 = new Select(new ConjunctivePredicate(Lists.newArrayList(p20)), access1.getOutput());
		this.join2 = new Join(selection2.getOutput(), join1.getOutput());

		this.plan = new NormalisedPlan(Lists.newArrayList(this.access0, this.projection0, this.access1, this.selection1, this.join1, this.access2,
				this.selection2, this.join2));
	}

	@Test
	public void test4() {
		Pair<Integer, Boolean> estim1 = this.estimator.constraintDriven(this.projection0.getOutput(), this.plan, this.catalog);
	}

	@Test
	public void test5() {
		Integer estim3 = this.estimator.commandDriven(this.projection0.getOutput(), this.plan, this.catalog);
	}


	@Test
	public void test6() {
		Integer estim5 = this.estimator.simple(this.projection0.getOutput(),this.plan, this.catalog);
	}


}
