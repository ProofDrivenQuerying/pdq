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

/**
 * A normalised plan
 * 
 * @author Efthymia Tsamoura
 *
 */
public class NormalisedPlan {

	/** The list of commands of the plan**/
	private final List<Command> commands;

	/** Maps each table to the command that produced it and the order of appearance of the command**/
	private final Map<Table, Pair<Command,Integer>> tables;
	
	private final List<AccessCommand> accessCommands = Lists.newArrayList();
	
	public NormalisedPlan(Command command) {
		Preconditions.checkNotNull(command);
		this.commands = Lists.newArrayList(command);
		this.tables = Maps.newHashMap();
		this.tables.put(command.getOutput(), Pair.of(command, 0));
		if(command instanceof AccessCommand) {
			this.accessCommands.add((AccessCommand) command);
		}
	}

	public NormalisedPlan(List<Command> commands) {
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

	public NormalisedPlan(NormalisedPlan plan, Command command) {
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
	
	public NormalisedPlan(NormalisedPlan plan, Command... commands) {
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
	
	public void addCommand(Command command) {
		Preconditions.checkNotNull(command);
		this.tables.put(command.getOutput(), Pair.of(command, this.commands.size()));
		this.commands.add(command);
		if(command instanceof AccessCommand) {
			this.accessCommands.add((AccessCommand) command);
		}
	}

	public List<Command> getCommands() {
		return this.commands;
	}

	public Command getFirst() {
		return this.commands.get(0);
	}

	public Command getLast() {
		return this.commands.get(this.commands.size()-1);
	}
	
	/**
	 * 
	 * @param table
	 * @return
	 * 		the command that produced the input table
	 */
	public Command getCommand(Table table) {
		Preconditions.checkNotNull(table);
		Preconditions.checkNotNull(this.tables.get(table));
		return this.tables.get(table).getLeft();
	}
	
	public List<AccessCommand> getAccessCommands() {
		return this.accessCommands;
	}
	
	/**
	 * 
	 * @param table
	 * @return
	 * 		a normalised plan that consists of the commands of this plan up to the command that produced the input table 
	 */
	public NormalisedPlan getAncestor(Table table) {
		Preconditions.checkNotNull(table);
		Preconditions.checkNotNull(this.tables.get(table));
		int order = this.tables.get(table).getRight();
		return new NormalisedPlan(this.commands.subList(0, order+1));
	}
	
	/**
	 * 
	 * @param table
	 * @return
	 * 		a normalised plan that consists of the commands of this plan up to the command that produced the input table 
	 */
	public NormalisedPlan getAncestorExclusive(Table table) {
		Preconditions.checkNotNull(table);
		Preconditions.checkNotNull(this.tables.get(table));
		int order = this.tables.get(table).getRight();
		return new NormalisedPlan(this.commands.subList(0, order));
	}
	
	/**
	 * 
	 * @param command
	 * @return
	 * 		a normalised plan that consists of the commands of this plan up to the input command
	 */
	public NormalisedPlan getAncestor(Command command) {
		Preconditions.checkNotNull(command);
		int order = this.commands.indexOf(command);
		return new NormalisedPlan(this.commands.subList(0, order));
	}
	
	public Collection<Table> getTables() {
		return this.tables.keySet();
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
		return NormalisedPlan.class.isInstance(o)
				&& this.commands.equals(((NormalisedPlan) o).commands);
	}

	/**
	 * @return int
	 */
	@Override
	public int hashCode() {
		return Objects.hash(this.commands);
	}

	/**
	 * @return String
	 */
	@Override
	public String toString() {
		return Joiner.on("\n").join(this.commands);
	}
	
	@Override
	public NormalisedPlan clone() {
		return new NormalisedPlan(this);
	}
		
	private static String READ_RELATION_METHOD = "^(RE:(\\w+)(\\s+)MT:(\\w+)(\\s+))";
	/** Reads single-word constants**/
	private static String READ_STATIC_INPUTS = "(\\(CONSTANT:(\\w+)(\\s+)POS:(\\d+)\\))+";
	/** Reads two-word constants**/
	private static String READ_STATIC_INPUTS_ALT = "(\\(CONSTANT:(\\w+)(\\s+)(\\w+)(\\s+)POS:(\\d+)\\))+";
	/** Reads the input table**/
	private static String READ_COLUMNS = "(COLUMNS:)(\"([\\p{Alnum}&\\s]+,)+[\\p{Alnum}&\\s]+\")";
	
	/** Logger. */
	private static Logger log = Logger.getLogger(NormalisedPlan.class);

	/**
	 * 
	 * @param schema
	 * @param fileName
	 * @return
	 */
	public static NormalisedPlan reader(Schema schema, String fileName) {
		List<Command> commands = new ArrayList<>();
		String line = null;
		try {
			FileReader fileReader = new FileReader(fileName);
			BufferedReader bufferedReader = new BufferedReader(fileReader);
			while((line = bufferedReader.readLine()) != null) {
				commands.add(NormalisedPlan.parseAccess(schema, line));
			}
			bufferedReader.close();            
		}
		catch(FileNotFoundException ex) {      
			ex.printStackTrace(System.out);
		}
		catch(IOException ex) {
			ex.printStackTrace(System.out);
		}
		return new NormalisedPlan(commands);
	}
	
	/**
	 * Parses the statistics file
	 * @param schema
	 * @param line
	 */
	private static AccessCommand parseAccess(Schema schema, String access) {
		Preconditions.checkNotNull(schema);
		Preconditions.checkNotNull(access);
		Pattern p = Pattern.compile(READ_RELATION_METHOD);
		Matcher m = p.matcher(access);
		Relation r = null;
		AccessMethod binding = null;
		Map<Integer, TypedConstant<?>> staticInputs = Maps.newHashMap();
		
		if (m.find()) {
			String relation = m.group(2);
			String method = m.group(4);
			if(schema.contains(relation)) {
				r = schema.getRelation(relation);
				binding = r.getAccessMethod(method);
				if(binding == null) {
					throw new java.lang.IllegalStateException("RELATION " + relation + " DOES NOT CONTAINT ATTRIBUTE " + method);
				}
				Pattern p2 = Pattern.compile(READ_STATIC_INPUTS);
				Matcher m2 = p2.matcher(access);
				
				while (m2.find()) {
					String constant = m2.group(2);
					String position = m2.group(4);
					staticInputs.put(new Integer(position), new TypedConstant(constant));
				}
				if(staticInputs.isEmpty()) {
					p2 = Pattern.compile(READ_STATIC_INPUTS_ALT);
					m2 = p2.matcher(access);
					while (m2.find()) {
						String constant = m2.group(2) + m2.group(3) + m2.group(4);
						String position = m2.group(6);
						staticInputs.put(new Integer(position), new TypedConstant(constant));
					}
					if(staticInputs.isEmpty()) {
						log.info("No static input provided");
					}
				}
			}
			else {
				throw new java.lang.IllegalStateException("SCHEMA DOES NOT CONTAINT RELATION " + relation);
			}
			Pattern p2 = Pattern.compile(READ_COLUMNS);
			Matcher m2 = p2.matcher(access);
			if(m2.find()) {
				List<Term> columns = Lists.newArrayList();
				for(String column:m2.group(2).split(",")) {
					columns.add(new Skolem(column));
				}
				return new AccessCommand(r, binding, columns, null, staticInputs);
			}
			else {
				throw new java.lang.IllegalStateException("UNPROVIDED LIST OF COLUMNS");
			}
		}
		return null;
	}

	
	public static void main(String... args) {
		
	}
}
