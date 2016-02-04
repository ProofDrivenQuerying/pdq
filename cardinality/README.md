PDQ Planner Library

The cardinality library features classes for query cardinality estimation

The source code is available for free for non-commercial use.
See the LICENCE file for details.

I. Requirements
   
 * Java 1.7 or higher
 * Maven 2 or higher
   You can build each library outside Maven, e.g. in your favorite IDE, but
   you will have to managed dependencies manually.

II. Dependencies
 
Internal: (pdq-)common, (pdq-)cost, (pdq-)reasoning
External: jcommander
	
III. Installing & running the planner

Under the top directory, type:

	mvn install
	
Two JARs will be built and placed in the project's "target/" directory.

	- pdq-cardinality-<version>.jar, contains the bytecode for the planner
	  only, i.e. you need to make sure all dependencies are on the CLASSPATH to
	  run it.

	- pdq-cardinality-<version>.one-jar.jar, is fully self-contained, and can be
	  run directly.

To run the planner, type:

	java -jar /path/to/JAR/file --help
	
This will printout all required command line arguments.

Most cardinality parameters can be passed through the command line, however, you 
may want to set those parameters in a separate file.
See pdq-planner.properties for an overview of all parameters that can be used.

Example:
	mvn install
	java -jar target/pdq-cardinality-<version>.one-jar.jar \
			-c path/to/properties/file     \
			-s path/to/schema/file          \
			-q path/to/query/file           \
			-v
