package uk.ac.ox.cs.pdq.test.reasoning.utility;

import org.apache.commons.collections4.CollectionUtils;
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
public class ReasonerUtilityTest2 extends ReasonerUtilityTest0 {

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
	
	//Creates the this.plan for the configuration 
	//PCOMPOSE(PCOMPOSE(JCOMPOSE(JCOMPOSE(APPLYRULE(TargetComponentFree(){TargetComponentFree(c446,c447,PROTEIN,k488,c443,k489,k490,k491)}),
	//APPLYRULE(ProteinLimited(4){ProteinLimited(k492,c446,k493,c443)})),
	//APPLYRULE(TargetLimited(1){TargetLimited(c443,c444,c445,c433,c446,c447,c448,SINGLE PROTEIN)})),
	//APPLYRULE(AssayLimited(22){AssayLimited(c412,c413,c414,c415,c416,c417,c418,c419,c420,c421,c422,c423,c424,c425,c426,c427,c428,c429,c430,c431,c432,c433)})),
	//APPLYRULE(DocumentLimited(3){DocumentLimited(c434,PUBLICATION,c428,c435,c436,c437,c438,c439,c440,c441,c442,2015)}))
	@Override
	public NormalisedPlan loadPlan() {
		//Define all schema and chase constants
		Term _accession = new Variable("accession");
		Term _component_id = new Variable("component_id");
		Term protein = new Variable("cprotein");
		Term _description0 = new Variable("_description0");
		Term _organism = new Variable("organism");
		Term _protein_classification_id = new Variable("protein_classification_id");
		Term _sequence = new Variable("sequence");
		Term _tax_id = new Variable("tax_id");
		Term _input_id = new Variable("input_id");
		Term _entry_name = new Variable("entry_name");
		Term _pref_name = new Variable("pref_name");
		Term _species_group_flag = new Variable("species_group_flag");
		Term _target_chembl_id = new Variable("target_chembl_id");
		Term _target_component_type = new Variable("target_component_type");
		Term singleprotein = new Variable("csingleprotein");
		Term _assay_category = new Variable("assay_category");
		Term _assay_cell_type = new Variable("assay_cell_type");
		Term _assay_chembl_id = new Variable("assay_chembl_id");
		Term _assay_organism = new Variable("assay_organism");
		Term _assay_strain = new Variable("assay_strain");
		Term _assay_subcellular_fraction = new Variable("assay_subcellular_fraction");
		Term _assay_tax_id = new Variable("assay_tax_id");
		Term _assay_test_type = new Variable("assay_test_type");
		Term _assay_tissue = new Variable("assay_tissue");
		Term _assay_type = new Variable("assay_type");
		Term _assay_type_description = new Variable("assay_type_description");
		Term _bao_format = new Variable("bao_format");
		Term _cell_chembl_id = new Variable("cell_chembl_id");
		Term _confidence_description = new Variable("confidence_description");
		Term _confidence_score = new Variable("confidence_score");
		Term _description = new Variable("description");
		Term _document_chembl_id = new Variable("document_chembl_id");
		Term _relationship_description = new Variable("relationship_description");
		Term _relationship_type = new Variable("relationship_type");
		Term _src_assay_id = new Variable("src_assay_id");
		Term _src_id = new Variable("src_id");
		
		Term _authors = new Variable("authors");
		Term pub = new Variable("cpub");
		Term _doi = new Variable("doi");
		Term _first_page = new Variable("first_page");
		Term _issue = new Variable("issue");
		Term _journal = new Variable("journal");
		Term _last_page = new Variable("last_page");
		Term _pubmed_id = new Variable("pubmed_id");
		Term _title = new Variable("title");
		Term _volume = new Variable("volume");
		Term year = new Variable("2015");
		
		
		//Define the this.plan
		this.access0 = new Access(schema.getRelation("TargetComponentFree"), this.schema.getRelation("TargetComponentFree").getAccessMethod("chembl_target_component_free"), 
				Lists.newArrayList(_accession,_component_id,protein,_description0,_organism,_protein_classification_id,_sequence,_tax_id), null, null);
		ConstantEqualityPredicate p00 = new ConstantEqualityPredicate(2, new TypedConstant<String>("PROTEIN"));
		this.selection0 = new Select(new ConjunctivePredicate(Lists.newArrayList(p00)), access0.getOutput());
		Attribute attr = (Attribute) selection0.getOutput().getHeader().get(3);
		this.projection0 = new Project(Lists.newArrayList(attr), selection0.getOutput());
				
		this.access1 = new Access(schema.getRelation("ProteinLimited"), this.schema.getRelation("ProteinLimited").getAccessMethod("uniprot_protein_2"), 
				Lists.newArrayList(_input_id,_accession,_entry_name,_organism), projection0.getOutput(), null);
		this.join1 = new Join(access1.getOutput(), selection0.getOutput());
		Attribute attr1 = (Attribute) join1.getOutput().getHeader().get(3);
		this.projection1 = new Project(Lists.newArrayList(attr1), join1.getOutput());
		
		this.access2 = new Access(schema.getRelation("TargetLimited"), this.schema.getRelation("TargetLimited").getAccessMethod("chembl_target_limited_1"), 
				Lists.newArrayList(_organism,_pref_name, _species_group_flag,_target_chembl_id,_accession,_component_id,_target_component_type,singleprotein), projection1.getOutput(), null);
		ConstantEqualityPredicate p20 = new ConstantEqualityPredicate(7, new TypedConstant<String>("SINGLE PROTEIN"));
		this.selection2 = new Select(new ConjunctivePredicate(Lists.newArrayList(p20)), access2.getOutput());
		this.join2 = new Join(selection2.getOutput(), join1.getOutput());
		Attribute attr2 = (Attribute) join2.getOutput().getHeader().get(3);
		this.projection2 = new Project(Lists.newArrayList(attr2), join2.getOutput());
		
		this.access3 = new Access(schema.getRelation("AssayLimited"), this.schema.getRelation("AssayLimited").getAccessMethod("chembl_assay_limited_3"), 
				Lists.newArrayList(_assay_category,_assay_cell_type,_assay_chembl_id,_assay_organism,_assay_strain,_assay_subcellular_fraction,_assay_tax_id,_assay_test_type,_assay_tissue,_assay_type,_assay_type_description,_bao_format,_cell_chembl_id,_confidence_description,_confidence_score,_description,_document_chembl_id,_relationship_description,_relationship_type,_src_assay_id,_src_id,_target_chembl_id), 
				projection2.getOutput(), null);
		this.join3 = new Join(access3.getOutput(), join2.getOutput());
		Attribute attr3 = (Attribute) join3.getOutput().getHeader().get(16);
		this.projection3 = new Project(Lists.newArrayList(attr3), join3.getOutput());
		
		this.access4 = new Access(schema.getRelation("DocumentLimited"), this.schema.getRelation("DocumentLimited").getAccessMethod("chembl_document_limited"), 
				Lists.newArrayList(_authors,pub,_document_chembl_id,_doi,_first_page,_issue,_journal,_last_page,_pubmed_id,_title,_volume,year), projection3.getOutput(), null);
		ConstantEqualityPredicate p40 = new ConstantEqualityPredicate(1, new TypedConstant<String>("PUBLICATION"));
		this.selection4 = new Select(new ConjunctivePredicate(Lists.newArrayList(p40)), access4.getOutput());
		this.join4 = new Join(selection4.getOutput(), join3.getOutput());
		
		
		return new NormalisedPlan(Lists.newArrayList(this.access0, this.selection0, this.projection0, this.access1, this.join1, this.projection1, 
				this.access2, this.selection2, this.join2,
				this.projection2, this.access3, this.join3, this.projection3, this.access4, this.selection4, this.join4));
	}
	
