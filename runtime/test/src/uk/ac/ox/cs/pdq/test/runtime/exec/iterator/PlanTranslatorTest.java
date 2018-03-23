package uk.ac.ox.cs.pdq.test.runtime.exec.iterator;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.junit.Assert;
import org.junit.Test;

import uk.ac.ox.cs.pdq.algebra.AccessTerm;
import uk.ac.ox.cs.pdq.algebra.DependentJoinTerm;
import uk.ac.ox.cs.pdq.algebra.RelationalTerm;
import uk.ac.ox.cs.pdq.algebra.RenameTerm;
import uk.ac.ox.cs.pdq.datasources.utility.Result;
import uk.ac.ox.cs.pdq.datasources.utility.Table;
import uk.ac.ox.cs.pdq.datasources.utility.Tuple;
import uk.ac.ox.cs.pdq.db.AccessMethod;
import uk.ac.ox.cs.pdq.db.Attribute;
import uk.ac.ox.cs.pdq.db.Relation;
import uk.ac.ox.cs.pdq.db.Schema;
import uk.ac.ox.cs.pdq.db.TypedConstant;
import uk.ac.ox.cs.pdq.fol.Atom;
import uk.ac.ox.cs.pdq.fol.Term;
import uk.ac.ox.cs.pdq.fol.Variable;
import uk.ac.ox.cs.pdq.runtime.EvaluationException;
import uk.ac.ox.cs.pdq.runtime.TimeoutException;
import uk.ac.ox.cs.pdq.runtime.exec.PipelinedPlanExecutor.TimeoutChecker;
import uk.ac.ox.cs.pdq.runtime.exec.PlanTranslator;
import uk.ac.ox.cs.pdq.runtime.exec.iterator.Distinct;
import uk.ac.ox.cs.pdq.runtime.exec.iterator.TupleIterator;

/**
 * Tests the translation from a RelationalTerm object to a TupleIterator plan.
 * 
 * @author gabor
 *
 */
public class PlanTranslatorTest {

	/**
	 * <Pre>
	 * DependentJoin{[(#4=#7)]
	 * 		DependentJoin{[(#0=#3)]
	 * 			Rename{[c1,c2,c3]
	 * 				Access{R0.mt_0[]}
	 * 			},
	 * 			Rename{[c1,c4,c5]
	 * 				Access{R1.mt_1[#0=a]}
	 * 			}
	 * 		},
	 * 		Rename{[c6,c4,c7]
	 * 			Access{R2.mt_2[#1=b]}
	 * 		}
	 * 	}
	 * </Pre>	 
	 * @throws EvaluationException 
	 */
	@Test
	public void testScenario1() throws EvaluationException {
		Schema s = getScenario1();
		/*R0*/
		RelationalTerm access1 = AccessTerm.create(s.getRelation("R0"),s.getRelation("R0").getAccessMethod("mt_0"));
		Attribute[] renamings1 = new Attribute[] {
				Attribute.create(String.class,"c1"),
				Attribute.create(String.class,"c2"),
				Attribute.create(String.class,"c3")
		};
		RelationalTerm rename1 = RenameTerm.create(renamings1,access1);
		
		/*R1*/
		Map<Integer, TypedConstant> inputConstants = new HashMap<>();
		//inputConstants.put(0, TypedConstant.create("a"));
		RelationalTerm access2 = AccessTerm.create(s.getRelation("R1"),s.getRelation("R1").getAccessMethod("mt_1"),inputConstants);
		Attribute[] renamings2 = new Attribute[] {
				Attribute.create(String.class,"c1"),
				Attribute.create(String.class,"c4"),
				Attribute.create(String.class,"c5")
		};
		RelationalTerm rename2 = RenameTerm.create(renamings2,access2);
		/*R2*/
		Map<Integer, TypedConstant> inputConstants2 = new HashMap<>();
		//inputConstants2.put(1, TypedConstant.create("b"));
		RelationalTerm access3 = AccessTerm.create(s.getRelation("R2"),s.getRelation("R2").getAccessMethod("mt_2"),inputConstants2);
		Attribute[] renamings3 = new Attribute[] {
				Attribute.create(String.class,"c6"),
				Attribute.create(String.class,"c4"),
				Attribute.create(String.class,"c7")
		};
		RelationalTerm rename3 = RenameTerm.create(renamings3,access3);
	
		/*Join*/
		RelationalTerm dep1 = DependentJoinTerm.create(rename1,rename2);
		RelationalTerm dep2 = DependentJoinTerm.create(dep1,rename3);
		
		/*translate*/
		Result results = execute(dep2);
		Assert.assertNotNull(results);
		
	}
	
	public Result execute(RelationalTerm logOp) throws EvaluationException {
		Table results;
		// Non-boolean query
		try (TupleIterator phyOp = PlanTranslator.translate(logOp)) {
			TupleIterator top = new Distinct(phyOp);
			results = new Table(phyOp.getOutputAttributes());

			ExecutorService execService = Executors.newFixedThreadPool(1);
			execService.execute(new TimeoutChecker(1000*60*5, top)); // 5 min
			top.open();
			while (top.hasNext()) {
				Tuple t = top.next();
				results.appendRow(t);
			}
			execService.shutdownNow();
			if (top.isInterrupted()) {
				throw new TimeoutException();
			}
		}
		return results;
	}
	
	protected Attribute a_s = Attribute.create(String.class, "a");
	protected Attribute b_s = Attribute.create(String.class, "b");
	protected Attribute c_s = Attribute.create(String.class, "c");
	protected Variable x = Variable.create("x");
	protected Variable y = Variable.create("y");
	protected Variable z = Variable.create("z");

	public Schema getScenario1() {
		// Create the relations
		Relation[] relations = new Relation[4];
		relations[0] = Relation.create("R0", new Attribute[] { this.a_s, this.b_s, this.c_s }, new AccessMethod[] { AccessMethod.create(new Integer[] {}) });
		relations[1] = Relation.create("R1", new Attribute[] { this.a_s, this.b_s, this.c_s }, new AccessMethod[] { AccessMethod.create(new Integer[] { 0 }) });
		relations[2] = Relation.create("R2", new Attribute[] { this.a_s, this.b_s, this.c_s }, new AccessMethod[] { AccessMethod.create(new Integer[] { 1 }) });
		relations[3] = Relation.create("Accessible", new Attribute[] { this.a_s });
		// Create query
		Atom[] atoms = new Atom[3];
		atoms[0] = Atom.create(relations[0], new Term[] { x, Variable.create("y1"), Variable.create("z1") });
		atoms[1] = Atom.create(relations[1], new Term[] { x, y, Variable.create("z2") });
		atoms[2] = Atom.create(relations[2], new Term[] { Variable.create("x1"), y, z });

		// Create schema
		return new Schema(relations);
	}

}
