package uk.ac.ox.cs.pdq.datasources.services;

/**
	@author Michael Benedikt
	
	This package contains the modeling and atomic execution of REST web service accesses
	
	- AccessEvent.java
 		* Event for single access to an online service.
 	- AccessPostProcessor
 		* An interface that is implemented by sub-classes of UsagePolicy to process a
 		* RESTResponseEvent
 	- AccessPreProcessor
 		* An interface that is implemented by sub-classes of UsagePolicy to process a
 		* RESTRequestEvent
 	- JsonResponseUmarshaller
 		* Derived from ResponseUnmarshaller, this is the Json version that does the
 		* bulk of the work during unmarshalling.
 	- RequestEvent.java
 		* The interface which is implemented by RESTRequestEvent
 	- ResponseEvent.java
 		* The interface which is implemented by RESTRequestEvent
 	- RESTExecutableAccessMethod
 		* The main class which implements the bulk of the work for parsing XML objects
 		* and setting up for a REST access with associated RESTRequest and RESTResponse
 		* events.
 	- RESTRequestEvent.java
 	 	* The main event class representing a REST request, which occurs immediately
 	 	* before a REST access event
 	- RESTResponseEvent.java
 	 	* The main event class representing a REST response, which occurs immediately
 	 	* after a REST access event
 	- ServiceManager.java
 		* ServiceManager calls JAXB and ServiceGroup or Service to marshal or unmarshal
 		* a file
 	- XmlResponseUmarshaller
 		* Derived from ResponseUnmarshaller, this is the Xml version that does the
 		* bulk of the work during unmarshalling.

**/