package uk.ac.ox.cs.pdq.test.cost.estimators;

import java.util.Collection;



import uk.ac.ox.cs.pdq.db.AccessMethod;
import uk.ac.ox.cs.pdq.db.AccessMethod.Types;
import uk.ac.ox.cs.pdq.db.Attribute;
import uk.ac.ox.cs.pdq.db.Relation;
import uk.ac.ox.cs.pdq.db.Schema;
import uk.ac.ox.cs.pdq.db.TypedConstant;
import uk.ac.ox.cs.pdq.db.View;
import uk.ac.ox.cs.pdq.db.wrappers.InMemoryTableWrapper;
import uk.ac.ox.cs.pdq.fol.Conjunction;
import uk.ac.ox.cs.pdq.fol.Dependency;
import uk.ac.ox.cs.pdq.fol.TGD;
import uk.ac.ox.cs.pdq.fol.Atom;
import uk.ac.ox.cs.pdq.fol.Term;
import uk.ac.ox.cs.pdq.fol.Variable;

import com.google.common.collect.Lists;


// TODO: Auto-generated Javadoc
/**
 * The Class TestSchema.
 *
 * @author Efthymia Tsamoura
 */

public final class TestSchema {

	/** The schema. */
	private final Schema schema;

	/**
	 * Instantiates a new test schema.
	 */
	public TestSchema() {
		Attribute activity_comment = new Attribute(String.class, "activity_comment");
		Attribute activity_id = new Attribute(String.class, "activity_id");
		Attribute assay_chembl_id = new Attribute(String.class, "assay_chembl_id");
		Attribute assay_description = new Attribute(String.class, "assay_description");
		Attribute assay_type = new Attribute(String.class, "assay_type");
		Attribute bao_endpoint = new Attribute(String.class, "bao_endpoint");
		Attribute bao_format = new Attribute(String.class, "bao_format");
		Attribute canonical_smiles = new Attribute(String.class, "canonical_smiles");
		Attribute data_validity_comment = new Attribute(String.class, "data_validity_comment");
		Attribute document_chembl_id = new Attribute(String.class, "document_chembl_id");
		Attribute document_journal = new Attribute(String.class, "document_journal");
		Attribute document_year = new Attribute(String.class, "document_year");
		Attribute molecule_chembl_id = new Attribute(String.class, "molecule_chembl_id");
		Attribute pchembl_value = new Attribute(String.class, "pchembl_value");
		Attribute potential_duplicate = new Attribute(String.class, "potential_duplicate");
		Attribute published_relation = new Attribute(String.class, "published_relation");
		Attribute published_type = new Attribute(String.class, "published_type");
		Attribute published_units = new Attribute(String.class, "published_units");
		Attribute published_value = new Attribute(String.class, "published_value");
		Attribute qudt_units = new Attribute(String.class, "qudt_units");
		Attribute record_id = new Attribute(String.class, "record_id");
		Attribute standard_flag = new Attribute(String.class, "standard_flag");
		Attribute standard_relation = new Attribute(String.class, "standard_relation");
		Attribute standard_type = new Attribute(String.class, "standard_type");
		Attribute standard_units = new Attribute(String.class, "standard_units");
		Attribute standard_value = new Attribute(String.class, "standard_value");
		Attribute target_chembl_id = new Attribute(String.class, "target_chembl_id");
		Attribute target_organism = new Attribute(String.class, "target_organism");
		Attribute target_pref_name = new Attribute(String.class, "target_pref_name");
		Attribute uo_units = new Attribute(String.class, "uo_units");

		AccessMethod chembl_activity_free = new AccessMethod("chembl_activity_free", Types.FREE, Lists.<Integer>newArrayList());
		AccessMethod chembl_activity_limited = new AccessMethod("chembl_activity_limited", Types.LIMITED, Lists.newArrayList(2));
		AccessMethod chembl_activity_limited_1 = new AccessMethod("chembl_activity_limited_1", Types.LIMITED, Lists.newArrayList(10));
		AccessMethod chembl_activity_limited_2 = new AccessMethod("chembl_activity_limited_2", Types.LIMITED, Lists.newArrayList(13));
		AccessMethod chembl_activity_limited_3 = new AccessMethod("chembl_activity_limited_3", Types.LIMITED, Lists.newArrayList(27));
		AccessMethod chembl_activity_limited_4 = new AccessMethod("chembl_activity_limited_4", Types.LIMITED, Lists.newArrayList(28));

		InMemoryTableWrapper ActivityFree = new InMemoryTableWrapper("ActivityFree", 
				Lists.<Attribute>newArrayList(activity_comment, activity_id, assay_chembl_id, assay_description, assay_type,
						bao_endpoint, bao_format, canonical_smiles, data_validity_comment, document_chembl_id, document_journal, document_year, molecule_chembl_id,
						pchembl_value, potential_duplicate, published_relation, published_type, published_units,
						published_value, qudt_units, record_id, standard_flag, standard_relation, standard_type, standard_units, standard_value, target_chembl_id,
						target_organism, target_pref_name, uo_units),
						Lists.<AccessMethod>newArrayList(chembl_activity_free));
		ActivityFree.setKey(Lists.newArrayList(activity_id));

		InMemoryTableWrapper ActivityLimited = new InMemoryTableWrapper("ActivityLimited", 
				Lists.<Attribute>newArrayList(activity_comment, activity_id, assay_chembl_id, assay_description, assay_type,
						bao_endpoint, bao_format, canonical_smiles, data_validity_comment, document_chembl_id, document_journal, document_year, molecule_chembl_id,
						pchembl_value, potential_duplicate, published_relation, published_type, published_units,
						published_value, qudt_units, record_id, standard_flag, standard_relation, standard_type, standard_units, standard_value, target_chembl_id,
						target_organism, target_pref_name, uo_units),
						Lists.<AccessMethod>newArrayList(chembl_activity_limited,chembl_activity_limited_1, chembl_activity_limited_2, 
								chembl_activity_limited_3, chembl_activity_limited_4));
		ActivityLimited.setKey(Lists.newArrayList(activity_id));

		View Activity = new View("Activity", 
				Lists.<Attribute>newArrayList(activity_comment, activity_id, assay_chembl_id, assay_description, assay_type,
						bao_endpoint, bao_format, canonical_smiles, data_validity_comment, document_chembl_id, document_journal, document_year, molecule_chembl_id,
						pchembl_value, potential_duplicate, published_relation, published_type, published_units,
						published_value, qudt_units, record_id, standard_flag, standard_relation, standard_type, standard_units, standard_value, target_chembl_id,
						target_organism, target_pref_name, uo_units));
		Activity.setKey(Lists.newArrayList(activity_id));

		Attribute assay_category = new Attribute(String.class, "assay_category");
		Attribute assay_cell_type = new Attribute(String.class, "assay_cell_type");
		Attribute assay_organism = new Attribute(String.class, "assay_organism");
		Attribute assay_strain = new Attribute(String.class, "assay_strain");
		Attribute assay_subcellular_fraction = new Attribute(String.class, "assay_subcellular_fraction");
		Attribute assay_tax_id = new Attribute(String.class, "assay_tax_id");
		Attribute assay_test_type = new Attribute(String.class, "assay_test_type");
		Attribute assay_tissue = new Attribute(String.class, "assay_tissue");
		Attribute assay_type_description = new Attribute(String.class, "assay_type_description");
		Attribute cell_chembl_id = new Attribute(String.class, "cell_chembl_id");
		Attribute confidence_description = new Attribute(String.class, "confidence_description");
		Attribute confidence_score = new Attribute(String.class, "confidence_score");
		Attribute description = new Attribute(String.class, "description");
		Attribute relationship_description = new Attribute(String.class, "relationship_description");
		Attribute relationship_type = new Attribute(String.class, "relationship_type");
		Attribute src_assay_id = new Attribute(String.class, "src_assay_id");
		Attribute src_id = new Attribute(String.class, "src_id");

		AccessMethod chembl_assay_free = new AccessMethod("chembl_assay_free", Types.FREE, Lists.<Integer>newArrayList());
		AccessMethod chembl_assay_limited = new AccessMethod("chembl_assay_limited", Types.LIMITED, Lists.newArrayList(3));
		AccessMethod chembl_assay_limited_1 = new AccessMethod("chembl_assay_limited_1", Types.LIMITED, Lists.newArrayList(4));
		AccessMethod chembl_assay_limited_2 = new AccessMethod("chembl_assay_limited_2", Types.LIMITED, Lists.newArrayList(17));
		AccessMethod chembl_assay_limited_3 = new AccessMethod("chembl_assay_limited_3", Types.LIMITED, Lists.newArrayList(22));

		InMemoryTableWrapper AssayFree = new InMemoryTableWrapper("AssayFree", 
				Lists.<Attribute>newArrayList(assay_category, assay_cell_type, assay_chembl_id, assay_organism, assay_strain, assay_subcellular_fraction, assay_tax_id, assay_test_type,
						assay_tissue, assay_type, assay_type_description, bao_format, cell_chembl_id, confidence_description, 
						confidence_score, description, document_chembl_id, relationship_description, relationship_type, src_assay_id, src_id, target_chembl_id),
						Lists.<AccessMethod>newArrayList(chembl_assay_free));
		AssayFree.setKey(Lists.newArrayList(assay_chembl_id));

		InMemoryTableWrapper AssayLimited = new InMemoryTableWrapper("AssayLimited", 
				Lists.<Attribute>newArrayList(assay_category, assay_cell_type, assay_chembl_id, assay_organism, assay_strain, assay_subcellular_fraction, assay_tax_id, assay_test_type,
						assay_tissue, assay_type, assay_type_description, bao_format, cell_chembl_id, confidence_description, 
						confidence_score, description, document_chembl_id, relationship_description, relationship_type, src_assay_id, src_id, target_chembl_id),
						Lists.<AccessMethod>newArrayList(chembl_assay_limited, chembl_assay_limited_1, chembl_assay_limited_2, chembl_assay_limited_3));
		AssayLimited.setKey(Lists.newArrayList(assay_chembl_id));

		View Assay = new View("Assay", 
				Lists.<Attribute>newArrayList(assay_category, assay_cell_type, assay_chembl_id, assay_organism, assay_strain, assay_subcellular_fraction, assay_tax_id, assay_test_type,
						assay_tissue, assay_type, assay_type_description, bao_format, cell_chembl_id, confidence_description, 
						confidence_score, description, document_chembl_id, relationship_description, relationship_type, src_assay_id, src_id, target_chembl_id)
				);
		Assay.setKey(Lists.newArrayList(assay_chembl_id));

		Attribute cell_description = new Attribute(String.class, "cell_description");
		Attribute cell_id = new Attribute(String.class, "cell_id");
		Attribute cell_name = new Attribute(String.class, "cell_name");
		Attribute cell_source_organism = new Attribute(String.class, "cell_source_organism");
		Attribute cell_source_tax_id = new Attribute(String.class, "cell_source_tax_id");
		Attribute cell_source_tissue = new Attribute(String.class, "cell_source_tissue");
		Attribute cellosaurus_id = new Attribute(String.class, "cellosaurus_id");
		Attribute clo_id = new Attribute(String.class, "clo_id");
		Attribute efo_id = new Attribute(String.class, "efo_id");

		AccessMethod chembl_cellline_free = new AccessMethod("chembl_cellline_free", Types.FREE, Lists.<Integer>newArrayList());
		AccessMethod chembl_cellline_limited = new AccessMethod("chembl_cellline_limited", Types.LIMITED, Lists.newArrayList(3));

		InMemoryTableWrapper CellLineFree = new InMemoryTableWrapper("CellLineFree", 
				Lists.<Attribute>newArrayList(cell_chembl_id, cell_description, cell_id, 
						cell_name, cell_source_organism, cell_source_tax_id, cell_source_tissue, cellosaurus_id, clo_id, efo_id),
						Lists.<AccessMethod>newArrayList(chembl_cellline_free));
		CellLineFree.setKey(Lists.newArrayList(cell_chembl_id));

		InMemoryTableWrapper CellLineLimited = new InMemoryTableWrapper("CellLineLimited", 
				Lists.<Attribute>newArrayList(cell_chembl_id, cell_description, cell_id, 
						cell_name, cell_source_organism, cell_source_tax_id, cell_source_tissue, cellosaurus_id, clo_id, efo_id),
						Lists.<AccessMethod>newArrayList(chembl_cellline_limited));
		CellLineLimited.setKey(Lists.newArrayList(cell_chembl_id));

		View CellLine = new View("CellLine", 
				Lists.<Attribute>newArrayList(cell_chembl_id, cell_description, cell_id, 
						cell_name, cell_source_organism, cell_source_tax_id, cell_source_tissue, cellosaurus_id, clo_id, efo_id)
				);
		CellLine.setKey(Lists.newArrayList(cell_chembl_id));

		Attribute authors = new Attribute(String.class, "authors");
		Attribute doc_type = new Attribute(String.class, "doc_type");
		Attribute doi = new Attribute(String.class, "doi");
		Attribute first_page = new Attribute(String.class, "first_page");
		Attribute issue = new Attribute(String.class, "issue");
		Attribute journal = new Attribute(String.class, "journal");
		Attribute last_page = new Attribute(String.class, "last_page");
		Attribute pubmed_id = new Attribute(String.class, "pubmed_id");
		Attribute title = new Attribute(String.class, "title");
		Attribute volume = new Attribute(String.class, "volume");
		Attribute year = new Attribute(String.class, "year");

		AccessMethod chembl_document_free = new AccessMethod("chembl_document_free", Types.FREE, Lists.<Integer>newArrayList());
		AccessMethod chembl_document_limited = new AccessMethod("chembl_document_limited", Types.LIMITED, Lists.newArrayList(3));
		AccessMethod chembl_document_limited_1 = new AccessMethod("chembl_document_limited_1", Types.LIMITED, Lists.newArrayList(9));

		InMemoryTableWrapper DocumentFree = new InMemoryTableWrapper("DocumentFree", 
				Lists.<Attribute>newArrayList(authors,doc_type,document_chembl_id,doi,first_page,issue,
						journal,last_page,pubmed_id,title,volume,year),
						Lists.<AccessMethod>newArrayList(chembl_document_free));
		DocumentFree.setKey(Lists.newArrayList(document_chembl_id));

		InMemoryTableWrapper DocumentLimited = new InMemoryTableWrapper("DocumentLimited", 
				Lists.<Attribute>newArrayList(authors,doc_type,document_chembl_id,doi,first_page,issue,
						journal,last_page,pubmed_id,title,volume,year),
						Lists.<AccessMethod>newArrayList(chembl_document_limited,chembl_document_limited_1));
		DocumentLimited.setKey(Lists.newArrayList(document_chembl_id));

		View Document = new View("Document", 
				Lists.<Attribute>newArrayList(authors,doc_type,document_chembl_id,doi,first_page,issue,
						journal,last_page,pubmed_id,title,volume,year)
				);
		Document.setKey(Lists.newArrayList(document_chembl_id));

		Attribute atc_classifications = new Attribute(String.class, "atc_classifications");
		Attribute availability_type = new Attribute(String.class, "availability_type");
		Attribute biocomponents = new Attribute(String.class, "biocomponents");
		Attribute biotherapeutic = new Attribute(String.class, "biotherapeutic");
		Attribute black_box_warning = new Attribute(String.class, "black_box_warning");
		Attribute chebi_par_id = new Attribute(String.class, "chebi_par_id");
		Attribute chirality = new Attribute(String.class, "chirality");
		Attribute dosed_ingredient = new Attribute(String.class, "dosed_ingredient");
		Attribute first_approval = new Attribute(String.class, "first_approval");
		Attribute first_in_class = new Attribute(String.class, "first_in_class");
		Attribute helm_notation = new Attribute(String.class, "helm_notation");
		Attribute indication_class = new Attribute(String.class, "indication_class");
		Attribute inorganic_flag = new Attribute(String.class, "inorganic_flag");
		Attribute max_phase = new Attribute(String.class, "max_phase");
		Attribute parent_chembl_id = new Attribute(String.class, "parent_chembl_id");
		Attribute acd_logd = new Attribute(String.class, "acd_logd");
		Attribute acd_logp = new Attribute(String.class, "acd_logp");
		Attribute acd_most_apka = new Attribute(String.class, "acd_most_apka");
		Attribute acd_most_bpka = new Attribute(String.class, "acd_most_bpka");
		Attribute alogp = new Attribute(String.class, "alogp");
		Attribute aromatic_rings = new Attribute(String.class, "aromatic_rings");
		Attribute full_molformula = new Attribute(String.class, "full_molformula");
		Attribute full_mwt = new Attribute(String.class, "full_mwt");
		Attribute hba = new Attribute(String.class, "hba");
		Attribute hbd = new Attribute(String.class, "hbd");
		Attribute heavy_atoms = new Attribute(String.class, "heavy_atoms");
		Attribute med_chem_friendly = new Attribute(String.class, "med_chem_friendly");
		Attribute molecular_species = new Attribute(String.class, "molecular_species");
		Attribute mw_freebase = new Attribute(String.class, "mw_freebase");
		Attribute mw_monoisotopic = new Attribute(String.class, "mw_monoisotopic");
		Attribute num_alerts = new Attribute(String.class, "num_alerts");
		Attribute num_ro5_violations = new Attribute(String.class, "num_ro5_violations");
		Attribute psa = new Attribute(String.class, "psa");
		Attribute qed_weighted = new Attribute(String.class, "qed_weighted");
		Attribute ro3_pass = new Attribute(String.class, "ro3_pass");
		Attribute rtb = new Attribute(String.class, "rtb");
		Attribute standard_inchi = new Attribute(String.class, "standard_inchi");
		Attribute standard_inchi_key = new Attribute(String.class, "standard_inchi_key");
		Attribute molecule_synonyms = new Attribute(String.class, "molecule_synonyms");
		Attribute molecule_type = new Attribute(String.class, "molecule_type");
		Attribute natural_product = new Attribute(String.class, "natural_product");
		Attribute oral = new Attribute(String.class, "oral");
		Attribute parenteral = new Attribute(String.class, "parenteral");
		Attribute polymer_flag = new Attribute(String.class, "polymer_flag");
		Attribute pref_name = new Attribute(String.class, "pref_name");
		Attribute prodrug = new Attribute(String.class, "prodrug");
		Attribute structure_type = new Attribute(String.class, "structure_type");
		Attribute therapeutic_flag = new Attribute(String.class, "therapeutic_flag");
		Attribute topical = new Attribute(String.class, "topical");
		Attribute usan_stem = new Attribute(String.class, "usan_stem");
		Attribute usan_stem_definition = new Attribute(String.class, "usan_stem_definition");
		Attribute usan_substem = new Attribute(String.class, "usan_substem");
		Attribute usan_year = new Attribute(String.class, "usan_year");

		AccessMethod chembl_molecule_free = new AccessMethod("chembl_molecule_free", Types.FREE, Lists.<Integer>newArrayList());
		AccessMethod chembl_molecule_limited = new AccessMethod("chembl_molecule_limited", Types.LIMITED, Lists.newArrayList(15));

		InMemoryTableWrapper MoleculeFree = new InMemoryTableWrapper("MoleculeFree", 
				Lists.<Attribute>newArrayList(atc_classifications,availability_type,biocomponents,biotherapeutic,
						black_box_warning,chebi_par_id,chirality,dosed_ingredient,first_approval,first_in_class,helm_notation,
						indication_class,inorganic_flag,max_phase,molecule_chembl_id,parent_chembl_id,acd_logd,acd_logp,
						acd_most_apka,acd_most_bpka,alogp,aromatic_rings,full_molformula,full_mwt,
						hba,hbd,heavy_atoms,med_chem_friendly,molecular_species,mw_freebase,mw_monoisotopic,num_alerts,num_ro5_violations,
						psa,qed_weighted,ro3_pass,rtb,
						canonical_smiles,standard_inchi,standard_inchi_key,molecule_synonyms,molecule_type,natural_product,oral,parenteral,polymer_flag,pref_name,
						prodrug,structure_type,therapeutic_flag,topical,usan_stem,usan_stem_definition,usan_substem,usan_year),
						Lists.<AccessMethod>newArrayList(chembl_molecule_free));
		MoleculeFree.setKey(Lists.newArrayList(molecule_chembl_id));

		InMemoryTableWrapper MoleculeLimited = new InMemoryTableWrapper("MoleculeLimited", 
				Lists.<Attribute>newArrayList(atc_classifications,availability_type,biocomponents,biotherapeutic,
						black_box_warning,chebi_par_id,chirality,dosed_ingredient,first_approval,first_in_class,helm_notation,
						indication_class,inorganic_flag,max_phase,molecule_chembl_id,parent_chembl_id,acd_logd,acd_logp,
						acd_most_apka,acd_most_bpka,alogp,aromatic_rings,full_molformula,full_mwt,
						hba,hbd,heavy_atoms,med_chem_friendly,molecular_species,mw_freebase,mw_monoisotopic,num_alerts,num_ro5_violations,
						psa,qed_weighted,ro3_pass,rtb,
						canonical_smiles,standard_inchi,standard_inchi_key,molecule_synonyms,molecule_type,natural_product,oral,parenteral,polymer_flag,pref_name,
						prodrug,structure_type,therapeutic_flag,topical,usan_stem,usan_stem_definition,usan_substem,usan_year),
						Lists.<AccessMethod>newArrayList(chembl_molecule_limited));
		MoleculeLimited.setKey(Lists.newArrayList(molecule_chembl_id));

		View Molecule = new View("Molecule", 
				Lists.<Attribute>newArrayList(atc_classifications,availability_type,biocomponents,biotherapeutic,
						black_box_warning,chebi_par_id,chirality,dosed_ingredient,first_approval,first_in_class,helm_notation,
						indication_class,inorganic_flag,max_phase,molecule_chembl_id,parent_chembl_id,acd_logd,acd_logp,
						acd_most_apka,acd_most_bpka,alogp,aromatic_rings,full_molformula,full_mwt,
						hba,hbd,heavy_atoms,med_chem_friendly,molecular_species,mw_freebase,mw_monoisotopic,num_alerts,num_ro5_violations,
						psa,qed_weighted,ro3_pass,rtb,
						canonical_smiles,standard_inchi,standard_inchi_key,molecule_synonyms,molecule_type,natural_product,oral,parenteral,polymer_flag,pref_name,
						prodrug,structure_type,therapeutic_flag,topical,usan_stem,usan_stem_definition,usan_substem,usan_year)
				);
		Molecule.setKey(Lists.newArrayList(molecule_chembl_id));

		Attribute organism = new Attribute(String.class, "organism");
		Attribute species_group_flag = new Attribute(String.class, "species_group_flag");
		Attribute target_component_accession = new Attribute(String.class, "target_component_accession");
		Attribute target_component_id = new Attribute(String.class, "target_component_id");
		Attribute target_component_type = new Attribute(String.class, "target_component_type");
		Attribute target_type = new Attribute(String.class, "target_type");

		AccessMethod chembl_target_free = new AccessMethod("chembl_target_free", Types.FREE, Lists.<Integer>newArrayList());
		AccessMethod chembl_target_limited = new AccessMethod("chembl_target_limited", Types.LIMITED, Lists.newArrayList(4));
		AccessMethod chembl_target_limited_1 = new AccessMethod("chembl_target_limited_1", Types.LIMITED, Lists.newArrayList(1));

		InMemoryTableWrapper TargetFree = new InMemoryTableWrapper("TargetFree", 
				Lists.<Attribute>newArrayList(organism,pref_name,species_group_flag,target_chembl_id,
						target_component_accession,target_component_id,target_component_type,target_type),
						Lists.<AccessMethod>newArrayList(chembl_target_free));
		TargetFree.setKey(Lists.newArrayList(target_chembl_id));

		InMemoryTableWrapper TargetLimited = new InMemoryTableWrapper("TargetLimited", 
				Lists.<Attribute>newArrayList(organism,pref_name,species_group_flag,target_chembl_id,
						target_component_accession,target_component_id,target_component_type,target_type),
						Lists.<AccessMethod>newArrayList(chembl_target_limited,chembl_target_limited_1));
		TargetLimited.setKey(Lists.newArrayList(target_chembl_id));

		View Target = new View("Target", 
				Lists.<Attribute>newArrayList(organism,pref_name,species_group_flag,target_chembl_id,
						target_component_accession,target_component_id,target_component_type,target_type)
				);
		Target.setKey(Lists.newArrayList(target_chembl_id));

		Attribute accession = new Attribute(String.class, "accession");
		Attribute component_id = new Attribute(String.class, "component_id");
		Attribute component_type = new Attribute(String.class, "component_type");
		Attribute protein_classification_id = new Attribute(String.class, "protein_classification_id");
		Attribute sequence = new Attribute(String.class, "sequence");
		Attribute tax_id = new Attribute(String.class, "tax_id");

		AccessMethod chembl_target_component_free = new AccessMethod("chembl_target_component_free", Types.FREE, Lists.<Integer>newArrayList());
		AccessMethod chembl_target_component_limited = new AccessMethod("chembl_target_component_limited", Types.LIMITED, Lists.newArrayList(2));
		AccessMethod chembl_target_component_limited_1 = new AccessMethod("chembl_target_component_limited_1", Types.LIMITED, Lists.newArrayList(1));
		AccessMethod chembl_target_component_limited_2 = new AccessMethod("chembl_target_component_limited_2", Types.LIMITED, Lists.newArrayList(5));

		InMemoryTableWrapper TargetComponentFree = new InMemoryTableWrapper("TargetComponentFree", 
				Lists.<Attribute>newArrayList(accession,component_id,component_type,description,organism,protein_classification_id,sequence,tax_id),
				Lists.<AccessMethod>newArrayList(chembl_target_component_free));
		TargetComponentFree.setKey(Lists.newArrayList(component_id));

		InMemoryTableWrapper TargetComponentLimited = new InMemoryTableWrapper("TargetComponentLimited", 
				Lists.<Attribute>newArrayList(accession,component_id,component_type,description,organism,protein_classification_id,sequence,tax_id),
				Lists.<AccessMethod>newArrayList(chembl_target_component_limited,chembl_target_component_limited_1,chembl_target_component_limited_2));
		TargetComponentLimited.setKey(Lists.newArrayList(component_id));

		View TargetComponent = new View("TargetComponent", 
				Lists.<Attribute>newArrayList(accession,component_id,component_type,description,organism,protein_classification_id,sequence,tax_id)
				);
		TargetComponent.setKey(Lists.newArrayList(component_id));

		Attribute id = new Attribute(String.class, "id");
		Attribute source = new Attribute(String.class, "source");
		Attribute pmid = new Attribute(String.class, "pmid");
		Attribute pmcid = new Attribute(String.class, "pmcid");
		Attribute DOI = new Attribute(String.class, "DOI");
		Attribute authorString = new Attribute(String.class, "authorString");
		Attribute journalTitle = new Attribute(String.class, "journalTitle");
		Attribute journalVolume = new Attribute(String.class, "journalVolume");
		Attribute pubYear = new Attribute(String.class, "pubYear");
		Attribute journalIssn = new Attribute(String.class, "journalIssn");
		Attribute pubType = new Attribute(String.class, "pubType");
		Attribute inEPMC = new Attribute(String.class, "inEPMC");
		Attribute inPMC = new Attribute(String.class, "inPMC");
		Attribute citedByCount = new Attribute(String.class, "citedByCount");
		Attribute hasReferences = new Attribute(String.class, "hasReferences");
		Attribute hasTextMinedTerms = new Attribute(String.class, "hasTextMinedTerms");
		Attribute hasDbCrossReferences = new Attribute(String.class, "hasDbCrossReferences");
		Attribute hasLabsLinks = new Attribute(String.class, "hasLabsLinks");
		Attribute hasTMAccessionNumbers = new Attribute(String.class, "hasTMAccessionNumbers");
		Attribute gene = new Attribute(String.class, "gene");
		Attribute uniprotPubs = new Attribute(String.class, "uniprotPubs");

		AccessMethod pubmed_pub_1 = new AccessMethod("pubmed_pub_1", Types.LIMITED, Lists.newArrayList(1));
		AccessMethod pubmed_pub_2 = new AccessMethod("pubmed_pub_2", Types.LIMITED, Lists.newArrayList(4));
		AccessMethod pubmed_pub_3 = new AccessMethod("pubmed_pub_3", Types.LIMITED, Lists.newArrayList(5));
		AccessMethod pubmed_pub_4 = new AccessMethod("pubmed_pub_4", Types.LIMITED, Lists.newArrayList(8));
		AccessMethod pubmed_pub_5 = new AccessMethod("pubmed_pub_5", Types.LIMITED, Lists.newArrayList(9));
		AccessMethod pubmed_pub_6 = new AccessMethod("pubmed_pub_6", Types.LIMITED, Lists.newArrayList(10));
		AccessMethod pubmed_pub_7 = new AccessMethod("pubmed_pub_7", Types.LIMITED, Lists.newArrayList(11));
		AccessMethod pubmed_pub_8 = new AccessMethod("pubmed_pub_8", Types.LIMITED, Lists.newArrayList(13));
		AccessMethod pubmed_pub_9 = new AccessMethod("pubmed_pub_9", Types.LIMITED, Lists.newArrayList(14));
		AccessMethod pubmed_pub_10 = new AccessMethod("pubmed_pub_10", Types.LIMITED, Lists.newArrayList(15));
		AccessMethod pubmed_pub_11 = new AccessMethod("pubmed_pub_11", Types.LIMITED, Lists.newArrayList(22));
		AccessMethod pubmed_pub_12 = new AccessMethod("pubmed_pub_12", Types.LIMITED, Lists.newArrayList(23));
		AccessMethod pubmed_pub_13 = new AccessMethod("pubmed_pub_13", Types.LIMITED, Lists.newArrayList(24));
		AccessMethod pubmed_pub_14 = new AccessMethod("pubmed_pub_14", Types.LIMITED, Lists.newArrayList(25));
		AccessMethod pubmed_pub_15 = new AccessMethod("pubmed_pub_15", Types.LIMITED, Lists.newArrayList(2));

		InMemoryTableWrapper PublicationFull = new InMemoryTableWrapper("PublicationFull", 
				Lists.<Attribute>newArrayList(id,source,pmid,pmcid,DOI,title,authorString,journalTitle,issue,journalVolume,pubYear,
						journalIssn,pubType,inEPMC,inPMC,citedByCount,hasReferences,hasTextMinedTerms,hasDbCrossReferences,
						hasLabsLinks,hasTMAccessionNumbers,authors,gene,organism,uniprotPubs),
						Lists.<AccessMethod>newArrayList(pubmed_pub_1,pubmed_pub_2,pubmed_pub_3,pubmed_pub_4,pubmed_pub_5,pubmed_pub_6,pubmed_pub_7,pubmed_pub_8,
								pubmed_pub_9,pubmed_pub_10,pubmed_pub_11,pubmed_pub_12,pubmed_pub_13,pubmed_pub_14,pubmed_pub_15));
		PublicationFull.setKey(Lists.newArrayList(id));

		View Publication = new View("Publication", 
				Lists.<Attribute>newArrayList(id,source,pmid,pmcid,DOI,title,authorString,journalTitle,issue,journalVolume,pubYear,
						journalIssn,pubType,inEPMC,inPMC,citedByCount,hasReferences,hasTextMinedTerms,hasDbCrossReferences,
						hasLabsLinks,hasTMAccessionNumbers,authors,gene,organism,uniprotPubs));
		Publication.setKey(Lists.newArrayList(id));

		Attribute citationType = new Attribute(String.class, "citationType");
		Attribute journalAbbreviation = new Attribute(String.class, "journalAbbreviation");
		Attribute pageInfo = new Attribute(String.class, "pageInfo");
		Attribute src = new Attribute(String.class, "src");
		Attribute ext_id = new Attribute(String.class, "ext_id");
		AccessMethod pubmed_citation_1 = new AccessMethod("pubmed_citation_1", Types.LIMITED, Lists.newArrayList(12,13));

		InMemoryTableWrapper Citation = new InMemoryTableWrapper("Citation", 
				Lists.<Attribute>newArrayList(id,source,citationType,title,authorString,journalAbbreviation,pubYear,volume,issue,pageInfo,citedByCount,src,ext_id),
				Lists.<AccessMethod>newArrayList(pubmed_citation_1));
		Citation.setKey(Lists.newArrayList(id));

		AccessMethod pubmed_reference_1 = new AccessMethod("pubmed_reference_1", Types.LIMITED, Lists.newArrayList(11,12));

		InMemoryTableWrapper Reference = new InMemoryTableWrapper("Reference", 
				Lists.<Attribute>newArrayList(id,source,citationType,title,authorString,journalAbbreviation,pubYear,volume,issue,pageInfo,src,ext_id),
				Lists.<AccessMethod>newArrayList(pubmed_reference_1));
		Reference.setKey(Lists.newArrayList(id));

		Attribute pathwayId = new Attribute(String.class, "pathwayId");
		Attribute pathwayName = new Attribute(String.class, "pathwayName");
		Attribute stableIdentifier = new Attribute(String.class, "stableIdentifier");
		Attribute goBiologicalProcess = new Attribute(String.class, "goBiologicalProcess");
		Attribute isInDisease = new Attribute(String.class, "isInDisease");
		Attribute isInferred = new Attribute(String.class, "isInferred");
		Attribute organismId = new Attribute(String.class, "organismId");
		Attribute hasDiagram = new Attribute(String.class, "hasDiagram");

		AccessMethod reactome_pathway_1 = new AccessMethod("reactome_pathway_1", Types.LIMITED, Lists.newArrayList(8));
		AccessMethod reactome_pathway_2 = new AccessMethod("reactome_pathway_2", Types.LIMITED, Lists.newArrayList(1));
		AccessMethod reactome_organism_1 = new AccessMethod("reactome_organism_1", Types.LIMITED, Lists.newArrayList(1));
		AccessMethod reactome_species_1 = new AccessMethod("reactome_species_1", Types.FREE, Lists.<Integer>newArrayList());

		InMemoryTableWrapper PathwayBySpecies = new InMemoryTableWrapper("PathwayBySpecies", 
				Lists.<Attribute>newArrayList(pathwayId,pathwayName,stableIdentifier,goBiologicalProcess,isInDisease,isInferred,organismId,organism,doi,hasDiagram),
				Lists.<AccessMethod>newArrayList(reactome_pathway_1));
		PathwayBySpecies.setKey(Lists.newArrayList(pathwayId));

		InMemoryTableWrapper PathwayById = new InMemoryTableWrapper("PathwayById", 
				Lists.<Attribute>newArrayList(pathwayId,pathwayName,stableIdentifier,goBiologicalProcess,isInDisease,isInferred,organismId,organism,doi,hasDiagram),
				Lists.<AccessMethod>newArrayList(reactome_pathway_2));
		PathwayById.setKey(Lists.newArrayList(pathwayId));

		View Pathway = new View("Pathway", 
				Lists.<Attribute>newArrayList(pathwayId,pathwayName,stableIdentifier,goBiologicalProcess,isInDisease,isInferred,organismId,organism,doi,hasDiagram));
		Pathway.setKey(Lists.newArrayList(pathwayId));

		InMemoryTableWrapper OrganismById = new InMemoryTableWrapper("OrganismById", 
				Lists.<Attribute>newArrayList(organismId,organism),
				Lists.<AccessMethod>newArrayList(reactome_organism_1));
		OrganismById.setKey(Lists.newArrayList(organismId));

		InMemoryTableWrapper OrganismFree = new InMemoryTableWrapper("OrganismFree", 
				Lists.<Attribute>newArrayList(organismId,organism),
				Lists.<AccessMethod>newArrayList(reactome_species_1));
		OrganismFree.setKey(Lists.newArrayList(organismId));

		View Organism = new View("Organism", 
				Lists.<Attribute>newArrayList(organismId,organism));
		Organism.setKey(Lists.newArrayList(organismId));

		Attribute input_id = new Attribute(String.class, "input_id");
		Attribute entry_name = new Attribute(String.class, "entry_name");

		AccessMethod uniprot_protein_1 = new AccessMethod("uniprot_protein_1", Types.LIMITED, Lists.newArrayList(1));
		AccessMethod uniprot_protein_2 = new AccessMethod("uniprot_protein_2", Types.LIMITED, Lists.newArrayList(4));
		AccessMethod uniprot_active = new AccessMethod("uniprot_active", Types.FREE, Lists.<Integer>newArrayList());

		InMemoryTableWrapper ProteinLimited = new InMemoryTableWrapper("ProteinLimited", 
				Lists.<Attribute>newArrayList(input_id,id,entry_name,organism),
				Lists.<AccessMethod>newArrayList(uniprot_protein_1,uniprot_protein_2));
		ProteinLimited.setKey(Lists.newArrayList(id));

		InMemoryTableWrapper ProteinFree = new InMemoryTableWrapper("ProteinFree", 
				Lists.<Attribute>newArrayList(id,entry_name,organism),
				Lists.<AccessMethod>newArrayList(uniprot_active));
		ProteinFree.setKey(Lists.newArrayList(id));

		View Protein = new View("Protein", 
				Lists.<Attribute>newArrayList(input_id,id,entry_name,organism));
		Protein.setKey(Lists.newArrayList(id));
		
		Collection<Relation> relations = Lists.<Relation>newArrayList(ActivityFree,ActivityLimited,Activity,
				AssayFree,AssayLimited,Assay,CellLineFree,CellLineLimited,CellLine,
				DocumentFree,DocumentLimited,Document,MoleculeFree,MoleculeLimited,Molecule,
				TargetFree,TargetLimited,Target,TargetComponentFree,TargetComponentLimited,TargetComponent,
				PublicationFull,Publication,Citation,Reference,PathwayBySpecies,PathwayById,Pathway,
				OrganismById,OrganismFree,Organism,ProteinLimited,ProteinFree,Protein);
		
		Collection<Dependency> dependencies = this.defineDependencies(new Schema(relations));
		this.schema = new Schema(relations, dependencies);
	}


