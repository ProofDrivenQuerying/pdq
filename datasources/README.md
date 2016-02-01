PDQ web services connectors library

The source code is available for free for non-commercial use.
See the LICENCE file for details.

I. Requirements
   
 * Java 1.7 or higher
 * Maven 2 or higher
   You can build each library outside Maven, e.g. in your favorite IDE, but
   you will have to managed dependencies manually.

II. Dependencies

Internal: common
External: MySQL, Postgresql, org.apache.jcs, javax.ws.rs, 
	com.fasterxml.jackson.core, com.fasterxml.jackson.jaxrs, 
	com.fasterxml.jackson.dataformat, org.glassfish.jersey.core, 
	org.glassfish.jersey.core

III. Installation

Under the top directory, type:

	mvn install
	
The JAR will be built and placed in each project's "target/" directory.
