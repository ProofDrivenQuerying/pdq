package uk.ac.ox.cs.pdq.databasemanagement.sqlcommands;

import java.util.ArrayList;
import java.util.List;

import uk.ac.ox.cs.pdq.db.Attribute;
import uk.ac.ox.cs.pdq.db.PrimaryKey;
import uk.ac.ox.cs.pdq.db.Relation;
import uk.ac.ox.cs.pdq.db.Schema;

public class CreateTable extends Command {
	/**
	 * SQL specific type. Normally it can be TEXT, VCHAR, VARCHAR etc.
	 */
	private String typeNameForText;

	public CreateTable(Relation[] relations) {
		super();
	}

	@Override
	public List<String> toPostgresStatement(String databaseName, Schema schema) {
		typeNameForText = "VARCHAR(500)";
		List<String> statements = new ArrayList<String>();
		for (Relation r : schema.getRelations()) {
			statements.add(createTableStatement(r));
		}
		return replaceTags(statements, DATABASENAME, databaseName);
	}

	@Override
	public List<String> toMySqlStatement(String databaseName, Schema schema) {
		typeNameForText = "TEXT";
		List<String> statements = new ArrayList<String>();
		for (Relation r : schema.getRelations()) {
			statements.add(createTableStatement(r));
		}
		return replaceTags(statements, DATABASENAME, databaseName);
	}

	@Override
	public List<String> toDerbyStatement(String databaseName, Schema schema) {
		typeNameForText = "VARCHAR(500)";
		List<String> statements = new ArrayList<String>();
		for (Relation r : schema.getRelations()) {
			statements.add(createTableStatement(r));
		}
		return replaceTags(statements, DATABASENAME, databaseName);
	}

	public String createTableStatement(Relation relation) {
		StringBuilder result = new StringBuilder();
		result.append("CREATE TABLE ").append(DATABASENAME + "." + relation.getName()).append('(');
		for (int attributeIndex = 0; attributeIndex < relation.getArity(); ++attributeIndex) {
			Attribute attribute = relation.getAttribute(attributeIndex);
			result.append(' ').append(attribute.getName());
			if (String.class.isAssignableFrom((Class<?>) attribute.getType())) {
				result.append(' ');
				result.append(typeNameForText);
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
