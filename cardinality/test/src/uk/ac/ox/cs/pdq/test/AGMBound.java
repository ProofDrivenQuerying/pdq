package uk.ac.ox.cs.pdq.test;

import static org.mockito.Mockito.when;

import java.math.BigInteger;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import uk.ac.ox.cs.pdq.cost.statistics.Catalog;
import uk.ac.ox.cs.pdq.db.Relation;
import uk.ac.ox.cs.pdq.db.Schema;
import uk.ac.ox.cs.pdq.fol.Atom;
import uk.ac.ox.cs.pdq.fol.Conjunction;
import uk.ac.ox.cs.pdq.fol.ConjunctiveQuery;
import uk.ac.ox.cs.pdq.fol.Predicate;
import uk.ac.ox.cs.pdq.fol.Term;
import uk.ac.ox.cs.pdq.fol.Variable;

import com.google.common.collect.Lists;

/**
 * Tests the methods of the AGMBound class with three queries from a bioinformatics schema
 * 
 * Q(year,document_chembl_id, molecule_chembl_id) = 
 * Document(year,document_chembl_id), Molecule(year, molecule_chembl_id), Activity(document_chembl_id, molecule_chembl_id)
 * 
 * Q(organism,tissue,target_chembl_id) = 
 * CellLine(organism,tissue), Assay(tissue,target_chembl_id), Target(organism, target_chembl_id)
 * 
 * Q(organism, target_chembl_id, assay_chembl_id) = 
 * Target(organism, target_chembl_id), Assay(organism,assay_chembl_id), Activity(target_chembl_id, assay_chembl_id)
 * 
 * @author Efthymia tsamoura
 *
 */
public class AGMBound {

	private Schema schema = new TestSchema().getSchema();

	@Mock 
	private Catalog catalog;

