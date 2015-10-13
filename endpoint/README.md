PDQ Endpoint application

This project is the web frontend for PDQ.

The source code is available for free for non-commercial use.
See the LICENCE file for details.

I. Requirements
   
 * Java 1.8 or higher

 * Maven 2 or higher
   You can build each library outside Maven, e.g. in your favorite IDE, but
   you will have to managed dependencies manually.
 * A J2EE application server. Currently, this endpoint has only been tested
   with tomcat-8.0.0

II. Dependencies
 
Internal: runtime, planner 

External:  jsp-api-2.0, javax.servlet-api-3.1.0
	
III. Installation

Under the top directory, type:

	mvn install
	
The WAR file will be builder and place in each project's "target/" directory.
To start the endpoint, drop the WAR file in the deployment directory of your 
favourite web application.