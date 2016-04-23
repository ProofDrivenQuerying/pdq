package uk.ac.ox.cs.pdq.cardinality.estimator;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.collections4.CollectionUtils;
import org.gnu.glpk.GLPK;
import org.gnu.glpk.GLPKConstants;
import org.gnu.glpk.GlpkException;
import org.gnu.glpk.SWIGTYPE_p_double;
import org.gnu.glpk.SWIGTYPE_p_int;
import org.gnu.glpk.glp_prob;
import org.gnu.glpk.glp_smcp;

import uk.ac.ox.cs.pdq.cardinality.dag.DAGAnnotatedPlan;
import uk.ac.ox.cs.pdq.cardinality.dag.UnaryAnnotatedPlan;
import uk.ac.ox.cs.pdq.cost.statistics.Catalog;
import uk.ac.ox.cs.pdq.db.Attribute;
import uk.ac.ox.cs.pdq.db.Relation;
import uk.ac.ox.cs.pdq.db.TypedConstant;
import uk.ac.ox.cs.pdq.fol.Atom;
import uk.ac.ox.cs.pdq.fol.Conjunction;
import uk.ac.ox.cs.pdq.fol.ConjunctiveQuery;
import uk.ac.ox.cs.pdq.fol.Constant;
import uk.ac.ox.cs.pdq.fol.Predicate;
import uk.ac.ox.cs.pdq.fol.Skolem;
import uk.ac.ox.cs.pdq.fol.Term;
import uk.ac.ox.cs.pdq.fol.Variable;
import uk.ac.ox.cs.pdq.util.Utility;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * Estimates the AGM bound of a conjunctive query.
 * Consider a conjunctive query with variables $X_1 \ldots X_n$, free variables $X_1 \ldots X_k$ and atoms $R_1 \ldots R_m$.
 * Let $N_i$ be the number of tuples in $R_i$.
 * Consider the following optimization problem.

	\medskip
	{\bf Mimimize:} $\Sigma_{i \leq m}  ~ x_i \cdot \log_2 N_i$
	\medskip

	Subject to constraints:

	\begin{itemize}
	\item For each fixed $j \leq  k$ the constraint
	\[
	 \Sigma_{ i \leq m, ~ X_j \in R_i} ~ x_i \geq 1
	\]
	\item $x_i \geq 0$
	\end{itemize}
	Above $X_j \in R_i$ indicates that variable $X_j$ appears in $R_i$.
	Then, the so-called ``AGM bound'' states that the query result size is  bounded by $2^o$, where $o$ is a solution to the optimization problem above.
 * 
 * This implementation uses the GNU Linear Programming Kit Java Binding for solving linear programs. 
 * @author Efthymia Tsamoura
 *
 */
public class AGMBound {