	@Before
	public void prepare() {
		//--|Document|=59559 (log2 = 15.86203)
		//--|Molecule|=6049 (log2 = 12.56248)
		//--|Activity|=908626 (log2 = 19.79332)
		//--|Document.document_year|=39 (log2 = 5.285402)
		//--|Document.document_chembl_id|=59559 (log2 = 15.86203)
		//--|Molecule.usan_year|=54 (log2 = 5.75488)
		//--|Molecule.molecule_chembl_id|=6049 (log2 = 12.56248)
		//--|Activity.document_chembl_id|=24883 (log2 = 14.60287)
		//--|Activity.molecule_chembl_id|=326695 (log2 = 18.317585)
		Relation Document = this.schema.getRelation("Document");
		Relation Molecule = this.schema.getRelation("Molecule");
		Relation Activity = this.schema.getRelation("Activity");
		MockitoAnnotations.initMocks(this);
		when(this.catalog.getCardinality(Document)).thenReturn(59559);
		when(this.catalog.getCardinality(Molecule)).thenReturn(6049);
		when(this.catalog.getCardinality(Activity)).thenReturn(908626);
		when(this.catalog.getCardinality(Document, Document.getAttribute("year"))).thenReturn(39);
		when(this.catalog.getCardinality(Document, Document.getAttribute("document_chembl_id"))).thenReturn(59559);
		when(this.catalog.getCardinality(Molecule, Molecule.getAttribute("usan_year"))).thenReturn(54);
		when(this.catalog.getCardinality(Molecule, Molecule.getAttribute("molecule_chembl_id"))).thenReturn(6049); 
		when(this.catalog.getCardinality(Activity, Activity.getAttribute("document_chembl_id"))).thenReturn(24883);
		when(this.catalog.getCardinality(Activity, Activity.getAttribute("molecule_chembl_id"))).thenReturn(326695);


		//--|CellLine|=759 (log2 = 9.56795)
		//--|Assay|=299936 (log2 = 18.194295)
		//--|Target|=6018 (log2 = 12.555068)
		//--|CellLine.cell_source_tissue|=341 (log2 = 8.413628)
		//--|CellLine.cell_source_organism|=16 (log2 = 4)
		//--|Assay.assay_tissue|=101 (log2 = 6.65821)
		//--|Assay.target_chembl_id|=473 (log2 = 8.88569)
		//--|Target.target_organism|=501 (log2 = 8.968667)
		//--|Target.target_chembl_id|=6018 (log2 = 12.55506)
		Relation CellLine = this.schema.getRelation("CellLine");
		Relation Assay = this.schema.getRelation("Assay");
		Relation Target = this.schema.getRelation("Target");
		when(this.catalog.getCardinality(CellLine)).thenReturn(759);
		when(this.catalog.getCardinality(Assay)).thenReturn(299936);
		when(this.catalog.getCardinality(Target)).thenReturn(6018);
		when(this.catalog.getCardinality(CellLine, CellLine.getAttribute("cell_source_tissue"))).thenReturn(341);
		when(this.catalog.getCardinality(CellLine, CellLine.getAttribute("cell_source_organism"))).thenReturn(16);
		when(this.catalog.getCardinality(Assay, Assay.getAttribute("assay_tissue"))).thenReturn(101);
		when(this.catalog.getCardinality(Assay, Assay.getAttribute("target_chembl_id"))).thenReturn(473); 
		when(this.catalog.getCardinality(Target, Target.getAttribute("organism"))).thenReturn(501);
		when(this.catalog.getCardinality(Target, Target.getAttribute("target_chembl_id"))).thenReturn(6018);

		//--|Target|=6018 (log2 = 12.555068)
		//--|Assay|=298797 (log2 = 18.18880)
		//--|Activity|=908626 (log2 = 19.793327)
		//--|Target.target_organism|=501 (log2 = 8.968667)
		//--|Target.target_chembl_id|=6018 (log2 = 12.55506)
		//--|Assay.assay_organism|=51 (log2 = 5.67242)
		//--|Assay.assay_chembl_id|=298797 (log2 = 18.18880)
		//--|Activity.target_chembl_id|=5080 (log2 = 12.31061)
		//--|Activity.assay_chembl_id|=226923 (log2 = 17.79184)
		when(this.catalog.getCardinality(Target, Target.getAttribute("organism"))).thenReturn(501);
		when(this.catalog.getCardinality(Target, Target.getAttribute("target_chembl_id"))).thenReturn(6018);
		when(this.catalog.getCardinality(Assay, Assay.getAttribute("assay_organism"))).thenReturn(51);
		when(this.catalog.getCardinality(Assay, Assay.getAttribute("assay_chembl_id"))).thenReturn(298797); 
		when(this.catalog.getCardinality(Activity, Activity.getAttribute("target_chembl_id"))).thenReturn(5080);
		when(this.catalog.getCardinality(Activity, Activity.getAttribute("assay_chembl_id"))).thenReturn(226923);
	}

