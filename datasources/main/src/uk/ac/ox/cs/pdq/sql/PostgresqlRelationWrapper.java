package uk.ac.ox.cs.pdq.sql;

import java.lang.reflect.Type;
import java.sql.Connection;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import uk.ac.ox.cs.pdq.db.AccessMethod;
import uk.ac.ox.cs.pdq.db.Attribute;
import uk.ac.ox.cs.pdq.db.Relation;
import uk.ac.ox.cs.pdq.db.metadata.StaticMetadata;
import uk.ac.ox.cs.pdq.plan.Cost;
import uk.ac.ox.cs.pdq.plan.DoubleCost;

/**
 * A wrapper for PostgresqlTranslator-based SQL relations. This adds the 
 * SQLRelationWrapper some postgres-specific functionalities, mainly 
 * retrieving the cost of an access from the underlying database.
 * 
 * @author Efi Tsamoura
 * @author Julien Leblay
 */
public final class PostgresqlRelationWrapper extends SQLRelationWrapper {

	/** */
	private static final long serialVersionUID = 6004751694940794606L;

	/** Logger. */
	private static Logger log = Logger.getLogger(PostgresqlRelationWrapper.class);
	
	/**
	 * The regular expression used to retrieve the costs from the answer of
	 * an EXPLAIN ANALYZE query to postgresql.
	 */
	private static final String COST_REGEXP_PATTERN = "\\(cost=\\d+\\.\\d+\\.\\.(?<cost>\\d+\\.\\d+)\\s.*\\)";

	/**
	 * @param properties
	 * @param relation Relation
	 */
	public PostgresqlRelationWrapper(Properties properties, Relation relation) {
		super(properties, relation);
	}

	/**
	 * @param properties
	 * @param name String
	 * @param attributes List<Attribute>
	 */
	public PostgresqlRelationWrapper(Properties properties, String name, List<Attribute> attributes) {
		super(properties, name, attributes);
	}

	/**
	 * @param properties
	 * @param name String
	 * @param attributes List<Attribute>
	 * @param bm List<AccessMethod>
	 */
	public PostgresqlRelationWrapper(Properties properties, String name, List<Attribute> attributes, List<AccessMethod> bm) {
		super(properties, name, attributes, bm);
	}
	
	/**
	 * @param bindingPositions
	 * @return the cost of an access with the given bindingPositions as given 
	 * by postgresql.
	 */
	private Double initializeCost(List<Integer> bindingPositions) {
		String sql = this.makeCostStatement(bindingPositions);
		try(Connection conn = this.getConnection();
			Statement stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery(sql)) {
			if (rs.next()) {
				String s = rs.getString(1);
				Pattern p = Pattern.compile(COST_REGEXP_PATTERN);
				Matcher m = p.matcher(s);
				if (m.find()) {
					String cost = m.group("cost");
					return Double.valueOf(cost);
				}
			}
		} catch (SQLException e) {
			log.warn(sql, e);
			e.printStackTrace();
		}
		return -1.0;
	}
	
	/**
	 * In addition to add the given access method to the relations, initialize
	 * the cost of the binding by querying the underlying database, if the 
	 * given access method does not already have a cost assigned. 
	 * @param bm
	 * @see uk.ac.ox.cs.pdq.db.Relation#addAccessMethod(uk.ac.ox.cs.pdq.db.AccessMethod)
	 */
	@Override
	public void addAccessMethod(AccessMethod bm) {
		Cost accessCost = this.getMetadata().getPerInputTupleCost(bm);
		if (accessCost == null || accessCost.getValue().equals(Double.POSITIVE_INFINITY)) {
			AccessMethod b = new AccessMethod(bm.getName(), bm.getType(), bm.getInputs());
			accessCost = new DoubleCost(this.initializeCost(bm.getInputs()));
			super.addAccessMethod(b);
			((StaticMetadata) this.getMetadata()).setPerInputTupleCost(b, accessCost);
		} else {
			super.addAccessMethod(bm);
		}
	}

	/**
	 * @param bindingPositions
	 * @return a SQL statement asking for the cost of an access to the 
	 * underlying relation in postgres with the given bindingPositions.
	 */
	private String makeCostStatement(List<Integer> bindingPositions) {
		StringBuilder result = new StringBuilder("EXPLAIN ANALYSE SELECT * FROM ");
		result.append(this.name);
		String sep = " WHERE ";
		for (int i: bindingPositions) {
			result.append(sep).append(this.getAttribute(i - 1).getName()).append('=');
			Type cl = this.getAttribute(i - 1).getType();
			if (cl.equals(String.class)) {
				result.append('\'').append(i).append('\'');
			} else if (cl.equals(Date.class)) {
				result.append("to_date('01011970', 'DDMMYYYY')");
			} else {
				result.append(i);
			}
			sep = " AND ";
		}
		return result.toString();
	}
}
