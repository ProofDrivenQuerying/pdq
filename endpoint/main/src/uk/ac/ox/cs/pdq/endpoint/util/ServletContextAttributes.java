package uk.ac.ox.cs.pdq.endpoint.util;

/**
 * This class gather all servlet context attributes used by the PDQ endpoint
 * servlets.
 *  
 * @author Julien LEBLAY
 */
public final class ServletContextAttributes {
	public static final String QUERIES = "SERVLET_QUERIES"; //$NON-NLS-1$
//	public static final String PAGE_PRE_CACHE = "PAGE_PRE_CACHE"; //$NON-NLS-1$
	public static final String SOURCE_PREFIX = "source"; //$NON-NLS-1$
	public static final String SOURCE_PREFIX_SEPARATOR = ":"; //$NON-NLS-1$
	public static final String SOURCES = "sources"; //$NON-NLS-1$
	public static final String ENTITY_EXTRACTION_PROXY = "ENTITY_EXTRACTOR"; //$NON-NLS-1$
}
