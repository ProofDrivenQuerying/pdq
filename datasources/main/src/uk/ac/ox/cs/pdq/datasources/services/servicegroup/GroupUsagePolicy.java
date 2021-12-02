// This file is part of PDQ (https://github.com/ProofDrivenQuerying/pdq) which is released under the MIT license.
// See accompanying LICENSE for copyright notice and full details.

package uk.ac.ox.cs.pdq.datasources.services.servicegroup;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

/**
 * @author Mark Ridler
 *
 */
// GroupUsagePolicy is the XML element which corresponds to the <usage-policy> tag
// It represents the in-memory results of parsing the XML file for the GroupUsagePolicy object
// It has attributes for name, type, limit, period, wait and attribute-encoding
@XmlType (propOrder= {"name", "type", "limit", "period", "wait", "attributeEncoding"})
public class GroupUsagePolicy {
	
	private String name;
	private String type;
	private String limit;
	private String period;
	private String wait;
	private String attributeEncoding;
	
	@XmlAttribute (required=true)
	public String getName() {
		return name;
	}


	public void setName(String name) {
		this.name = name;
	}


	@XmlAttribute (required=true)
	public String getType() {
		return type;
	}


	public void setType(String type) {
		this.type = type;
	}


	@XmlAttribute (required=false)
	public String getLimit() {
		return limit;
	}


	public void setLimit(String limit) {
		this.limit = limit;
	}


	@XmlAttribute (required=false)
	public String getPeriod() {
		return period;
	}


	public void setPeriod(String period) {
		this.period = period;
	}


	@XmlAttribute (required=false)
	public String getWait() {
		return wait;
	}


	public void setWait(String wait) {
		this.wait = wait;
	}


	@XmlAttribute (name = "attribute-encoding", required=false)
	public String getAttributeEncoding() {
		return attributeEncoding;
	}


	public void setAttributeEncoding(String attributeEncoding) {
		this.attributeEncoding = attributeEncoding;
	}


	public String toString()
	{
		return "UsagePolicy";
	}

}