	@Test
	public void test1() {
		boolean r1 = new ReasonerUtility().existsInclustionDependency(this.access1.getOutput(), this.selection0.getOutput(), CollectionUtils.union(this.schema.getDependencies(), this.planConstraints), this.restrictedChaser, (DBHomomorphismManager) this.detector);
		System.out.println(r1);
	}
	
	@Test
	public void test2() {
		boolean r2 = new ReasonerUtility().existsInclustionDependency(this.selection0.getOutput(), this.access1.getOutput(), CollectionUtils.union(this.schema.getDependencies(), this.planConstraints), this.restrictedChaser, (DBHomomorphismManager) this.detector);
		System.out.println(r2);
	}
	
	@Test
	public void test3() {
		Attribute k1 = (Attribute) access1.getOutput().getHeader().get(1);
		boolean r3 = new ReasonerUtility().isKey(access1.getOutput(), Lists.newArrayList(k1), CollectionUtils.union(this.keys, CollectionUtils.union(this.schema.getDependencies(), this.planConstraints)), this.egdChaser, (DBHomomorphismManager) this.detector);
		boolean r4 = new ReasonerUtility().isKey(selection0.getOutput(), Lists.newArrayList(k1), CollectionUtils.union(this.keys, CollectionUtils.union(this.schema.getDependencies(), this.planConstraints)), this.egdChaser, (DBHomomorphismManager) this.detector);
		System.out.println(r3);
		System.out.println(r4);
	}
	