	/**
	 * Tests the AGM bound estimate of the query 
	 * Q(year,document_chembl_id, molecule_chembl_id) = 
	 * Document(year,document_chembl_id), Molecule(year, molecule_chembl_id), Activity(document_chembl_id, molecule_chembl_id)
	 */
	public void test1() {
		ConjunctiveQuery query = null;		

		Term authors = new Variable("authors");
		Term doc_type = new Variable("doc_type");
		Term document_chembl_id = new Variable("document_chembl_id");
		Term doi = new Variable("doi");
		Term first_page = new Variable("first_page");
		Term issue = new Variable("issue");
		Term journal = new Variable("journal");
		Term last_page = new Variable("last_page");
		Term pubmed_id = new Variable("pubmed_id");
		Term title = new Variable("title");
		Term volume = new Variable("volume");
		Term year = new Variable("year");


		Atom Document = new Atom(this.schema.getRelation("Document"), 
				Lists.<Term>newArrayList(authors,doc_type,document_chembl_id,doi,first_page,issue,
						journal,last_page,pubmed_id,title,volume,year)
				);

		Term atc_classifications = new Variable("atc_classifications");
		Term availability_type = new Variable("availability_type");
		Term biocomponents = new Variable("biocomponents");
		Term biotherapeutic = new Variable("biotherapeutic");
		Term black_box_warning = new Variable("black_box_warning");
		Term chebi_par_id = new Variable("chebi_par_id");
		Term chirality = new Variable("chirality");
		Term dosed_ingredient = new Variable("dosed_ingredient");
		Term first_approval = new Variable("first_approval");
		Term first_in_class = new Variable("first_in_class");
		Term helm_notation = new Variable("helm_notation");
		Term indication_class = new Variable("indication_class");
		Term inorganic_flag = new Variable("inorganic_flag");
		Term max_phase = new Variable("max_phase");
		Term parent_chembl_id = new Variable("parent_chembl_id");
		Term acd_logd = new Variable("acd_logd");
		Term acd_logp = new Variable("acd_logp");
		Term acd_most_apka = new Variable("acd_most_apka");
		Term acd_most_bpka = new Variable("acd_most_bpka");
		Term alogp = new Variable("alogp");
		Term aromatic_rings = new Variable("aromatic_rings");
		Term full_molformula = new Variable("full_molformula");
		Term full_mwt = new Variable("full_mwt");
		Term hba = new Variable("hba");
		Term hbd = new Variable("hbd");
		Term heavy_atoms = new Variable("heavy_atoms");
		Term med_chem_friendly = new Variable("med_chem_friendly");
		Term molecular_species = new Variable("molecular_species");
		Term mw_freebase = new Variable("mw_freebase");
		Term mw_monoisotopic = new Variable("mw_monoisotopic");
		Term num_alerts = new Variable("num_alerts");
		Term num_ro5_violations = new Variable("num_ro5_violations");
		Term psa = new Variable("psa");
		Term qed_weighted = new Variable("qed_weighted");
		Term ro3_pass = new Variable("ro3_pass");
		Term rtb = new Variable("rtb");
		Term standard_inchi = new Variable("standard_inchi");
		Term standard_inchi_key = new Variable("standard_inchi_key");
		Term molecule_synonyms = new Variable("molecule_synonyms");
		Term molecule_type = new Variable("molecule_type");
		Term natural_product = new Variable("natural_product");
		Term oral = new Variable("oral");
		Term parenteral = new Variable("parenteral");
		Term polymer_flag = new Variable("polymer_flag");
		Term pref_name = new Variable("pref_name");
		Term prodrug = new Variable("prodrug");
		Term structure_type = new Variable("structure_type");
		Term therapeutic_flag = new Variable("therapeutic_flag");
		Term topical = new Variable("topical");
		Term usan_stem = new Variable("usan_stem");
		Term usan_stem_definition = new Variable("usan_stem_definition");
		Term usan_substem = new Variable("usan_substem");
		//Term usan_year = new Variable("year");
		Term molecule_chembl_id = new Variable("molecule_chembl_id");
		Term canonical_smiles = new Variable("canonical_smiles");


		Atom Molecule = new Atom(this.schema.getRelation("Molecule"), 
				Lists.<Term>newArrayList(atc_classifications,availability_type,biocomponents,biotherapeutic,
						black_box_warning,chebi_par_id,chirality,dosed_ingredient,first_approval,first_in_class,helm_notation,
						indication_class,inorganic_flag,max_phase,molecule_chembl_id,parent_chembl_id,acd_logd,acd_logp,
						acd_most_apka,acd_most_bpka,alogp,aromatic_rings,full_molformula,full_mwt,
						hba,hbd,heavy_atoms,med_chem_friendly,molecular_species,mw_freebase,mw_monoisotopic,num_alerts,num_ro5_violations,
						psa,qed_weighted,ro3_pass,rtb,
						canonical_smiles,standard_inchi,standard_inchi_key,molecule_synonyms,molecule_type,natural_product,oral,parenteral,polymer_flag,pref_name,
						prodrug,structure_type,therapeutic_flag,topical,usan_stem,usan_stem_definition,usan_substem,year)
				);

		Term activity_comment = new Variable("activity_comment");
		Term activity_id = new Variable("activity_id");
		Term assay_chembl_id = new Variable("assay_chembl_id");
		Term assay_description = new Variable("assay_description");
		Term assay_type = new Variable("assay_type");
		Term bao_endpoint = new Variable("bao_endpoint");
		Term bao_format = new Variable("bao_format");
		//Term canonical_smiles = new Variable("canonical_smiles");
		Term data_validity_comment = new Variable("data_validity_comment");
		//Term document_chembl_id = new Variable("document_chembl_id");
		Term document_journal = new Variable("document_journal");
		Term document_year = new Variable("document_year");
		//Term molecule_chembl_id = new Variable("molecule_chembl_id");
		Term pchembl_value = new Variable("pchembl_value");
		Term potential_duplicate = new Variable("potential_duplicate");
		Term published_relation = new Variable("published_relation");
		Term published_type = new Variable("published_type");
		Term published_units = new Variable("published_units");
		Term published_value = new Variable("published_value");
		Term qudt_units = new Variable("qudt_units");
		Term record_id = new Variable("record_id");
		Term standard_flag = new Variable("standard_flag");
		Term standard_relation = new Variable("standard_relation");
		Term standard_type = new Variable("standard_type");
		Term standard_units = new Variable("standard_units");
		Term standard_value = new Variable("standard_value");
		Term target_chembl_id = new Variable("target_chembl_id");
		Term target_organism = new Variable("target_organism");
		Term target_pref_name = new Variable("target_pref_name");
		Term uo_units = new Variable("uo_units");

		Atom Activity = new Atom(this.schema.getRelation("Activity"), 
				Lists.<Term>newArrayList(activity_comment, activity_id, assay_chembl_id, assay_description, assay_type,
						bao_endpoint, bao_format, canonical_smiles, data_validity_comment, document_chembl_id, document_journal, document_year, molecule_chembl_id,
						pchembl_value, potential_duplicate, published_relation, published_type, published_units,
						published_value, qudt_units, record_id, standard_flag, standard_relation, standard_type, standard_units, standard_value, target_chembl_id,
						target_organism, target_pref_name, uo_units));


		Atom head = new Atom(new Predicate("Q", 3), Lists.<Term>newArrayList(year,document_chembl_id, molecule_chembl_id));
		query = new ConjunctiveQuery(head, Conjunction.of(Document, Molecule, Activity));
		this.schema.updateConstants(query.getSchemaConstants());
		BigInteger estimate = uk.ac.ox.cs.pdq.cardinality.estimator.AGMBound.estimate(query, this.catalog);

	}


