package uk.ac.ox.cs.pdq.endpoint;

import java.io.IOException;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.log4j.Logger;

import uk.ac.ox.cs.pdq.builder.SchemaDiscoverer;
import uk.ac.ox.cs.pdq.endpoint.util.ServletContextAttributes;

/**
 * Servlet listener uses to perform various initializations common to all XR
 * servlets.
 * 
 * @author Julien LEBLAY
 */
public class SourceContextListener implements ServletContextListener {

	/** Static logger */
	private static final Logger log = Logger.getLogger(SourceContextListener.class);

	/**
	 * Remove all custom attributes from the servlet context.
	 * 
	 * @see javax.servlet.ServletContextListener#contextDestroyed(javax.servlet.ServletContextEvent)
	 */
	@Override
	public void contextDestroyed(ServletContextEvent sce) {
		ServletContext sc = sce.getServletContext();
		sc.removeAttribute(ServletContextAttributes.SOURCES);
//		try {
//			this.deregisterDrivers();
//		} catch (InstantiationException | IllegalAccessException | SQLException e) {
//			throw new IllegalStateException();
//		}
	}

	/**
	 * Initializes available data sources, prefixes and page cache, and stores
	 * them in the servlet context.
	 */
	@Override
	public void contextInitialized(ServletContextEvent sce) {
		try {
//			this.registerDrivers();
			this.initSources(sce.getServletContext());
		} catch (IOException 
				| InstantiationException
				| IllegalAccessException
				| ClassNotFoundException e) {
			e.printStackTrace();
//			throw new IllegalStateException();
		}
	}
//
//	/**
//	 * Initialized default data sources
//	 * 
//	 * @param sc
//	 *            the servlet context
//	 * @throws SQLException
//	 * @throws IllegalAccessException
//	 * @throws InstantiationException
//	 * @throws XRException
//	 */
//	private void registerDrivers() throws InstantiationException, IllegalAccessException, SQLException {
//		DriverManager.registerDriver(org.postgresql.Driver.class.newInstance());
//		DriverManager.registerDriver(com.mysql.jdbc.Driver.class.newInstance());
//	}
//
//	/**
//	 * Initialized default data sources
//	 * 
//	 * @param sc
//	 *            the servlet context
//	 * @throws SQLException
//	 * @throws IllegalAccessException
//	 * @throws InstantiationException
//	 * @throws XRException
//	 */
//	private void deregisterDrivers() throws InstantiationException, IllegalAccessException, SQLException {
//		DriverManager.deregisterDriver(org.postgresql.Driver.class.newInstance());
//		DriverManager.deregisterDriver(com.mysql.jdbc.Driver.class.newInstance());
//	}

	/**
	 * Initialized default data sources
	 * 
	 * @param sc
	 *            the servlet context
	 * @throws IOException 
	 * @throws ClassNotFoundException 
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 * @throws XRException
	 */
	private void initSources(ServletContext sc) throws IOException, InstantiationException, IllegalAccessException, ClassNotFoundException {
		Map<String, SchemaDiscoverer> sources = new LinkedHashMap<>();
		for (Enumeration<String> en = sc.getInitParameterNames(); en.hasMoreElements();) {
			String name = en.nextElement();
			if (name.startsWith(ServletContextAttributes.SOURCE_PREFIX)) {
				String sourceName = name.substring(name.indexOf(ServletContextAttributes.SOURCE_PREFIX_SEPARATOR) + 1);
				Properties prop = asProperties(sc.getInitParameter(name));
				SchemaDiscoverer disco = this.discoverSchema(prop, sc);
				sources.put(sourceName, disco);
			}
		}
		log.info("Registered sources: " + sources);
		sc.setAttribute(ServletContextAttributes.SOURCES, sources);
	}

	/**
	 * 
	 * @param properties
	 * @return
	 * @throws ClassNotFoundException
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 * @throws MalformedURLException 
	 */
	private SchemaDiscoverer discoverSchema(Properties properties, ServletContext sc)
			throws InstantiationException, IllegalAccessException, ClassNotFoundException, MalformedURLException {
		String className = properties.getProperty("discoverer");
		SchemaDiscoverer discoverer = (SchemaDiscoverer) Class.forName(className).newInstance();
		if (properties.containsKey("path")) {
			properties.put("url", sc.getResource(properties.getProperty("path")));
		}
		discoverer.setProperties(properties);
		return discoverer;
	}

	/**
	 * 
	 * @param params
	 * @return
	 * @throws IOException
	 */
	private static Properties asProperties(String params) throws IOException {
		Properties result = new Properties();
		result.load(new StringReader(params));
		return result;
	}
}
