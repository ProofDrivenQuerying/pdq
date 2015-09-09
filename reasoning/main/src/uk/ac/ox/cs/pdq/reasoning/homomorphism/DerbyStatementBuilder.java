package uk.ac.ox.cs.pdq.reasoning.homomorphism;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;

import uk.ac.ox.cs.pdq.fol.Evaluatable;
import uk.ac.ox.cs.pdq.reasoning.homomorphism.HomomorphismConstraint.TopK;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

/**
 * Builds queries for detecting homomorphisms in Derby
 *
 * @author Efthymia Tsamoura
 * @author Julien leblay
 */
public class DerbyStatementBuilder extends SQLStatementBuilder {

	private static BiMap<String, String> nameEncodings = HashBiMap.create();
	static {
		nameEncodings.put(":", "cl_");
		nameEncodings.put("$", "dl_");
		nameEncodings.put("#", "hs_");
		nameEncodings.put("%", "pc_");
		nameEncodings.put("-", "hp_");
		nameEncodings.put("{", "lbc_");
		nameEncodings.put("}", "rbc_");
		nameEncodings.put("[", "lbk_");
		nameEncodings.put("]", "rbk_");
		nameEncodings.put("(", "lpr_");
		nameEncodings.put(")", "rpr_");
		nameEncodings.put("int", "int_");
		nameEncodings.put("float", "flt_");
		nameEncodings.put("string", "str_");
		nameEncodings.put("boolean", "bool_");
		nameEncodings.put("decimal", "dec_");
		nameEncodings.put("datetime", "date_");
		nameEncodings.put("both", "both_");
		nameEncodings.put("none", "none_");
		nameEncodings.put("check", "chk_");
	}

	/**
	 * Default constructor
	 */
	public DerbyStatementBuilder() {
		super();
	}

	/*
	 * (non-Javadoc)
	 * @see uk.ac.ox.cs.pdq.homomorphism.AbstractHomomorphismStatementBuilder#setupStatements(java.lang.String)
	 */
	@Override
	public Collection<String> setupStatements(String databaseName) {
		return new LinkedList<>();
	}

	/*
	 * (non-Javadoc)
	 * @see uk.ac.ox.cs.pdq.homomorphism.AbstractHomomorphismStatementBuilder#cleanupStatements(java.lang.String)
	 */
	@Override
	public Collection<String> cleanupStatements(String databaseName) {
		return new LinkedList<>();
	}
	
	
	@Override
	protected String translateLimitConstraints(Evaluatable source, HomomorphismConstraint... constraints) {
		for(HomomorphismConstraint c:constraints) {
			if(c instanceof TopK) {
				return "FETCH NEXT " + ((TopK) c).k + " ROWS ONLY  ";
			}
		}
		return null;
	}

	/**
	 * @return DerbyHomomorphismStatementBuilder
	 */
	@Override
	public DerbyStatementBuilder clone() {
		return new DerbyStatementBuilder();
	}

	@Override
	public String encodeName(String name) {
		String result = name;
		for (Map.Entry<String, String> entry: nameEncodings.entrySet()) {
			result = result.replace(entry.getKey(), entry.getValue());
		}
		return result;
	}
}