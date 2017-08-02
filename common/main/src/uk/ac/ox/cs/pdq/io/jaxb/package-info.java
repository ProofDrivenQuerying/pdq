/**
 * The io.jaxb package will replace the old io.xml package with a jaxb xml
 * parser.
 * 
 * This implementation has minimal impact on the Exported objects, usually the
 * only change is some added annotations.
 * 
 * In some other cases we have to create wrapper objects because immutable
 * classes cannot be used in a traditional bean-type xml-building. The classes
 * in the "adapted" sub-package bypass this problem by having setters and
 * getters for the parameters necessary to create the immutable version of the
 * class.
 * 
 * The classes in the adapters sub-package will convert between the original and
 * the adapted versions.
 * 
 * IOManager has the main functions to import export schemas and queries.
 */
package uk.ac.ox.cs.pdq.io.jaxb;
