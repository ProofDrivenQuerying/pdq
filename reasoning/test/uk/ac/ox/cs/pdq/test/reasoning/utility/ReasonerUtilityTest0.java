package uk.ac.ox.cs.pdq.test.reasoning.utility;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Collection;

import org.apache.commons.collections4.CollectionUtils;
import org.junit.Before;

import uk.ac.ox.cs.pdq.db.Attribute;
import uk.ac.ox.cs.pdq.db.Constraint;
import uk.ac.ox.cs.pdq.db.EGD;
import uk.ac.ox.cs.pdq.db.Relation;
import uk.ac.ox.cs.pdq.db.Schema;
import uk.ac.ox.cs.pdq.db.TGD;
import uk.ac.ox.cs.pdq.io.xml.SchemaReader;
import uk.ac.ox.cs.pdq.plan.CommandToTGDTranslator;
import uk.ac.ox.cs.pdq.plan.NormalisedPlan;
import uk.ac.ox.cs.pdq.reasoning.HomomorphismException;
import uk.ac.ox.cs.pdq.reasoning.ReasoningParameters.HomomorphismDetectorTypes;
import uk.ac.ox.cs.pdq.reasoning.chase.EGDChaser;
import uk.ac.ox.cs.pdq.reasoning.chase.RestrictedChaser;
import uk.ac.ox.cs.pdq.reasoning.homomorphism.DBHomomorphismManager;
import uk.ac.ox.cs.pdq.reasoning.homomorphism.HomomorphismDetector;
import uk.ac.ox.cs.pdq.reasoning.homomorphism.HomomorphismManagerFactory;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
/**
 * 
 * @author Efthymia Tsamoura
 *
 */
public abstract class ReasonerUtilityTest0 {

	protected String driver = null;
	protected String url = "jdbc:mysql://localhost/";
	protected String database = "pdq_chase";
	protected String username = "root";
	protected String password ="root";
	
	protected Schema schema;
	protected Collection<EGD> keyDependencies;
	protected HomomorphismDetector detector;
	protected NormalisedPlan plan;
	protected Collection<Constraint> constraints;
	
	protected final RestrictedChaser restrictedChaser = new RestrictedChaser(null);
	protected final EGDChaser egdChaser = new EGDChaser(null);
	
