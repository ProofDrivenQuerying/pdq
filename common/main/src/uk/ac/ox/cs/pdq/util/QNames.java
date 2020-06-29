// This file is part of PDQ (https://github.com/michaelbenedikt/pdq) which is released under the MIT license.
// See accompanying LICENSE for copyright notice and full details.

package uk.ac.ox.cs.pdq.util;

import javax.xml.namespace.QName;

import com.google.common.base.CaseFormat;
/**
 * Gather all the qualified names appearing in a schema file 
 * query file or plan file
 * 
 * @author Julien Leblay
 * @author Efthymia Tsamoura
 * 
 */
public enum QNames {

	/** The schema. */
		
	SCHEMA, 
 DISCOVERER, 
 RELATIONS, 
 RELATION, 
 VIEWS, 
 VIEW, 
 DEPENDENCIES,
	DEPENDENCY, 
 ACCESS_METHOD, 
 ATTRIBUTE, 
 ATTRIBUTES,

	ACCESS, 
 JOIN, 
 SELECT, 
 PROJECT, 
 ASSIGN, 
 RENAME, 
	
	FORMULA, 
 AXIOM, 
 QUERY, 
 HEAD, 
 BODY, 
 STATE, 
 ATOM, 
 VARIABLE, 
 CONSTANT,

	PROOF, 
 CANDIDATE, 
 CHILDREN, 
 CHILD, 
 MATCH, 
 PLAN, 
 OPERATOR, 
 COMMAND, 
 ENTRY,
	
	INPUTS, 
 OUTPUTS, 
 PREDICATE, 
 CONJUNCTION,

	SERVICES, 
 SERVICE, 
 PROTOCOL, 
 PARAM, 
 METHOD, 
 PATH, 
 STATIC, 
 RANGE,
	
	USAGE_POLICIES, 
 POLICY, 
 RESULT_DELIMITER, 
 BATCH_DELIMITER, 
 BATCH_SIZE,
	
	INPUT_METHODS, 
 INPUT_METHOD, 
 STATIC_INPUT, 
 TEMPLATE, 
 MEDIA_TYPE,
	TYPE, 
 URL, 
 USERNAME, 
 PASSWORD, 
 SOURCES, 
 SOURCE, 
 TARGET, 
 NAME, 
 DESCRIPTION,
	
	SIZE, 
 COST, 
 KEY, 
 VALUE, 
 LEFT, 
 RIGHT, 
 INPUT, 
 OUTPUT, 
 VARIANT, 
 CONTROL_FLOW, 
	
	EMPTY, 
 EQUALITY;

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
	 * 
	 * 
	 *
	 * @param lowerHyphen the qname in lower hyphen case.
	 * @return the enum item corresponding to given param, in lower-case, hyphenated form.
	 */
	public static QNames parse(String lowerHyphen) {
		return QNames.valueOf(
				CaseFormat.LOWER_HYPHEN.to(
						CaseFormat.UPPER_UNDERSCORE, lowerHyphen));
	}

	/**
	 * 
	 * Format.
	 *
	 * @return the String serialization of qname associated with this enum item
	 */
	public String format() {
		return this.qname.toString();
	}
}
