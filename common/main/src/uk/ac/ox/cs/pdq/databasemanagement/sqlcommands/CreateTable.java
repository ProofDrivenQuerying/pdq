package uk.ac.ox.cs.pdq.databasemanagement.sqlcommands;

import uk.ac.ox.cs.pdq.db.Attribute;
import uk.ac.ox.cs.pdq.db.PrimaryKey;
import uk.ac.ox.cs.pdq.db.Relation;

public class CreateTable extends Command {
	/**
	 * SQL specific type for strings. Normally it can be TEXT, VCHAR, VARCHAR etc.
	 */
	private final String STRING_TYPE_NAME = "{TYPENAME}";

	public CreateTable(Relation[] relations) {
		super();
		replaceTagsMySql.put(STRING_TYPE_NAME, "TEXT");
		replaceTagsDerby.put(STRING_TYPE_NAME, "VARCHAR(500)");
		replaceTagsPostgres.put(STRING_TYPE_NAME, "VARCHAR(500)");
		
		for (Relation r : relations) {
			statements.add(createTableStatement(r));
		}
	}

	public String createTableStatement(Relation relation) {
		StringBuilder result = new StringBuilder();
		result.append("CREATE TABLE ").append(DATABASENAME + "." + relation.getName()).append('(');
		for (int attributeIndex = 0; attributeIndex < relation.getArity(); ++attributeIndex) {
			Attribute attribute = relation.getAttribute(attributeIndex);
			result.append(' ').append(attribute.getName());
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
			if (attributeIndex < relation.getArity() - 1)
				result.append(", ");
		}
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

}
