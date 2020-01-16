package uk.ac.ox.cs.pdq.rest;

/**
 *
 * @author Camilo Ortiz
 *
 * 	This package is the entry point to PDQ's REST API.
 *
 * 	The following sub-packages are included:
 *
 * 	-- util, which contains classes that are used by Controller to initialize serializable versions of PDQ
 * 		subproject objects.
 *
 * 	-- jsonobjects, which contains the serializable PDQ classes that Controller returns to API calls.
 *
 * 	The top-level files include:
 * 	- Application is the SpringBoot application and is the entry point to the REST API application.
 * 	- Controller maps API calls to its methods and returns JSONified PDQ objects via HTTP.
 *
**/