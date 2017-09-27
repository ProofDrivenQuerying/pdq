package uk.ac.ox.cs.pdq.test;

import org.junit.Before;
import org.mockito.MockitoAnnotations;

import uk.ac.ox.cs.pdq.db.AccessMethod;
import uk.ac.ox.cs.pdq.db.Attribute;
import uk.ac.ox.cs.pdq.db.Relation;
import uk.ac.ox.cs.pdq.util.GlobalCounterProvider;
import uk.ac.ox.cs.pdq.util.Utility;

public class PdqTest {
	protected AccessMethod method0 = AccessMethod.create(new Integer[]{});
	protected AccessMethod method1 = AccessMethod.create(new Integer[]{0});
	protected AccessMethod method2 = AccessMethod.create(new Integer[]{0,1});
	protected AccessMethod method3 = AccessMethod.create(new Integer[]{1});

    protected Attribute a = Attribute.create(Integer.class, "a");
    protected Attribute b = Attribute.create(Integer.class, "b");
    protected Attribute c = Attribute.create(Integer.class, "c");
    protected Attribute d = Attribute.create(Integer.class, "d");
    
	protected Relation R= Relation.create("R", new Attribute[]{a,b,c}, new AccessMethod[]{this.method0, this.method2});
	protected Relation S=Relation.create("S", new Attribute[]{b,c}, new AccessMethod[]{this.method0, this.method1, this.method2});	
    
	/**
	 * Setup.
	 */
	@Before 
	public void setup() {
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
