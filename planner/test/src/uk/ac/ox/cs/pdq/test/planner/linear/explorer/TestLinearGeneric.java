package uk.ac.ox.cs.pdq.test.planner.linear.explorer;

import static org.mockito.Mockito.when;

import org.junit.Test;
import org.mockito.Mockito;

import uk.ac.ox.cs.pdq.algebra.RelationalTerm;
import uk.ac.ox.cs.pdq.cost.DoubleCost;
import uk.ac.ox.cs.pdq.cost.estimators.CostEstimator;
import uk.ac.ox.cs.pdq.db.Attribute;
import uk.ac.ox.cs.pdq.db.Relation;
import uk.ac.ox.cs.pdq.db.Schema;
import uk.ac.ox.cs.pdq.fol.ConjunctiveQuery;
import uk.ac.ox.cs.pdq.planner.linear.explorer.LinearGeneric;

public class TestLinearGeneric {

	protected Attribute a = Attribute.create(Integer.class, "a");
	protected Attribute b = Attribute.create(Integer.class, "b");
	protected Attribute c = Attribute.create(Integer.class, "c");
	protected Attribute d = Attribute.create(Integer.class, "d");
    
	protected Relation R;
	protected Relation S;	
	protected Relation T;	
	protected ConjunctiveQuery query;
	protected Schema schema;
	
	//Asserts that a 
	@Test public void test1() {
		//Create schema
		//Create accessible schema
		//Create query
		//Create accessible query
		
		//Mock the cost estimator 
		CostEstimator costEstimator = Mockito.mock(CostEstimator.class);
		when(costEstimator.cost(Mockito.any(RelationalTerm.class))).thenReturn(new DoubleCost(1.0));
				
		//Create nodeFactory
		
		//Create database connection
		
		//Create the chaser 
//		Chaser chaser = Mockito.mock(Chaser.class);
//		Mockito.doNothing().when(chaser).reasonUntilTermination(Mockito.any(), Mockito.any());
		
		//Create linear explorer
		LinearGeneric explorer = null;
		
		//Call the explorer for first time 
		
		//Verify the outputs  
		
		//Call the explorer for second time 
		
		//Verify the outputs  
		
		//Call the explorer for third time 
		
		//Verify the outputs  
	}
}