	@Test
	public void test4() {
		boolean r5 = new ReasonerUtility().existsInclustionDependency(selection2.getOutput(), join1.getOutput(), CollectionUtils.union(this.schema.getDependencies(), this.planConstraints), this.restrictedChaser, (DBHomomorphismManager) this.detector);
		System.out.println(r5);
	}
	
	@Test
	public void test5() {
		boolean r6 = new ReasonerUtility().existsInclustionDependency(join1.getOutput(), selection2.getOutput(), CollectionUtils.union(this.schema.getDependencies(), this.planConstraints), this.restrictedChaser, (DBHomomorphismManager) this.detector);
		System.out.println(r6);
	}
	
	
	@Test
	public void test6() {
		Attribute k2 = (Attribute) this.selection2.getOutput().getHeader().get(0);
		boolean r7 = new ReasonerUtility().isKey(this.selection2.getOutput(), Lists.newArrayList(k2), CollectionUtils.union(this.keys, CollectionUtils.union(this.schema.getDependencies(), this.planConstraints)), this.egdChaser, (DBHomomorphismManager) this.detector);
		boolean r8 = new ReasonerUtility().isKey(this.join1.getOutput(), Lists.newArrayList(k2), CollectionUtils.union(this.keys, CollectionUtils.union(this.schema.getDependencies(), this.planConstraints)), this.egdChaser, (DBHomomorphismManager) this.detector);
		System.out.println(r7);
		System.out.println(r8);
	}
	
	@Test
	public void test7() {
		boolean r9 = new ReasonerUtility().existsInclustionDependency(this.access3.getOutput(), this.join2.getOutput(), CollectionUtils.union(this.schema.getDependencies(), this.planConstraints), this.restrictedChaser, (DBHomomorphismManager) this.detector);
		System.out.println(r9);
	}
	
	@Test
	public void test8() {
		boolean r10 = new ReasonerUtility().existsInclustionDependency(this.join2.getOutput(), this.access3.getOutput(), CollectionUtils.union(this.schema.getDependencies(), this.planConstraints), this.restrictedChaser, (DBHomomorphismManager) this.detector);
		System.out.println(r10);
	}
	
	@Test
	public void test9() {
		Attribute k3 = (Attribute) join2.getOutput().getHeader().get(3);
		boolean r11 = new ReasonerUtility().isKey(this.access3.getOutput(), Lists.newArrayList(k3), CollectionUtils.union(this.keys, CollectionUtils.union(this.schema.getDependencies(), this.planConstraints)), this.egdChaser, (DBHomomorphismManager) this.detector);
		boolean r12 = new ReasonerUtility().isKey(this.join2.getOutput(), Lists.newArrayList(k3), CollectionUtils.union(this.keys, CollectionUtils.union(this.schema.getDependencies(), this.planConstraints)), this.egdChaser, (DBHomomorphismManager) this.detector);
		System.out.println(r11);
		System.out.println(r12);
	}
	
	@Test
	public void test10() {
		boolean r13 = new ReasonerUtility().existsInclustionDependency(this.selection4.getOutput(), this.join3.getOutput(), CollectionUtils.union(this.schema.getDependencies(), this.planConstraints), this.restrictedChaser, (DBHomomorphismManager) this.detector);
		System.out.println(r13);
	}
	
	@Test
	public void test11() {
		boolean r14 = new ReasonerUtility().existsInclustionDependency(this.join3.getOutput(), this.selection4.getOutput(), CollectionUtils.union(this.schema.getDependencies(), this.planConstraints), this.restrictedChaser, (DBHomomorphismManager) this.detector);
		System.out.println(r14);
	}
	
	@Test
	public void test12() {
		Attribute k4 = (Attribute) this.selection4.getOutput().getHeader().get(1);
		boolean r15 = new ReasonerUtility().isKey(this.selection4.getOutput(), Lists.newArrayList(k4), CollectionUtils.union(this.keys, CollectionUtils.union(this.schema.getDependencies(), this.planConstraints)), this.egdChaser, (DBHomomorphismManager) this.detector);
		boolean r16 = new ReasonerUtility().isKey(this.join3.getOutput(), Lists.newArrayList(k4), CollectionUtils.union(this.keys, CollectionUtils.union(this.schema.getDependencies(), this.planConstraints)), this.egdChaser, (DBHomomorphismManager) this.detector);
		System.out.println(r15);
		System.out.println(r16);
	}


}
