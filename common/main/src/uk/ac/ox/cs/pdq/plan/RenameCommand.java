package uk.ac.ox.cs.pdq.plan;

import java.util.List;
import java.util.Objects;

import uk.ac.ox.cs.pdq.db.Attribute;
import uk.ac.ox.cs.pdq.db.TGD;
import uk.ac.ox.cs.pdq.util.Table;

import com.google.common.base.Preconditions;

// TODO: Auto-generated Javadoc
/**
 * The Class RenameCommand.
 *
 * @author Efthymia Tsamoura
 */
public class RenameCommand implements Command {

	/**  The input table *. */
	private final Table input;
	
	/**  The output table *. */
	private final Table output;
	
	/**  The attributes after the renaming *. */
	private final List<Attribute> toRename;
	
	/**  Caches the constraint that captures this access command *. */
	private final TGD command;
	
	/**
	 * Creates a project command based on the input table and the input new attributes.
	 *
	 * @param toRename the to rename
	 * @param input the input
	 */
	public RenameCommand(List<Attribute> toRename, Table input) {
		Preconditions.checkNotNull(toRename);
		Preconditions.checkNotNull(input);
		Preconditions.checkArgument(toRename.size()==input.getHeader().size());
		this.input = input;
		this.toRename = toRename;
		this.output = new Table(toRename);
		this.command = new CommandToTGDTranslator().toTGD(this);
	}

	/* (non-Javadoc)
	 * @see uk.ac.ox.cs.pdq.plan.Command#getOutput()
	 */
	@Override
	public Table getOutput() {
		return this.output;
	}

	/**
	 * Gets the input.
	 *
	 * @return the input
	 */
	public Table getInput() {
		return input;
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
		return RenameCommand.class.isInstance(o)
				&& this.toRename.equals(((RenameCommand) o).toRename)
				&& this.input.equals(((RenameCommand) o).input);
	}

	/**
	 * Hash code.
	 *
	 * @return int
	 */
	@Override
	public int hashCode() {
		return Objects.hash(this.toRename, this.input);
	}

	/**
	 * To string.
	 *
	 * @return String
	 */
	@Override
	public String toString() {
		return this.command.toString();
	}
}
