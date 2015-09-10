package uk.ac.ox.cs.pdq.test.reasoning.utility;

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

public class ReasonerUtilityTest1 extends ReasonerUtilityTest0 {
	
	Command access0; 
	Command access1; 
	Command access2; 
	Command selection0;
	Command selection2;
	Command join1;
	Command join2;
	Command projection0;
	Command projection1;
	
	//Creates the plan for the configuration 
	//PCOMPOSE(PCOMPOSE(APPLYRULE(DocumentFree(){DocumentFree(c434,PUBLICATION,c428,c435,c436,c437,c438,c439,c440,c441,c442,2015)}),
	//APPLYRULE(AssayLimited(17){AssayLimited(c412,c413,c414,c415,c416,c417,c418,c419,c420,c421,c422,c423,c424,c425,c426,c427,c428,c429,c430,c431,c432,c433)})),
	//APPLYRULE(TargetLimited(4){TargetLimited(c443,c444,c445,c433,c446,c447,c448,SINGLE PROTEIN)}))

	@Override
	public NormalisedPlan loadPlan() {
		//Define all schema and chase constants
		Term _authors = new Variable("authors");
		Term _document_chembl_id = new Variable("document_chembl_id");
		Term _doi = new Variable("doi");
		Term _first_page = new Variable("first_page");
		Term _issue = new Variable("issue");
		Term _journal = new Variable("journal");
		Term _last_page = new Variable("last_page");
		Term _pubmed_id = new Variable("pubmed_id");
		Term _title = new Variable("title");
		Term _volume = new Variable("volume");
		Term _publication = new Variable("cpub");
		Term _year = new Variable("cyear");
		
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
		Term _relationship_description = new Variable("relationship_description");
		Term _relationship_type = new Variable("relationship_type");
		Term _src_assay_id = new Variable("src_assay_id");
		Term _src_id = new Variable("src_id");
		Term _target_chembl_id = new Variable("target_chembl_id");
		
		Term _organism = new Variable("organism");
		Term _pref_name = new Variable("pref_name");
		Term _species_group_flag = new Variable("species_group_flag");
		Term _target_component_accession = new Variable("target_component_accession");
		Term _target_component_id = new Variable("target_component_id");
		Term _target_component_type = new Variable("target_component_type");
		Term _target_type = new Variable("ctype");
		
		//Define the plan
		this.access0 = new Access(this.schema.getRelation("DocumentFree"), this.schema.getRelation("DocumentFree").getAccessMethod("chembl_document_free"), 
				Lists.newArrayList(_authors,_publication, _document_chembl_id,_doi,_first_page,_issue,_journal,_last_page,_pubmed_id,_title,_volume,_year), null, null);
		ConstantEqualityPredicate p00 = new ConstantEqualityPredicate(1, new TypedConstant<String>("PUBLICATION"));
		ConstantEqualityPredicate p01 = new ConstantEqualityPredicate(11, new TypedConstant<String>("2015"));
		this.selection0 = new Select(new ConjunctivePredicate(Lists.newArrayList(p00,p01)), access0.getOutput());
		Attribute attr = (Attribute) selection0.getOutput().getHeader().get(1);
		this.projection0 = new Project(Lists.newArrayList(attr), selection0.getOutput());
		
		this.access1 = new Access(this.schema.getRelation("AssayLimited"), this.schema.getRelation("AssayLimited").getAccessMethod("chembl_assay_limited_2"), 
				Lists.newArrayList(_assay_category, _assay_cell_type, _assay_chembl_id, _assay_organism, _assay_strain, _assay_subcellular_fraction, 
						_assay_tax_id, _assay_test_type, _assay_tissue, _assay_type, _assay_type_description, _bao_format, 
						_cell_chembl_id, _confidence_description, _confidence_score, _description, 
						_document_chembl_id, _relationship_description, _relationship_type, _src_assay_id, _src_id, _target_chembl_id), projection0.getOutput(), null);
		this.join1 = new Join(access1.getOutput(), selection0.getOutput());
		Attribute attr1 = (Attribute) join1.getOutput().getHeader().get(21);
		this.projection1 = new Project(Lists.newArrayList(attr1), join1.getOutput());
		
		access2 = new Access(this.schema.getRelation("TargetLimited"), this.schema.getRelation("TargetLimited").getAccessMethod("chembl_target_limited"), 
				Lists.newArrayList(_organism,_pref_name,_species_group_flag,_target_chembl_id,_target_component_accession,_target_component_id,_target_component_type,_target_type), projection1.getOutput(), null);
		ConstantEqualityPredicate p20 = new ConstantEqualityPredicate(7, new TypedConstant<String>("SINGLE PROTEIN"));
		this.selection2 = new Select(new ConjunctivePredicate(Lists.newArrayList(p20)), access2.getOutput());
		this.join2 = new Join(selection2.getOutput(), join1.getOutput());
		
		return new NormalisedPlan(Lists.newArrayList(this.access0, this.selection0, this.projection0, this.access1, this.join1, 
				this.projection1, this.access2, this.selection2, this.join2));
	}
	