	@Before
	protected void loadSchema() throws FileNotFoundException, IOException {
		
		String PATH = "C:/Users/tsamoura/workspace2/dev4.benchmark/local/bio/queries/schema2/DAG/CONSTRAINT_CARDINALITY";
		String schemaPath = "/case_008b/schema.xml";

		try(FileInputStream sis = new FileInputStream(PATH + schemaPath);) {

			//Load query and schema
			this.schema = new SchemaReader().read(sis);
			if (this.schema == null) {
				throw new IllegalStateException("Schema and query must be provided.");
			}
			
			//Define keys 
			Relation activityFree = this.schema.getRelation("ActivityFree");
			Attribute activity_id = activityFree.getAttribute(1);
			activityFree.setKey(Lists.newArrayList(activity_id));
			Relation activityLimited = this.schema.getRelation("ActivityLimited");
			activity_id = activityLimited.getAttribute(1);
			activityLimited.setKey(Lists.newArrayList(activity_id));
			
			Relation AssayFree = this.schema.getRelation("AssayFree");
			Attribute assay_chembl_id = AssayFree.getAttribute(2);
			AssayFree.setKey(Lists.newArrayList(assay_chembl_id));
			Relation AssayLimited = this.schema.getRelation("AssayLimited");
			assay_chembl_id = AssayLimited.getAttribute(2);
			AssayLimited.setKey(Lists.newArrayList(assay_chembl_id));
			
			Relation DocumentFree = this.schema.getRelation("DocumentFree");
			Attribute document_chembl_id = DocumentFree.getAttribute(2);
			DocumentFree.setKey(Lists.newArrayList(document_chembl_id));
			Relation DocumentLimited = this.schema.getRelation("DocumentLimited");
			document_chembl_id = DocumentLimited.getAttribute(2);
			DocumentLimited.setKey(Lists.newArrayList(document_chembl_id));
			
			Relation MoleculeFree = this.schema.getRelation("MoleculeFree");
			Attribute molecule_chembl_id = MoleculeFree.getAttribute(14);
			MoleculeFree.setKey(Lists.newArrayList(molecule_chembl_id));
			Relation MoleculeLimited = this.schema.getRelation("MoleculeLimited");
			molecule_chembl_id = MoleculeLimited.getAttribute(14);
			MoleculeLimited.setKey(Lists.newArrayList(molecule_chembl_id));
			
			Relation TargetFree = this.schema.getRelation("TargetFree");
			Attribute target_chembl_id = TargetFree.getAttribute(3);
			TargetFree.setKey(Lists.newArrayList(target_chembl_id));
			Relation TargetLimited = this.schema.getRelation("TargetLimited");
			target_chembl_id = TargetLimited.getAttribute(3);
			TargetLimited.setKey(Lists.newArrayList(target_chembl_id));
			
			Relation TargetComponentFree = this.schema.getRelation("TargetComponentFree");
			Attribute component_id = TargetComponentFree.getAttribute(1);
			TargetComponentFree.setKey(Lists.newArrayList(component_id));
			Relation TargetComponentLimited = this.schema.getRelation("TargetComponentLimited");
			component_id = TargetComponentLimited.getAttribute(1);
			TargetComponentLimited.setKey(Lists.newArrayList(component_id));
			
			Relation PublicationFull = this.schema.getRelation("PublicationFull");
			Attribute id = PublicationFull.getAttribute(0);
			PublicationFull.setKey(Lists.newArrayList(id));
			
			Relation Citation = this.schema.getRelation("Citation");
			id = Citation.getAttribute(0);
			Citation.setKey(Lists.newArrayList(id));
			
			Relation Reference = this.schema.getRelation("Reference");
			id = Reference.getAttribute(0);
			Reference.setKey(Lists.newArrayList(id));
			
			Relation PathwayBySpecies = this.schema.getRelation("PathwayBySpecies");
			Attribute pathwayId = PathwayBySpecies.getAttribute(0);
			PathwayBySpecies.setKey(Lists.newArrayList(pathwayId));
			
			Relation PathwayById = this.schema.getRelation("PathwayById");
			pathwayId = PathwayById.getAttribute(0);
			PathwayById.setKey(Lists.newArrayList(pathwayId));
			
			Relation OrganismById = this.schema.getRelation("OrganismById");
			Attribute organismId = OrganismById.getAttribute(0);
			OrganismById.setKey(Lists.newArrayList(organismId));
			
			Relation OrganismFree = this.schema.getRelation("OrganismFree");
			organismId = OrganismFree.getAttribute(0);
			OrganismFree.setKey(Lists.newArrayList(organismId));
			
			Relation ProteinLimited = this.schema.getRelation("ProteinLimited");
			id = ProteinLimited.getAttribute(1);
			ProteinLimited.setKey(Lists.newArrayList(id));
			
			Relation ProteinFree = this.schema.getRelation("ProteinFree");
			id = ProteinFree.getAttribute(0);
			ProteinFree.setKey(Lists.newArrayList(id));
			
			this.keyDependencies = Lists.newArrayList();
			for(Relation relation:this.schema.getRelations()) {
				if(!relation.getKey().isEmpty()) {
					this.keyDependencies.add(EGD.getEGDs(relation, relation.getKey()));
				}
			}
		}
	}
		
	@Before
	protected abstract void loadPlan();	
	
	@Before
	protected void loadDetector() {
		try {
			this.detector = new HomomorphismManagerFactory().getInstance(
					this.schema, 
					null, 
					HomomorphismDetectorTypes.DATABASE,
					this.driver,
					this.url,
					this.database,
					this.username,			
					this.password);
			((DBHomomorphismManager) this.detector).consolidateBaseTables(this.plan.getTables());
		} catch (HomomorphismException | SQLException e) {
			e.printStackTrace();
		}
	}
	
	@Before
	protected void loadConstraints() {
		//Get the tgds from the input commands;
		Collection<TGD> forwardTgds = new CommandToTGDTranslator().toTGD(this.plan);
		Collection<TGD> backwardTgds = Sets.newLinkedHashSet();
		for(TGD tgd:forwardTgds) {
			backwardTgds.add(tgd.invert());
		}
		this.constraints = CollectionUtils.union(CollectionUtils.union(this.schema.getDependencies(), forwardTgds), backwardTgds);
	}	
}
