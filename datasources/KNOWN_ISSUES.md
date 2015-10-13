Issues with web services 
	-	ChEBI limits the amount of output data. Need to find out in which order are the elements returned.
	-	Rhea has ambiguous text search. Need to find out in which order are the elements returned.   
	-	Cannot find out how a pathway in Reactome is linked to ChEBI and Uniprot
	-	For entry CHEBI:15377 the web service call http://www.ebi.ac.uk/webservices/chebi/2.0/test/getCompleteEntity?chebiId= CHEBI:15377 returns different cross references from what the HTML interface returns  
	-	For entry RHEA:43144 the web service call www.ebi.ac.uk/rhea/rest/1.0/ws/reaction/cmlreact/43144 returns different cross references from what the HTML interface returns  
	-	Do not know how to get the list of cross-references for a protein when using the tab output format in Uniprot 
	-	Do not know with which input ID the service https://www.ebi.ac.uk/chembl/api/data/atc_class can be called 
	-	The json return format of Uniprot does not return the protein sequence 
	-	Do not understand what information is returned by the MoleculeForm service 
	-	Do not understand how BindingSite is associated with the other tables 
	-	In Reactome.Pathways the DOI are different from the ones in PubMed 
	-	In Chembl.Document the data is corrupted. Although it should be an inclusion dependency to Pubmed.Publication 
		the data in Chembl.Document is not correct  

Issues with wrappers source code 
	-	No support for web services that return attributes consisting of a list of elements 
	-	Each service description is coupled to a relation. Thus, different web service descriptions cannot return the same relation
	-	Cannot parse XML responses where elements have one or more attributes. Cannot use the XML output format for Uniprot at the moment
	-	No support for plugging in service clients 

Comments about linking 
	-	UniProt links to ChEMBL, KEGG, Reactome, SABIO-RK, PubMed
	-	Reactome is linked to ChEBI, Uniprot, PubMed
	-	ChEBI is linked to *
	-	Rhea is linked to ChEBI, KEGG, Reactome, Uniprot, PubMed
	-	ChEBML links to PubMed via the document relation, to UniProt via the Target relation. “Organism” field in Target relation is common with the organism field in Reactome. 
	-	PubMed links to UniProt, ChEMBL, ChEBI. We can search by “Organism” and by Gene proteins  

Comments about UniProt tab-separated column format
	-	citation: PubMed identifiers separated by ;
	-	ec: Enzyme commission numbers separated by ;  
	-	id: Entry id
	-	entry name: Unique entry name
	-	existence: Protein existence. Single value witch takes the possible values {Evidence at protein level, Evidence at transcript level, Inferred from homology, Predicted, Uncertain}
	-	families: Protein families separated by ,
	-	genes: The first entry returned corresponds to the preferred gene name 
	-	interactor: A single protein that interacts with 
	-	last-modified: Last modification dates 
	-	length: Single value
	-	organism: The source organism. Format: Latin name (English name). English name is optional
	-	organism-id: Id of the source organism in a taxonomic database 
	-	protein names: Protein names. The recommended one appears first and the alternative names are enclosed into parenthesis 
	-	reviewed: Possible values {Reviewed, Unreviewed}
	-	sequence: The protein sequence 
	-	version: Entry version