	@Test
	public void test1() {
		boolean r1 = new ReasonerUtility().existsInclustionDependency(this.access1.getOutput(), this.selection0.getOutput(), CollectionUtils.union(this.schema.getDependencies(), this.planConstraints), this.restrictedChaser, (DBHomomorphismManager) this.detector);
		System.out.println(r1);
	}
	
	@Test
	public void test2() {
		boolean r2 = new ReasonerUtility().existsInclustionDependency(this.selection0.getOutput(), this.access1.getOutput(), CollectionUtils.union(this.schema.getDependencies(), this.planConstraints),this.restrictedChaser, (DBHomomorphismManager) this.detector);
		System.out.println(r2);
	}
	
	@Test
	public void test3() {
		Attribute k1 = (Attribute) this.access1.getOutput().getHeader().get(16);
		boolean r3 = new ReasonerUtility().isKey(this.access1.getOutput(), Lists.newArrayList(k1), CollectionUtils.union(this.keys, CollectionUtils.union(this.schema.getDependencies(), this.planConstraints)), this.egdChaser, (DBHomomorphismManager) this.detector);
		boolean r4 = new ReasonerUtility().isKey(this.selection0.getOutput(), Lists.newArrayList(k1), CollectionUtils.union(this.keys, CollectionUtils.union(this.schema.getDependencies(), this.planConstraints)), this.egdChaser, (DBHomomorphismManager) this.detector);
		System.out.println(r3);
		System.out.println(r4);
	}
	
	@Test
	public void test4() {
		boolean r5 = new ReasonerUtility().existsInclustionDependency(this.selection2.getOutput(), this.join1.getOutput(), CollectionUtils.union(this.schema.getDependencies(), this.planConstraints), this.restrictedChaser, (DBHomomorphismManager) this.detector);
		System.out.println(r5);
	}
	
	@Test
	public void test5() {
		boolean r6 = new ReasonerUtility().existsInclustionDependency(this.join1.getOutput(), this.selection2.getOutput(), CollectionUtils.union(this.schema.getDependencies(), this.planConstraints), this.restrictedChaser, (DBHomomorphismManager) this.detector);
		System.out.println(r6);
	}
	
	@Test
	public void test12() {
		Attribute k2 = (Attribute) this.selection2.getOutput().getHeader().get(3);
		boolean r5 = new ReasonerUtility().isKey(this.selection2.getOutput(), Lists.newArrayList(k2), CollectionUtils.union(this.keys, CollectionUtils.union(this.schema.getDependencies(), this.planConstraints)), this.egdChaser, (DBHomomorphismManager) this.detector);
		boolean r6 = new ReasonerUtility().isKey(this.join1.getOutput(), Lists.newArrayList(k2), CollectionUtils.union(this.keys, CollectionUtils.union(this.schema.getDependencies(), this.planConstraints)), this.egdChaser, (DBHomomorphismManager) this.detector);
		System.out.println(r5);
		System.out.println(r6);
	}	
}
