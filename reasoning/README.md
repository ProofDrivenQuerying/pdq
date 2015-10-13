PDQ Reasoning Library

The reasoning library features classes and algorithm for reasoning purposes. 
Currently, the only implemented reasoning algorithm is the Chase.

The source code is available for free for non-commercial use.
See the LICENCE file for details.

I. Requirements
   
 * Java 1.7 or higher
 * Maven 2 or higher
   You can build each library outside Maven, e.g. in your favorite IDE, but
   you will have to managed dependencies manually.

II. Dependencies
 
Internal: (pdq-)common
External: derby-10.10, mysql-connector-5.1.30
	
III. Installing & running the planner

Under the top directory, type:

	mvn install

The JAR will be places under the "target/" directory.

