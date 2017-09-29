package uk.ac.ox.cs.pdq.test;

import org.junit.Before;
import org.mockito.MockitoAnnotations;

import uk.ac.ox.cs.pdq.db.AccessMethod;
import uk.ac.ox.cs.pdq.db.Attribute;
import uk.ac.ox.cs.pdq.db.Relation;
import uk.ac.ox.cs.pdq.db.Schema;
import uk.ac.ox.cs.pdq.fol.Atom;
import uk.ac.ox.cs.pdq.fol.Dependency;
import uk.ac.ox.cs.pdq.fol.EGD;
import uk.ac.ox.cs.pdq.fol.Predicate;
import uk.ac.ox.cs.pdq.fol.TGD;
import uk.ac.ox.cs.pdq.fol.Term;
import uk.ac.ox.cs.pdq.fol.Variable;
import uk.ac.ox.cs.pdq.util.GlobalCounterProvider;
import uk.ac.ox.cs.pdq.util.Utility;

public class PdqTest {
	/* example access methods */
	protected AccessMethod method0 = AccessMethod.create(new Integer[]{});
	protected AccessMethod method1 = AccessMethod.create(new Integer[]{0});
	protected AccessMethod method2 = AccessMethod.create(new Integer[]{0,1});
	protected AccessMethod method3 = AccessMethod.create(new Integer[]{1});

	/* example attributes */
    protected Attribute a = Attribute.create(Integer.class, "a");
    protected Attribute b = Attribute.create(Integer.class, "b");
    protected Attribute c = Attribute.create(Integer.class, "c");
    protected Attribute d = Attribute.create(Integer.class, "d");
    protected Attribute instanceID = Attribute.create(Integer.class, "InstanceID");
    
    protected Attribute at11 = Attribute.create(String.class, "at11");
    protected Attribute at12 = Attribute.create(String.class, "at12");
    protected Attribute at13 = Attribute.create(String.class, "at13");
	
    protected Attribute at21 = Attribute.create(String.class, "at21");
    protected Attribute at22 = Attribute.create(String.class, "at22");
	
    protected Attribute at31 = Attribute.create(String.class, "at31");
    protected Attribute at32 = Attribute.create(String.class, "at32");
    
	/* example relations */
	protected Relation R= Relation.create("R", new Attribute[]{a,b,c}, new AccessMethod[]{this.method0, this.method2});
	protected Relation S=Relation.create("S", new Attribute[]{b,c}, new AccessMethod[]{this.method0, this.method1, this.method2});	
	
	protected Relation rel1 = Relation.create("R1", new Attribute[]{at11, at12, at13,instanceID});
	protected Relation rel2 = Relation.create("R2", new Attribute[]{at21, at22,instanceID});
	protected Relation rel3 = Relation.create("R3", new Attribute[]{at31, at32,instanceID});
	
	/* example atoms */
	protected Atom a1 = Atom.create(this.rel1, new Term[]{Variable.create("x"),Variable.create("y"),Variable.create("z")});
	protected Atom a2 = Atom.create(this.rel2, new Term[]{Variable.create("y"),Variable.create("z")});
	protected Atom a3 = Atom.create(this.rel2, new Term[]{Variable.create("y"),Variable.create("w")});
	protected Atom a4 = Atom.create(this.rel3, new Term[]{Variable.create("y"),Variable.create("w")});
	
	/* example dependencies */
	protected TGD tgd = TGD.create(new Atom[]{a1},new Atom[]{a2});
	protected TGD tgd2 = TGD.create(new Atom[]{a1},new Atom[]{a4});	
	protected EGD egd = EGD.create(new Atom[]{a2,a3}, new Atom[]{Atom.create(Predicate.create("EQUALITY", 2, true), 
			Variable.create("z"),Variable.create("w"))});

	/* example schemas */ 
	protected Schema testSchema1 = new Schema(new Relation[]{this.rel1, this.rel2, this.rel3}, new Dependency[]{this.tgd,this.tgd2, this.egd});
	
	/**
	 * Setup.
	 */
	@Before 
	public void setup() throws Exception {
		PdqTest.reInitalize(this);     
	}
	
	public static void reInitalize(Object o) {
		Utility.assertsEnabled();
		if (o != null)
			MockitoAnnotations.initMocks(o);
        GlobalCounterProvider.resetCounters();
        uk.ac.ox.cs.pdq.fol.Cache.reStartCaches();
        uk.ac.ox.cs.pdq.fol.Cache.reStartCaches();
        uk.ac.ox.cs.pdq.fol.Cache.reStartCaches();
	}
}
