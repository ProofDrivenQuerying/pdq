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

// TODO: Auto-generated Javadoc
/**
 *  An access command over a schema $\aschema$ with access methods is of the form 
	\[
	T \Leftarrow_{\outmap} \mt \Leftarrow_{\inmap} E
	\]
	
	where: \begin{inparaenum}
	\item  $E$ is a relational
	algebra expression $E$ over some set of relations not in $\aschema$
	(``temporary
	tables'' henceforward)
	\item $\mt$ is a method from $\aschema$ on some relation $R$
	\item  $\inmap$, the \emph{input mapping} of the command, is a partial function from 
	the output attributes of $E$ onto the input positions of $\mt$
	\item $T$, the \emph{output table} of the command, is a temporary table
	\item  $\outmap$, the \emph{output mapping} of the command,  is a bijection from  positions of $R$ to attributes  of $T$.
	\end{inparaenum}
	Note that an access method may have an empty collection of inputs positions.
	In such a case, the corresponding access is defined over the empty binding, and an
	access command using the method must take  the empty relation algebra expression $\emptyset$ as
	input.
 * 
 * @author Efthymia Tsamoura
 *
 */
public class AccessCommand implements Command{

	/**  The accessed relation *. */
	private final Relation relation;

	/**  The method applied to access the corresponding relation*. */
	private final AccessMethod method;

	/**  The output columns. */
	private final List<Term> columns;

	/**  The constants used to call the underlying access method. */
	private final Map<Integer, TypedConstant<?>> staticInputs;

	/**  The input table*. */
	private final Table input;

	/**  The output table*. */
	private final Table output;

	/**
	 * Creates an access command that takes as input the input table.
	 *
	 * @param access the access
	 * @param input the input
	 */
	public AccessCommand(AccessOperator access, Table input) {
		this(access.getRelation(), access.getAccessMethod(), 
				access.getColumns(),
				input,
				access instanceof DependentAccess ? ((DependentAccess)access).getStaticInputs() : null);
	}

	/**
	 * Instantiates a new access command.
	 *
	 * @param relation 		The accessed relation
	 * @param method 		The method applied to access the corresponding relation
	 * @param columns 		The output columns
	 * @param input 		The input table
	 * @param staticInputs 		Schema constants that are input positions
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
	}

	/**
	 * Gets the input.
	 *
	 * @return the input
	 */
	public Table getInput() {
		return this.input;
	}

	/* (non-Javadoc)
	 * @see uk.ac.ox.cs.pdq.plan.Command#getOutput()
	 */
	public Table getOutput() {
		return this.output;
	}

	/**
	 * Gets the relation.
	 *
	 * @return the relation
	 */
	public Relation getRelation() {
		return this.relation;
	}

	/**
	 * Gets the method.
	 *
	 * @return the method
	 */
	public AccessMethod getMethod() {
		return this.method;
	}

	/**
	 * Gets the static inputs.
	 *
	 * @return the static inputs
	 */
	public Map<Integer, TypedConstant<?>> getStaticInputs() {
		return this.staticInputs;
	}

	/**
	 * Gets the columns.
	 *
	 * @return the columns
	 */
	public List<Term> getColumns() {
		return this.columns;
	}

	/**
	 * Equals.
	 *
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
	 * Hash code.
	 *
	 * @return int
	 */
	@Override
	public int hashCode() {
		return Objects.hash(this.relation, this.method, this.columns, this.staticInputs);
	}
}
