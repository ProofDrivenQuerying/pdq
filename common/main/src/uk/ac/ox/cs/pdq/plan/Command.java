package uk.ac.ox.cs.pdq.plan;

import uk.ac.ox.cs.pdq.util.Table;

/**
 * Interface for access or middleware commands.
 * 
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
	
	
 * A middleware query command is of the form T := Q, where Q is a relational algebra 
 * query over temporary tables and T is a temporary table.
 * 
 * @author Efthymia Tsamoura
 *
 */
public interface Command {

	Table getOutput();
}
