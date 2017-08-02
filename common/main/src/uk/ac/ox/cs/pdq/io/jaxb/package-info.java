/**
 * Since most classes are immutable traditional bean-type building of those
 * classes is not possible. The classes in the "adapted" sub-package bypass this
 * problem by having setters and getters for the parameters necessary to create
 * the immutable version of the class.
 * 
 * The classes in the adapters sub-package will convert between the original and
 * the adapted versions.
 * 
 * IOManager has the main functions to import export schemas and queries.
 */
package uk.ac.ox.cs.pdq.io.jaxb;
