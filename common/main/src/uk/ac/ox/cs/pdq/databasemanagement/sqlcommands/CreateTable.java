package uk.ac.ox.cs.pdq.databasemanagement.sqlcommands;

import uk.ac.ox.cs.pdq.databasemanagement.exception.DatabaseException;
import uk.ac.ox.cs.pdq.db.Attribute;
import uk.ac.ox.cs.pdq.db.PrimaryKey;
import uk.ac.ox.cs.pdq.db.Relation;

/**
 * This class represents a CREATE TABLE sql command.
 * 
 * @author Gabor
 *
 */
public class CreateTable extends Command {
	/**
	 * SQL specific type for strings. Normally it can be TEXT, VCHAR, VARCHAR etc.
	 */
	private final String STRING_TYPE_NAME = "{TYPENAME}";

	/**
	 * Constructs the create table statements. Multiple statements are allowed, so
	 * we can create multiple tables.
	 * 
	 * @param relations
	 * @throws DatabaseException in case it contains not allowed table or attribute names.
	 */
	public CreateTable(Relation[] relations) throws DatabaseException {
		super();
		
		int maxlen = 500;
		for (Relation r:relations) {
			int newMax = (int) Math.round(1000.0 / r.getAttributes().length);
			if (maxlen > newMax) {
				maxlen = newMax;
			}
		}
		// add SQL dialect specific tags
		replaceTagsMySql.put(STRING_TYPE_NAME, "VARCHAR("+ maxlen+")"); // mysql doesn't like long unique texts.
		replaceTagsDerby.put(STRING_TYPE_NAME, "VARCHAR(500)");
		replaceTagsPostgres.put(STRING_TYPE_NAME, "VARCHAR(500)");

		// add the CREATE TABLE statements for each table
		for (Relation r : relations) {
			statements.add(createTableStatement(r));
		}
		// add the UNIQUE constraint to the TABLE.
		for (Relation r : relations) {
			statements.add(createUniqueConstraintsStatement(r));
		}
	}
	public CreateTable(Relation relation) throws DatabaseException {
		this(new Relation[] {relation});
	}

	private String createUniqueConstraintsStatement(Relation r) {
		String stmnt = "ALTER TABLE "+DATABASENAME+"." + r.getName() + " ADD CONSTRAINT " + r.getName() + "_Constraint UNIQUE(";
		String columnNames = null;
		for (Attribute a: r.getAttributes()) {
			if (columnNames == null) columnNames = "";
			else columnNames += ",";
			columnNames +=a.getName();
		}
		return stmnt + columnNames + ")";
	}

	/**
	 * Creates a single CREATE TABLE statement for a Realation. Table and attribute
	 * names are hard mapped, so the relation name and the attribute names have to
	 * be set, and must not contain special characters, have to be shorter then 256
	 * characters.
	 * 
	 * @param relation
	 * @return
	 * @throws DatabaseException  in case table or attribute names are not allowed.
	 */
	public String createTableStatement(Relation relation) throws DatabaseException {
		// check for invalid characters and other naming errors.
		checkForErrorInNames(relation);
		// create the statement
		StringBuilder result = new StringBuilder();
		result.append("CREATE TABLE ").append(DATABASENAME + "." + relation.getName()).append('(');
		for (int attributeIndex = 0; attributeIndex < relation.getArity(); ++attributeIndex) {
			// add every attribute,
			Attribute attribute = relation.getAttribute(attributeIndex);
			result.append(' ').append(attribute.getName());
			
			// map attribute type
			if (String.class.isAssignableFrom((Class<?>) attribute.getType())) {
				result.append(' ');
				result.append(STRING_TYPE_NAME);
			} else if (Integer.class.isAssignableFrom((Class<?>) attribute.getType()))
				result.append(" INT");
			else if (Double.class.isAssignableFrom((Class<?>) attribute.getType()))
				result.append(" DOUBLE");
			else if (Float.class.isAssignableFrom((Class<?>) attribute.getType()))
				result.append(" FLOAT");
			else
				throw new RuntimeException("Unsupported type");
			
			// add comma if there are more attributes to add.
			if (attributeIndex < relation.getArity() - 1)
				result.append(", ");
		}
		
		// set primary keys if it was configured in the relation.
		String keyAttributes = null;
		PrimaryKey pk = relation.getKey();
		if (pk != null) {
			for (Attribute a : pk.getAttributes()) {
				if (keyAttributes == null) {
					keyAttributes = "";
				} else {
					keyAttributes += ",";
				}
				keyAttributes += a.getName();
				keyAttributes += " ";
			}
			result.append(" PRIMARY KEY ").append("(").append(keyAttributes).append(")");
		}
		result.append(')');
		return result.toString();
	}

	/**
	 * Table and attribute names are hard mapped, so the relation name and the
	 * attribute names have to be set, and must not contain special characters, have
	 * to be shorter then 126 characters.
	 * 
	 * @param relation
	 * @throws DatabaseException 
	 */
	private void checkForErrorInNames(Relation relation) throws DatabaseException {
		// check table name.
		checkForErrorInNames(relation.getName());
		
		// check attribute names
		for (Attribute a:relation.getAttributes()) {
			checkForErrorInNames(a.getName());
		}
	}

	/** Checks a single string for null/empty/too long, and not allowed characters.
	 * @param name
	 * @throws DatabaseException
	 */
	private void checkForErrorInNames(String name) throws DatabaseException {
		if (name == null || name.isEmpty()) {
			throw new DatabaseException("Name cannot be null!");
		}
		if (name.length()>=126) {
			throw new DatabaseException("Name is too long: \"" + name + "\"");
		}
		if (!name.matches("^[\\p{L}_][\\p{L}\\p{N}@$#_]{0,127}$"))  {
			throw new DatabaseException("Invalid characters in name: \"" + name + "\"");
		}
	}

}