	/**
	 * 
	 * @param query
	 * 		A conjunctive query
	 * @param catalog
	 * 		Provides base statistics 
	 * @return
	 * 		the AGM bound of the input query
	 */
	public static BigInteger estimate(ConjunctiveQuery query, Catalog catalog) {
		
		/** The linear program that we will solve
		 * All GLPK API routines deal with so called problem object, which is a program object of type
		glp_prob and intended to represent a particular LP or MIP instance.**/
		glp_prob lp;
		/** The control parameters of the simplex solver**/
		glp_smcp parm;
		/** Arrays. They are used to set up the coefficients of the variables in the constraints of the linear program **/
		
		int ret = 0;
		try {
			//Create the LP
			lp = GLPK.glp_create_prob();
			GLPK.glp_set_prob_name(lp, "myProblem");
			
			/**
			 * 	Define the variables of the LP.
				The variables of the LP will be the atoms of the input query. 
				We add additional atoms to the input query if we know the sizes of specific projections. 
			 */		
			Map<Atom, BigInteger> queryAtoms = AGMBound.queryAtomsForLP(query, catalog);
			GLPK.glp_add_cols(lp, queryAtoms.size());
			int i = 1; 
			for(Entry<Atom, BigInteger> entry:queryAtoms.entrySet()) {
				Atom atom = entry.getKey();
				GLPK.glp_set_col_name(lp, i, atom.getPredicate().getName());
				GLPK.glp_set_col_kind(lp, i, GLPKConstants.GLP_CV);
				GLPK.glp_set_col_bnds(lp, i, GLPKConstants.GLP_LO, 0, 0);
				++i;
			}
			
			//Create constraints
			//Add one constraint for each free variable of the query
			GLPK.glp_add_rows(lp, query.getFree().size());
			
			//Create the coefficients for the variables in each constraint
			i = 1;
			for(Term freeVariable:query.getFree()) {				
				//Add the constraint
				GLPK.glp_set_row_name (lp , i, "row" + i);
				GLPK.glp_set_row_bnds (lp , i, GLPKConstants.GLP_LO, 1, 1);
				//Set up the columns at specific positions
				SWIGTYPE_p_int ind = GLPK.new_intArray(queryAtoms.size());
				//Holds the coefficients of the columns for each constraint
				SWIGTYPE_p_double val = GLPK.new_doubleArray(queryAtoms.size());
				//Find the query atoms where this free variable appears;
				int position = 1;
				int column = 1;
				int lowest_column = Integer.MAX_VALUE;
				for(Entry<Atom, BigInteger> entry:queryAtoms.entrySet()) {
					if(entry.getKey().getTerms().contains(freeVariable)) {
						//Put the column variable at position 
						GLPK.intArray_setitem(ind, position, column);
						//Put the value of variable at column equal to 1 
						GLPK.doubleArray_setitem(val, position, 1);
						if(lowest_column > column) {
							lowest_column = column;
						}
						position++;
					}
					column++;
				}
				GLPK.glp_set_mat_row(lp, i, position-1, ind, val);
				GLPK.delete_intArray(ind);
				GLPK.delete_doubleArray(val);
				++i;
			}
			
			//Define objective
			GLPK.glp_set_obj_name(lp, "z");
			GLPK.glp_set_obj_dir(lp, GLPKConstants.GLP_MIN);
			int j = 1; 
			GLPK.glp_set_obj_coef(lp, 0, 0);
			for(Entry<Atom, BigInteger> entry:queryAtoms.entrySet()) {
				//The coefficients of the variables in the objective function are the log2 sizes
				double log = Math.log(entry.getValue().doubleValue())/Math.log(2);
				GLPK.glp_set_obj_coef(lp, j, log);
				++j;
			}
			
			//Solve model
			parm = new glp_smcp();
			//Print out the LP
			//GLPK.glp_write_lp(lp, null, "linear_program.txt");
	
			GLPK.glp_init_smcp(parm);
			ret = GLPK.glp_simplex(lp, parm);
			
			//Retrieve solution. The cardinality estimate is 2^{Solution of LP}
			if (ret == 0) {
				double solution = GLPK.glp_get_obj_val(lp);
				// Free memory
				GLPK.glp_delete_prob(lp);
				return new BigDecimal(Math.pow(2.0, solution)).toBigInteger();
			} else {	
				// Free memory
				GLPK.glp_delete_prob(lp);
			}
		} catch (GlpkException ex) {
			ex.printStackTrace();
		}
		return null;
	}
	
	/**
	 * Adds extra atoms to the input query if we know the sizes of specific projections.
	 * The procedure of adding query atoms is described below:
	 * Suppose we know the cardinality of some projection. E.g.  say we know that
	 * the one-dimensional projection $R_i[j_i]$ has cardinality $N^{j_i}_i$.  We can
	 * just create a new unary relation  called $R_i[j_i]$ (whose associated cardinality is $N^{j_i}_i$).
	 * Then for every atom with $R_i$ having a variable $X$ in position $j_i$, we add an atom $R_i[j_i](X)$ to the query.
	 * This method adds predicates only for projections that include at least one of the free variables of the input query.
	 * If we don't know the size of a query atom then we approximate it using the independence assumption. 
	 * @param query
	 * 		A conjunctive query
	 * @param catalog
	 * 		Provides base statistics 
	 * @return
	 * 		the atoms of the augmented query along with their cardinality
	 */
	public static LinkedHashMap<Atom, BigInteger> queryAtomsForLP(ConjunctiveQuery query, Catalog catalog) {
		
		//The atoms of the augmented query along with their cardinality
		LinkedHashMap<Atom, BigInteger> map = Maps.newLinkedHashMap();
		int signature = 0;
		//for each atom in the query body
		for(Atom fact:query.getBody()) {
			Collection<Term> freeVariables = CollectionUtils.intersection(fact.getTerms(), query.getFree());
			if(!freeVariables.isEmpty()) {
				
				//Estimate its size 
				//Use the the independence assumption if it contains more than one schema constants 
				//and if the catalog does not provide us with the size of this conjunction
				BigInteger sizeSelectivity = CardinalityUtility.sizeOf(fact, catalog);
				map.put(fact,sizeSelectivity);			
				
				//If this atom contains at least one free variable and if we know the size of a projection 
				//on a subset S of the free variables then add a new query atom with variables S.
				for(Term term:freeVariables) {
					//Get the relation attribute that maps to this variable
					Relation relation = (Relation)fact.getPredicate();
					//Find the positions where this variable appears
					List<Integer> positions = Utility.search(fact.getTerms(), term);
					Preconditions.checkArgument(!positions.isEmpty());
					//For each appearing position
					for(Integer i:positions) {
						//Get the corresponding attribute
						//and ask the catalog for the size of its projection
						Attribute attribute = relation.getAttribute(i);
						//TODO it should return null if the estimate does not exist
						int size = catalog.getCardinality(relation, attribute);
						//Create a new atom for this projection
						Atom p = new Atom(new Predicate("S" + signature++, 1), term);
						//TODO this needs serious attention
						double multiple = BigInteger.valueOf(size).doubleValue() * sizeSelectivity.doubleValue();
						Preconditions.checkArgument(map.put(p, new BigDecimal(multiple).toBigInteger())==null);
					}
				}
				
				
				//If this atom contains at least one free variable and if it contains more than one schema constants 
				//and if we know the size of the projection on any of these constants 
				//then add a new query atom.
				List<TypedConstant<?>> schemaConstants = fact.getSchemaConstantsList();				
				if(schemaConstants.size() > 1) {
					//Find the positions of all the schema constants
					List<Integer> indices = Lists.newArrayList();
					for(TypedConstant<?> schemaConstant:schemaConstants) {
						List<Integer> positions = Utility.search(fact.getTerms(), schemaConstant);
						Preconditions.checkArgument(!positions.isEmpty());
						indices.addAll(indices);
					}					
					for(int i = 0; i < indices.size(); ++i) {
						//Get the schema constant on the i-th position
						TypedConstant<?> schemaConstant = schemaConstants.get(indices.get(i));
						//Get the relation attribute that maps to this schema constant
						Relation relation = (Relation)fact.getPredicate();
						//Get the corresponding attribute
						//and ask the catalog for the size of its projection
						Attribute attribute = relation.getAttribute(indices.get(i));
						//TODO it should return null if the estimate does not exist
						int size = catalog.getSize(relation, attribute, schemaConstant);
						//TODO Create a new atom for this projection
					}
				}
			}
		}
		return map;
	}

