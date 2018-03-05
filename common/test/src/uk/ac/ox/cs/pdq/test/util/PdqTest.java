package uk.ac.ox.cs.pdq.test.util;

import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.runners.MethodSorters;
import org.mockito.MockitoAnnotations;

import uk.ac.ox.cs.pdq.algebra.AccessTerm;
import uk.ac.ox.cs.pdq.algebra.DependentJoinTerm;
import uk.ac.ox.cs.pdq.algebra.RelationalTerm;
import uk.ac.ox.cs.pdq.db.AccessMethod;
import uk.ac.ox.cs.pdq.db.Attribute;
import uk.ac.ox.cs.pdq.db.Relation;
import uk.ac.ox.cs.pdq.db.Schema;
import uk.ac.ox.cs.pdq.db.TypedConstant;
import uk.ac.ox.cs.pdq.fol.Atom;
import uk.ac.ox.cs.pdq.fol.ConjunctiveQuery;
import uk.ac.ox.cs.pdq.fol.Dependency;
import uk.ac.ox.cs.pdq.fol.EGD;
import uk.ac.ox.cs.pdq.fol.Predicate;
import uk.ac.ox.cs.pdq.fol.TGD;
import uk.ac.ox.cs.pdq.fol.Term;
import uk.ac.ox.cs.pdq.fol.Variable;
import uk.ac.ox.cs.pdq.util.GlobalCounterProvider;
import uk.ac.ox.cs.pdq.util.Utility;

