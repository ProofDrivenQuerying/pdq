package uk.ac.ox.cs.pdq.io.xml;

import javax.xml.namespace.QName;

import com.google.common.base.CaseFormat;

// TODO: Auto-generated Javadoc
/**
 * Gather all the qualified names required for reading and writing schemas,
 * queries, dependencies, etc.
 * 
 * @author Julien Leblay
 * @author Efthymia Tsamoura
 * 
 */
public enum QNames {

	/** The schema. */
	// Schema, relation and access
	SCHEMA, 
 /** The discoverer. */
 DISCOVERER, 
 /** The relations. */
 RELATIONS, 
 /** The relation. */
 RELATION, 
 /** The views. */
 VIEWS, 
 /** The view. */
 VIEW, 
 /** The dependencies. */
 DEPENDENCIES,
	
	/** The dependency. */
	DEPENDENCY, 
 /** The access method. */
 ACCESS_METHOD, 
 /** The attribute. */
 ATTRIBUTE, 
 /** The attributes. */
 ATTRIBUTES,

	/** The access. */
	// Access types
	ACCESS, 
 /** The join. */
 JOIN, 
 /** The select. */
 SELECT, 
 /** The project. */
 PROJECT, 
 /** The assign. */
 ASSIGN, 
 /** The rename. */
 RENAME, 
	
	/** The formula. */
	// FOL, query
	FORMULA, 
 /** The axiom. */
 AXIOM, 
 /** The query. */
 QUERY, 
 /** The head. */
 HEAD, 
 /** The body. */
 BODY, 
 /** The state. */
 STATE, 
 /** The atom. */
 ATOM, 
 /** The variable. */
 VARIABLE, 
 /** The constant. */
 CONSTANT,

	/** The proof. */
	// Plan, proof
	PROOF, 
 /** The candidate. */
 CANDIDATE, 
 /** The children. */
 CHILDREN, 
 /** The child. */
 CHILD, 
 /** The match. */
 MATCH, 
 /** The plan. */
 PLAN, 
 /** The operator. */
 OPERATOR, 
 /** The command. */
 COMMAND, 
 /** The entry. */
 ENTRY,
	
	/** The inputs. */
	INPUTS, 
 /** The outputs. */
 OUTPUTS, 
 /** The predicate. */
 PREDICATE, 
 /** The conjunction. */
 CONJUNCTION,

	/** The services. */
	// Services
	SERVICES, 
 /** The service. */
 SERVICE, 
 /** The protocol. */
 PROTOCOL, 
 /** The param. */
 PARAM, 
 /** The method. */
 METHOD, 
 /** The path. */
 PATH, 
 /** The static. */
 STATIC, 
 /** The range. */
 RANGE,
	
	/** The usage policies. */
	USAGE_POLICIES, 
 /** The policy. */
 POLICY, 
 /** The result delimiter. */
 RESULT_DELIMITER, 
 /** The batch delimiter. */
 BATCH_DELIMITER, 
 /** The batch size. */
 BATCH_SIZE,
	
	/** The input methods. */
	INPUT_METHODS, 
 /** The input method. */
 INPUT_METHOD, 
 /** The static input. */
 STATIC_INPUT, 
 /** The template. */
 TEMPLATE, 
 /** The media type. */
 MEDIA_TYPE,

	/** The type. */
	// Misc.
	TYPE, 
 /** The url. */
 URL, 
 /** The username. */
 USERNAME, 
 /** The password. */
 PASSWORD, 
 /** The sources. */
 SOURCES, 
 /** The source. */
 SOURCE, 
 /** The target. */
 TARGET, 
 /** The name. */
 NAME, 
 /** The description. */
 DESCRIPTION,
	
	/** The size. */
	SIZE, 
 /** The cost. */
 COST, 
 /** The key. */
 KEY, 
 /** The value. */
 VALUE, 
 /** The left. */
 LEFT, 
 /** The right. */
 RIGHT, 
 /** The input. */
 INPUT, 
 /** The output. */
 OUTPUT, 
 /** The variant. */
 VARIANT, 
 /** The control flow. */
 CONTROL_FLOW, 
	
	/** The empty. */
	EMPTY, 
 /** The equality. */
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
	 * Parses the.
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
	 * Format.
	 *
	 * @return the String serialization of qname associated with this enum item
	 */
	public String format() {
		return this.qname.toString();
	}
}
