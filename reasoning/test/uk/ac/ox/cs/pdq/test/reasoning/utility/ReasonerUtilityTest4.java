package uk.ac.ox.cs.pdq.test.reasoning.utility;

import junit.framework.Assert;

import org.apache.commons.collections4.CollectionUtils;
import org.junit.Ignore;
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
import uk.ac.ox.cs.pdq.reasoning.homomorphism.DBHomomorphismManager;
import uk.ac.ox.cs.pdq.reasoning.utility.ReasonerUtility;

import com.google.common.collect.Lists;

/**
 * 
 * @author Efthymia Tsamoura
 *
 */

public class ReasonerUtilityTest4  extends ReasonerUtilityTest0{

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

	//Creates the optimal plan for case_018a	
	//RE:DocumentFree BI:chembl_document_free
	//RE:AssayLimited BI:chembl_assay_limited_2
	//RE:TargetLimited BI:chembl_target_limited
	@Override
	public NormalisedPlan loadPlan() {
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

		this.access0 = new Access(this.schema.getRelation("DocumentFree"), this.schema.getRelation("DocumentFree").getAccessMethod("chembl_document_free"), 
				Lists.newArrayList(authors, doc_type, document_chembl_id, doi, first_page, issue, journal, last_page,
						pubmed_id, title, volume, year), null, null);
		ConstantEqualityPredicate p00 = new ConstantEqualityPredicate(11, new TypedConstant<String>("2007"));
		this.selection0 = new Select(new ConjunctivePredicate(Lists.newArrayList(p00)), access0.getOutput());
		Attribute attr = (Attribute) this.access0.getOutput().getHeader().get(2);
		this.projection0 = new Project(Lists.newArrayList(attr), this.selection0.getOutput());

		this.access1 = new Access(schema.getRelation("AssayLimited"), schema.getRelation("AssayLimited").getAccessMethod("chembl_assay_limited_2"), 
				Lists.newArrayList(assay_category, assay_cell_type, assay_chembl_id, organism, assay_strain, assay_subcellular_fraction, 
						assay_tax_id, assay_test_type, assay_tissue, assay_type, assay_type_description, bao_format,
						cell_chembl_id, confidence_description, confidence_score, description, document_chembl_id,
						relationship_description, relationship_type, src_assay_id, src_id, target_chembl_id), projection0.getOutput(), null);
		ConstantEqualityPredicate p10 = new ConstantEqualityPredicate(8, new TypedConstant<String>("Liver"));
		this.selection1 = new Select(new ConjunctivePredicate(Lists.newArrayList(p10)), access1.getOutput());
		this.join1 = new Join(this.selection1.getOutput(), this.selection0.getOutput());
		Attribute attr1 = (Attribute) this.join1.getOutput().getHeader().get(21);
		this.projection1 = new Project(Lists.newArrayList(attr1), this.join1.getOutput());

		this.access2 = new Access(this.schema.getRelation("TargetLimited"), this.schema.getRelation("TargetLimited").getAccessMethod("chembl_target_limited"), 
				Lists.newArrayList(organism,pref_name,species_group_flag,target_chembl_id,target_component_accession,target_component_id,
						target_component_type,target_type), projection1.getOutput(), null);
		this.join2 = new Join(this.access2.getOutput(), this.join1.getOutput());


		return new NormalisedPlan(Lists.newArrayList(this.access0, this.selection0, this.projection0, this.access1, this.selection1, 
				this.join1, this.projection1, 
				this.access2, this.join2));
	}


	@Test
	public void test1() {
		Attribute k1 = (Attribute) selection0.getOutput().getHeader().get(2);
		boolean r3 = new ReasonerUtility().isKey(this.selection1.getOutput(), Lists.newArrayList(k1), CollectionUtils.union(this.keys, CollectionUtils.union(this.schema.getDependencies(), this.planConstraints)), this.egdChaser, (DBHomomorphismManager) this.detector);
		boolean r4 = new ReasonerUtility().isKey(this.selection0.getOutput(), Lists.newArrayList(k1), CollectionUtils.union(this.keys, CollectionUtils.union(this.schema.getDependencies(), this.planConstraints)), this.egdChaser, (DBHomomorphismManager) this.detector);
		Assert.assertEquals(false, r3);
		Assert.assertEquals(true, r4);
		System.out.println(r3);
		System.out.println(r4);
	}

	@Test
	public void test2() {
		Attribute k2 = (Attribute) access2.getOutput().getHeader().get(3);
		boolean r7 = new ReasonerUtility().isKey(this.access2.getOutput(), Lists.newArrayList(k2), CollectionUtils.union(this.keys, CollectionUtils.union(this.schema.getDependencies(), this.planConstraints)), this.egdChaser, (DBHomomorphismManager) this.detector);
		boolean r8 = new ReasonerUtility().isKey(this.join1.getOutput(), Lists.newArrayList(k2), CollectionUtils.union(this.keys, CollectionUtils.union(this.schema.getDependencies(), this.planConstraints)), this.egdChaser, (DBHomomorphismManager) this.detector);
		Assert.assertEquals(true, r7);
		Assert.assertEquals(false, r8);
		System.out.println(r7);
		System.out.println(r8);
	}

	@Test
	public void test3() {
		boolean r1 = new ReasonerUtility().existsInclustionDependency(this.selection1.getOutput(), this.selection0.getOutput(), CollectionUtils.union(this.schema.getDependencies(), this.planConstraints), this.restrictedChaser, (DBHomomorphismManager) this.detector);
		Assert.assertEquals(true, r1);
		System.out.println(r1);
	}

	@Test
	public void test4() {
		boolean r2 = new ReasonerUtility().existsInclustionDependency(this.selection0.getOutput(), this.selection1.getOutput(), CollectionUtils.union(this.schema.getDependencies(), this.planConstraints), this.restrictedChaser, (DBHomomorphismManager) this.detector);
		Assert.assertEquals(false, r2);
		System.out.println(r2);
	}

	@Test
	public void test5() {
		boolean r5 = new ReasonerUtility().existsInclustionDependency(this.access2.getOutput(), this.join1.getOutput(), CollectionUtils.union(this.schema.getDependencies(), this.planConstraints), this.restrictedChaser, (DBHomomorphismManager) this.detector);
		Assert.assertEquals(false, r5);
		System.out.println(r5);
	}


	@Test
	public void test6() {
		boolean r6 = new ReasonerUtility().existsInclustionDependency(this.join1.getOutput(), this.access2.getOutput(), CollectionUtils.union(this.schema.getDependencies(), this.planConstraints), this.restrictedChaser, (DBHomomorphismManager) this.detector);
		Assert.assertEquals(false, r6);
		System.out.println(r6);
	}

}