	/**
	 * Transforms an annotated plan to a conjunctive query and computes its AGM bound.
	 * @param configuration
	 * @param catalog
	 * 		Provides base statistics 
	 * @return
	 * 		the AGM bound of the input annotated plan
	 */
	public static BigInteger estimate(DAGAnnotatedPlan configuration, Catalog catalog) {
		return AGMBound.estimate(AGMBound.transform(configuration), catalog);
	}

	/**
	 * 
	 * @param configuration
	 * 		An input binary or unary or binary annotated plan
	 * @return
	 * 		a conjunctive query with conjuncts the predicates P of the unary annotated subplans of configuration and
	 * 		variables the chase constants of P. All variables are treated as free variables.  
	 */
	public static ConjunctiveQuery transform(DAGAnnotatedPlan configuration) {
		List<Constant> constants = Lists.newArrayList();
		for(UnaryAnnotatedPlan unary:configuration.getUnaryAnnotatedPlans()) {
			constants.addAll(unary.getExportedConstants());
		}
		return AGMBound.transform(configuration, constants);
	}
	
	/**
	 * 
	 * @param configuration
	 * 		An input binary or unary or binary annotated plan
	 * @param constants
	 * 		the constants which will map to free variables for the output query
	 * @return
	 * 	 	a conjunctive query with conjuncts the predicates P of the unary annotated subplans of configuration and
	 * 		variables the chase constants of P. 
	 */
	public static ConjunctiveQuery transform(DAGAnnotatedPlan configuration, List<Constant> constants) {
		//The atoms in the query body
		List<Atom> atoms = Lists.newArrayList();
		//Maps each chase constant to a fresh variable
		Map<Term,Term> map = Maps.newLinkedHashMap();
		
		for(UnaryAnnotatedPlan unary:configuration.getUnaryAnnotatedPlans()) {
			//The terms of the newly created query atom
			List<Term> newTerms = Lists.newArrayList();
			for(Term constant:unary.getFact().getTerms()) {
				if(!map.containsKey(constant)) {
					if(constant instanceof TypedConstant) {
						map.put(constant, constant);
					}
					else {
						map.put(constant, new Variable(((Skolem)constant).getName()));
					}
				}
				newTerms.add(map.get(constant));

			}
			atoms.add(new Atom(unary.getRelation(), newTerms));
		}
		
		//The head terms of the newly created query atom
		List<Term> headTerms = Lists.newArrayList();
		for(Constant term:constants) {
			headTerms.add(map.get(term));
		}
		return new ConjunctiveQuery(new Atom(new Predicate("Q", constants.size()), headTerms), Conjunction.of(atoms));
	}
}
