package uk.ac.ox.cs.pdq.plan;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.log4j.Logger;

import uk.ac.ox.cs.pdq.db.AccessMethod;
import uk.ac.ox.cs.pdq.db.Relation;
import uk.ac.ox.cs.pdq.db.Schema;
import uk.ac.ox.cs.pdq.db.TypedConstant;
import uk.ac.ox.cs.pdq.fol.Skolem;
import uk.ac.ox.cs.pdq.fol.Term;
import uk.ac.ox.cs.pdq.util.Table;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

// TODO: Auto-generated Javadoc
/**
 * A normalised plan.
 *
 * @author Efthymia Tsamoura
 */
public class SequentialPlan {

	/**  The list of commands of the plan*. */
	private final List<Command> commands;

	/**  Maps each table to the command that produced it and the order of appearance of the command*. */
	private final Map<Table, Pair<Command,Integer>> tables;
	
	/**  The list of access commands of this plan*. */
	private final List<AccessCommand> accessCommands = Lists.newArrayList();
	
	/**
	 * Creates a normalised plan consisting of a single command.
	 *
	 * @param command the command
	 */
	public SequentialPlan(Command command) {
		Preconditions.checkNotNull(command);
		this.commands = Lists.newArrayList(command);
		this.tables = Maps.newHashMap();
		this.tables.put(command.getOutput(), Pair.of(command, 0));
		if(command instanceof AccessCommand) {
			this.accessCommands.add((AccessCommand) command);
		}
	}

	/**
	 * Creates a normalised plan consisting of a list of commands.
	 *
	 * @param commands the commands
	 */
	public SequentialPlan(List<Command> commands) {
		Preconditions.checkNotNull(commands);
		this.commands = Lists.newArrayList(commands);
		this.tables = Maps.newHashMap();
		int order = 0;
		for(Command command:commands) {
			this.tables.put(command.getOutput(), Pair.of(command, order++));
			if(command instanceof AccessCommand) {
				this.accessCommands.add((AccessCommand) command);
			}
		}
	}

	/**
	 * Creates a normalised plan by appending the input list of commands to the input normalised plan.
	 *
	 * @param plan the plan
	 * @param command the command
	 */
	public SequentialPlan(SequentialPlan plan, Command command) {
		Preconditions.checkNotNull(plan);
		Preconditions.checkNotNull(command);
		this.commands = Lists.newArrayList(plan.getCommands());
		this.tables = Maps.newHashMap(plan.tables);
		int order = plan.tables.size();
		
		this.commands.add(command);
		this.tables.put(command.getOutput(), Pair.of(command, order));
		this.accessCommands.addAll(plan.accessCommands);
		if(command instanceof AccessCommand) {
			this.accessCommands.add((AccessCommand) command);
		}
	}
	
	/**
	 * Creates a normalised plan by appending the input list of commands to the input normalised plan.
	 *
	 * @param plan the plan
	 * @param commands the commands
	 */
	public SequentialPlan(SequentialPlan plan, Command... commands) {
		Preconditions.checkNotNull(plan);
		Preconditions.checkNotNull(commands);
		this.commands = Lists.newArrayList(plan.getCommands());
		this.tables = Maps.newHashMap(plan.tables);
		int order = plan.tables.size();
		
		this.accessCommands.addAll(plan.getAccessCommands());
		for(Command c:commands) {
			this.commands.add(c);
			this.tables.put(c.getOutput(), Pair.of(c, order));
			if(c instanceof AccessCommand) {
				this.accessCommands.add((AccessCommand) c);
			}
		}
	}
	
	/**
	 * Appends the input command to the plan.
	 *
	 * @param command the command
	 */
	public void addCommand(Command command) {
		Preconditions.checkNotNull(command);
		this.tables.put(command.getOutput(), Pair.of(command, this.commands.size()));
		this.commands.add(command);
		if(command instanceof AccessCommand) {
			this.accessCommands.add((AccessCommand) command);
		}
	}

	/**
	 * Gets the commands.
	 *
	 * @return this plan's commands
	 */
	public List<Command> getCommands() {
		return this.commands;
	}

	/**
	 * Gets the first.
	 *
	 * @return the first command
	 */
	public Command getFirst() {
		return this.commands.get(0);
	}

	/**
	 * Gets the last.
	 *
	 * @return the last command
	 */
	public Command getLast() {
		return this.commands.get(this.commands.size()-1);
	}
	
	/**
	 * Gets the command.
	 *
	 * @param table the table
	 * @return 		the command that produced the input table
	 */
	public Command getCommand(Table table) {
		Preconditions.checkNotNull(table);
		Preconditions.checkNotNull(this.tables.get(table));
		return this.tables.get(table).getLeft();
	}
	
	/**
	 * Gets the access commands.
	 *
	 * @return the access commands
	 */
	public List<AccessCommand> getAccessCommands() {
		return this.accessCommands;
	}
	
	/**
	 * Gets the ancestor.
	 *
	 * @param table the table
	 * @return 		a normalised plan that consists of the commands of this plan up to the command that produced the input table
	 */
	public SequentialPlan getAncestor(Table table) {
		Preconditions.checkNotNull(table);
		Preconditions.checkNotNull(this.tables.get(table));
		int order = this.tables.get(table).getRight();
		return new SequentialPlan(this.commands.subList(0, order+1));
	}
	
	/**
	 * Gets the ancestor exclusive.
	 *
	 * @param table the table
	 * @return 		a normalised plan that consists of the commands of this plan up to the command that produced the input table
	 */
	public SequentialPlan getAncestorExclusive(Table table) {
		Preconditions.checkNotNull(table);
		Preconditions.checkNotNull(this.tables.get(table));
		int order = this.tables.get(table).getRight();
		return new SequentialPlan(this.commands.subList(0, order));
	}
	
	/**
	 * Gets the ancestor.
	 *
	 * @param command the command
	 * @return 		a normalised plan that consists of the commands of this plan up to the input command
	 */
	public SequentialPlan getAncestor(Command command) {
		Preconditions.checkNotNull(command);
		int order = this.commands.indexOf(command);
		return new SequentialPlan(this.commands.subList(0, order));
	}
	
	/**
	 * Gets the tables.
	 *
	 * @return the tables
	 */
	public Collection<Table> getTables() {
		return this.tables.keySet();
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
		return SequentialPlan.class.isInstance(o)
				&& this.commands.equals(((SequentialPlan) o).commands);
	}

	/**
	 * Hash code.
	 *
	 * @return int
	 */
	@Override
	public int hashCode() {
		return Objects.hash(this.commands);
	}

	/**
	 * To string.
	 *
	 * @return String
	 */
	@Override
	public String toString() {
		return Joiner.on("\n").join(this.commands);
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	@Override
	public SequentialPlan clone() {
		return new SequentialPlan(this);
	}
}