/**
 * Creates the most commonly used objects for testing purposes. Also able to
 * provide full test scenarios with schemas and queries.
 * 
 * @author Gabor
 *
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class PdqTest {

	/* example variables */
	protected Variable x = Variable.create("x");
	protected Variable y = Variable.create("y");
	protected Variable z = Variable.create("z");
	protected Variable w = Variable.create("w");

	/* example access methods */
	protected AccessMethod method0 = AccessMethod.create(new Integer[] {});
	protected AccessMethod method1 = AccessMethod.create(new Integer[] { 0 });
	protected AccessMethod method2 = AccessMethod.create(new Integer[] { 0, 1 });
	protected AccessMethod method3 = AccessMethod.create(new Integer[] { 1 });

	/* example attributes */
	protected Attribute a = Attribute.create(Integer.class, "a");
	protected Attribute b = Attribute.create(Integer.class, "b");
	protected Attribute c = Attribute.create(Integer.class, "c");
	protected Attribute d = Attribute.create(Integer.class, "d");
	protected Attribute i = Attribute.create(String.class, "i");

	/* same as a,b,c,d but with String attribute type */
	protected Attribute a_s = Attribute.create(String.class, "a");
	protected Attribute b_s = Attribute.create(String.class, "b");
	protected Attribute c_s = Attribute.create(String.class, "c");
	protected Attribute d_s = Attribute.create(String.class, "d");

	protected Attribute at11 = Attribute.create(String.class, "at11");
	protected Attribute at12 = Attribute.create(String.class, "at12");
	protected Attribute at13 = Attribute.create(String.class, "at13");

	protected Attribute at21 = Attribute.create(String.class, "at21");
	protected Attribute at22 = Attribute.create(String.class, "at22");

	protected Attribute at31 = Attribute.create(String.class, "at31");
	protected Attribute at32 = Attribute.create(String.class, "at32");

	/* example relations */
	protected Relation R = Relation.create("R", new Attribute[] { a, b, c }, new AccessMethod[] { this.method0, this.method2 });
	protected Relation S = Relation.create("S", new Attribute[] { b, c }, new AccessMethod[] { this.method0, this.method1, this.method2 });
	protected Relation T = Relation.create("T", new Attribute[] { b, c, d }, new AccessMethod[] { this.method0, this.method1, this.method2 });
	/* same as R,S,T relations but with instanceID */
	protected Relation Ri = Relation.create("R", new Attribute[] { a, b, c }, new AccessMethod[] { this.method0, this.method2 });
	protected Relation Si = Relation.create("S", new Attribute[] { b, c }, new AccessMethod[] { this.method0, this.method1, this.method2 });
	protected Relation Ti = Relation.create("T", new Attribute[] { b, c, d }, new AccessMethod[] { this.method0, this.method1, this.method2 });

	/* same as the Ri,Si,Ri tables but with string attribute types. */
	protected Relation R_s = Relation.create("R", new Attribute[] { a_s, b_s, c_s }, new AccessMethod[] { this.method0, this.method2 });
	protected Relation S_s = Relation.create("S", new Attribute[] { b_s, c_s }, new AccessMethod[] { this.method0, this.method1, this.method2 });
	protected Relation T_s = Relation.create("T", new Attribute[] { b_s, c_s, d_s }, new AccessMethod[] { this.method0, this.method1, this.method2 });

	protected Relation access = Relation.create("Accessible", new Attribute[] { i });

	protected Relation rel1 = Relation.create("R1", new Attribute[] { at11, at12, at13 });
	protected Relation rel2 = Relation.create("R2", new Attribute[] { at21, at22 }, new AccessMethod[] { this.method0, this.method2 });
	protected Relation rel3 = Relation.create("R3", new Attribute[] { at31, at32 });

	/* example atoms */
	protected Atom a1 = Atom.create(this.rel1, new Term[] { Variable.create("x"), Variable.create("y"), Variable.create("z") });
	protected Atom a2 = Atom.create(this.rel2, new Term[] { Variable.create("y"), Variable.create("z") });
	protected Atom a3 = Atom.create(this.rel2, new Term[] { Variable.create("y"), Variable.create("w") });
	protected Atom a4 = Atom.create(this.rel3, new Term[] { Variable.create("y"), Variable.create("w") });

	/* example dependencies */
	protected TGD tgd = TGD.create(new Atom[] { a1 }, new Atom[] { a2 });
	protected TGD tgd2 = TGD.create(new Atom[] { a1 }, new Atom[] { a4 });
	protected EGD egd = EGD.create(new Atom[] { a2, a3 }, new Atom[] { Atom.create(Predicate.create("EQUALITY", 2, true), Variable.create("z"), Variable.create("w")) });

	/* example schemas */
	protected Schema testSchema1 = new Schema(new Relation[] { this.rel1, this.rel2, this.rel3 }, new Dependency[] { this.tgd, this.tgd2, this.egd });

	public PdqTest() {
	}

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
		//DatabaseChaseInstance.resetFacts(); // can't be added here since DatabaseChaseInstance is not part of common, but it often needs to be reseted too. 				
	}

	/**
	 * <pre>
	 * Schema has 3 relations : R0(a,b,c) where a,b,c are integer attributes, with a
	 * free access method <br>
	 * R1(a,b,c) where a,b,c are integer attributes, with one access method that
	 * needs input on the second attribute<br>
	 * R2(a,b,c) where a,b,c are integer attributes, with one access method that
	 * needs input on the third attribute.<br>
	 * In this scenario there are no dependencies. <br>
	 * The query is Q(x,y,z) = R0(x,y1,z1) R1(x,y,z2) R2(x1,y,z)
	 * 
	 * Chasing this should provide a valid plan. like this:
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
	 * </pre>
	 */
	public TestScenario getScenario1() {
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
		ConjunctiveQuery query = ConjunctiveQuery.create(new Variable[] { x, y, z }, atoms);

		// Create schema
		Schema schema = new Schema(relations);
		TestScenario ts = new TestScenario();
		ts.setSchema(schema);
		ts.setQuery(query);
		
		Atom[] facts = new Atom[] {
				Atom.create(relations[0], new Term[] { TypedConstant.create(12), TypedConstant.create(31), TypedConstant.create(100)}), 
				Atom.create(relations[0], new Term[] { TypedConstant.create(13), TypedConstant.create(32), TypedConstant.create(101)}), 
				Atom.create(relations[0], new Term[] { TypedConstant.create(14), TypedConstant.create(33), TypedConstant.create(102)}), 
				Atom.create(relations[0], new Term[] { TypedConstant.create(15), TypedConstant.create(34), TypedConstant.create(103)}), 

				Atom.create(relations[1], new Term[] { TypedConstant.create(12), TypedConstant.create(31), TypedConstant.create(200)}), 
				Atom.create(relations[1], new Term[] { TypedConstant.create(13), TypedConstant.create(32), TypedConstant.create(201)}), 
				Atom.create(relations[1], new Term[] { TypedConstant.create(14), TypedConstant.create(33), TypedConstant.create(202)}), 
				Atom.create(relations[1], new Term[] { TypedConstant.create(15), TypedConstant.create(34), TypedConstant.create(203)}), 

				Atom.create(relations[2], new Term[] { TypedConstant.create(12), TypedConstant.create(31), TypedConstant.create(300)}), 
				Atom.create(relations[2], new Term[] { TypedConstant.create(13), TypedConstant.create(32), TypedConstant.create(301)}), 
				Atom.create(relations[2], new Term[] { TypedConstant.create(14), TypedConstant.create(33), TypedConstant.create(302)}), 
				Atom.create(relations[2], new Term[] { TypedConstant.create(15), TypedConstant.create(34), TypedConstant.create(303)}), 
		};
		ts.setExampleAtoms1(Arrays.asList(facts));
		return ts;
	}

	/**
	 * Scenario2 has different access methods, but the relations and the query
	 * otherwise are the same as in scenario1.
	 * 
	 * Schema has 3 relations : R0(a,b,c) where a,b,c are integer attributes, with
	 * one access method that needs input on the first attribute<br>
	 * R1(a,b,c) where a,b,c are integer attributes, with one access method that
	 * needs input on the first attribute<br>
	 * R2(a,b,c) where a,b,c are integer attributes, with one access method that
	 * needs input on the second attribute<br>
	 * In this scenario there are no dependencies. <br>
	 * The query is Q(x,y,z) = R0(x,y1,z1) R1(x,y,z2) R2(x1,y,z)
	 * 
	 * Chasing this should NOT provide any plan.
	 */
	public TestScenario getScenario2() {
		// Create the relations
		Relation[] relations = new Relation[4];
		relations[0] = Relation.create("R0", new Attribute[] { this.a_s, this.b_s, this.c_s }, new AccessMethod[] { AccessMethod.create(new Integer[] { 0 }) });
		relations[1] = Relation.create("R1", new Attribute[] { this.a_s, this.b_s, this.c_s }, new AccessMethod[] { AccessMethod.create(new Integer[] { 0 }) });
		relations[2] = Relation.create("R2", new Attribute[] { this.a_s, this.b_s, this.c_s }, new AccessMethod[] { AccessMethod.create(new Integer[] { 1 }) });
		relations[3] = Relation.create("Accessible", new Attribute[] { this.a_s });

		// Create query
		Atom[] atoms = new Atom[3];
		atoms[0] = Atom.create(relations[0], new Term[] { x, Variable.create("y1"), Variable.create("z1") });
		atoms[1] = Atom.create(relations[1], new Term[] { x, y, Variable.create("z2") });
		atoms[2] = Atom.create(relations[2], new Term[] { Variable.create("x1"), y, z });
		ConjunctiveQuery query = ConjunctiveQuery.create(new Variable[] { x, y, z }, atoms);

		// Create schema
		Schema schema = new Schema(relations);
		TestScenario ts = new TestScenario();
		ts.setSchema(schema);
		ts.setQuery(query);
		return ts;
	}

	/**
	 * <pre>
	 * Scenario3 has different access methods, and a modified query. It has multiple
	 * valid plans.
	 * 
	 * Schema has 3 relations : R0(a,b,c) where a,b,c are integer attributes, with
	 * one access method with free access<br>
	 * R1(a,b,c) where a,b,c are integer attributes, with two access method, one
	 * needs input on the first attribute, the other on the third attribute<br>
	 * R2(a,b,c) where a,b,c are integer attributes, with one access method that
	 * needs input on the second attribute<br>
	 * In this scenario there are no dependencies. <br>
	 * 
	 * The query is: 
	 * Q(x,y,z) = R0(x,y1,z1) R1(x,y,5) R2(x1,y,z)
	 * 
	 * Chasing this should provide more then one plan, for example with DAG there should be 8 valid plans, 
	 * similar to this one:
	 * Asserts that we found 8 plans, similar to this one:
	 * DependentJoin{[(#4=#7)]
	 *  	DependentJoin{[(#0=#3)]
	 *   		Rename{[c0,c1,c2]
	 *   			Access{R0.mt_0[]}},
	 *   		Select{[(#2=5)]
	 *   			Rename{[c0,c3,5]
	 *   				Access{R1.mt_1[#0=a]}}}},
	 *   	Rename{[c4,c3,c5]
	 *   		Access{R2.mt_3[#1=b]}}}
	 * cost = 1.0
	 * </pre>
	 */
	public TestScenario getScenario3() {
		// Create the relations
		Relation[] relations = new Relation[4];
		relations[0] = Relation.create("R0", new Attribute[] { this.a_s, this.b_s, this.c_s }, new AccessMethod[] { AccessMethod.create(new Integer[] {}) });
		relations[1] = Relation.create("R1", new Attribute[] { this.a_s, this.b_s, this.c_s },
				new AccessMethod[] { AccessMethod.create(new Integer[] { 0 }), AccessMethod.create(new Integer[] { 2 }) });
		relations[2] = Relation.create("R2", new Attribute[] { this.a_s, this.b_s, this.c_s }, new AccessMethod[] { AccessMethod.create(new Integer[] { 1 }) });
		relations[3] = Relation.create("Accessible", new Attribute[] { this.a_s });

		// Create query
		Atom[] atoms = new Atom[3];
		atoms[0] = Atom.create(relations[0], new Term[] { x, Variable.create("y1"), Variable.create("z1") });
		atoms[1] = Atom.create(relations[1], new Term[] { x, y, TypedConstant.create("five") });
		atoms[2] = Atom.create(relations[2], new Term[] { Variable.create("x1"), y, z });
		ConjunctiveQuery query = ConjunctiveQuery.create(new Variable[] { x, y, z }, atoms);

		// Create schema
		Schema schema = new Schema(relations);
		TestScenario ts = new TestScenario();
		ts.setSchema(schema);
		ts.setQuery(query);
		return ts;
	}

	/**
	 * <pre>
	 * Tables:
	 *	R0(a,b,c,d) accesses: Free, [0]
	 *	R1(a,b,c,d) accesses: [0], [2,3]
	 * 	R2(a,b,c,d) accesses: Free
	 *	R3(a,b,c,d) accesses: [2,3]
	 * Query:
	 * Q(x,y) -> R0(x,y,z,w) R1(_,_,z,w) R2(x,y,z',w') R3(_,_,z',w')
	 * </pre>
	 * 
	 * @return
	 */
	public TestScenario getScenario4() {
		// Create the relations
		Relation[] relations = new Relation[5];
		relations[0] = Relation.create("R0", new Attribute[] { this.a_s, this.b_s, this.c_s, this.d_s },
				new AccessMethod[] { AccessMethod.create(new Integer[] {}), AccessMethod.create(new Integer[] { 0 }) });
		relations[1] = Relation.create("R1", new Attribute[] { this.a_s, this.b_s, this.c_s, this.d_s },
				new AccessMethod[] { AccessMethod.create(new Integer[] { 0 }), AccessMethod.create(new Integer[] { 2, 3 }) });

		relations[2] = Relation.create("R2", new Attribute[] { this.a_s, this.b_s, this.c_s, this.d_s }, new AccessMethod[] { AccessMethod.create(new Integer[] {}) });
		relations[3] = Relation.create("R3", new Attribute[] { this.a_s, this.b_s, this.c_s, this.d_s },
				new AccessMethod[] { AccessMethod.create(new Integer[] { 2, 3 }) });
		relations[4] = Relation.create("Accessible", new Attribute[] { this.a_s });
		// Create query
		// R0(x,y,z,w) R1(_,_,z,w) R2(x,y,z',w') R3(_,_,z',w')
		Atom[] atoms = new Atom[4];
		atoms[0] = Atom.create(relations[0], new Term[] { x, y, z, w });
		atoms[1] = Atom.create(relations[1], new Term[] { Variable.create("x2"), Variable.create("y2"), z, w });
		atoms[2] = Atom.create(relations[2], new Term[] { x, y, Variable.create("z3"), Variable.create("w3") });
		atoms[3] = Atom.create(relations[3], new Term[] { Variable.create("x4"), Variable.create("y4"), Variable.create("z3"), Variable.create("w3") });
		ConjunctiveQuery query = ConjunctiveQuery.create(new Variable[] { x, y }, atoms);

		// Create schema
		Schema schema = new Schema(relations);
		TestScenario ts = new TestScenario();
		ts.setSchema(schema);
		ts.setQuery(query);
		return ts;
	}

	/**
	 * <pre>
	 * Tables:
	 *	R0(a,b,c,d) accesses: Free
	 *	R1(a,b,c,d) accesses: [0], [2,3]
	 * 	R2(a,b,c,d) accesses: Free
	 *	R3(a,b,c,d) accesses: [2,3]
	 * Query:
	 *  Q(x,y) -> R0(x,y,z,w) R1(_,_,z,w) R2(x,y,z',w') R3(_,_,z',w') where "_" means some unique variable.
	 * </pre>
	 * For example with DAG explorer in this test scenario there should be 30 valid plans similar to this:
	 * <pre>
	 * Join{[(#0=#8&#1=#9)]
	 * 		DependentJoin{[(#2=#6&#3=#7)]
	 * 			Rename{[c1,c2,c7,c8]Access{R2.mt_3[]}},
	 * 			Rename{[c9,c10,c7,c8]Access{R3.mt_4[#2=c,#3=d]}}},
	 * 		DependentJoin{[(#2=#6&#3=#7)]
	 * 			Rename{[c1,c2,c3,c4]Access{R0.mt_0[]}},
	 * 			Rename{[c5,c6,c3,c4]Access{R1.mt_2[#2=c,#3=d]}}}}
	 * </pre> 
	 * @return
	 */
	public TestScenario getScenario5() {
		// Create the relations
		Relation[] relations = new Relation[5];
		relations[0] = Relation.create("R0", new Attribute[] { this.a_s, this.b_s, this.c_s, this.d_s }, new AccessMethod[] { AccessMethod.create(new Integer[] {}) });
		relations[1] = Relation.create("R1", new Attribute[] { this.a_s, this.b_s, this.c_s, this.d_s },
				new AccessMethod[] { AccessMethod.create(new Integer[] { 0 }), AccessMethod.create(new Integer[] { 2, 3 }) });

		relations[2] = Relation.create("R2", new Attribute[] { this.a_s, this.b_s, this.c_s, this.d_s }, new AccessMethod[] { AccessMethod.create(new Integer[] {}) });
		relations[3] = Relation.create("R3", new Attribute[] { this.a_s, this.b_s, this.c_s, this.d_s },
				new AccessMethod[] { AccessMethod.create(new Integer[] { 2, 3 }) });
		relations[4] = Relation.create("Accessible", new Attribute[] { this.a_s });
		// Create query
		// R0('constant1',y,z,w) R1('constant2',_,z,w) R2(x,y,z',w') R3(_,_,z',w')
		Atom[] atoms = new Atom[4];
		atoms[0] = Atom.create(relations[0], new Term[] { x, y, z, w });
		atoms[1] = Atom.create(relations[1], new Term[] { Variable.create("x13"), Variable.create("y12"), z, w });
		atoms[2] = Atom.create(relations[2], new Term[] { x, y, Variable.create("z3"), Variable.create("w3") });
		atoms[3] = Atom.create(relations[3], new Term[] { Variable.create("x14"), Variable.create("y14"), Variable.create("z3"), Variable.create("w3") });
		ConjunctiveQuery query = ConjunctiveQuery.create(new Variable[] { x, y }, atoms);

		// Create schema
		Schema schema = new Schema(relations);
		TestScenario ts = new TestScenario();
		ts.setSchema(schema);
		ts.setQuery(query);
		return ts;
	}

	/**
	 * <pre>
	 * Tables:
	 *	R0(a,b,c,d) accesses: Free
	 *	R1(a,b,c,d) accesses: [2,3]
	 * 	R2(a,b,c,d) accesses: Free
	 *	R3(a,b,c,d) accesses: [2,3]
	 * Query:
	 *  Q(x,y) -> R0(x,y,z,w) R1(_,_,z,w) R2(x,y,z',w') R3(_,_,z',w') where "_" means some unique variable.
	 * </pre>
	 * 
	 * @return
	 */
	public TestScenario getScenario6() {
		// Create the relations
		Relation[] relations = new Relation[5];
		relations[0] = Relation.create("R0", new Attribute[] { this.a_s, this.b_s, this.c_s, this.d_s }, new AccessMethod[] { AccessMethod.create(new Integer[] {}) });
		relations[1] = Relation.create("R1", new Attribute[] { this.a_s, this.b_s, this.c_s, this.d_s },
				new AccessMethod[] { AccessMethod.create(new Integer[] { 2, 3 }) });

		relations[2] = Relation.create("R2", new Attribute[] { this.a_s, this.b_s, this.c_s, this.d_s }, new AccessMethod[] { AccessMethod.create(new Integer[] {}) });
		relations[3] = Relation.create("R3", new Attribute[] { this.a_s, this.b_s, this.c_s, this.d_s },
				new AccessMethod[] { AccessMethod.create(new Integer[] { 2, 3 }) });
		relations[4] = Relation.create("Accessible", new Attribute[] { this.a_s });
		// Create query
		// R0('constant1',y,z,w) R1('constant2',_,z,w) R2(x,y,z',w') R3(_,_,z',w')
		Atom[] atoms = new Atom[4];
		atoms[0] = Atom.create(relations[0], new Term[] { x, y, z, w });
		atoms[1] = Atom.create(relations[1], new Term[] { Variable.create("x13"), Variable.create("y12"), z, w });
		atoms[2] = Atom.create(relations[2], new Term[] { x, y, Variable.create("z3"), Variable.create("w3") });
		atoms[3] = Atom.create(relations[3], new Term[] { Variable.create("x14"), Variable.create("y14"), Variable.create("z3"), Variable.create("w3") });
		ConjunctiveQuery query = ConjunctiveQuery.create(new Variable[] { x, y }, atoms);

		// Create schema
		Schema schema = new Schema(relations);
		TestScenario ts = new TestScenario();
		ts.setSchema(schema);
		ts.setQuery(query);
		return ts;
	}

	/**
	 * The query is Q(x,z) = \exists y R0(x,y) R1(y,z) We also have the dependencies
	 * R0(x,y) -> R2(x,y) R1(y,z) -> R3(y,z) R2(x,y), R3(y,z) -> R0(x,w) R1(w,z)
	 * Every relation has a free access.
	 * 
	 * We should find at least the following plans R0(x,y) R1(y,z) R3(x,y) R0(x,y)
	 * R1(y,z) R4(y,z) R0(x,y) R1(y,z) R3(x,y) R4(y,z) R4(y,z) R3(x,y) R4(y,z)
	 * R3(x,y) R0(x,y) R1(y,z)
	 */
	public TestScenario getStandardScenario1() {
		// Create the relations
		Relation[] relations = new Relation[5];
		relations[0] = Relation.create("R0", new Attribute[] { this.a_s, this.b_s }, new AccessMethod[] { AccessMethod.create(new Integer[] {}) });
		relations[1] = Relation.create("R1", new Attribute[] { this.a_s, this.b_s }, new AccessMethod[] { AccessMethod.create(new Integer[] {}) });
		relations[2] = Relation.create("R2", new Attribute[] { this.a_s, this.b_s }, new AccessMethod[] { AccessMethod.create(new Integer[] {}) });
		relations[3] = Relation.create("R3", new Attribute[] { this.a_s, this.b_s }, new AccessMethod[] { AccessMethod.create(new Integer[] {}) });
		relations[4] = Relation.create("Accessible", new Attribute[] { this.a_s });
		// Create query
		Atom[] atoms = new Atom[2];
		atoms[0] = Atom.create(relations[0], new Term[] { x, y });
		atoms[1] = Atom.create(relations[1], new Term[] { y, z });
		ConjunctiveQuery query = ConjunctiveQuery.create(new Variable[] { x, z }, atoms);
		Dependency dependency1 = TGD.create(new Atom[] { Atom.create(relations[0], new Term[] { x, y }) }, new Atom[] { Atom.create(relations[2], new Term[] { x, y }) });
		Dependency dependency2 = TGD.create(new Atom[] { Atom.create(relations[1], new Term[] { y, z }) }, new Atom[] { Atom.create(relations[3], new Term[] { y, z }) });
		// R2(x,y), R3(y,z) -> R0(x,w) R1(w,z)
		Dependency dependency3 = TGD.create(new Atom[] { Atom.create(relations[2], new Term[] { y, z }), Atom.create(relations[3], new Term[] { y, z }) },
				new Atom[] { Atom.create(relations[0], new Term[] { x, w }), Atom.create(relations[1], new Term[] { w, z }) });
		// Create schema
		Schema schema = new Schema(relations, new Dependency[] { dependency1, dependency2, dependency3 });

		TestScenario ts = new TestScenario();
		ts.setSchema(schema);
		ts.setQuery(query);
		return ts;
	}

	/**
	 * Describes most of the necessary inputs for a test scenario.
	 * 
	 * @author Gabor
	 *
	 */
	public class TestScenario {
		protected Schema schema;
		protected ConjunctiveQuery query;
		protected List<Atom> exampleAtoms1;
		protected List<Atom> exampleAtoms2;
		protected List<Atom> exampleAtoms3;

		public TestScenario() {

		}

		public Schema getSchema() {
			return schema;
		}

		public void setSchema(Schema schema) {
			this.schema = schema;
		}

		public ConjunctiveQuery getQuery() {
			return query;
		}

		public void setQuery(ConjunctiveQuery query) {
			this.query = query;
		}

		public List<Atom> getExampleAtoms1() {
			return exampleAtoms1;
		}

		public void setExampleAtoms1(List<Atom> exampleAtoms1) {
			this.exampleAtoms1 = exampleAtoms1;
		}

		public List<Atom> getExampleAtoms2() {
			return exampleAtoms2;
		}

		public void setExampleAtoms2(List<Atom> exampleAtoms2) {
			this.exampleAtoms2 = exampleAtoms2;
		}

		public List<Atom> getExampleAtoms3() {
			return exampleAtoms3;
		}

		public void setExampleAtoms3(List<Atom> exampleAtoms3) {
			this.exampleAtoms3 = exampleAtoms3;
		}

	}

	/**
	 * Utility function to check if a plan contains any child with the type access
	 * term. Throws Assertion error in case - the given relationalTerm is null - the
	 * given relationalTerm have no children - the given relationalTerm have only
	 * one children - the given relational term does not have any AccessTerm
	 * children.
	 * 
	 * The completed check is not recursive, it only cecks the immediate children.
	 * 
	 * @param relationalTerm
	 *            the root of the plan tree to check for accessTerm child.
	 */
	public static void AssertHasAccessTermChild(RelationalTerm relationalTerm) {
		Assert.assertNotNull(relationalTerm);
		Assert.assertNotNull(relationalTerm.getChildren());
		Assert.assertEquals(1, relationalTerm.getChildren().length);
		Assert.assertTrue(relationalTerm.getChild(0) instanceof AccessTerm);
	}

	/**
	 * Counts the number of DependentJoinTerm objects in a plan recursively.
	 * 
	 * @param plan
	 *            The input plan for the counting.
	 * @return the number of occurrences of DependentJoinTerm objects in the tree.
	 *         The top level (root) relational term is also checked and counted if
	 *         it is a DependentJoinTerm.
	 */
	public int countDependentJoinsInPlan(RelationalTerm plan) {
		int ret=0;
		for (int i=0;i < plan.getChildren().length; ret += countDependentJoinsInPlan(plan.getChildren()[i++]));
		return ret + ((plan instanceof DependentJoinTerm)?1:0);
	}

}