	/**
	 * Tests the AGM bound estimate of the query 
	 * Q(organism,tissue,target_chembl_id) = 
	 * CellLine(organism,tissue), Assay(tissue,target_chembl_id), Target(organism, target_chembl_id)
	 */
	public void test2() {
		ConjunctiveQuery query = null;

		Term cell_chembl_id = new Variable("cell_chembl_id");
		Term cell_description = new Variable("cell_description");
		Term cell_id = new Variable("cell_id");
		Term cell_name = new Variable("cell_name");
		Term organism = new Variable("organism");
		Term cell_source_tax_id = new Variable("cell_source_tax_id");
		Term tissue = new Variable("tissue");
		Term cellosaurus_id = new Variable("cellosaurus_id");
		Term clo_id = new Variable("clo_id");
		Term efo_id = new Variable("efo_id");

		Atom CellLine = new Atom(this.schema.getRelation("CellLine"), 
				Lists.<Term>newArrayList(cell_chembl_id, cell_description, cell_id, 
						cell_name, organism, cell_source_tax_id, tissue, cellosaurus_id, clo_id, efo_id)
				);

		Term assay_category = new Variable("assay_category");
		Term assay_cell_type = new Variable("assay_cell_type");
		//Term assay_organism = new Variable("assay_organism");
		Term assay_strain = new Variable("assay_strain");
		Term assay_subcellular_fraction = new Variable("assay_subcellular_fraction");
		Term assay_tax_id = new Variable("assay_tax_id");
		Term assay_test_type = new Variable("assay_test_type");
		//Term assay_tissue = new Variable("assay_tissue");
		Term assay_type_description = new Variable("assay_type_description");
		//Term cell_chembl_id = new Variable("cell_chembl_id");
		Term confidence_description = new Variable("confidence_description");
		Term confidence_score = new Variable("confidence_score");
		Term description = new Variable("description");
		Term relationship_description = new Variable("relationship_description");
		Term relationship_type = new Variable("relationship_type");
		Term src_assay_id = new Variable("src_assay_id");
		Term src_id = new Variable("src_id");
		Term assay_type = new Variable("assay_type");
		Term assay_chembl_id = new Variable("assay_chembl_id");
		Term bao_format = new Variable("bao_format");
		Term document_chembl_id = new Variable("document_chembl_id");
		Term target_chembl_id = new Variable("target_chembl_id");

		Atom Assay = new Atom(this.schema.getRelation("Assay"), 
				Lists.<Term>newArrayList(assay_category, assay_cell_type, assay_chembl_id, organism, assay_strain, assay_subcellular_fraction, assay_tax_id, assay_test_type,
						tissue, assay_type, assay_type_description, bao_format, cell_chembl_id, confidence_description, 
						confidence_score, description, document_chembl_id, relationship_description, relationship_type, src_assay_id, src_id, target_chembl_id)
				);

		Term species_group_flag = new Variable("species_group_flag");
		Term target_component_accession = new Variable("target_component_accession");
		Term target_component_id = new Variable("target_component_id");
		Term target_component_type = new Variable("target_component_type");
		Term target_type = new Variable("target_type");
		Term pref_name = new Variable("pref_name");

		Atom Target = new Atom(this.schema.getRelation("Target"),
				Lists.<Term>newArrayList(organism,pref_name,species_group_flag,target_chembl_id,
						target_component_accession,target_component_id,target_component_type,target_type)
				);

		Atom head = new Atom(new Predicate("Q", 3), Lists.<Term>newArrayList(organism, tissue,target_chembl_id));
		query = new ConjunctiveQuery(head, Conjunction.of(CellLine, Assay, Target));
		this.schema.updateConstants(query.getSchemaConstants());
		BigInteger estimate = uk.ac.ox.cs.pdq.cardinality.estimator.AGMBound.estimate(query, this.catalog);
	}

