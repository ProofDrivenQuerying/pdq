--Find distinct documents
SELECT COUNT(DISTINCT(chembl_19.activities.doc_id)) FROM chembl_19.activities;
--Find distinct molecules
SELECT COUNT(DISTINCT(chembl_19.activities.record_id)) FROM chembl_19.activities;
--Find distinct targets
SELECT COUNT(DISTINCT(chembl_19.target_dictionary.tid)) FROM chembl_19.activities, chembl_19.assays, chembl_19.target_dictionary WHERE chembl_19.activities.assay_id = chembl_19.assays.assay_id AND chembl_19.assays.tid = chembl_19.target_dictionary.tid;
--Find distinct target organisms
SELECT COUNT(DISTINCT(chembl_19.target_dictionary.organism)) FROM chembl_19.activities, chembl_19.assays, chembl_19.target_dictionary WHERE chembl_19.activities.assay_id = chembl_19.assays.assay_id AND chembl_19.assays.tid = chembl_19.target_dictionary.tid;
--Find distinct assays
SELECT COUNT(DISTINCT(chembl_19.activities.assay_id)) FROM chembl_19.activities;

--Find distinct organisms, documents, targets
SELECT COUNT(DISTINCT(chembl_19.assays.assay_organism)) FROM chembl_19.assays;
SELECT COUNT(DISTINCT(chembl_19.assays.doc_id)) FROM chembl_19.assays;
SELECT COUNT(DISTINCT(chembl_19.assays.tid)) FROM chembl_19.assays;
SELECT COUNT(DISTINCT(chembl_19.assays.assay_tissue)) FROM chembl_19.assays;
SELECT COUNT(DISTINCT(chembl_19.assays.assay_type)) FROM chembl_19.assays;
SELECT COUNT(*) FROM chembl_19.assays WHERE chembl_19.assays.assay_type='F';
SELECT COUNT(DISTINCT(chembl_19.assays.relationship_type)) FROM chembl_19.assays;
SELECT COUNT(*) FROM chembl_19.assays WHERE chembl_19.assays.relationship_type='H';

SELECT COUNT(DISTINCT(chembl_19.docs.pubmed_id)) FROM chembl_19.docs;
SELECT COUNT(DISTINCT(chembl_19.docs.doc_type)) FROM chembl_19.docs;

SELECT COUNT(DISTINCT(chembl_19.target_dictionary.organism)) FROM chembl_19.target_dictionary;
SELECT COUNT(DISTINCT(chembl_19.target_dictionary.target_type)) FROM chembl_19.target_dictionary;
SELECT COUNT(*) FROM chembl_19.target_dictionary WHERE chembl_19.target_dictionary.target_type='SINGLE PROTEIN';

SELECT COUNT(DISTINCT(chembl_19.target_components.component_id)) FROM chembl_19.target_dictionary, chembl_19.target_components WHERE chembl_19.target_dictionary.tid = chembl_19.target_components.tid;

SELECT COUNT(DISTINCT(chembl_19.component_sequences.accession)) FROM chembl_19.target_dictionary, chembl_19.target_components, chembl_19.component_sequences WHERE  chembl_19.target_dictionary.tid = chembl_19.target_components.tid AND chembl_19.target_components.component_id=chembl_19.component_sequences.component_id;
SELECT COUNT(DISTINCT(chembl_19.component_sequences.component_id)) FROM chembl_19.component_sequences;
SELECT COUNT(DISTINCT(chembl_19.component_sequences.accession)) FROM chembl_19.component_sequences;
SELECT COUNT(DISTINCT(chembl_19.component_sequences.organism)) FROM chembl_19.component_sequences;

CREATE TABLE `uniprotkb`.`protein` (
  `Entry` VARCHAR(45) NOT NULL,
  `Entry_name` VARCHAR(100) NULL,
  `Status` VARCHAR(100) NULL,
  `Protein_names` VARCHAR(100) NULL,
  `Gene_names` VARCHAR(100) NULL,
  `Organism` VARCHAR(100) NULL,
  `Length` INT NULL,
  `Gene_names_primary` VARCHAR(100) NULL,
  `PubMed_ID` VARCHAR(45) NULL,
  PRIMARY KEY (`Entry`));

LOAD DATA LOCAL INFILE 'C:\\Users\\tsamoura\\Desktop\\Bioinformatics workflows\\UniProt\\uniprot-reviewed.tab'
INTO TABLE uniprotkb_swiss_prot.protein
FIELDS TERMINATED BY '\t'
LINES TERMINATED BY '\n'
(Entry,Entry_name,Status,Protein_names,Gene_names,Organism,Length); 

SELECT COUNT(DISTINCT(uniprotkb_swiss_prot.protein.Organism)) FROM uniprotkb_swiss_prot.protein;
SELECT COUNT(DISTINCT(uniprotkb.protein.Organism)) FROM uniprotkb.protein;
SELECT COUNT(DISTINCT(uniprotkb.protein.PubMed_ID)) FROM uniprotkb.protein WHERE uniprotkb.protein.PubMed_ID IS NOT NULL;
SELECT COUNT(DISTINCT(uniprotkb.protein.Gene_names_primary)) FROM uniprotkb.protein WHERE uniprotkb.protein.Gene_names_primary IS NOT NULL;
SELECT COUNT(DISTINCT(chembl_19.molecule_dictionary.max_phase)) FROM chembl_19.molecule_dictionary;

SELECT COUNT(DISTINCT(chembl_19.assay_type.assay_type)) FROM chembl_19.assay_type;
SELECT COUNT(DISTINCT(chembl_19.assays.relationship_type)) FROM chembl_19.assays;