	/**
	 * Define dependencies.
	 *
	 * @param schema the schema
	 * @return the collection
	 */
	private Collection<Dependency> defineDependencies(Schema schema) {
		Term activity_comment = new Variable("activity_comment");
		Term activity_id = new Variable("activity_id");
		Term assay_chembl_id = new Variable("assay_chembl_id");
		Term assay_description = new Variable("assay_description");
		Term assay_type = new Variable("assay_type");
		Term bao_endpoint = new Variable("bao_endpoint");
		Term bao_format = new Variable("bao_format");
		Term canonical_smiles = new Variable("canonical_smiles");
		Term data_validity_comment = new Variable("data_validity_comment");
		Term document_chembl_id = new Variable("document_chembl_id");
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
		Term target_chembl_id = new Variable("target_chembl_id");
		Term target_organism = new Variable("target_organism");
		Term target_pref_name = new Variable("target_pref_name");
		Term uo_units = new Variable("uo_units");

		Term assay_category = new Variable("assay_category");
		Term assay_cell_type = new Variable("assay_cell_type");
		Term assay_organism = new Variable("assay_organism");
		Term assay_strain = new Variable("assay_strain");
		Term assay_subcellular_fraction = new Variable("assay_subcellular_fraction");
		Term assay_tax_id = new Variable("assay_tax_id");
		Term assay_test_type = new Variable("assay_test_type");
		Term assay_tissue = new Variable("assay_tissue");
		Term assay_type_description = new Variable("assay_type_description");
		Term cell_chembl_id = new Variable("cell_chembl_id");
		Term confidence_description = new Variable("confidence_description");
		Term confidence_score = new Variable("confidence_score");
		Term description = new Variable("description");
		Term relationship_description = new Variable("relationship_description");
		Term relationship_type = new Variable("relationship_type");
		Term src_assay_id = new Variable("src_assay_id");
		Term src_id = new Variable("src_id");

		Term cell_description = new Variable("cell_description");
		Term cell_id = new Variable("cell_id");
		Term cell_name = new Variable("cell_name");
		Term cell_source_organism = new Variable("cell_source_organism");
		Term cell_source_tax_id = new Variable("cell_source_tax_id");
		Term cell_source_tissue = new Variable("cell_source_tissue");
		Term cellosaurus_id = new Variable("cellosaurus_id");
		Term clo_id = new Variable("clo_id");
		Term efo_id = new Variable("efo_id");

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
		Term usan_year = new Variable("usan_year");

		Term organism = new Variable("organism");
		Term species_group_flag = new Variable("species_group_flag");
		Term target_component_accession = new Variable("target_component_accession");
		Term target_component_id = new Variable("target_component_id");
		Term target_component_type = new Variable("target_component_type");
		Term target_type = new Variable("target_type");

		Term accession = new Variable("accession");
		Term component_id = new Variable("component_id");
		Term component_type = new Variable("component_type");
		Term protein_classification_id = new Variable("protein_classification_id");
		Term sequence = new Variable("sequence");
		Term tax_id = new Variable("tax_id");

		Term id = new Variable("id");
		Term source = new Variable("source");
		Term pmid = new Variable("pmid");
		Term pmcid = new Variable("pmcid");
		Term DOI = new Variable("DOI");
		Term authorString = new Variable("authorString");
		Term journalTitle = new Variable("journalTitle");
		Term journalVolume = new Variable("journalVolume");
		Term pubYear = new Variable("pubYear");
		Term journalIssn = new Variable("journalIssn");
		Term pubType = new Variable("pubType");
		Term inEPMC = new Variable("inEPMC");
		Term inPMC = new Variable("inPMC");
		Term citedByCount = new Variable("citedByCount");
		Term hasReferences = new Variable("hasReferences");
		Term hasTextMinedTerms = new Variable("hasTextMinedTerms");
		Term hasDbCrossReferences = new Variable("hasDbCrossReferences");
		Term hasLabsLinks = new Variable("hasLabsLinks");
		Term hasTMAccessionNumbers = new Variable("hasTMAccessionNumbers");
		Term gene = new Variable("gene");
		Term uniprotPubs = new Variable("uniprotPubs");

		Term citationType = new Variable("citationType");
		Term journalAbbreviation = new Variable("journalAbbreviation");
		Term pageInfo = new Variable("pageInfo");
		Term src = new Variable("src");
		Term ext_id = new Variable("ext_id");

		Term pathwayId = new Variable("pathwayId");
		Term pathwayName = new Variable("pathwayName");
		Term stableIdentifier = new Variable("stableIdentifier");
		Term goBiologicalProcess = new Variable("goBiologicalProcess");
		Term isInDisease = new Variable("isInDisease");
		Term isInferred = new Variable("isInferred");
		Term organismId = new Variable("organismId");
		Term hasDiagram = new Variable("hasDiagram");

		Term input_id = new Variable("input_id");
		Term entry_name = new Variable("entry_name");

		Atom ActivityFree = new Atom(schema.getRelation("ActivityFree"),activity_comment, activity_id, assay_chembl_id, assay_description, assay_type,
				bao_endpoint, bao_format, canonical_smiles, data_validity_comment, document_chembl_id, document_journal, document_year, molecule_chembl_id,
				pchembl_value, potential_duplicate, published_relation, published_type, published_units,
				published_value, qudt_units, record_id, standard_flag, standard_relation, standard_type, standard_units, standard_value, target_chembl_id,
				target_organism, target_pref_name, uo_units);

		Atom ActivityLimited = new Atom(schema.getRelation("ActivityLimited"),activity_comment, activity_id, assay_chembl_id, assay_description, assay_type,
				bao_endpoint, bao_format, canonical_smiles, data_validity_comment, document_chembl_id, document_journal, document_year, molecule_chembl_id,
				pchembl_value, potential_duplicate, published_relation, published_type, published_units,
				published_value, qudt_units, record_id, standard_flag, standard_relation, standard_type, standard_units, standard_value, target_chembl_id,
				target_organism, target_pref_name, uo_units);

		Atom Activity = new Atom(schema.getRelation("Activity"),activity_comment, activity_id, assay_chembl_id, assay_description, assay_type,
				bao_endpoint, bao_format, canonical_smiles, data_validity_comment, document_chembl_id, document_journal, document_year, molecule_chembl_id,
				pchembl_value, potential_duplicate, published_relation, published_type, published_units,
				published_value, qudt_units, record_id, standard_flag, standard_relation, standard_type, standard_units, standard_value, target_chembl_id,
				target_organism, target_pref_name, uo_units);

		Atom AssayFree = new Atom(schema.getRelation("AssayFree"),
				assay_category, assay_cell_type, assay_chembl_id, assay_organism, assay_strain, assay_subcellular_fraction, assay_tax_id, assay_test_type,
				assay_tissue, assay_type, assay_type_description, bao_format, cell_chembl_id, confidence_description, 
				confidence_score, description, document_chembl_id, relationship_description, relationship_type, src_assay_id, src_id, target_chembl_id);

		Atom AssayLimited = new Atom(schema.getRelation("AssayLimited"),
				assay_category, assay_cell_type, assay_chembl_id, assay_organism, assay_strain, assay_subcellular_fraction, assay_tax_id, assay_test_type,
				assay_tissue, assay_type, assay_type_description, bao_format, cell_chembl_id, confidence_description, 
				confidence_score, description, document_chembl_id, relationship_description, relationship_type, src_assay_id, src_id, target_chembl_id);

		Atom Assay = new Atom(schema.getRelation("Assay"),
				assay_category, assay_cell_type, assay_chembl_id, assay_organism, assay_strain, assay_subcellular_fraction, assay_tax_id, assay_test_type,
				assay_tissue, assay_type, assay_type_description, bao_format, cell_chembl_id, confidence_description, 
				confidence_score, description, document_chembl_id, relationship_description, relationship_type, src_assay_id, src_id, target_chembl_id);


		Atom CellLineFree = new Atom(schema.getRelation("CellLineFree"),
				cell_chembl_id, cell_description, cell_id, 
				cell_name, cell_source_organism, cell_source_tax_id, cell_source_tissue, cellosaurus_id, clo_id, efo_id);

		Atom CellLineLimited = new Atom(schema.getRelation("CellLineLimited"),
				cell_chembl_id, cell_description, cell_id, 
				cell_name, cell_source_organism, cell_source_tax_id, cell_source_tissue, cellosaurus_id, clo_id, efo_id);

		Atom CellLine = new Atom(schema.getRelation("CellLine"),
				cell_chembl_id, cell_description, cell_id, 
				cell_name, cell_source_organism, cell_source_tax_id, cell_source_tissue, cellosaurus_id, clo_id, efo_id);

		Atom DocumentFree = new Atom(schema.getRelation("DocumentFree"),
				authors,doc_type,document_chembl_id,doi,first_page,issue,
				journal,last_page,pubmed_id,title,volume,year);

		Atom DocumentLimited = new Atom(schema.getRelation("DocumentLimited"),
				authors,doc_type,document_chembl_id,doi,first_page,issue,
				journal,last_page,pubmed_id,title,volume,year);

		Atom Document = new Atom(schema.getRelation("Document"),
				authors,doc_type,document_chembl_id,doi,first_page,issue,
				journal,last_page,pubmed_id,title,volume,year);

		Atom MoleculeFree = new Atom(schema.getRelation("MoleculeFree"),
				atc_classifications,availability_type,biocomponents,biotherapeutic,
				black_box_warning,chebi_par_id,chirality,dosed_ingredient,first_approval,first_in_class,helm_notation,
				indication_class,inorganic_flag,max_phase,molecule_chembl_id,parent_chembl_id,acd_logd,acd_logp,
				acd_most_apka,acd_most_bpka,alogp,aromatic_rings,full_molformula,full_mwt,
				hba,hbd,heavy_atoms,med_chem_friendly,molecular_species,mw_freebase,mw_monoisotopic,num_alerts,num_ro5_violations,
				psa,qed_weighted,ro3_pass,rtb,
				canonical_smiles,standard_inchi,standard_inchi_key,molecule_synonyms,molecule_type,natural_product,oral,parenteral,polymer_flag,pref_name,
				prodrug,structure_type,therapeutic_flag,topical,usan_stem,usan_stem_definition,usan_substem,usan_year);

		Atom MoleculeLimited = new Atom(schema.getRelation("MoleculeLimited"),
				atc_classifications,availability_type,biocomponents,biotherapeutic,
				black_box_warning,chebi_par_id,chirality,dosed_ingredient,first_approval,first_in_class,helm_notation,
				indication_class,inorganic_flag,max_phase,molecule_chembl_id,parent_chembl_id,acd_logd,acd_logp,
				acd_most_apka,acd_most_bpka,alogp,aromatic_rings,full_molformula,full_mwt,
				hba,hbd,heavy_atoms,med_chem_friendly,molecular_species,mw_freebase,mw_monoisotopic,num_alerts,num_ro5_violations,
				psa,qed_weighted,ro3_pass,rtb,
				canonical_smiles,standard_inchi,standard_inchi_key,molecule_synonyms,molecule_type,natural_product,oral,parenteral,polymer_flag,pref_name,
				prodrug,structure_type,therapeutic_flag,topical,usan_stem,usan_stem_definition,usan_substem,usan_year);

		Atom Molecule = new Atom(schema.getRelation("Molecule"),
				atc_classifications,availability_type,biocomponents,biotherapeutic,
				black_box_warning,chebi_par_id,chirality,dosed_ingredient,first_approval,first_in_class,helm_notation,
				indication_class,inorganic_flag,max_phase,molecule_chembl_id,parent_chembl_id,acd_logd,acd_logp,
				acd_most_apka,acd_most_bpka,alogp,aromatic_rings,full_molformula,full_mwt,
				hba,hbd,heavy_atoms,med_chem_friendly,molecular_species,mw_freebase,mw_monoisotopic,num_alerts,num_ro5_violations,
				psa,qed_weighted,ro3_pass,rtb,
				canonical_smiles,standard_inchi,standard_inchi_key,molecule_synonyms,molecule_type,natural_product,oral,parenteral,polymer_flag,pref_name,
				prodrug,structure_type,therapeutic_flag,topical,usan_stem,usan_stem_definition,usan_substem,usan_year);

		Atom TargetFree = new Atom(schema.getRelation("TargetFree"),
				organism,pref_name,species_group_flag,target_chembl_id,
				target_component_accession,target_component_id,target_component_type,target_type);

		Atom TargetLimited = new Atom(schema.getRelation("TargetLimited"),
				organism,pref_name,species_group_flag,target_chembl_id,
				target_component_accession,target_component_id,target_component_type,target_type);

		Atom Target = new Atom(schema.getRelation("Target"),
				organism,pref_name,species_group_flag,target_chembl_id,
				target_component_accession,target_component_id,target_component_type,target_type);

		Atom TargetComponentFree = new Atom(schema.getRelation("TargetComponentFree"),
				accession,component_id,component_type,description,organism,protein_classification_id,sequence,tax_id);

		Atom TargetComponentLimited = new Atom(schema.getRelation("TargetComponentLimited"),
				accession,component_id,component_type,description,organism,protein_classification_id,sequence,tax_id);

		Atom TargetComponent = new Atom(schema.getRelation("TargetComponent"),
				accession,component_id,component_type,description,organism,protein_classification_id,sequence,tax_id);

		Atom PublicationFull = new Atom(schema.getRelation("PublicationFull"),
				id,source,pmid,pmcid,DOI,title,authorString,journalTitle,issue,journalVolume,pubYear,
				journalIssn,pubType,inEPMC,inPMC,citedByCount,hasReferences,hasTextMinedTerms,hasDbCrossReferences,
				hasLabsLinks,hasTMAccessionNumbers,authors,gene,organism,uniprotPubs);

		Atom Publication = new Atom(schema.getRelation("Publication"),
				id,source,pmid,pmcid,DOI,title,authorString,journalTitle,issue,journalVolume,pubYear,
				journalIssn,pubType,inEPMC,inPMC,citedByCount,hasReferences,hasTextMinedTerms,hasDbCrossReferences,
				hasLabsLinks,hasTMAccessionNumbers,authors,gene,organism,uniprotPubs);

		Atom Citation = new Atom(schema.getRelation("Citation"),
				id,source,citationType,title,authorString,journalAbbreviation,pubYear,volume,issue,pageInfo,citedByCount,src,ext_id);

		Atom Reference = new Atom(schema.getRelation("Reference"),
				id,source,citationType,title,authorString,journalAbbreviation,pubYear,volume,issue,pageInfo,src,ext_id);

		Atom PathwayBySpecies = new Atom(schema.getRelation("PathwayBySpecies"),
				pathwayId,pathwayName,stableIdentifier,goBiologicalProcess,isInDisease,isInferred,organismId,organism,doi,hasDiagram);

		Atom PathwayById = new Atom(schema.getRelation("PathwayById"),
				pathwayId,pathwayName,stableIdentifier,goBiologicalProcess,isInDisease,isInferred,organismId,organism,doi,hasDiagram);

		Atom Pathway = new Atom(schema.getRelation("Pathway"),
				pathwayId,pathwayName,stableIdentifier,goBiologicalProcess,isInDisease,isInferred,organismId,organism,doi,hasDiagram);

		Atom OrganismById = new Atom(schema.getRelation("OrganismById"),
				organismId,organism);

		Atom OrganismFree = new Atom(schema.getRelation("OrganismFree"),
				organismId,organism);

		Atom Organism = new Atom(schema.getRelation("Organism"),
				organismId,organism);

		Atom ProteinLimited = new Atom(schema.getRelation("ProteinLimited"),
				input_id,id,entry_name,organism);

		Atom ProteinFree = new Atom(schema.getRelation("ProteinFree"),
				id,entry_name,organism);

		Atom Protein = new Atom(schema.getRelation("Protein"),
				input_id,id,entry_name,organism);

		TGD tgd1 = new TGD(Conjunction.of(Activity),Conjunction.of(ActivityFree));
		TGD tgd2 = new TGD(Conjunction.of(Activity),Conjunction.of(ActivityLimited));
		TGD tgd3 = new TGD(Conjunction.of(ActivityFree),Conjunction.of(Activity));
		TGD tgd4 = new TGD(Conjunction.of(ActivityLimited),Conjunction.of(Activity));

		TGD tgd5 = new TGD(Conjunction.of(Assay),Conjunction.of(AssayFree));
		TGD tgd6 = new TGD(Conjunction.of(Assay),Conjunction.of(AssayLimited));
		TGD tgd7 = new TGD(Conjunction.of(AssayFree),Conjunction.of(Assay));
		TGD tgd8 = new TGD(Conjunction.of(AssayLimited),Conjunction.of(Assay));

		TGD tgd9 = new TGD(Conjunction.of(Document),Conjunction.of(DocumentFree));
		TGD tgd10 = new TGD(Conjunction.of(Document),Conjunction.of(DocumentLimited));
		TGD tgd11 = new TGD(Conjunction.of(DocumentFree),Conjunction.of(Document));
		TGD tgd12 = new TGD(Conjunction.of(DocumentLimited),Conjunction.of(Document));

		TGD tgd13 = new TGD(Conjunction.of(Molecule),Conjunction.of(MoleculeFree));
		TGD tgd14 = new TGD(Conjunction.of(Molecule),Conjunction.of(MoleculeLimited));
		TGD tgd15 = new TGD(Conjunction.of(MoleculeFree),Conjunction.of(Molecule));
		TGD tgd16 = new TGD(Conjunction.of(MoleculeLimited),Conjunction.of(Molecule));

		TGD tgd17 = new TGD(Conjunction.of(Target),Conjunction.of(TargetFree));
		TGD tgd18 = new TGD(Conjunction.of(Target),Conjunction.of(TargetLimited));
		TGD tgd19 = new TGD(Conjunction.of(TargetFree),Conjunction.of(Target));
		TGD tgd20 = new TGD(Conjunction.of(TargetLimited),Conjunction.of(Target));

		TGD tgd21 = new TGD(Conjunction.of(TargetComponent),Conjunction.of(TargetComponentFree));
		TGD tgd22 = new TGD(Conjunction.of(TargetComponent),Conjunction.of(TargetComponentLimited));
		TGD tgd23 = new TGD(Conjunction.of(TargetComponentFree),Conjunction.of(TargetComponent));
		TGD tgd24 = new TGD(Conjunction.of(TargetComponentLimited),Conjunction.of(TargetComponent));

		TGD tgd25 = new TGD(Conjunction.of(Publication),Conjunction.of(PublicationFull));
		TGD tgd26 = new TGD(Conjunction.of(PublicationFull),Conjunction.of(Publication));

		TGD tgd27 = new TGD(Conjunction.of(Protein),Conjunction.of(ProteinFree));
		TGD tgd28 = new TGD(Conjunction.of(ProteinFree),Conjunction.of(Protein));
		TGD tgd29 = new TGD(Conjunction.of(Protein),Conjunction.of(ProteinLimited));
		TGD tgd30 = new TGD(Conjunction.of(ProteinLimited),Conjunction.of(Protein));

		TGD tgd31 = new TGD(Conjunction.of(Pathway),Conjunction.of(PathwayBySpecies));
		TGD tgd32 = new TGD(Conjunction.of(Pathway),Conjunction.of(PathwayById));
		TGD tgd33 = new TGD(Conjunction.of(PathwayBySpecies),Conjunction.of(Pathway));
		TGD tgd34 = new TGD(Conjunction.of(PathwayById),Conjunction.of(Pathway));

		TGD tgd35 = new TGD(Conjunction.of(Organism),Conjunction.of(OrganismById));
		TGD tgd36 = new TGD(Conjunction.of(Organism),Conjunction.of(OrganismFree));
		TGD tgd37 = new TGD(Conjunction.of(OrganismById),Conjunction.of(Organism));
		TGD tgd38 = new TGD(Conjunction.of(OrganismFree),Conjunction.of(Organism));

		TGD tgd39 = new TGD(Conjunction.of(Pathway),Conjunction.of(Organism));

		TGD tgd40 = new TGD(Conjunction.of(
				new Atom(schema.getRelation("Document"), 
						Lists.newArrayList(new Variable("_1"), 
								new TypedConstant<>("PUBLICATION"),
								new Variable("_3"),
								new Variable("_4"),
								new Variable("_5"),
								issue, 
								new Variable("_7"),
								new Variable("_8"), 
								ext_id, 
								new Variable("_10"),
								volume, 
								pubYear))
				),
				Conjunction.of(new Atom(schema.getRelation("Publication"), 
						Lists.newArrayList(ext_id,source,pmid,pmcid,DOI,title,authorString,
								journalTitle,issue,volume,pubYear,journalIssn,pubType,inEPMC,
								inPMC,citedByCount,hasReferences,hasTextMinedTerms,hasDbCrossReferences,
								hasLabsLinks,hasTMAccessionNumbers,
								new Variable("_11"),
								new Variable("_12"),
								new Variable("_13"),
								new Variable("_14")
								))));

		TGD tgd41 = new TGD(Conjunction.of(Activity),Conjunction.of(Molecule));
		TGD tgd42 = new TGD(Conjunction.of(Activity),Conjunction.of(Target));
		TGD tgd43 = new TGD(Conjunction.of(Activity),Conjunction.of(Document));
		TGD tgd44 = new TGD(Conjunction.of(Assay),Conjunction.of(Document));
		TGD tgd45 = new TGD(Conjunction.of(Assay),Conjunction.of(Target));

		TGD tgd46 = new TGD(Conjunction.of(
				new Atom(schema.getRelation("Target"), 
						Lists.newArrayList(organism,
								pref_name,
								species_group_flag,
								target_chembl_id,
								target_component_accession,
								target_component_id,
								target_component_type,
								new TypedConstant<>("SINGLE PROTEIN")))
				),
				Conjunction.of(new Atom(schema.getRelation("TargetComponent"), 
						Lists.newArrayList(
								target_component_accession,
								target_component_id,
								new TypedConstant<>("PROTEIN"),
								description,
								organism,
								protein_classification_id,
								sequence,
								tax_id
								))));


		TGD tgd47 = new TGD(Conjunction.of(
				new Atom(schema.getRelation("TargetComponent"), 
						Lists.newArrayList(
								target_component_accession,
								target_component_id,
								new TypedConstant<>("PROTEIN"),
								description,
								organism,
								protein_classification_id,
								sequence,
								tax_id))
				),
				Conjunction.of(new Atom(schema.getRelation("Protein"), 
						Lists.newArrayList(
								input_id,
								target_component_accession,
								entry_name,
								organism))
						));

		TGD tgd48 = new TGD(Conjunction.of(
				new Atom(schema.getRelation("Target"), 
						Lists.newArrayList(
								organism,
								pref_name,
								species_group_flag,
								target_chembl_id,
								target_component_accession,
								target_component_id,
								target_component_type,
								new TypedConstant<>("SINGLE PROTEIN")
								))
				),
				Conjunction.of(new Atom(schema.getRelation("Protein"), 
						Lists.newArrayList(
								input_id,
								target_component_accession,
								entry_name,
								organism))
						));

		TGD tgd49 = new TGD(Conjunction.of(
				Publication
				),
				Conjunction.of(new Atom(schema.getRelation("Protein"), 
						Lists.newArrayList(
								new Variable("_1"),
								uniprotPubs,
								new Variable("_3"),
								new Variable("_4")))
						));
		
		TGD tgd50 = new TGD(Conjunction.of(
				Citation
				),
				Conjunction.of(Publication
						));
		
		TGD tgd51 = new TGD(Conjunction.of(
				Reference
				),
				Conjunction.of(Publication
						));
		
		
		return Lists.<Dependency>newArrayList(tgd1,tgd2,tgd3,tgd4,tgd5,tgd6,tgd7,tgd8,tgd9,tgd10,
				tgd11,tgd12,tgd13,tgd14,tgd15,tgd16,tgd17,tgd18,tgd19,tgd20,
				tgd21,tgd22,tgd23,tgd24,tgd25,tgd26,tgd27,tgd28,tgd29,tgd30,
				tgd31,tgd32,tgd33,tgd34,tgd35,tgd36,tgd37,tgd38,tgd39,tgd40,
				tgd41,tgd42,tgd43,tgd44,tgd45,tgd46,tgd47,tgd48,tgd49,tgd50,
				tgd51);
	}


	/**
	 * Gets the schema.
	 *
	 * @return the schema
	 */
	public Schema getSchema() {
		return schema;
	}

}