	/**
	 * Tests the AGM bound estimate of the query 
	 * Q(organism, target_chembl_id, assay_chembl_id) = 
	 * Target(organism, target_chembl_id), Assay(organism,assay_chembl_id), Activity(target_chembl_id, assay_chembl_id)
	 */
	@Test
	public void test3() {
		ConjunctiveQuery query = null;

		Term species_group_flag = new Variable("species_group_flag");
		Term target_component_accession = new Variable("target_component_accession");
		Term target_component_id = new Variable("target_component_id");
		Term target_component_type = new Variable("target_component_type");
		Term target_type = new Variable("target_type");
		Term pref_name = new Variable("pref_name");
		Term organism = new Variable("organism");
		Term target_chembl_id = new Variable("target_chembl_id");

		Atom Target = new Atom(this.schema.getRelation("Target"),
				Lists.<Term>newArrayList(organism,pref_name,species_group_flag,target_chembl_id,
						target_component_accession,target_component_id,target_component_type,target_type)
				);

		Term assay_category = new Variable("assay_category");
		Term assay_cell_type = new Variable("assay_cell_type");
		//Term assay_organism = new Variable("assay_organism");
		Term assay_strain = new Variable("assay_strain");
		Term assay_subcellular_fraction = new Variable("assay_subcellular_fraction");
		Term assay_tax_id = new Variable("assay_tax_id");
		Term assay_test_type = new Variable("assay_test_type");
		Term assay_tissue = new Variable("assay_tissue");
		Term assay_type_description = new Variable("assay_type_description");
		//Term cell_chembl_id = new Variable("cell_chembl_id");
		Term confidence_description = new Variable("confidence_description");
		Term confidence_score = new Variable("confidence_score");
		Term description = new Variable("description");
		Term relationship_description = new Variable("relationship_description");
		Term relationship_type = new Variable("relationship_type");
		Term src_assay_id = new Variable("src_assay_id");
		Term src_id = new Variable("src_id");
		Term assay_type = new Variable("assay_type");
		Term assay_chembl_id = new Variable("assay_chembl_id");
		Term bao_format = new Variable("bao_format");
		Term document_chembl_id = new Variable("document_chembl_id");
		Term assay_target_chembl_id = new Variable("assay_target_chembl_id");
		Term cell_chembl_id = new Variable("cell_chembl_id");

		Atom Assay = new Atom(this.schema.getRelation("Assay"), 
				Lists.<Term>newArrayList(assay_category, assay_cell_type, assay_chembl_id, organism, assay_strain, assay_subcellular_fraction, assay_tax_id, assay_test_type,
						assay_tissue, assay_type, assay_type_description, bao_format, cell_chembl_id, confidence_description, 
						confidence_score, description, document_chembl_id, relationship_description, relationship_type, src_assay_id, src_id, assay_target_chembl_id)
				);

		Term activity_comment = new Variable("activity_comment");
		Term activity_id = new Variable("activity_id");
		//Term assay_chembl_id = new Variable("assay_chembl_id");
		Term assay_description = new Variable("assay_description");
		//Term assay_type = new Variable("assay_type");
		Term bao_endpoint = new Variable("bao_endpoint");
		//Term bao_format = new Variable("bao_format");
		Term canonical_smiles = new Variable("canonical_smiles");
		Term data_validity_comment = new Variable("data_validity_comment");
		//Term document_chembl_id = new Variable("document_chembl_id");
		Term document_journal = new Variable("document_journal");
		Term document_year = new Variable("document_year");
		Term molecule_chembl_id = new Variable("molecule_chembl_id");
		Term pchembl_value = new Variable("pchembl_value");
		Term potential_duplicate = new Variable("potential_duplicate");
		Term published_relation = new Variable("published_relation");
		Term published_type = new Variable("published_type");
		Term published_units = new Variable("published_units");
		Term published_value = new Variable("published_value");
		Term qudt_units = new Variable("qudt_units");
		Term record_id = new Variable("record_id");
		Term standard_flag = new Variable("standard_flag");
		Term standard_relation = new Variable("standard_relation");
		Term standard_type = new Variable("standard_type");
		Term standard_units = new Variable("standard_units");
		Term standard_value = new Variable("standard_value");
		//Term target_chembl_id = new Variable("target_chembl_id");
		Term target_organism = new Variable("target_organism");
		Term target_pref_name = new Variable("target_pref_name");
		Term uo_units = new Variable("uo_units");

		Atom Activity = new Atom(this.schema.getRelation("Activity"), 
				Lists.<Term>newArrayList(activity_comment, activity_id, assay_chembl_id, assay_description, assay_type,
						bao_endpoint, bao_format, canonical_smiles, data_validity_comment, document_chembl_id, document_journal, document_year, molecule_chembl_id,
						pchembl_value, potential_duplicate, published_relation, published_type, published_units,
						published_value, qudt_units, record_id, standard_flag, standard_relation, standard_type, standard_units, standard_value, target_chembl_id,
						target_organism, target_pref_name, uo_units));

		Atom head = new Atom(new Predicate("Q", 3), Lists.<Term>newArrayList(organism, target_chembl_id, assay_chembl_id));
		query = new ConjunctiveQuery(head, Conjunction.of(Target, Assay, Activity));
		this.schema.updateConstants(query.getSchemaConstants());
		BigInteger estimate = uk.ac.ox.cs.pdq.cardinality.estimator.AGMBound.estimate(query, this.catalog);
	}
}
