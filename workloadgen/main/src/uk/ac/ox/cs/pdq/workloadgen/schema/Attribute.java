/**
 * 
 */
package uk.ac.ox.cs.pdq.workloadgen.schema;

import java.sql.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;

/**
 * Represent a table's attribute.
 * 
 * @author herodotos.herodotou
 */
public class Attribute {

	/**
	 * Enumerates the possible data types for attributes. Note: we generalize the
	 * various SQL data types to the most basic java types.
	 * 
	 * @author herodotos.herodotou
	 */
	public enum AttrType {

		AT_INTEGER, // for integer, int(*)
		AT_DOUBLE, // for double, numeric, decimal
		AT_STRING, // for varchar, text
		AT_CHAR, // for char
		AT_DATE, // for various date types
		AT_UNKNOWN; // unknown data type

		public static AttrType getEnum(String value) {
			if (value.equalsIgnoreCase("integer"))
				return AT_INTEGER;
			else if (value.equalsIgnoreCase("double"))
				return AT_DOUBLE;
			else if (value.equalsIgnoreCase("string"))
				return AT_STRING;
			else if (value.equalsIgnoreCase("char"))
				return AT_CHAR;
			else if (value.equalsIgnoreCase("date"))
				return AT_DATE;
			else
				return AT_UNKNOWN;
		}
	}

	/**
	 * Attempts to cast the given string to given class. If the class is not
	 * supported, the return object is the same as s.
	 * @param cl
	 * @param o
	 * @return a representation of s cast to the given class.
	 * @throws ParseException 
	 */
	public static <T> T cast(AttrType type, Object o) {
		if (o == null) {
			return null;
		}
		String s = String.valueOf(o);
		switch(type) {
		case AT_INTEGER:
			return (T) (s.isEmpty() ? null : Integer.valueOf(s.trim()));
		case AT_DOUBLE:
			return (T) (s.isEmpty() ? null : Double.valueOf(s.trim()));
		case AT_STRING:
			return (T) String.valueOf(o);
		case AT_DATE:
			java.util.Date d;
			try {
				d = (java.util.Date) new SimpleDateFormat("yyyy-MM-dd").parseObject(s.trim());
			} catch (ParseException e) {
				e.printStackTrace();
				throw new java.lang.UnsupportedOperationException("Unsupported date format");
			}
			return (T) new Date(d.getTime());
		default: 
			throw new java.lang.UnsupportedOperationException("Unknown database type");
		}
	}
	
	/**
	 * @param type
	 * @return the canonical name of the given type. By default, toString() is
	 * used. If the type is a conventional class, then getCanonicalName is used,
	 * if it is a DataType, then getName() is used.
	 */
	public static String canonicalName(AttrType type) {
		switch(type) {
		case AT_INTEGER:
			return "java.lang.Integer";
		case AT_DOUBLE:
			return "java.lang.Double";
		case AT_STRING:
			return "java.lang.String";
		case AT_DATE:
			return "java.lang.Date";
		default: 
			throw new java.lang.UnsupportedOperationException("Unknown database type");
		}

	}

	// Private data members
	private Table parent;
	private String fullName; // tableName.attrName
	private AttrType type;
	private AttrStats stats;

	/**
	 * @param parent
	 * @param fullName
	 * @param type
	 */
	public Attribute(Table parent, String fullName, AttrType type) {
		this.parent = parent;
		this.fullName = fullName;
		this.type = type;
		this.stats = null;
	}

	/**
	 * @return the parent table
	 */
	public Table getTable() {
		return parent;
	}

	/**
	 * @return the "tableName.attrName"
	 */
	public String getFullName() {
		return fullName;
	}

	public String getName() {
		return this.fullName.split("\\.")[1];
	}

	/**
	 * @return the type
	 */
	public AttrType getType() {
		return type;
	}

	/**
	 * @return the stats (could be null)
	 */
	public AttrStats getStats() {
		return stats;
	}

	/**
	 * @param stats
	 *           the stats to set
	 */
	public void setStats(AttrStats stats) {
		this.stats = stats;
	}

	/**
	 * @return true if there are stats associated with this attribute
	 */
	public boolean hasStats() {
		return this.stats != null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return fullName;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return fullName.hashCode();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof Attribute))
			return false;
		Attribute other = (Attribute) obj;
		if (!fullName.equals(other.fullName))
			return false;
		return true;
	}

}
