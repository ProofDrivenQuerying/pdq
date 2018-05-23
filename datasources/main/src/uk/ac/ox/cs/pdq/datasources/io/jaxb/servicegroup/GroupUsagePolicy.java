package uk.ac.ox.cs.pdq.datasources.io.jaxb.servicegroup;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

/**
 * @author Mark Ridler
 *
 */
// UsagePolicy is the XML element which corresponds to the <usage-policy> tag
@XmlType (propOrder= {"name", "type", "limit", "period", "wait"})
public class GroupUsagePolicy {
	
	private String name;
	private String type;
	private String limit;
	private String period;
	private String wait;
	
	@XmlAttribute
	public String getName() {
		return name;
	}


	public void setName(String name) {
		this.name = name;
	}


	@XmlAttribute
	public String getType() {
		return type;
	}


	public void setType(String type) {
		this.type = type;
	}


	@XmlAttribute
	public String getLimit() {
		return limit;
	}


	public void setLimit(String limit) {
		this.limit = limit;
	}


	@XmlAttribute
	public String getPeriod() {
		return period;
	}


	public void setPeriod(String period) {
		this.period = period;
	}


	@XmlAttribute
	public String getWait() {
		return wait;
	}


	public void setWait(String wait) {
		this.wait = wait;
	}


	public String toString()
	{
		return "UsagePolicy";
	}

}
