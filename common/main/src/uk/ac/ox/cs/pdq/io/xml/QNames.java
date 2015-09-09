package uk.ac.ox.cs.pdq.io.xml;

import javax.xml.namespace.QName;

import com.google.common.base.CaseFormat;

/**
 * Gather all the qualified names required for reading and writing schemas,
 * queries, dependencies, etc.
 * 
 * @author Julien Leblay
 * @author Efthymia Tsamoura
 * 
 */
public enum QNames {

	// Schema, relation and access
	SCHEMA, DISCOVERER, RELATIONS, RELATION, VIEWS, VIEW, DEPENDENCIES,
	DEPENDENCY, ACCESS_METHOD, ATTRIBUTE, ATTRIBUTES,

	// Access types
	ACCESS, JOIN, SELECT, PROJECT, ASSIGN, RENAME, 
	
	// FOL, query
	FORMULA, AXIOM, QUERY, HEAD, BODY, STATE, ATOM, VARIABLE, CONSTANT,

	// Plan, proof
	PROOF, CANDIDATE, CHILDREN, CHILD, MATCH, PLAN, OPERATOR, COMMAND, ENTRY,
	INPUTS, OUTPUTS, PREDICATE, CONJUNCTION,

	// Services
	SERVICES, SERVICE, PROTOCOL, PARAM, METHOD, PATH, STATIC, RANGE,
	USAGE_POLICIES, POLICY, RESULT_DELIMITER, BATCH_DELIMITER, BATCH_SIZE,
	INPUT_METHODS, INPUT_METHOD, STATIC_INPUT, TEMPLATE, MEDIA_TYPE,

	// Misc.
	TYPE, URL, USERNAME, PASSWORD, SOURCES, SOURCE, TARGET, NAME, DESCRIPTION,
	SIZE, COST, KEY, VALUE, LEFT, RIGHT, INPUT, OUTPUT, VARIANT, CONTROL_FLOW, 
	EMPTY, EQUALITY;

	/** The qname associated with this enum item. */
	private final QName qname;

	/**
	 * Private constructor. Associate a qname from each enum item, whose local
	 * name is the lower-case/hyphenated form of the enum item's name. 
	 */
	private QNames() {
		this.qname = new QName("",
				CaseFormat.UPPER_UNDERSCORE.to(
						CaseFormat.LOWER_HYPHEN, String.valueOf(this)));
	}

	/**
	 * @param lowerHyphen
	 * @return the enum item corresponding to given param, in lower-case, hyphenated form.
	 */
	public static QNames parse(String lowerHyphen) {
		return QNames.valueOf(
				CaseFormat.LOWER_HYPHEN.to(
						CaseFormat.UPPER_UNDERSCORE, lowerHyphen));
	}

	/**
	 * @return the String serialization of qname associated with this enum item
	 */
	public String format() {
		return this.qname.toString();
	}
}
