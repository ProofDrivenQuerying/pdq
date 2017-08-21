package uk.ac.ox.cs.pdq.datasources.io.jaxb;

import javax.xml.bind.annotation.XmlAttribute;

/**
 * Represents the sources tag in the schema.xml
 * 
 * @author Gabor
 *
 */
public class Source {
	private String name;
	private String file;
	private String discoverer;
	private String driver;
	private String url;
	private String database;
	private String username;
	private String password;
	
	public Source() {
	}

	@XmlAttribute
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@XmlAttribute
	public String getDiscoverer() {
		return discoverer;
	}

	public void setDiscoverer(String discoverer) {
		this.discoverer = discoverer;
	}

	@XmlAttribute
	public String getDriver() {
		return driver;
	}

	public void setDriver(String driver) {
		this.driver = driver;
	}

	@XmlAttribute
	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	@XmlAttribute
	public String getDatabase() {
		return database;
	}

	public void setDatabase(String database) {
		this.database = database;
	}

	@XmlAttribute
	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	@XmlAttribute
	public String getPassword() {
		return password;
	}
	
	@XmlAttribute
	public String getFile() {
		return file;
	}
	
	public void setFile(String file) {
		this.file = file;
	}

	public void setPassword(String password) {
		this.password = password;
	}
	
}
