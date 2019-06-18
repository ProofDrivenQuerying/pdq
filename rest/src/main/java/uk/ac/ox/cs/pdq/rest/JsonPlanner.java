package uk.ac.ox.cs.pdq.rest;

import uk.ac.ox.cs.pdq.fol.ConjunctiveQuery;
import java.io.File;
import uk.ac.ox.cs.pdq.db.Schema;
import java.util.Map.Entry;
import uk.ac.ox.cs.pdq.reasoning.ReasoningParameters;
import uk.ac.ox.cs.pdq.planner.ExplorationSetUp;
import uk.ac.ox.cs.pdq.reasoningdatabase.DatabaseParameters;
import uk.ac.ox.cs.pdq.cost.CostParameters;
import uk.ac.ox.cs.pdq.algebra.RelationalTerm;
import uk.ac.ox.cs.pdq.cost.Cost;
import java.util.Map.Entry;
import uk.ac.ox.cs.pdq.planner.PlannerParameters;




public class JsonPlanner{
  public static Entry<RelationalTerm, Cost> plan(Schema schema, ConjunctiveQuery query, File properties){
    PlannerParameters planParams = properties != null ?
      new PlannerParameters(properties) :
      new PlannerParameters() ;
    CostParameters costParams = properties != null ?
      new CostParameters(properties) :
      new CostParameters() ;

    ReasoningParameters reasoningParams = properties != null ?
      new ReasoningParameters(properties) :
      new ReasoningParameters() ;

    DatabaseParameters dbParams = properties != null ?
      new DatabaseParameters(properties) :
      DatabaseParameters.Postgres ;

    Entry<RelationalTerm, Cost> entry = null;

    ExplorationSetUp planner = new ExplorationSetUp(planParams, costParams, reasoningParams, dbParams, schema);
    try{

      entry = planner.search(query);
      
    }catch (Throwable e) {
      e.printStackTrace();
      System.exit(-1);
    }
    return entry;
  }
}
