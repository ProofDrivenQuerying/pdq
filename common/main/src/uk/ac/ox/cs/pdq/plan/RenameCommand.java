package uk.ac.ox.cs.pdq.plan;

import java.util.List;
import java.util.Objects;

import uk.ac.ox.cs.pdq.db.Attribute;
import uk.ac.ox.cs.pdq.db.TGD;
import uk.ac.ox.cs.pdq.util.Table;

import com.google.common.base.Preconditions;

/**
 * 
 * @author Efthymia Tsamoura
 *
 */
public class RenameCommand implements Command {

	private final Table input;
	
	private final Table output;
	
	private final List<Attribute> toRename;
	
	/** Caches the constraint that captures this access command **/
	private final TGD command;
	
	public RenameCommand(List<Attribute> toRename, Table input) {
		Preconditions.checkNotNull(toRename);
		Preconditions.checkNotNull(input);
		Preconditions.checkArgument(toRename.size()==input.getHeader().size());
		this.input = input;
		this.toRename = toRename;
		this.output = new Table(toRename);
		this.command = new CommandToTGDTranslator().toTGD(this);
	}

	@Override
	public Table getOutput() {
		return this.output;
	}

	public Table getInput() {
		return input;
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
		return RenameCommand.class.isInstance(o)
				&& this.toRename.equals(((RenameCommand) o).toRename)
				&& this.input.equals(((RenameCommand) o).input);
	}

	/**
	 * @return int
	 */
	@Override
	public int hashCode() {
		return Objects.hash(this.toRename, this.input);
	}

	/**
	 * @return String
	 */
	@Override
	public String toString() {
		return this.command.toString();
	}
}
