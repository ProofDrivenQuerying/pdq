package uk.ac.ox.cs.pdq.plan;

import java.util.List;
import java.util.Objects;

import com.google.common.base.Preconditions;

import uk.ac.ox.cs.pdq.db.Attribute;
import uk.ac.ox.cs.pdq.db.TGD;
import uk.ac.ox.cs.pdq.util.Table;

/**
 * 
 * @author Efthymia Tsamoura
 *
 */
public class ProjectCommand implements Command{
	
	private final Table input;
	
	private final Table output;
	
	private final List<Attribute> toProject;
	
	/** Caches the constraint that captures this access command **/
	private final TGD command;
	
	public ProjectCommand(List<Attribute> toProject, Table input) {
		Preconditions.checkNotNull(toProject);
		Preconditions.checkNotNull(input);
		this.input = input;
		this.toProject = toProject;
		this.output = new Table(toProject);
		this.command = new CommandToTGDTranslator().toTGD(this);
	}

	@Override
	public Table getOutput() {
		return this.output;
	}

	public Table getInput() {
		return input;
	}

	public List<Attribute> getToProject() {
		return toProject;
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
		return ProjectCommand.class.isInstance(o)
				&& this.toProject.equals(((ProjectCommand) o).toProject)
				&& this.input.equals(((ProjectCommand) o).input);
	}

	/**
	 * @return int
	 */
	@Override
	public int hashCode() {
		return Objects.hash(this.toProject, this.input);
	}

	/**
	 * @return String
	 */
	@Override
	public String toString() {
		return this.command.toString();
	}
}
