package uk.ac.ox.cs.pdq.plan;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import uk.ac.ox.cs.pdq.algebra.DependentAccess;
import uk.ac.ox.cs.pdq.db.AccessMethod;
import uk.ac.ox.cs.pdq.db.AccessMethod.Types;
import uk.ac.ox.cs.pdq.db.Attribute;
import uk.ac.ox.cs.pdq.db.Relation;
import uk.ac.ox.cs.pdq.db.TGD;
import uk.ac.ox.cs.pdq.db.TypedConstant;
import uk.ac.ox.cs.pdq.fol.Term;
import uk.ac.ox.cs.pdq.util.Table;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

/**
 * 
 * @author Efthymia Tsamoura
 *
 */
public class AccessCommand implements Command{

	/** The accessed relation **/
	private final Relation relation;

	/** The method applied to access the corresponding relation**/
	private final AccessMethod method;

	/** The output columns */
	private final List<Term> columns;

	/** The constants used to call the underlying access method */
	private final Map<Integer, TypedConstant<?>> staticInputs;

	/** The input table**/
	private final Table input;

	/** The output table**/
	private final Table output;

	/** Caches the constraint that captures this access command **/
	private final TGD command;

	/**
	 * Creates an access command that takes as input the input table
	 * @param access
	 * @param input
	 */
	public AccessCommand(AccessOperator access, Table input) {
		this(access.getRelation(), access.getAccessMethod(), 
				access.getColumns(),
				input,
				access instanceof DependentAccess ? ((DependentAccess)access).getStaticInputs() : null);
	}

	/**
	 * 
	 * @param relation
	 * 		The accessed relation
	 * @param method
	 * 		The method applied to access the corresponding relation
	 * @param columns
	 * 		The output columns
	 * @param input
	 * 		The input table
	 * @param staticInputs
	 * 		Schema constants that are input positions
	 */
	public AccessCommand(Relation relation, AccessMethod method, List<Term> columns, Table input, Map<Integer, TypedConstant<?>> staticInputs) {
		Preconditions.checkNotNull(relation);
		Preconditions.checkNotNull(method);
		Preconditions.checkNotNull(columns);
		Preconditions.checkArgument(columns.size()==relation.getArity());
		if(method.getType().equals(Types.FREE)) { 
			Preconditions.checkArgument(input == null);
		}
		else {
			Preconditions.checkArgument(input != null);
			if(staticInputs == null) {
				Preconditions.checkArgument(method.getInputs().size() == input.getHeader().size());
			}
		}
		this.relation = relation;
		this.method = method;
		this.columns = columns;
		this.staticInputs = staticInputs;
		this.input = input;
		
		//Building the output table
		List<Attribute> outputs = Lists.newArrayList();
		for(int pos = 0; pos < relation.getArity(); ++pos) {
			outputs.add(new Attribute(relation.getAttribute(pos).getType(), columns.get(pos).toString()));
		}
		this.output = new Table(outputs);
		this.command = new CommandToTGDTranslator().toTGD(this);
	}

	public Table getInput() {
		return this.input;
	}

	public Table getOutput() {
		return this.output;
	}

	public Relation getRelation() {
		return this.relation;
	}

	public AccessMethod getMethod() {
		return this.method;
	}

	public Map<Integer, TypedConstant<?>> getStaticInputs() {
		return this.staticInputs;
	}

	public List<Term> getColumns() {
		return this.columns;
	}

	/**
	 * @param o Object
	 * @return boolean
	 */
	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null) {
			return false;
		}
		return AccessCommand.class.isInstance(o)
				&& this.relation.equals(((AccessCommand) o).relation)
				&& this.method.equals(((AccessCommand) o).method)
				&& this.columns.equals(((AccessCommand) o).columns)
				&& this.staticInputs.equals(((AccessCommand) o).staticInputs);
	}

	/**
	 * @return int
	 */
	@Override
	public int hashCode() {
		return Objects.hash(this.relation, this.method, this.columns, this.staticInputs);
	}

	/**
	 * @return String
	 */
	@Override
	public String toString() {
		return this.command.toString();
	}


}
